run_cohort_characterization <- function(
  cohortDefinitionSqlPath,
  connectionDetails,
  cdmDatabaseSchema,
  resultsDatabaseSchema,
  cohortTable
){

    connection <- connect(connectionDetails)

    #Create a heracles tables
    print(paste("Creating Heracles results tables (if not exist)", Sys.time(), sep=" started: "))

    heraclesTablesSql <- readSql("createHeraclesTables.sql")
    heraclesTablesSql <- render(heraclesTablesSql,
                                results_schema=resultsDatabaseSchema)
    heraclesTablesSql <- translate(heraclesTablesSql, targetDialect = connectionDetails$dbms)
    executeSql(connection, heraclesTablesSql)

    # Setup cohortId
    cohortId <- sample(1:10^8, 1)
    print(paste("Cohort id ", cohortId, sep=" = "))

    print(paste("Calculating cohort", Sys.time(), sep=" started: "))

    # Generate cohort
    sql <- readSql(cohortDefinitionSqlPath)
    sql <- render(sql,
                  cdm_database_schema = cdmDatabaseSchema,
                  target_database_schema = resultsDatabaseSchema,
                  vocabulary_database_schema = cdmDatabaseSchema,
                  target_cohort_table = cohortTable,
                  target_cohort_id = cohortId)
    sql <- translate(sql, targetDialect = connectionDetails$dbms)
    executeSql(connection, sql)

    #Init
    print(paste("initHeraclesSql actions..", Sys.time(), sep=" started: "))
    initHeraclesSql <- readSql("initHeracles.sql")
    initHeraclesSql <- render(initHeraclesSql,
                              results_schema=resultsDatabaseSchema,
                              cohort_definition_id=cohortId)
    initHeraclesSql <- translate(initHeraclesSql, targetDialect = connectionDetails$dbms)
    executeSql(connection, initHeraclesSql)

    #run Heracles analyses
    print(paste("Running Cohort Characterization", Sys.time(), sep=" started: "))

    heraclesAnalyses <- readSql("analysesQueries.sql")
    heraclesAnalyses <- render(heraclesAnalyses,
                               CDM_schema=cdmDatabaseSchema,
                               results_schema=resultsDatabaseSchema,
                               cohort_table=cohortTable,
                               source_name=connectionDetails$connectionString(),
                               runHERACLESHeel = FALSE,
                               CDM_version='5',
                               cohort_definition_id=cohortId)
    heraclesAnalyses <- translate(heraclesAnalyses, targetDialect = connectionDetails$dbms)
    executeSql(connection, heraclesAnalyses)


    #save results
    #export to DB
    print(paste("save to heracles results...", Sys.time(), sep=" started: "))
    selectResultsQuery <- readSql("selectResultsQuery.sql")
    selectResultsQuery <- render(selectResultsQuery,
                                 results_schema=resultsDatabaseSchema,
                                 cohort_definition_id=cohortId)
    selectResultsQuery <- translate(selectResultsQuery, targetDialect = connectionDetails$dbms)
    executeSql(connection, selectResultsQuery)
    return(cohortId);
}

exportResults <- function(
  cohortId,
  outputFolder,
  connectionDetails,
  cdmDatabaseSchema,
  resultsDatabaseSchema,
  cohortTable,
  cohortDefinitionSqlPath,
  includeDrilldownReports,
  includedReports
){
    #export to json
    print(paste("Printing all results", Sys.time(), sep=" started: "))
    connection <- connect(connectionDetails)
    writeAllResults(connection, cdmDatabaseSchema, resultsDatabaseSchema,  cohortId, outputFolder, includeDrilldownReports, includedReports)

    for (key in c("count", "summary")){

        sqlFileName <-  paste("cohort-", ".ohdsi.sql", sep = key)
        sqlFileName <-  file.path(workDir, sqlFileName)

        if (file.exists(sqlFileName)){

            sql <- readSql(sqlFileName)
            sql <- render(sql,
                          target_database_schema = resultsDatabaseSchema,
                          target_cohort_table = cohortTable,
                          target_cohort_id = cohortId)
            sql <- translate(sql, targetDialect = connectionDetails$dbms)
            res <- querySql(connection, sql)

            resultFileName <- gsub("\\.ohdsi.sql$", paste("-", ".ohdsi.sql", sep = key), basename(cohortDefinitionSqlPath))
            resultFileName <- file.path(outputFolder, resultFileName)
            write.csv(res, file = paste(resultFileName, ".result.csv", sep = ""), row.names = FALSE, quote = FALSE, col.names = TRUE)
        }
    }
}

getCohortSpecificSummary <- function(connection, sqlReplacements, mapping) {
    queryMap <- list()

    # 1805, 1806
    queryMap$personsByDurationFromStartToEnd <- list(
      "sqlPath"="cohortresults-sql/cohortSpecific/observationPeriodTimeRelativeToIndex.sql"
      #"targetType"=fromJSON("./definitions/types/ObservationPeriodRecord.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToObservationPeriodRecord.json")$mappings
    )

    # 1815
    queryMap$prevalenceByMonth <- list(
      "sqlPath"="cohortresults-sql/cohortSpecific/prevalenceByMonth.sql"
      #"targetType"=fromJSON("./definitions/types/PrevalenceRecord.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToPrevalenceRecord.json")$mappings
    )

    # 1814
    queryMap$prevalenceByYearGenderSex <- list(
      "sqlPath"="cohortresults-sql/cohortSpecific/prevalenceByYearGenderSex.sql"
      #"targetType"=fromJSON("./definitions/types/PrevalenceByYearGenderSex.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToPrevalenceByYearGenderSex.json")$mappings
    )

    # 1801
    queryMap$ageAtIndexDistribution <- list(
      "sqlPath"="cohortresults-sql/cohortSpecific/ageAtIndexDistribution.sql"
      #"targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
    )

    # 1803
    queryMap$distributionOfAgeAtCohortStartByCohortStartYear <- list(
      "sqlPath"="cohortresults-sql/cohortSpecific/distributionOfAgeAtCohortStartByCohortStartYear.sql"
      #"targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
    )

    # 1802
    queryMap$distributionOfAgeAtCohortStartByGender <- list(
      "sqlPath"="cohortresults-sql/cohortSpecific/distributionOfAgeAtCohortStartByGender.sql"
      #"targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
    )

    # 1804
    queryMap$personsInCohortFromCohortStartToEnd <- list(
      "sqlPath"="cohortresults-sql/cohortSpecific/personsInCohortFromCohortStartToEnd.sql"
      #"targetType"=fromJSON("./definitions/types/PersonsInCohortFromCohortStartToEnd.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToPersonsInCohortFromCohortStartToEnd.json")$mappings
    )

    return (queryJsonCohortAnalysesResults(queryMap, connection, sqlReplacements, mapping));
}

getDeathSummary <- function(connection, sqlReplacements, mapping) {
    queryMap <- list()

    queryMap$ageAtDeath <- list(
      "sqlPath"="cohortresults-sql/death/sqlAgeAtDeath.sql"
      #"targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
    )

    queryMap$deathByType <- list(
      "sqlPath"="cohortresults-sql/death/sqlDeathByType.sql"
      #"targetType"=fromJSON("./definitions/types/ConceptCount.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToConceptCount.json")$mappings
    )

    queryMap$prevalenceByGenderAgeYear <- list(
      "sqlPath"="cohortresults-sql/death/sqlPrevalenceByGenderAgeYear.sql"
      #"targetType"=fromJSON("./definitions/types/PrevalenceByGenderAgeYear.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToPrevalenceByGenderAgeYear.json")$mappings
    )

    queryMap$prevalenceByMonth <- list(
      "sqlPath"="cohortresults-sql/death/sqlPrevalenceByMonth.sql"
      #"targetType"=fromJSON("./definitions/types/PrevalenceByMonth.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToPrevalenceByMonth.json")$mappings
    )

    return (queryJsonCohortAnalysesResults(queryMap, connection, sqlReplacements, mapping));
}


getCohortObservationPeriod <- function(connection, sqlReplacements, mapping) {
    queryMap <- list()

    queryMap$ageAtFirst <- list(
      "sqlPath"="cohortresults-sql/observationperiod/ageatfirst.sql"
      #"targetType"=fromJSON("./definitions/types/AgeAtFirst.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToAgeAtFirst.json")$mappings
    )

    queryMap$observationLengthData <- list(
      "sqlPath"="cohortresults-sql/observationperiod/observationlength_data.sql"
      #"targetType"=fromJSON("./definitions/types/AgeAtFirst.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToAgeAtFirst.json")$mappings
    )

    queryMap$observationLengthStats <- list(
      "sqlPath"="cohortresults-sql/observationperiod/observationlength_stats.sql"
      #"targetType"=fromJSON("./definitions/types/CohortStatsRecord.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToCohortStatsRecord.json")$mappings
    )

    queryMap$observedByYearStats <- list(
      "sqlPath"="cohortresults-sql/observationperiod/observedbyyear_stats.sql"
      #"targetType"=fromJSON("./definitions/types/CohortStatsRecord.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToCohortStatsRecord.json")$mappings
    )

    queryMap$observedByYearData <- list(
      "sqlPath"="cohortresults-sql/observationperiod/observedbyyear_data.sql"
      #"targetType"=fromJSON("./definitions/types/AgeAtFirst.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToAgeAtFirst.json")$mappings
    )

    queryMap$ageByGender <- list(
      "sqlPath"="cohortresults-sql/observationperiod/agebygender.sql"
      #"targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
    )

    queryMap$observationLengthByGender <- list(
      "sqlPath"="cohortresults-sql/observationperiod/observationlengthbygender.sql"
      #"targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
    )

    queryMap$observationLengthByAge <- list(
      "sqlPath"="cohortresults-sql/observationperiod/observationlengthbyage.sql"
      #"targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
    )

    queryMap$cumulativeDuration <- list(
      "sqlPath"="cohortresults-sql/observationperiod/cumulativeduration.sql"
      #"targetType"=fromJSON("./definitions/types/CumulativeObservationRecord.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToCumulativeObservationRecord.json")$mappings
    )

    queryMap$observedByMonth <- list(
      "sqlPath"="cohortresults-sql/observationperiod/observedbymonth.sql"
      #"targetType"=fromJSON("./definitions/types/MonthObservationRecord.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToMonthObservationRecord.json")$mappings
    )

    queryMap$periodPerPerson <- list(
      "sqlPath"="cohortresults-sql/observationperiod/periodsperperson.sql"
      #"targetType"=fromJSON("./definitions/types/ConceptCount.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToConceptCount.json")$mappings
    )

    return (queryJsonCohortAnalysesResults(queryMap, connection, sqlReplacements, mapping));
}

getPersonSummary <- function(connection, sqlReplacements, mapping) {
    queryMap <- list()

    queryMap$yearOfBirthData <- list(
      "sqlPath"="cohortresults-sql/person/yearofbirth_data.sql"
      #"targetType"=fromJSON("./definitions/types/YearOfBirthData.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToYearOfBirthData.json")$mappings
    )

    queryMap$yearOfBirthStats <- list(
      "sqlPath"="cohortresults-sql/person/yearofbirth_stats.sql"
      #"targetType"=fromJSON("./definitions/types/YearOfBirthStats.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToYearOfBirthStats.json")$mappings
    )

    queryMap$gender <- list(
      "sqlPath"="cohortresults-sql/person/gender.sql"
      #"targetType"=fromJSON("./definitions/types/Gender.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToGender.json")$mappings
    )

    queryMap$race <- list(
      "sqlPath"="cohortresults-sql/person/race.sql"
      #"targetType"=fromJSON("./definitions/types/Race.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToRace.json")$mappings
    )

    queryMap$ethnicity <- list(
      "sqlPath"="cohortresults-sql/person/ethnicity.sql"
      #"targetType"=fromJSON("./definitions/types/Ethnicity.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToEthnicity.json")$mappings
    )

    return (queryJsonCohortAnalysesResults(queryMap, connection, sqlReplacements, mapping));
}


getDataCompleteness <- function(connection, sqlReplacements, mapping) {
    queryMap <- list()

    queryMap$recordsPerPerson <- list(
      "sqlPath"="cohortresults-sql/datacompleteness/getCohortDataCompleteness.sql"
      #"targetType"=fromJSON("./definitions/types/SeriesPerPerson.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToSeriesPerPerson.json")$mappings
    )

    return (queryCohortAnalysesResults(queryMap, connection, sqlReplacements, mapping));
}

getDashboard <- function(connection, sqlReplacements, mapping) {
    queryMap <- list()

    queryMap$ageAtDeath <- list(
      "sqlPath"="cohortresults-sql/observationperiod/ageatfirst.sql"
      #"targetType"=fromJSON("./definitions/types/Ageatfirst.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToAgeatfirst.json")$mappings
    )

    queryMap$gender <- list(
      "sqlPath"="cohortresults-sql/person/gender.sql"
      #"targetType"=fromJSON("./definitions/types/Gender.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToGender.json")$mappings
    )

    queryMap$cumulativeDuration <- list(
      "sqlPath"="cohortresults-sql/observationperiod/cumulativeduration.sql"
      #"targetType"=fromJSON("./definitions/types/CumulativeDuration.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToCumulativeDuration.json")$mappings
    )

    queryMap$observedByMonth <- list(
      "sqlPath"="cohortresults-sql/observationperiod/observedbymonth.sql"
      #"targetType"=fromJSON("./definitions/types/ObservedByMonth.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToObservedByMonth.json")$mappings
    )

    return (queryJsonCohortAnalysesResults(queryMap, connection, sqlReplacements, mapping));
}

getEntropy <- function(connection, sqlReplacements, mapping) {
    queryMap <- list()

    queryMap$entropy <- list(
      "sqlPath"="cohortresults-sql/entropy/getEntropy.sql"
      #"targetType"=fromJSON("./definitions/types/AnalysisResults.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToAnalysisResults.json")$mappings
    )
    return (queryCohortAnalysesResults(queryMap, connection, sqlReplacements, mapping));
}

getConditionsByIndexTreemap <- function(connection, sqlReplacements, mapping) {
    queryMap <- list()

    queryMap$conditionOccurrencePrevalenceOfCondition <- list(
      "sqlPath"="cohortresults-sql/cohortSpecific/conditionOccurrencePrevalenceOfCondition.sql"
      #"targetType"=fromJSON("./definitions/types/Ageatfirst.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToAgeatfirst.json")$mappings
    )

    return (queryCohortAnalysesResults(queryMap, connection, sqlReplacements, mapping));
}

getProceduresByIndexTreemap <- function(connection, sqlReplacements, mapping) {
    queryMap <- list()

    queryMap$procedureOccurrencePrevalenceOfDrug <- list(
      "sqlPath"="cohortresults-sql/cohortSpecific/procedureOccurrencePrevalenceOfDrug.sql"
      #"targetType"=fromJSON("./definitions/types/Gender.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToGender.json")$mappings
    )
    return (queryCohortAnalysesResults(queryMap, connection, sqlReplacements, mapping));
}

getDrugsByIndexTreemap <- function(connection, sqlReplacements, mapping) {
    queryMap <- list()

    queryMap$drugEraPrevalenceOfDrug <- list(
      "sqlPath"="cohortresults-sql/cohortSpecific/drugEraPrevalenceOfDrug.sql"
      #"targetType"=fromJSON("./definitions/types/CumulativeDuration.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToCumulativeDuration.json")$mappings
    )

    return (queryCohortAnalysesResults(queryMap, connection, sqlReplacements, mapping));
}

getHeraclesHeel <- function(connection, sqlReplacements, mapping) {
    queryMap <- list()

    queryMap$heraclesHeel <- list(
      "sqlPath"="cohortresults-sql/heraclesHeel/sqlHeraclesHeel.sql"
      #"targetType"=fromJSON("./definitions/types/CohortAttribute.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToCohortAttribute.json")$mappings
    )

    return (queryJsonCohortAnalysesResults(queryMap, connection, sqlReplacements, mapping));
}


# drilldown reports

getDrugEraDrilldown <- function(connection, sqlReplacements, mapping) {
    queryMap <- list()

    queryMap$ageAtFirstExposure <- list(
      "sqlPath"="cohortresults-sql/drugera/byConcept/sqlAgeAtFirstExposure.sql"
      #"targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
    )

    queryMap$lengthOfEra <- list(
      "sqlPath"="cohortresults-sql/drugera/byConcept/sqlLengthOfEra.sql"
      #"targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
    )
    queryMap$prevalenceByGenderAgeYear <- list(
      "sqlPath"="cohortresults-sql/drugera/byConcept/sqlPrevalenceByGenderAgeYear.sql"
      #"targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
    )

    queryMap$prevalenceByMonth <- list(
      "sqlPath"="cohortresults-sql/drugera/byConcept/sqlPrevalenceByMonth.sql"
      #"targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
    )

    return (queryJsonCohortAnalysesResults(queryMap, connection, sqlReplacements, mapping));
}

getDrugExposureDrilldown <- function(connection, sqlReplacements, mapping) {
    queryMap <- list()

    queryMap$ageAtFirstExposure <- list(
      "sqlPath"="cohortresults-sql/drug/byConcept/allSqlAgeAtFirstExposure.sql"
      #"targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
    )

    queryMap$daysSupplyDistribution <- list(
      "sqlPath"="cohortresults-sql/drug/byConcept/allSqlDaysSupplyDistribution.sql"
      #"targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
    )

    queryMap$drugsByType <- list(
      "sqlPath"="cohortresults-sql/drug/byConcept/allSqlDrugsByType.sql"
      #"targetType"=fromJSON("./definitions/types/ConceptCount.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToConceptCount.json")$mappings
    )

    queryMap$drugsByType <- list(
      "sqlPath"="cohortresults-sql/drug/byConcept/allSqlDrugsByType.sql"
      #"targetType"=fromJSON("./definitions/types/ConceptCount.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToConceptCount.json")$mappings
    )

    queryMap$prevalenceByGenderAgeYear <- list(
      "sqlPath"="cohortresults-sql/drug/byConcept/allSqlPrevalenceByGenderAgeYear.sql"
      #"targetType"=fromJSON("./definitions/types/ConceptDecile.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToConceptDecile.json")$mappings
    )
    queryMap$prevalenceByMonth <- list(
      "sqlPath"="cohortresults-sql/drug/byConcept/allSqlPrevalenceByMonth.sql"
      #"targetType"=fromJSON("./definitions/types/PrevalanceConcept.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToPrevalanceConcept.json")$mappings
    )

    queryMap$prevalenceByMonth <- list(
      "sqlPath"="cohortresults-sql/drug/byConcept/allSqlPrevalenceByMonth.sql"
      #"targetType"=fromJSON("./definitions/types/PrevalanceConcept.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToPrevalanceConcept.json")$mappings
    )

    queryMap$quantityDistribution <- list(
      "sqlPath"="cohortresults-sql/drug/byConcept/allSqlQuantityDistribution.sql"
      #"targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
    )

    queryMap$refillsDistribution <- list(
      "sqlPath"="cohortresults-sql/drug/byConcept/allSqlRefillsDistribution.sql"
      #"targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
    )

    return (queryCohortAnalysesResults(queryMap, connection, sqlReplacements, mapping));
}

getProcedureDrilldown <- function(connection, sqlReplacements, mapping) {
    queryMap <- list()

    queryMap$ageAtFirstOccurrence <- list(
      "sqlPath"="cohortresults-sql/procedure/byConcept/sqlAgeAtFirstOccurrence.sql"
      #"targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
    )

    queryMap$proceduresByType <- list(
      "sqlPath"="cohortresults-sql/procedure/byConcept/sqlProceduresByType.sql"
      #"targetType"=fromJSON("./definitions/types/ConceptCount.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToConceptCount.json")$mappings
    )

    queryMap$prevalenceByGenderAgeYear <- list(
      "sqlPath"="cohortresults-sql/procedure/byConcept/sqlPrevalenceByGenderAgeYear.sql"
      #"targetType"=fromJSON("./definitions/types/ConceptDecile.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToConceptDecile.json")$mappings
    )

    queryMap$prevalenceByMonth <- list(
      "sqlPath"="cohortresults-sql/procedure/byConcept/sqlPrevalenceByMonth.sql"
      #"targetType"=fromJSON("./definitions/types/PrevalanceConcept.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToPrevalanceConcept.json")$mappings
    )

    return (queryJsonCohortAnalysesResults(queryMap, connection, sqlReplacements, mapping));
}

getVisitDrilldown <- function(connection, sqlReplacements, mapping) {
    queryMap <- list()

    queryMap$ageAtFirstOccurrence <- list(
      "sqlPath"="cohortresults-sql/visit/byConcept/sqlAgeAtFirstOccurrence.sql"
      #"targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
    )

    queryMap$visitDurationByType <- list(
      "sqlPath"="cohortresults-sql/visit/byConcept/sqlVisitDurationByType.sql"
      #"targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
    )

    queryMap$prevalenceByGenderAgeYear <- list(
      "sqlPath"="cohortresults-sql/visit/byConcept/sqlPrevalenceByGenderAgeYear.sql"
      #"targetType"=fromJSON("./definitions/types/ConceptDecile.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToConceptDecile.json")$mappings
    )

    queryMap$prevalenceByMonth <- list(
      "sqlPath"="cohortresults-sql/visit/byConcept/sqlPrevalenceByMonth.sql"
      #"targetType"=fromJSON("./definitions/types/PrevalanceConcept.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToPrevalanceConcept.json")$mappings
    )

    return (queryJsonCohortAnalysesResults(queryMap, connection, sqlReplacements, mapping));
}

getConditionDrilldown <- function(connection, sqlReplacements, mapping) {
    queryMap <- list()

    queryMap$ageAtFirstDiagnosis <- list(
      "sqlPath"="cohortresults-sql/condition/byConcept/sqlAgeAtFirstDiagnosis.sql"
      #"targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
    )

    queryMap$sqlConditionsByType <- list(
      "sqlPath"="cohortresults-sql/condition/byConcept/sqlConditionsByType.sql"
      #"targetType"=fromJSON("./definitions/types/ConceptConditionCount.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToConceptConditionCount.json")$mappings
    )

    queryMap$prevalenceByGenderAgeYear <- list(
      "sqlPath"="cohortresults-sql/condition/byConcept/sqlPrevalenceByGenderAgeYear.sql"
      #"targetType"=fromJSON("./definitions/types/ConceptDecile.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToConceptDecile.json")$mappings
    )

    queryMap$prevalenceByMonth <- list(
      "sqlPath"="cohortresults-sql/condition/byConcept/sqlPrevalenceByMonth.sql"
      #"targetType"=fromJSON("./definitions/types/PrevalanceConcept.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToPrevalanceConcept.json")$mappings
    )

    return (queryJsonCohortAnalysesResults(queryMap, connection, sqlReplacements, mapping));
}

getConditionEraDrilldown <- function(connection, sqlReplacements, mapping) {
    queryMap <- list()

    queryMap$ageAtFirstDiagnosis <- list(
      "sqlPath"="cohortresults-sql/conditionera/byConcept/sqlAgeAtFirstDiagnosis.sql"
      #"targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
    )

    queryMap$lengthOfEra <- list(
      "sqlPath"="cohortresults-sql/conditionera/byConcept/sqlLengthOfEra.sql"
      #"targetType"=fromJSON("./definitions/types/ConceptConditionCount.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToConceptConditionCount.json")$mappings
    )

    queryMap$prevalenceByGenderAgeYear <- list(
      "sqlPath"="cohortresults-sql/conditionera/byConcept/sqlPrevalenceByGenderAgeYear.sql"
      #"targetType"=fromJSON("./definitions/types/ConceptDecile.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToConceptDecile.json")$mappings
    )

    queryMap$prevalenceByMonth <- list(
      "sqlPath"="cohortresults-sql/conditionera/byConcept/sqlPrevalenceByMonth.sql"
      #"targetType"=fromJSON("./definitions/types/PrevalanceConcept.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToPrevalanceConcept.json")$mappings
    )

    return (queryJsonCohortAnalysesResults(queryMap, connection, sqlReplacements, mapping));
}

getProcedureByIndexDrilldown <- function(connection, sqlReplacements, mapping) {
    queryMap <- list()

    queryMap$procedureByIndex <- list(
      "sqlPath"="cohortresults-sql/cohortSpecific/byConcept/allProcedureOccursRelativeToIndex.sql"
      #"targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
      #"mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
    )

    return (queryCohortAnalysesResults(queryMap, connection, sqlReplacements, mapping));
}


getConditionByIndexDrilldown <- function(connection, sqlReplacements, mapping) {
    queryMap <- list()

    queryMap$conditionByIndex <- list(
      "sqlPath"="cohortresults-sql/cohortSpecific/byConcept/allFirstConditionRelativeToIndex.sql"
      # "targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
      # "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
    )

    return (queryCohortAnalysesResults(queryMap, connection, sqlReplacements, mapping));
}

getDrugByIndexDrilldown <- function(connection, sqlReplacements, mapping) {
    queryMap <- list()

    queryMap$drugByIndex <- list(
      "sqlPath"="cohortresults-sql/cohortSpecific/byConcept/allDrugOccursRelativeToIndex.sql"
      # "targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
      # "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
    )

    return (queryCohortAnalysesResults(queryMap, connection, sqlReplacements, mapping));
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

convertDataCompletenessData <- function(inputData){

    analysisMap <- list()
    elements<- list
    key<- list
    analysisId<- list
    resultRoot<- list()

    ind <- 0

    for (key in names(inputData)) {
        elements <- inputData[[key]]
        for (element in elements$ANALYSIS_ID) {
            analysisMap[[paste("" , element, sep="")]] <- list(str = elements$STRATUM_1[ind])
            ind <- ind+1
        }
    }

    resultList <- list()

    resultList[[1]] <- list(covariance = "0~10", genderP = getValue(analysisMap, "2001"), raceP = getValue(analysisMap, "2011"),  ethP = getValue(analysisMap, "2021"))
    resultList[[2]] <- list(covariance = "10~20", genderP = getValue(analysisMap, "2002"), raceP = getValue(analysisMap, "2012"),  ethP = getValue(analysisMap, "2022"))
    resultList[[3]] <- list(covariance = "20~30", genderP = getValue(analysisMap, "2003"), raceP = getValue(analysisMap, "2013"),  ethP = getValue(analysisMap, "2023"))
    resultList[[4]] <- list(covariance = "30~40", genderP = getValue(analysisMap, "2004"), raceP = getValue(analysisMap, "2014"),  ethP = getValue(analysisMap, "2024"))
    resultList[[5]] <- list(covariance = "40~50", genderP = getValue(analysisMap, "2005"), raceP = getValue(analysisMap, "2015"),  ethP = getValue(analysisMap, "2025"))
    resultList[[6]] <- list(covariance = "50~60", genderP = getValue(analysisMap, "2006"), raceP = getValue(analysisMap, "2016"),  ethP = getValue(analysisMap, "2026"))
    resultList[[7]] <- list(covariance = "60+", genderP = getValue(analysisMap, "2007"), raceP = getValue(analysisMap, "2017"),  ethP = getValue(analysisMap, "2027"))

    resultRoot[["recordsPerPerson"]] <- resultList

    return(resultRoot);
}

getValue <- function(analysisMap, key){
    val <- analysisMap[[key]]$str

    if (is.null(val)){
        val <- ''
    }
    return (val)
}

convertEntropyData <- function(inputData2031, inputData2032){

    analysisMap <- list()
    elements<- list
    key<- list
    analysisId<- list
    resultRoot<- list()
    resultList <- list()

    ind <- 1

    for (key in names(inputData2031)) {
        elements <- inputData2031[[key]]
        for (element in elements$STRATUM_1) {

            resultList[[ind]] <- list(date = as.numeric(as.POSIXct(element, format="%Y-%m-%d")), entropy = as.double(elements$STRATUM_2[ind]), insitution = "All sites")
            ind <- ind+1
        }
    }

    ind2 <- 1

    for (key in names(inputData2032)) {
        elements <- inputData2032[[key]]
        for (element in elements$STRATUM_1) {

            stratum2 <- elements$STRATUM_2[ind2]

            if (!is.na(stratum2) && trimws(stratum2) != '' ){
                careSite <- paste(element, stratum2, sep = ":")
            } else {
                careSite <- element
            }

            stratum3 <- elements$STRATUM_3[ind2]
            formatted <- as.numeric(as.POSIXct(stratum3, format="%Y-%m-%d"))
            resultList[[ind]] <- list(insitution = careSite, date = formatted, entropy = as.double(elements$STRATUM_4[ind2]))
            ind <- ind+1
            ind2<- ind2+1
        }
    }

    resultRoot[["entropy"]] <- resultList
    return(resultRoot);
}


queryJsonCohortAnalysesResults <- function(queryMap, connection, sqlReplacements, mapping) {

    result <- queryCohortAnalysesResults(queryMap, connection, sqlReplacements, mapping)

    json <- toJSON(result, pretty = TRUE, auto_unbox = TRUE)
    return(json)
}

queryCohortAnalysesResults <- function(queryMap, connection, sqlReplacements, mapping) {
    dbms <- attributes(connection)$dbms
    result <- list()

    for (key in names(queryMap)) {
        query <- queryMap[[key]]

        sqlPath <- query$sqlPath
        targetType <- query$targetType
        mappings <- query$mappings

        sql <- readSql(sqlPath)
        sql <- do.call(render, c(list(sql=sql), sqlReplacements))
        sql <- translate(sql, targetDialect = dbms)
        result[[key]] <- querySql(connection, sql)

        if (mapping){
            result[[key]] <- convertDataframe(result[[key]], targetType, mappings) #transformColnamesToCamelCase(dataframe = result[[key]])
        }
    }

    #json <- toJSON(result, pretty = TRUE, auto_unbox = TRUE)
    return(result)
}

getTreemap <- function(connection, outputDirName, sqlReplacements, entityName, mapping) {
    queryMap <- list()
    nameOfReport <-  paste(tolower(entityName), "s", sep ="")

    if (entityName == "Drug") {
        nameOfReport <- "drugexposures"
    }

    queryMap$report <- list(
      "sqlPath"=  paste("cohortresults-sql/",  tolower(entityName), "/sql" , entityName, "Treemap.sql", sep="")
      # "targetType"=fromJSON("./definitions/types/HierarchicalConcept.json"),
      # "mappings"=fromJSON("./definitions/mappings/ResultSetToHierarchicalConcept.json")$mappings
    )
    names(queryMap) <- c(nameOfReport)
    res <- (queryCohortAnalysesResults(queryMap, connection, sqlReplacements, mapping));
    writeToFile(paste(outputDirName, "/", tolower(entityName),"treemap.json", sep=""), toJSON(res, pretty = TRUE, auto_unbox = TRUE))
    return(res);
}

getDrillDownResults <- function(result, connection, outputDirName, sqlReplacements, entityName, packagingName, mapping) {

    dirName <- paste(outputDirName, packagingName, sep="/")
    dir.create(dirName)

    for (key in names(result)) {
        query <- result[[key]]
        for (conceptId in query$CONCEPT_ID) {
            # getXXXDrilldown()
            methodName <- paste("get", entityName, "Drilldown", sep="")

            sqlReplacements$conceptId <- conceptId;
            res <- do.call(methodName, list(connection, sqlReplacements, mapping))

            fileName <- paste(dirName, "/", toString(conceptId), ".json", sep="")
            writeToFile(fileName, res)
        }
    }
    zip(paste(dirName, "zip", sep = "."), dirName, flags = "-9Xrjm")
    unlink(dirName, recursive = TRUE, force = TRUE)
}

getAllByIndexDrillDownResults <- function(result, connection, outputDirName, sqlReplacements, entityName, packagingName, mapping) {
    dirName <- paste(outputDirName, packagingName, sep = "/")
    dir.create(dirName)
    for (key in names(result)) {
        query <- result[[key]]
        # getXXXDrilldown()
        methodName <- paste("get", entityName, "Drilldown", sep = "")
        results <- do.call(methodName, list(connection, sqlReplacements, mapping))
        for (conceptId in query$CONCEPT_ID) {
            conceptSlice <- list()
            for (keyname in names(results))
            {
                conceptSlice[[keyname]] <- subset(results[[keyname]], CONCEPT_ID == conceptId)
            }
            json <- toJSON(conceptSlice, pretty = TRUE, auto_unbox = TRUE)
            fileName <- paste0(dirName, "/", toString(conceptId), ".json")
            writeToFile(fileName, json)
        }
    }
    zip(paste(dirName, "zip", sep = "."), dirName, flags = "-9Xrjm")
    unlink(dirName, recursive = TRUE, force = TRUE)
}

processReport <- function(connection, outputDirName, sqlReplacements, entityName, packagingName, includeDrilldownReports){

    res <- do.call("getTreemap", list(connection, outputDirName, sqlReplacements, entityName, FALSE))
    if(includeDrilldownReports){
        getDrillDownResults(res, connection, outputDirName, sqlReplacements, entityName, packagingName, FALSE)
    }
}

writeAllResults <- function(connection, cdmDatabaseSchema, resultsDatabaseSchema, cohortId, outputDirName, includeDrilldownReports, includedReports) {

    sqlReplacements <- list(
      "ohdsi_database_schema" = resultsDatabaseSchema,
      "cohortDefinitionId" = cohortId,
      "cdm_database_schema" = cdmDatabaseSchema,
      "tableQualifier" = resultsDatabaseSchema
    )

    dir.create(outputDirName)
    if (includedReports$cohortSpecific){
        print(paste('Printing cohortspecific report', Sys.time(), sep=" : "))
        res <- getCohortSpecificSummary(connection, sqlReplacements, FALSE)
        writeToFile(paste(outputDirName, "cohortspecific.json", sep ="/"), res)
    }
    if (includedReports$death){
        print(paste('Printing death report', Sys.time(), sep=" : "))
        res <- getDeathSummary(connection, sqlReplacements, FALSE)
        writeToFile(paste(outputDirName, "death.json", sep ="/"), res)
    }
    if (includedReports$cohortObservationPeriod){
        print(paste('Printing cohortobservationperiod report', Sys.time(), sep=" : "))
        res <- getCohortObservationPeriod(connection, sqlReplacements, FALSE)
        writeToFile(paste(outputDirName, "cohortobservationperiod.json", sep ="/"), res)
    }
    if (includedReports$person){
        print(paste('Printing person report', Sys.time(), sep=" : "))
        res <- getPersonSummary(connection, sqlReplacements, FALSE)
        writeToFile(paste(outputDirName, "person.json", sep ="/"), res)
    }
    if (includedReports$dataCompleteness){
        print(paste('Printing datacompleteness report', Sys.time(), sep=" : "))
        res <- getDataCompleteness(connection, sqlReplacements, FALSE)
        writeToFile(paste(outputDirName, "datacompleteness.json", sep ="/"), toJSON(convertDataCompletenessData(res), pretty = TRUE, auto_unbox = TRUE))
    }
    if (includedReports$dashboard){
        print(paste('Printing dashboard report', Sys.time(), sep=" : "))
        res <- getDashboard(connection, sqlReplacements, FALSE)
        writeToFile(paste(outputDirName, "dashboard.json", sep ="/"), res)
    }
    if(includedReports$heraclesHeel){
        print(paste('Printing heraclesheel report', Sys.time(), sep=" : "))
        res <- getHeraclesHeel(connection, sqlReplacements, FALSE)
        writeToFile(paste(outputDirName, "heraclesheel.json", sep ="/"), res)
    }
    if(includedReports$entropy){
        print(paste('Printing entropy report', Sys.time(), sep=" : "))
        sqlReplacements$entroppAnalysisId <- 2031
        res2031 <- getEntropy(connection, sqlReplacements, FALSE)
        sqlReplacements$entroppAnalysisId <- 2032
        res2032 <- getEntropy(connection, sqlReplacements, FALSE)
        writeToFile(paste(outputDirName, "entropy.json", sep ="/"), toJSON(convertEntropyData(res2031, res2032), pretty = TRUE, auto_unbox = TRUE))
    }
    # treemap reports
    if(includedReports$conditionTreemap){
        print(paste('Printing condition treemap report', Sys.time(), sep=" : "))
        processReport(connection, outputDirName, sqlReplacements, "Condition", "conditions", includeDrilldownReports)
    }
    if(includedReports$drugEraTreemap){
        print(paste('Printing drug era treemap report', Sys.time(), sep=" : "))
        processReport(connection, outputDirName, sqlReplacements, "DrugEra", "drugeras", includeDrilldownReports)
    }
    if(includedReports$drugExposuresTreemap){
        print(paste('Printing drug exposures treemap report', Sys.time(), sep=" : "))
        res <- do.call("getTreemap", list(connection, outputDirName, sqlReplacements, "Drug", FALSE))
        writeToFile(paste(outputDirName, "drugtreemap.json", sep ="/"), toJSON(res, pretty = TRUE, auto_unbox = TRUE))
        if (includeDrilldownReports){
            print(paste('Printing all drug exposures drill down reports', Sys.time(), sep=" : "))
            getAllByIndexDrillDownResults(res, connection, outputDirName, sqlReplacements, "DrugExposure", "drugexposures", FALSE)
        }
    }
    if(includedReports$procedureTreemap){
        print(paste('Printing procedure treemap report', Sys.time(), sep=" : "))
        processReport(connection, outputDirName, sqlReplacements, "Procedure", "procedures", includeDrilldownReports)
    }
    if(includedReports$visitTreemap){
        print(paste('Printing visit treemap report', Sys.time(), sep=" : "))
        processReport(connection, outputDirName, sqlReplacements, "Visit", "visits", includeDrilldownReports)
    }
    if(includedReports$conditionEraTreemap){
        print(paste('Printing condition era treemap report', Sys.time(), sep=" : "))
        processReport(connection, outputDirName, sqlReplacements, "ConditionEra", "conditionera", includeDrilldownReports)
    }
    sqlReplacements$minCovariatePersonCount <- 10;
    sqlReplacements$minIntervalPersonCount <- 10;

    if(includedReports$conditionsByIndex){
        print(paste('Printing conditions by index report', Sys.time(), sep=" : "))
        res <- getConditionsByIndexTreemap(connection, sqlReplacements, FALSE)
        writeToFile(paste(outputDirName, "conditionsbyindextreemap.json", sep ="/"), toJSON(res, pretty = TRUE, auto_unbox = TRUE))
        if (includeDrilldownReports){
            getAllByIndexDrillDownResults(res, connection, outputDirName, sqlReplacements, "ConditionByIndex", "condbyindex", FALSE)
        }
    }
    if(includedReports$proceduresByIndex){
        print(paste('Printing procedures by index report', Sys.time(), sep=" : "))
        res <- getProceduresByIndexTreemap(connection, sqlReplacements, FALSE)
        writeToFile(paste(outputDirName, "proceduresbyindextreemap.json", sep ="/"), toJSON(res, pretty = TRUE, auto_unbox = TRUE))
        if (includeDrilldownReports){
            getAllByIndexDrillDownResults(res, connection, outputDirName, sqlReplacements, "ProcedureByIndex", "procbyindex", FALSE)
        }
    }
    if(includedReports$drugsByIndex){
        print(paste('Printing drugs by index report', Sys.time(), sep=" : "))
        res <- getDrugsByIndexTreemap(connection, sqlReplacements, FALSE)
        writeToFile(paste(outputDirName, "drugsbyindextreemap.json", sep ="/"), toJSON(res, pretty = TRUE, auto_unbox = TRUE))
        if (includeDrilldownReports){
            getAllByIndexDrillDownResults(res, connection, outputDirName, sqlReplacements, "DrugByIndex", "drugbyindex", FALSE)
        }
    }
}
