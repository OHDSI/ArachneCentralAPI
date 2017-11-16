# /definitions/types - using http://json-schema.org notation
# /definitions/mappings - using notation of Java's mapstruct library

  # Solves issue with Windows x64 (https://stackoverflow.com/questions/7019912/using-the-rjava-package-on-win7-64-bit-with-r)
  if (Sys.getenv("JAVA_HOME")!="")
    Sys.setenv(JAVA_HOME="")

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

  
  connectionDetails <- createConnectionDetails(dbms=dbms,
                                               connectionString=connectionString,
                                               user=user,
                                               password=password)
  connection <- connect(connectionDetails)
  
  # Setup variables
  
  cohortTable <- "cohort"
  #cohortId <- sample(1:10^8, 1)

  cohortId <- 1231666 #1231231

  #print("Calculating cohort")
  #
  #sql <- readSql(cohortDefinitionSqlPath)
  #sql <- renderSql(sql,
  #                 cdm_database_schema = cdmDatabaseSchema,
  #                 target_database_schema = resultsDatabaseSchema,
  #                 target_cohort_table = cohortTable,
  #                 target_cohort_id = cohortId)$sql
  #sql <- translateSql(sql, targetDialect = connectionDetails$dbms)$sql
  #executeSql(connection, sql)
  #
  #print("Creating Heracles results tables (if not exist)")
  #
  #heraclesTablesSql <- readSql("createHeraclesTables.sql")
  #heraclesTablesSql <- renderSql(heraclesTablesSql,
  #                               results_schema=resultsDatabaseSchema)$sql
  #heraclesTablesSql <- translateSql(heraclesTablesSql, targetDialect = connectionDetails$dbms)$sql
  #executeSql(connection, heraclesTablesSql)
  #
  #print("Running Cohort Characterization")
  #
  #heraclesAnalyses <- readSql("runHeraclesAnalyses.sql")
  #heraclesAnalyses <- renderSql(heraclesAnalyses,
  #                              CDM_schema=cdmDatabaseSchema,
  #                              results_schema=resultsDatabaseSchema,
  #                              cohort_table=cohortTable,
  #                              source_name=connectionString,
  #                              runHERACLESHeel = TRUE,
  #                              CDM_version=5,
  #                              cohort_definition_id=cohortId)$sql
  #heraclesAnalyses <- translateSql(heraclesAnalyses, targetDialect = connectionDetails$dbms)$sql
  #executeSql(connection, heraclesAnalyses)

  writeAllResults(dbms, connectionString, cdmDatabaseSchema, resultsDatabaseSchema,  user, password, cohortId)

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
  
  return (queryJsonCohortAnalysesResults(queryMap, connection, sqlReplacements, FALSE));
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

  return (queryJsonCohortAnalysesResults(queryMap, connection, sqlReplacements, FALSE));
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

  queryMap$periodPerPerson <- list(
         "sqlPath"="cohortresults-sql/observationperiod/periodsperperson.sql",
          "targetType"=fromJSON("./definitions/types/ConceptCount.json"),
          "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptCount.json")$mappings
  )

    sqlReplacements <- list(
    "ohdsi_database_schema"=resultsDatabaseSchema,
    "cohortDefinitionId"=cohortId,
    "cdm_database_schema" = cdmDatabaseSchema
    )

    return (queryJsonCohortAnalysesResults(queryMap, connection, sqlReplacements, FALSE));
}

getPersonSummary <- function(connection, resultsDatabaseSchema, cdmDatabaseSchema, cohortId) {
    queryMap <- list()

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

    return (queryJsonCohortAnalysesResults(queryMap, connection, sqlReplacements, FALSE));
}

getDataDensity <- function(connection, resultsDatabaseSchema, cdmDatabaseSchema, cohortId) {
    queryMap <- list()

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

    return (queryJsonCohortAnalysesResults(queryMap, connection, sqlReplacements, FALSE));
}

getDashboard <- function(connection, resultsDatabaseSchema, cdmDatabaseSchema, cohortId) {
    queryMap <- list()

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

    return (queryJsonCohortAnalysesResults(queryMap, connection, sqlReplacements, FALSE));
}

getDrugEraTreemap <- function(connection, resultsDatabaseSchema, cdmDatabaseSchema, cohortId) {
    queryMap <- list()

    queryMap$drugeras <- list(
         "sqlPath"="cohortresults-sql/drugera/sqlDrugEraTreemap.sql"
        # "targetType"=fromJSON("./definitions/types/HierarchicalConceptEra.json"),
        # HierarchicalConceptEraMapper
        # "mappings"=fromJSON("./definitions/mappings/ResultSetToHierarchicalConceptEra.json")$mappings
    )

    sqlReplacements <- list(
        "ohdsi_database_schema"=resultsDatabaseSchema,
        "cohortDefinitionId"=cohortId,
        "cdm_database_schema" = cdmDatabaseSchema
    )

    return (queryCohortAnalysesResults(queryMap, connection, sqlReplacements, FALSE));
}

getDrugEraDrilldown <- function(connection, resultsDatabaseSchema, cdmDatabaseSchema, cohortId, conceptId) {
    queryMap <- list()

    queryMap$ageAtFirstExposure <- list(
        "sqlPath"="cohortresults-sql/drugera/byConcept/sqlAgeAtFirstExposure.sql"
        # "targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
        # "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
    )

    queryMap$lengthOfEra <- list(
        "sqlPath"="cohortresults-sql/drugera/byConcept/sqlLengthOfEra.sql"
        # "targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
        # "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
    )
    queryMap$prevalenceByGenderAgeYear <- list(
        "sqlPath"="cohortresults-sql/drugera/byConcept/sqlPrevalenceByGenderAgeYear.sql"
        # "targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
        # "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
    )

    queryMap$prevalenceByMonth <- list(
        "sqlPath"="cohortresults-sql/drugera/byConcept/sqlPrevalenceByMonth.sql"
        # "targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
        # "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
    )

    sqlReplacements <- list(
        "ohdsi_database_schema"=resultsDatabaseSchema,
        "cohortDefinitionId"=cohortId,
        "cdm_database_schema"=cdmDatabaseSchema,
        "conceptId"=conceptId
    )

    return (queryJsonCohortAnalysesResults(queryMap, connection, sqlReplacements, FALSE));
}

getDrugExposuresTreemap <- function(connection, resultsDatabaseSchema, cdmDatabaseSchema, cohortId) {
    queryMap <- list()

    queryMap$drugexposures <- list(
        "sqlPath"="cohortresults-sql/drug/sqlDrugTreemap.sql"
        # "targetType"=fromJSON("./definitions/types/HierarchicalConceptEra.json"),
        # HierarchicalConceptEraMapper
        # "mappings"=fromJSON("./definitions/mappings/ResultSetToHierarchicalConceptEra.json")$mappings
    )

    sqlReplacements <- list(
        "ohdsi_database_schema"=resultsDatabaseSchema,
        "cohortDefinitionId"=cohortId,
        "cdm_database_schema" = cdmDatabaseSchema
    )

    return (queryCohortAnalysesResults(queryMap, connection, sqlReplacements, FALSE));
}

getDrugExposuresDrilldown <- function(connection, resultsDatabaseSchema, cdmDatabaseSchema, cohortId, conceptId) {
    queryMap <- list()

    queryMap$ageAtFirstExposure <- list(
        "sqlPath"="cohortresults-sql/drug/byConcept/sqlAgeAtFirstExposure.sql"
        # "targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
        # "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
    )

    queryMap$daysSupplyDistribution <- list(
        "sqlPath"="cohortresults-sql/drug/byConcept/sqlDaysSupplyDistribution.sql"
        # "targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
        # "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
    )
    queryMap$drugsByType <- list(
        "sqlPath"="cohortresults-sql/drug/byConcept/sqlDrugsByType.sql"
        # "targetType"=fromJSON("./definitions/types/ConceptCount.json"),
        # "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptCount.json")$mappings
    )

    queryMap$drugsByType <- list(
          "sqlPath"="cohortresults-sql/drug/byConcept/sqlDrugsByType.sql"
          # "targetType"=fromJSON("./definitions/types/ConceptCount.json"),
          # "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptCount.json")$mappings
     )

     queryMap$prevalenceByGenderAgeYear <- list(
              "sqlPath"="cohortresults-sql/drug/byConcept/sqlPrevalenceByGenderAgeYear.sql"
              # "targetType"=fromJSON("./definitions/types/ConceptDecile.json"),
              # "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptDecile.json")$mappings
      )
     queryMap$prevalenceByMonth <- list(
              "sqlPath"="cohortresults-sql/drug/byConcept/sqlPrevalenceByMonth.sql"
              # "targetType"=fromJSON("./definitions/types/PrevalanceConcept.json"),
              # "mappings"=fromJSON("./definitions/mappings/ResultSetToPrevalanceConcept.json")$mappings
      )

     queryMap$prevalenceByMonth <- list(
              "sqlPath"="cohortresults-sql/drug/byConcept/sqlPrevalenceByMonth.sql"
              # "targetType"=fromJSON("./definitions/types/PrevalanceConcept.json"),
              # "mappings"=fromJSON("./definitions/mappings/ResultSetToPrevalanceConcept.json")$mappings
      )

     queryMap$quantityDistribution <- list(
              "sqlPath"="cohortresults-sql/drug/byConcept/sqlQuantityDistribution.sql"
              # "targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
              # "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
      )

     queryMap$refillsDistribution <- list(
              "sqlPath"="cohortresults-sql/drug/byConcept/sqlRefillsDistribution.sql"
              # "targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
              # "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
      )

    sqlReplacements <- list(
        "ohdsi_database_schema"=resultsDatabaseSchema,
        "cohortDefinitionId"=cohortId,
        "cdm_database_schema"=cdmDatabaseSchema,
        "conceptId"=conceptId
    )

    return (queryJsonCohortAnalysesResults(queryMap, connection, sqlReplacements, FALSE));
}

getProcedureTreemap <- function(connection, resultsDatabaseSchema, cdmDatabaseSchema, cohortId) {
    queryMap <- list()

    queryMap$procedures <- list(
        "sqlPath"="cohortresults-sql/procedure/sqlProcedureTreemap.sql"
        # "targetType"=fromJSON("./definitions/types/HierarchicalConcept.json"),
        # "mappings"=fromJSON("./definitions/mappings/ResultSetToHierarchicalConcept.json")$mappings
    )

    sqlReplacements <- list(
        "ohdsi_database_schema"=resultsDatabaseSchema,
        "cohortDefinitionId"=cohortId,
        "cdm_database_schema" = cdmDatabaseSchema
    )

    return (queryCohortAnalysesResults(queryMap, connection, sqlReplacements, FALSE));
}

getProcedureDrilldown <- function(connection, resultsDatabaseSchema, cdmDatabaseSchema, cohortId, conceptId) {
    queryMap <- list()

    queryMap$ageAtFirstOccurrence <- list(
        "sqlPath"="cohortresults-sql/procedure/byConcept/sqlAgeAtFirstOccurrence.sql"
        # "targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
        # "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
    )

     queryMap$proceduresByType <- list(
         "sqlPath"="cohortresults-sql/procedure/byConcept/sqlProceduresByType.sql"
        # "targetType"=fromJSON("./definitions/types/ConceptCount.json"),
        # "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptCount.json")$mappings
     )

    queryMap$prevalenceByGenderAgeYear <- list(
       "sqlPath"="cohortresults-sql/procedure/byConcept/sqlPrevalenceByGenderAgeYear.sql"
      # "targetType"=fromJSON("./definitions/types/ConceptDecile.json"),
      # "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptDecile.json")$mappings
    )

     queryMap$prevalenceByMonth <- list(
      "sqlPath"="cohortresults-sql/procedure/byConcept/sqlPrevalenceByMonth.sql"
      # "targetType"=fromJSON("./definitions/types/PrevalanceConcept.json"),
      # "mappings"=fromJSON("./definitions/mappings/ResultSetToPrevalanceConcept.json")$mappings
    )

    sqlReplacements <- list(
        "ohdsi_database_schema"=resultsDatabaseSchema,
        "cohortDefinitionId"=cohortId,
        "cdm_database_schema"=cdmDatabaseSchema,
        "conceptId"=conceptId
    )

    return (queryJsonCohortAnalysesResults(queryMap, connection, sqlReplacements, FALSE));
}

getVisitTreemap <- function(connection, resultsDatabaseSchema, cdmDatabaseSchema, cohortId) {
    queryMap <- list()

    queryMap$visits <- list(
        "sqlPath"="cohortresults-sql/visit/sqlVisitTreemap.sql"
        # "targetType"=fromJSON("./definitions/types/HierarchicalConcept.json"),
        # "mappings"=fromJSON("./definitions/mappings/ResultSetToHierarchicalConcept.json")$mappings
    )

    sqlReplacements <- list(
        "ohdsi_database_schema"=resultsDatabaseSchema,
        "cohortDefinitionId"=cohortId,
        "cdm_database_schema" = cdmDatabaseSchema
    )

    return (queryCohortAnalysesResults(queryMap, connection, sqlReplacements, FALSE));
}

getVisitDrilldown <- function(connection, resultsDatabaseSchema, cdmDatabaseSchema, cohortId, conceptId) {
    queryMap <- list()

    queryMap$ageAtFirstOccurrence <- list(
        "sqlPath"="cohortresults-sql/visit/byConcept/sqlAgeAtFirstOccurrence.sql"
        # "targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
        # "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
    )

     queryMap$visitDurationByType <- list(
         "sqlPath"="cohortresults-sql/visit/byConcept/sqlVisitDurationByType.sql"
        # "targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
        # "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
     )

    queryMap$prevalenceByGenderAgeYear <- list(
       "sqlPath"="cohortresults-sql/visit/byConcept/sqlPrevalenceByGenderAgeYear.sql"
      # "targetType"=fromJSON("./definitions/types/ConceptDecile.json"),
      # "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptDecile.json")$mappings
    )

     queryMap$prevalenceByMonth <- list(
      "sqlPath"="cohortresults-sql/visit/byConcept/sqlPrevalenceByMonth.sql"
      # "targetType"=fromJSON("./definitions/types/PrevalanceConcept.json"),
      # "mappings"=fromJSON("./definitions/mappings/ResultSetToPrevalanceConcept.json")$mappings
    )

    sqlReplacements <- list(
        "ohdsi_database_schema"=resultsDatabaseSchema,
        "cohortDefinitionId"=cohortId,
        "cdm_database_schema"=cdmDatabaseSchema,
        "conceptId"=conceptId
    )

    return (queryJsonCohortAnalysesResults(queryMap, connection, sqlReplacements, FALSE));
}

getConditionTreemap <- function(connection, resultsDatabaseSchema, cdmDatabaseSchema, cohortId) {
    queryMap <- list()

    queryMap$conditions <- list(
        "sqlPath"="cohortresults-sql/condition/sqlConditionTreemap.sql"
        # "targetType"=fromJSON("./definitions/types/HierarchicalConcept.json"),
        # "mappings"=fromJSON("./definitions/mappings/ResultSetToHierarchicalConcept.json")$mappings
    )

    sqlReplacements <- list(
        "ohdsi_database_schema"=resultsDatabaseSchema,
        "cohortDefinitionId"=cohortId,
        "cdm_database_schema" = cdmDatabaseSchema
    )

    return (queryCohortAnalysesResults(queryMap, connection, sqlReplacements, FALSE));
}

getConditionDrilldown <- function(connection, resultsDatabaseSchema, cdmDatabaseSchema, cohortId, conceptId) {
    queryMap <- list()
    #getCohortSpecificTreemapResults

    queryMap$ageAtFirstDiagnosis <- list(
        "sqlPath"="cohortresults-sql/condition/byConcept/sqlAgeAtFirstDiagnosis.sql"
        # "targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
        # "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
    )

     queryMap$sqlConditionsByType <- list(
      "sqlPath"="cohortresults-sql/condition/byConcept/sqlConditionsByType.sql"
      # "targetType"=fromJSON("./definitions/types/ConceptConditionCount.json"),
      # "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptConditionCount.json")$mappings
     )

     queryMap$prevalenceByGenderAgeYear <- list(
         "sqlPath"="cohortresults-sql/condition/byConcept/sqlPrevalenceByGenderAgeYear.sql"
        # "targetType"=fromJSON("./definitions/types/ConceptDecile.json"),
        # "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptDecile.json")$mappings
     )

     queryMap$prevalenceByMonth <- list(
         "sqlPath"="cohortresults-sql/condition/byConcept/sqlPrevalenceByMonth.sql"
        # "targetType"=fromJSON("./definitions/types/PrevalanceConcept.json"),
        # "mappings"=fromJSON("./definitions/mappings/ResultSetToPrevalanceConcept.json")$mappings
     )

    sqlReplacements <- list(
        "ohdsi_database_schema"=resultsDatabaseSchema,
        "cohortDefinitionId"=cohortId,
        "cdm_database_schema" = cdmDatabaseSchema,
        "conceptId" = conceptId
    )

    return (queryJsonCohortAnalysesResults(queryMap, connection, sqlReplacements, FALSE));
}

getConditionEraTreemap <- function(connection, resultsDatabaseSchema, cdmDatabaseSchema, cohortId) {
    queryMap <- list()

    queryMap$conditioneras <- list(
        "sqlPath"="cohortresults-sql/conditionera/sqlConditionEraTreemap.sql"
        # "targetType"=fromJSON("./definitions/types/HierarchicalConcept.json"),
        # "mappings"=fromJSON("./definitions/mappings/ResultSetToHierarchicalConcept.json")$mappings
    )

    sqlReplacements <- list(
        "ohdsi_database_schema"=resultsDatabaseSchema,
        "cohortDefinitionId"=cohortId,
        "cdm_database_schema" = cdmDatabaseSchema
    )

    return (queryCohortAnalysesResults(queryMap, connection, sqlReplacements, FALSE));
}

getConditionEraDrilldown <- function(connection, resultsDatabaseSchema, cdmDatabaseSchema, cohortId, conceptId) {
    queryMap <- list()
    #getCohortSpecificTreemapResults

    queryMap$ageAtFirstDiagnosis <- list(
        "sqlPath"="cohortresults-sql/conditionera/byConcept/sqlAgeAtFirstDiagnosis.sql"
        # "targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
        # "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
    )

    queryMap$lengthOfEra <- list(
      "sqlPath"="cohortresults-sql/conditionera/byConcept/sqlLengthOfEra.sql"
      # "targetType"=fromJSON("./definitions/types/ConceptConditionCount.json"),
      # "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptConditionCount.json")$mappings
    )

    queryMap$prevalenceByGenderAgeYear <- list(
        "sqlPath"="cohortresults-sql/conditionera/byConcept/sqlPrevalenceByGenderAgeYear.sql"
        # "targetType"=fromJSON("./definitions/types/ConceptDecile.json"),
        # "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptDecile.json")$mappings
    )

    queryMap$prevalenceByMonth <- list(
        "sqlPath"="cohortresults-sql/conditionera/byConcept/sqlPrevalenceByMonth.sql"
        # "targetType"=fromJSON("./definitions/types/PrevalanceConcept.json"),
        # "mappings"=fromJSON("./definitions/mappings/ResultSetToPrevalanceConcept.json")$mappings
    )

    sqlReplacements <- list(
        "ohdsi_database_schema"=resultsDatabaseSchema,
        "cohortDefinitionId"=cohortId,
        "cdm_database_schema" = cdmDatabaseSchema,
        "conceptId" = conceptId
    )

    return (queryJsonCohortAnalysesResults(queryMap, connection, sqlReplacements, FALSE));
}


getObservationTreemap <- function(connection, resultsDatabaseSchema, cdmDatabaseSchema, cohortId) {
    queryMap <- list()

    queryMap$observations <- list(
        "sqlPath"="cohortresults-sql/observation/sqlObservationTreemap.sql"
        # "targetType"=fromJSON("./definitions/types/HierarchicalConcept.json"),
        # "mappings"=fromJSON("./definitions/mappings/ResultSetToHierarchicalConcept.json")$mappings
    )

    sqlReplacements <- list(
        "ohdsi_database_schema"=resultsDatabaseSchema,
        "cohortDefinitionId"=cohortId,
        "cdm_database_schema" = cdmDatabaseSchema
    )

    return (queryCohortAnalysesResults(queryMap, connection, sqlReplacements, FALSE));
}

getObservationDrilldown <- function(connection, resultsDatabaseSchema, cdmDatabaseSchema, cohortId, conceptId) {
    queryMap <- list()
    #getCohortSpecificTreemapResults

    queryMap$ageAtFirstOccurrence <- list(
        "sqlPath"="cohortresults-sql/observation/byConcept/sqlAgeAtFirstOccurrence.sql"
        # "targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
        # "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
    )

    queryMap$observationValueDistribution <- list(
      "sqlPath"="cohortresults-sql/observation/byConcept/sqlObservationValueDistribution.sql"
      # "targetType"=fromJSON("./definitions/types/ConceptQuartile.json"),
      # "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptQuartile.json")$mappings
    )

    queryMap$observationsByType <- list(
        "sqlPath"="cohortresults-sql/observation/byConcept/sqlObservationsByType.sql"
        # "targetType"=fromJSON("./definitions/types/ConceptObservationCount.json"),
        # "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptObservationCount.json")$mappings
    )

    queryMap$recordsByUnit <- list(
        "sqlPath"="cohortresults-sql/observation/byConcept/sqlRecordsByUnit.sql"
        # "targetType"=fromJSON("./definitions/types/ConceptObservationCount.json"),
        # "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptObservationCount.json")$mappings
    )

    queryMap$prevalenceByGenderAgeYear <- list(
        "sqlPath"="cohortresults-sql/observation/byConcept/sqlPrevalenceByGenderAgeYear.sql"
        # "targetType"=fromJSON("./definitions/types/ConceptDecile.json"),
        # "mappings"=fromJSON("./definitions/mappings/ResultSetToConceptDecile.json")$mappings
    )

   queryMap$prevalenceByMonth <- list(
            "sqlPath"="cohortresults-sql/observation/byConcept/sqlPrevalenceByMonth.sql"
            # "targetType"=fromJSON("./definitions/types/PrevalanceConceptName.json"),
            # "mappings"=fromJSON("./definitions/mappings/ResultSetToPrevalanceConceptName.json")$mappings
   )


    sqlReplacements <- list(
        "ohdsi_database_schema"=resultsDatabaseSchema,
        "cohortDefinitionId"=cohortId,
        "cdm_database_schema" = cdmDatabaseSchema,
        "conceptId" = conceptId
    )

    return (queryJsonCohortAnalysesResults(queryMap, connection, sqlReplacements, FALSE));
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

queryJsonCohortAnalysesResults <- function(queryMap, connection, sqlReplacements, convert) {

  result <- queryCohortAnalysesResults(queryMap, connection, sqlReplacements, convert)

  json <- toJSON(result, pretty = TRUE, auto_unbox = TRUE)
  return(json)
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

  #json <- toJSON(result, pretty = TRUE, auto_unbox = TRUE)
  return(result)
}

queryProcedureResults <- function(result, connection, cohortId) {

  dir.create("../output_1231666/procedures")

  for (key in names(result)) {
    query <- result[[key]]

    for (conceptId in query$CONCEPT_ID  ) {

        namef<-paste("../output/procedures/",  ".json", sep=toString(conceptId))
        res <- getProcedureDrilldown(connection, resultsDatabaseSchema = "results", cdmDatabaseSchema = "public", cohortId, conceptId)
        writeToFile(namef, res)
    }
  }
}

queryVisitResults <- function(result, connection, cohortId) {

  dir.create("../output_1231666/visits")

  for (key in names(result)) {
    query <- result[[key]]

    for (conceptId in query$CONCEPT_ID) {

        namef<-paste("../output/visits/",  ".json", sep=toString(conceptId))
        res <- getVisitDrilldown(connection, resultsDatabaseSchema = "results", cdmDatabaseSchema = "public", cohortId, conceptId)
        writeToFile(namef, res)
    }
  }
}

queryConditionResults <- function(result, connection, cohortId) {

  dir.create("../output_1231666/conditions")

  for (key in names(result)) {
    query <- result[[key]]

    for (conceptId in query$CONCEPT_ID) {

        namef<-paste("../output_1231666/conditions/",  ".json", sep=toString(conceptId))
        res <- getConditionDrilldown(connection, resultsDatabaseSchema = "results", cdmDatabaseSchema = "public", cohortId, conceptId)
        writeToFile(namef, res)
    }
  }
}

queryConditionEraResults <- function(result, connection, cohortId) {

  dir.create("../output_1231666/conditioneras")

  for (key in names(result)) {
    query <- result[[key]]

    for (conceptId in query$CONCEPT_ID) {

        namef<-paste("../output/conditioneras/",  ".json", sep=toString(conceptId))
        res <- getConditionEraDrilldown(connection, resultsDatabaseSchema = "results", cdmDatabaseSchema = "public", cohortId, conceptId)
        writeToFile(namef, res)
    }
  }
}

queryDrugExposuresResults <- function(result, connection, cohortId) {

   dir.create("../output_1231666/drugexposures")

   for (key in names(result)) {
       query <- result[[key]]
       for (conceptId in query$CONCEPT_ID) {

           namef<-paste("../output/drugexposures/",  ".json", sep=toString(conceptId))
           res <- getDrugExposuresDrilldown(connection, resultsDatabaseSchema = "results", cdmDatabaseSchema = "public", cohortId, conceptId)
           writeToFile(namef, res)
       }
   }
}

queryDrugEraResults <- function(result, connection, cohortId) {

   dir.create("../output_1231666/drugeras")

   for (key in names(result)) {
       query <- result[[key]]
       for (conceptId in query$CONCEPT_ID) {

           namef<-paste("../output/drugeras/",  ".json", sep=toString(conceptId))
           res <- getDrugEraDrilldown(connection, resultsDatabaseSchema = "results", cdmDatabaseSchema = "public", cohortId, conceptId)
           writeToFile(namef, res)
       }
   }
}

queryObservationResults <- function(result, connection, cohortId) {

   dir.create("../output_1231666/observations")

   for (key in names(result)) {
       query <- result[[key]]
       for (conceptId in query$CONCEPT_ID) {

           namef<-paste("../output/observations/",  ".json", sep=toString(conceptId))
           res <- getObservationDrilldown(connection, resultsDatabaseSchema = "results", cdmDatabaseSchema = "public", cohortId, conceptId)
           writeToFile(namef, res)
       }
   }
}

writeAllResults <- function(dbms, connectionString, cdmDatabaseSchema, resultsDatabaseSchema, user, password, cohortId) {

library(DatabaseConnector)
library(SqlRender)
library(jsonlite)
library(dplyr)

connectionDetails <- createConnectionDetails(dbms=dbms,
                                             connectionString=connectionString,
                                             user=user,
                                             password=password)
connection <- connect(connectionDetails)
dir.create("../output_1231666")

#res <- getCohortSpecificSummary(connection, resultsDatabaseSchema, cdmDatabaseSchema, cohortId)
#writeToFile("../output_1231666/cohortspecific.json", res)

#res <- getDeathSummary(connection, resultsDatabaseSchema, cdmDatabaseSchema, cohortId)
#writeToFile("../output_1231666/death.json", res)

#res <- getCohortObservationPeriod(connection, resultsDatabaseSchema, cdmDatabaseSchema , cohortId)
#writeToFile("../output_1231666/cohortobservationperiod.json", res)

#res <- getPersonSummary(connection, resultsDatabaseSchema, cdmDatabaseSchema , cohortId)
#writeToFile("../output_1231666/person.json", res)

#res <- getDataDensity(connection, resultsDatabaseSchema, cdmDatabaseSchema, cohortId)
#writeToFile("../output_1231666/datadensity.json", res)

#res <- getDashboard(connection, resultsDatabaseSchema, cdmDatabaseSchema, cohortId)
#writeToFile("../output_1231666/dashboard.json", res)

## todo
#res <- getConditionResults(connection, resultsDatabaseSchema, cdmDatabaseSchema , cohortId)
#writeToFile("../output_1231666/conditionresults.json", res)

res <- getConditionTreemap(connection, resultsDatabaseSchema, cdmDatabaseSchema , cohortId)
writeToFile("../output_1231666/conditiontreemap.json", toJSON(res, pretty = TRUE, auto_unbox = TRUE))
queryConditionResults(res, connection, cohortId)

res <- getDrugEraTreemap(connection, resultsDatabaseSchema, cdmDatabaseSchema , cohortId)
writeToFile("../output_1231666/drugeratreemap.json", toJSON(res, pretty = TRUE, auto_unbox = TRUE))
queryDrugEraResults(res, connection, cohortId)

res <- getDrugExposuresTreemap(connection, resultsDatabaseSchema, cdmDatabaseSchema, cohortId)
writeToFile("../output_1231666/drugexposurestreemap.json", toJSON(res, pretty = TRUE, auto_unbox = TRUE))
queryDrugExposuresResults(res, connection, cohortId)

res <- getProcedureTreemap(connection, resultsDatabaseSchema , cdmDatabaseSchema, cohortId)
writeToFile("../output_1231666/proceduretreemap.json", toJSON(res, pretty = TRUE, auto_unbox = TRUE))
queryProcedureResults(res, connection, cohortId)

#res <- getVisitTreemap(connection, resultsDatabaseSchema , cdmDatabaseSchema, cohortId)
#writeToFile("../output_1231666/visittreemap.json", toJSON(res, pretty = TRUE, auto_unbox = TRUE))
#queryVisitResults(res, connection, cohortId)

#res <- getConditionEraTreemap(connection, resultsDatabaseSchema , cdmDatabaseSchema, cohortId)
#writeToFile("../output_1231666/conditioneratreemap.json", toJSON(res, pretty = TRUE, auto_unbox = TRUE))
#queryConditionEraResults(res, connection, cohortId)

#res <- getObservationTreemap(connection, resultsDatabaseSchema , cdmDatabaseSchema, cohortId)
#writeToFile("../output_1231666/observationstreemap.json", toJSON(res, pretty = TRUE, auto_unbox = TRUE))
#queryObservationResults(res, connection, cohortId)

}

 workDir <- getwd();

 run_cohort_characterization(
   file.path(workDir, "cohort_with_drug_era.sql"),
   file.path(workDir, "output"),
   "postgresql",
   "jdbc:postgresql://odysseusovh02.odysseusinc.com:5432/cdm_v500_synpuf_v101_110k",
   "ohdsi",
   "ohdsi",
   "public",
   "results"
 )