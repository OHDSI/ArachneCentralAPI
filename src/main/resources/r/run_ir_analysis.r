run_ir_analysis <- function(basicDir, analysisId, analysisDescriptionFile, cohortDefinitions, dbms, connectionString, user, password, cdmDatabaseSchema, resultsDatabaseSchema, cohortsDatabaseSchema, cohortTable = "cohort", outcomeTable = "cohort"){
    start.time <- Sys.time()
    library(SqlRender)
    library(DatabaseConnector)
    library(rJava)
    .jinit()
    .jinit(classpath=".")
    .jaddClassPath("circe-1.2.2-SNAPSHOT.jar")
    .jaddClassPath("commons-io-2.6.jar")
    .jaddClassPath("commons-lang3-3.7.jar")
    .jaddClassPath("jackson-annotations-2.9.2.jar")
    # Data extraction ---
    library("rjson")
    analysisDescription <- fromJSON(paste(readLines(analysisDescriptionFile), collapse = ""))

    connectionDetails <- DatabaseConnector::createConnectionDetails(dbms = dbms,
    connectionString = connectionString,
    user = user,
    password = password)
    connection <- DatabaseConnector::connect(connectionDetails)

    query <- SqlRender::readSql("delete_strata.sql")
    query <- SqlRender::renderSql(query, tableQualifier = resultsDatabaseSchema, analysis_id = analysisId)$sql
    query <- SqlRender::translateSql(query, targetDialect = connectionDetails$dbms)$sql

    DatabaseConnector::executeSql(connection, query)

    # Cohort definitions
    for(cohortFile in cohortDefinitions){
        cf <- file.path(basicDir, cohortFile)
        sql <- readSql(cf)
        sql <- renderSql(sql,
        cdm_database_schema = cdmDatabaseSchema,
        target_database_schema = cohortsDatabaseSchema,
        target_cohort_table = cohortTable,
        output = "output")$sql
        sql <- translateSql(sql, targetDialect = connectionDetails$dbms)$sql
        executeSql(connection, sql)
    }

    # Insert strata rules ---

    strataRules <- analysisDescription$strata

    for (i in seq_along(strataRules)) {
        strata <- strataRules[i]
        query <- SqlRender::readSql("strata_rules.sql")
        query <- SqlRender::renderSql(query, results_schema = resultsDatabaseSchema,
        analysis_id = analysisId,
        strata_sequence = i,
        name = strata[[1]]$name,
        description = "")$sql
        query <- SqlRender::translateSql(query, targetDialect = connectionDetails$dbms)$sql
        DatabaseConnector::executeSql(connection, query)
    }

    source('ir_analysis_query_builder.r')
    expressionSql <- buildAnalysisQuery(analysisDescription, analysisId, dbms, cdmDatabaseSchema, resultsDatabaseSchema)
    translatedSql <- translateSql(expressionSql, targetDialect = dbms)$sql
    DatabaseConnector::executeSql(connection, translatedSql)

    # Save results
    sql <- SqlRender::readSql("analysis_summary.sql")
    sql <- SqlRender::renderSql(sql,
    resultsSchema = resultsDatabaseSchema,
    id = analysisId)$sql
    sql <- SqlRender::translateSql(sql, targetDialect = dbms)$sql
    result <- DatabaseConnector::querySql(connection, sql)
    write.csv(result, file.path(workDir, "summary.csv"), na = "")

    disconnect(connection)

    end.time <- Sys.time()
    time.taken <- end.time - start.time
    time.taken
}