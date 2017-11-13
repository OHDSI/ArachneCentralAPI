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


getCohortSpecificSummary <- function(connection, resultsDatabaseSchema, cdmDatabaseSchema, cohortId) {
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

  # 1814
  queryMap$prevalenceByYearGenderSex <- list(
    "sqlPath"="cohortresults-sql/cohortSpecific/prevalenceByYearGenderSex.sql",
    "targetType"=fromJSON("./definitions/types/PrevalenceByYearGenderSex.json"),
    "mappings"=fromJSON("./definitions/mappings/ResultSetToPrevalenceByYearGenderSex.json")$mappings
  )

  # 1801
  queryMap$ageAtIndexDistribution <- list(
    "sqlPath"="cohortresults-sql/cohortSpecific/ageAtIndexDistribution.sql",
    "targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
    "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
  )

  # 1803
  queryMap$distributionOfAgeAtCohortStartByCohortStartYear <- list(
    "sqlPath"="cohortresults-sql/cohortSpecific/distributionOfAgeAtCohortStartByCohortStartYear.sql",
    "targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
    "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
  )

  # 1802
  queryMap$distributionOfAgeAtCohortStartByGender <- list(
    "sqlPath"="cohortresults-sql/cohortSpecific/distributionOfAgeAtCohortStartByGender.sql",
    "targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
    "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
  )

  # 1804
  queryMap$personsInCohortFromCohortStartToEnd <- list(
    "sqlPath"="cohortresults-sql/cohortSpecific/personsInCohortFromCohortStartToEnd.sql",
    "targetType"=fromJSON("./definitions/types/PersonsInCohortFromCohortStartToEnd.json"),
    "mappings"=fromJSON("./definitions/mappings/ResultSetToPersonsInCohortFromCohortStartToEnd.json")$mappings
  )

  sqlReplacements <- list(
    "ohdsi_database_schema"=resultsDatabaseSchema,
    "cohortDefinitionId"=cohortId,
    "cdm_database_schema" = cdmDatabaseSchema
  )
  
  return (queryCohortAnalysesResults(queryMap, connection, sqlReplacements, FALSE));
}

getDeathSummary <- function(connection, resultsDatabaseSchema, cdmDatabaseSchema, cohortId) {
  queryMap <- list()

  queryMap$ageAtDeath <- list(
    "sqlPath"="cohortresults-sql/death/sqlAgeAtDeath.sql",
    "targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
    "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
  )

  queryMap$deathByType <- list(
    "sqlPath"="cohortresults-sql/death/sqlDeathByType.sql",
    "targetType"=fromJSON("./definitions/types/ConceptCount.json"),
    "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptCount.json")$mappings
  )

  queryMap$prevalenceByGenderAgeYear <- list(
    "sqlPath"="cohortresults-sql/death/sqlPrevalenceByGenderAgeYear.sql",
    "targetType"=fromJSON("./definitions/types/PrevalenceByGenderAgeYear.json"),
    "mappings"=fromJSON("./definitions/mappings/ResultSetToPrevalenceByGenderAgeYear.json")$mappings
  )

  queryMap$prevalenceByMonth <- list(
    "sqlPath"="cohortresults-sql/death/sqlPrevalenceByMonth.sql",
    "targetType"=fromJSON("./definitions/types/PrevalenceByMonth.json"),
    "mappings"=fromJSON("./definitions/mappings/ResultSetToPrevalenceByMonth.json")$mappings
  )

  sqlReplacements <- list(
    "ohdsi_database_schema"=resultsDatabaseSchema,
    "cohortDefinitionId"=cohortId,
    "cdm_database_schema" = cdmDatabaseSchema
  )

  return (queryCohortAnalysesResults(queryMap, connection, sqlReplacements, FALSE));
}


getCohortObservationPeriod <- function(connection, resultsDatabaseSchema, cdmDatabaseSchema, cohortId) {
  queryMap <- list()

  queryMap$ageAtFirst <- list(
    "sqlPath"="cohortresults-sql/observationperiod/ageatfirst.sql",
    "targetType"=fromJSON("./definitions/types/AgeAtFirst.json"),
    "mappings"=fromJSON("./definitions/mappings/ResultSetToAgeAtFirst.json")$mappings
  )

  queryMap$observationLengthData <- list(
    "sqlPath"="cohortresults-sql/observationperiod/observationlength_data.sql",
    "targetType"=fromJSON("./definitions/types/AgeAtFirst.json"),
    "mappings"=fromJSON("./definitions/mappings/ResultSetToAgeAtFirst.json")$mappings
  )

  queryMap$observationLengthStats <- list(
    "sqlPath"="cohortresults-sql/observationperiod/observationlength_stats.sql",
    "targetType"=fromJSON("./definitions/types/CohortStatsRecord.json"),
    "mappings"=fromJSON("./definitions/mappings/ResultSetToCohortStatsRecord.json")$mappings
  )

  queryMap$observedByYearStats <- list(
    "sqlPath"="cohortresults-sql/observationperiod/observedbyyear_stats.sql",
    "targetType"=fromJSON("./definitions/types/CohortStatsRecord.json"),
    "mappings"=fromJSON("./definitions/mappings/ResultSetToCohortStatsRecord.json")$mappings
  )

  queryMap$observedByYearData <- list(
    "sqlPath"="cohortresults-sql/observationperiod/observedbyyear_data.sql",
    "targetType"=fromJSON("./definitions/types/AgeAtFirst.json"),
    "mappings"=fromJSON("./definitions/mappings/ResultSetToAgeAtFirst.json")$mappings
  )

  queryMap$ageByGender <- list(
      "sqlPath"="cohortresults-sql/observationperiod/agebygender.sql",
      "targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
      "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
  )

  queryMap$observationLengthByGender <- list(
            "sqlPath"="cohortresults-sql/observationperiod/observationlengthbygender.sql",
             "targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
             "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
  )

  queryMap$observationLengthByAge <- list(
       "sqlPath"="cohortresults-sql/observationperiod/observationlengthbyage.sql",
        "targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
        "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
  )

  queryMap$cumulativeDuration <- list(
        "sqlPath"="cohortresults-sql/observationperiod/cumulativeduration.sql",
         "targetType"=fromJSON("./definitions/types/CumulativeObservationRecord.json"),
         "mappings"=fromJSON("./definitions/mappings/ResultSetToCumulativeObservationRecord.json")$mappings
  )

  queryMap$observedByMonth <- list(
         "sqlPath"="cohortresults-sql/observationperiod/observedbymonth.sql",
          "targetType"=fromJSON("./definitions/types/MonthObservationRecord.json"),
          "mappings"=fromJSON("./definitions/mappings/ResultSetToMonthObservationRecord.json")$mappings
  )

  queryMap$observedByMonth <- list(
         "sqlPath"="cohortresults-sql/observationperiod/periodsperperson.sql",
          "targetType"=fromJSON("./definitions/types/ConceptCount.json"),
          "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptCount.json")$mappings
  )

    sqlReplacements <- list(
    "ohdsi_database_schema"=resultsDatabaseSchema,
    "cohortDefinitionId"=cohortId,
    "cdm_database_schema" = cdmDatabaseSchema
    )

    return (queryCohortAnalysesResults(queryMap, connection, sqlReplacements, FALSE));
}

getPersonSummary <- function(connection, resultsDatabaseSchema, cdmDatabaseSchema, cohortId) {
    queryMap <- list()
    #getCohortSpecificTreemapResults

    queryMap$yearOfBirthData <- list(
    "sqlPath"="cohortresults-sql/person/yearofbirth_data.sql"
    # "targetType"=fromJSON("./definitions/types/YearOfBirthData.json"),
    # "mappings"=fromJSON("./definitions/mappings/ResultSetToYearOfBirthData.json")$mappings
    )

    queryMap$yearOfBirthStats <- list(
    "sqlPath"="cohortresults-sql/person/yearofbirth_stats.sql"
    # "targetType"=fromJSON("./definitions/types/YearOfBirthStats.json"),
    # "mappings"=fromJSON("./definitions/mappings/ResultSetToYearOfBirthStats.json")$mappings
    )

    queryMap$gender <- list(
    "sqlPath"="cohortresults-sql/person/gender.sql"
    # "targetType"=fromJSON("./definitions/types/Gender.json"),
    # "mappings"=fromJSON("./definitions/mappings/ResultSetToGender.json")$mappings
    )

    queryMap$race <- list(
    "sqlPath"="cohortresults-sql/person/race.sql"
    # "targetType"=fromJSON("./definitions/types/Race.json"),
    # "mappings"=fromJSON("./definitions/mappings/ResultSetToRace.json")$mappings
    )

    queryMap$ethnicity <- list(
    "sqlPath"="cohortresults-sql/person/ethnicity.sql"
    # "targetType"=fromJSON("./definitions/types/Ethnicity.json"),
    # "mappings"=fromJSON("./definitions/mappings/ResultSetToEthnicity.json")$mappings
    )

    sqlReplacements <- list(
    "ohdsi_database_schema"=resultsDatabaseSchema,
    "cohortDefinitionId"=cohortId,
    "cdm_database_schema" = cdmDatabaseSchema
    )

    return (queryCohortAnalysesResults(queryMap, connection, sqlReplacements, FALSE));
}

getDataDensity <- function(connection, resultsDatabaseSchema, cdmDatabaseSchema, cohortId) {
    queryMap <- list()
    #getCohortSpecificTreemapResults

    queryMap$recordsPerPerson <- list(
        "sqlPath"="cohortresults-sql/datadensity/recordsperperson.sql"
        # "targetType"=fromJSON("./definitions/types/SeriesPerPerson.json"),
        # "mappings"=fromJSON("./definitions/mappings/ResultSetToSeriesPerPerson.json")$mappings
    )

    queryMap$totalRecords <- list(
        "sqlPath"="cohortresults-sql/datadensity/totalrecords.sql"
        # "targetType"=fromJSON("./definitions/types/SeriesPerPerson.json"),
        # "mappings"=fromJSON("./definitions/mappings/ResultSetToSeriesPerPerson.json")$mappings
    )

    queryMap$conceptsPerPerson <- list(
        "sqlPath"="cohortresults-sql/datadensity/conceptsperperson.sql"
        # "targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
        # "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
    )

    sqlReplacements <- list(
        "ohdsi_database_schema"=resultsDatabaseSchema,
        "cohortDefinitionId"=cohortId,
        "cdm_database_schema" = cdmDatabaseSchema
    )

    return (queryCohortAnalysesResults(queryMap, connection, sqlReplacements, FALSE));
}

getDashboard <- function(connection, resultsDatabaseSchema, cdmDatabaseSchema, cohortId) {
    queryMap <- list()
    #getCohortSpecificTreemapResults

    queryMap$ageAtDeath <- list(
        "sqlPath"="cohortresults-sql/observationperiod/ageatfirst.sql"
    # "targetType"=fromJSON("./definitions/types/Ageatfirst.json"),
    # "mappings"=fromJSON("./definitions/mappings/ResultSetToAgeatfirst.json")$mappings
    )

    queryMap$gender <- list(
        "sqlPath"="cohortresults-sql/person/gender.sql"
      # "targetType"=fromJSON("./definitions/types/Gender.json"),
      # "mappings"=fromJSON("./definitions/mappings/ResultSetToGender.json")$mappings
    )

    queryMap$cumulativeDuration <- list(
        "sqlPath"="cohortresults-sql/observationperiod/cumulativeduration.sql"
        # "targetType"=fromJSON("./definitions/types/CumulativeDuration.json"),
        # "mappings"=fromJSON("./definitions/mappings/ResultSetToCumulativeDuration.json")$mappings
    )

     queryMap$observedByMonth <- list(
         "sqlPath"="cohortresults-sql/observationperiod/observedbymonth.sql"
         # "targetType"=fromJSON("./definitions/types/ObservedByMonth.json"),
         # "mappings"=fromJSON("./definitions/mappings/ResultSetToObservedByMonth.json")$mappings
     )

    sqlReplacements <- list(
        "ohdsi_database_schema"=resultsDatabaseSchema,
        "cohortDefinitionId"=cohortId,
        "cdm_database_schema" = cdmDatabaseSchema
    )

    return (queryCohortAnalysesResults(queryMap, connection, sqlReplacements, FALSE));
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

queryCohortAnalysesResults <- function(queryMap, connection, sqlReplacements, convert) {
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

    if (convert){
      result[[key]] <- convertDataframe(result[[key]], targetType, mappings) #transformColnamesToCamelCase(dataframe = result[[key]])
    }
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
#res <- getCohortSpecificSummary(connection, resultsDatabaseSchema = "results", cdmDatabaseSchema = "public", cohortId = 1231231)
#dir.create("../output")
#writeToFile("../output/cohortspecific.json", res)

res <- getDeathSummary(connection, resultsDatabaseSchema = "results", cdmDatabaseSchema = "public", cohortId = 1231231)
writeToFile("../output/death.json", res)

res <- getCohortObservationPeriod(connection, resultsDatabaseSchema = "results", cdmDatabaseSchema = "public", cohortId = 1231231)
writeToFile("../output/cohortobservationperiod.json", res)

#res <- getPersonSummary(connection, resultsDatabaseSchema = "results", cdmDatabaseSchema = "public", cohortId = 1231231)
#writeToFile("../output/person.json", res)

#res <- getDataDensity(connection, resultsDatabaseSchema = "results", cdmDatabaseSchema = "public", cohortId = 1231231)
#writeToFile("../output/datadensity.json", res)

#res <- getDashboard(connection, resultsDatabaseSchema = "results", cdmDatabaseSchema = "public", cohortId = 1231231)
#writeToFile("../output/dashboard.json", res)

#res <- getConditionResults(connection, resultsDatabaseSchema = "results", cdmDatabaseSchema = "public", cohortId = 1231231)
#writeToFile("../output/conditionresults.json", res)

#res <- getConditionTreemap(connection, resultsDatabaseSchema = "results", cdmDatabaseSchema = "public", cohortId = 1231231)
#writeToFile("../output/conditiontreemap.json", res)

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