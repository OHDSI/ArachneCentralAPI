# /definitions/types - using http://json-schema.org notation
# /definitions/mappings - using notation of Java's mapstruct library

library(DatabaseConnector)
library(SqlRender)

run_cohort_characterization <- function(
    cohortDefinitionSqlPath,
    outputFolder,
    dbms,
    connectionString,
    user,
    password,
    cdmDatabaseSchema,
    resultsDatabaseSchema
) {
  
  # Solves issue with Windows x64 (https://stackoverflow.com/questions/7019912/using-the-rjava-package-on-win7-64-bit-with-r)
  if (Sys.getenv("JAVA_HOME")!="")
    Sys.setenv(JAVA_HOME="")
  
  connectionDetails <- createConnectionDetails(dbms=dbms,
                                               connectionString=connectionString,
                                               user=user,
                                               password=password)
  connection <- connect(connectionDetails)
  
  # Setup variables
  
  cohortTable <- "cohort"
  cohortId <- 1231231# sample(1:10^8, 1)
  
  print("Calculating cohort")
  
  sql <- readSql(cohortDefinitionSqlPath)
  sql <- renderSql(sql,
                   cdm_database_schema = cdmDatabaseSchema,
                   target_database_schema = resultsDatabaseSchema,
                   target_cohort_table = cohortTable,
                   target_cohort_id = cohortId)$sql
  sql <- translateSql(sql, targetDialect = connectionDetails$dbms)$sql
  executeSql(connection, sql)
  
  print("Creating Heracles results tables (if not exist)")
  
  heraclesTablesSql <- readSql("createHeraclesTables.sql")
  heraclesTablesSql <- renderSql(heraclesTablesSql,
                                 results_schema=resultsDatabaseSchema)$sql
  heraclesTablesSql <- translateSql(heraclesTablesSql, targetDialect = connectionDetails$dbms)$sql
  executeSql(connection, heraclesTablesSql)
  
  print("Running Cohort Characterization")
  
  heraclesAnalyses <- readSql("runHeraclesAnalyses.sql")
  heraclesAnalyses <- renderSql(heraclesAnalyses,
                                CDM_schema=cdmDatabaseSchema,
                                results_schema=resultsDatabaseSchema,
                                cohort_table=cohortTable,
                                source_name=connectionString,
                                runHERACLESHeel = TRUE,
                                CDM_version=5,
                                cohort_definition_id=cohortId)$sql
  heraclesAnalyses <- translateSql(heraclesAnalyses, targetDialect = connectionDetails$dbms)$sql
  executeSql(connection, heraclesAnalyses)
  
  # Save results
  
  # 
  
  # findByCohortDefinitionIdAndSourceIdAndVisualizationKey
  
  # Need db-independent way to convert everything JSONs
  
  # public CohortSpecificSummary CohortResultsAnalysisRunner.getCohortSpecificSummary
  
  # Clean up
  # ?
}


getCohortSpecificSummary <- function(connection, resultsDatabaseSchema, cohortId) {
  
  queryMap <- list()
  
  # 1805, 1806
  queryMap$personsByDurationFromStartToEnd <- list(
    "sqlPath"="cohortresults-sql/cohortSpecific/observationPeriodTimeRelativeToIndex.sql",
    "targetType"=fromJSON("./definitions/types/ObservationPeriodRecord.json"),
    "mappings"=fromJSON("./definitions/mappings/ResultSetToObservationPeriodRecord.json")$mappings
  )
  
  # 1815
  queryMap$prevalenceByMonth <- list(
    "sqlPath"="cohortresults-sql/cohortSpecific/prevalenceByMonth.sql",
    "targetType"=fromJSON("./definitions/types/PrevalenceRecord.json"),
    "mappings"=fromJSON("./definitions/mappings/ResultSetToPrevalenceRecord.json")$mappings
  )
  
  sqlReplacements <- list(
    "ohdsi_database_schema"=resultsDatabaseSchema,
    "cohortDefinitionId"=cohortId
  )
  
  return (queryCohortAnalysesResults(queryMap, connection, sqlReplacements));
}

writeToFile <- function(filename, content) {
  file.create(filename)
  fileConn <- file(filename)
  writeLines(paste(content), fileConn)
  close(fileConn)
}

getTargetToSourceMap <- function(rawMappings) {
  mappings <- list()
  
  for (i in 1:nrow(rawMappings)) {
    row <- rawMappings[i,]
    source <- as.character(row["source"])
    target <- as.character(row["target"])
    mappings[[target]] <- source
  }
  
  return(mappings)
}

typesMapping <- list(
  "string"="character",
  "long"="integer"
)

convertDataframe <- function(dataframe, toType, mappings) {
  result <- list()
  targetToSourceMap <- getTargetToSourceMap(mappings)
  props <- toType$properties
  
  for (prop in names(props)) {
    targetCol <- prop
    
    javaTargetType <- props[[targetCol]][["type"]]
    targetType <- if (is.null(typesMapping[[javaTargetType]])) javaTargetType else typesMapping[[javaTargetType]]
    
    sourceCol <- targetToSourceMap[[targetCol]]
    
    if (is.null(sourceCol)) {
      result[[targetCol]] <- vector(mode=targetType, length=nrow(dataframe)) # NA
    } else {
      sourceData <- dataframe[[sourceCol]];
      result[[targetCol]] <- do.call(paste("as", targetType, sep="."), list(sourceData))
    }
  }
  
  return(do.call("data.frame", result))
}

queryCohortAnalysesResults <- function(queryMap, connection, sqlReplacements) {
  dbms <- attributes(connection)$dbms
  result <- list()
  
  for (key in names(queryMap)) {
    query <- queryMap[[key]]
    
    sqlPath <- query$sqlPath
    targetType <- query$targetType
    mappings <- query$mappings
    
    sql <- readSql(sqlPath)
    sql <- do.call(renderSql, c(list(sql=sql), sqlReplacements))$sql
    sql <- translateSql(sql, targetDialect = dbms)$sql
    result[[key]] <- querySql(connection, sql)
    
    result[[key]] <- convertDataframe(result[[key]], targetType, mappings) #transformColnamesToCamelCase(dataframe = result[[key]])
  }
  
  json <- toJSON(result, pretty = TRUE, auto_unbox = TRUE)
  return(json)
}

library(DatabaseConnector)
library(SqlRender)
library(jsonlite)
library(dplyr)

connectionDetails <- createConnectionDetails(dbms="postgresql",
                                             connectionString="jdbc:postgresql://odysseusovh02.odysseusinc.com:5432/cdm_v500_synpuf_v101_110k",
                                             user="ohdsi",
                                             password="ohdsi")
connection <- connect(connectionDetails)
res <- getCohortSpecificSummary(connection, resultsDatabaseSchema = "results", cohortId = 1231231)
dir.create("output")
writeToFile("output/cohortspecific.json", res)



# workDir <- getwd();

# run_cohort_characterization(
#   file.path(workDir, "cohort.sql"),
#   file.path(workDir, "output"),
#   "postgresql",
#   "jdbc:postgresql://odysseusovh02.odysseusinc.com:5432/cdm_v500_synpuf_v101_110k",
#   "ohdsi",
#   "ohdsi",
#   "public",
#   "results"
# )