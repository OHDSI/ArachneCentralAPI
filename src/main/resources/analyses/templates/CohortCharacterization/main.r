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


getCohortSpecificSummary <- function(connection, resultsDatabaseSchema, cdmDatabaseSchema, sqlReplacements, mapping, cohortId) {
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
  
  return (queryJsonCohortAnalysesResults(queryMap, connection, sqlReplacements, mapping));
}

getDeathSummary <- function(connection, resultsDatabaseSchema, cdmDatabaseSchema, sqlReplacements, mapping, cohortId) {
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

  return (queryJsonCohortAnalysesResults(queryMap, connection, sqlReplacements, mapping));
}


getCohortObservationPeriod <- function(connection, resultsDatabaseSchema, cdmDatabaseSchema, sqlReplacements, mapping, cohortId) {
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

  return (queryJsonCohortAnalysesResults(queryMap, connection, sqlReplacements, mapping));
}

getPersonSummary <- function(connection, resultsDatabaseSchema, cdmDatabaseSchema, sqlReplacements, mapping, cohortId) {
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

    return (queryJsonCohortAnalysesResults(queryMap, connection, sqlReplacements, mapping));
}

getDataDensity <- function(connection, resultsDatabaseSchema, cdmDatabaseSchema, sqlReplacements, mapping, cohortId) {
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

    return (queryJsonCohortAnalysesResults(queryMap, connection, sqlReplacements, mapping));
}

getDashboard <- function(connection, resultsDatabaseSchema, cdmDatabaseSchema, sqlReplacements, mapping, cohortId) {
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

    return (queryJsonCohortAnalysesResults(queryMap, connection, sqlReplacements, mapping));
}

# drilldown reports

getDrugEraDrilldown <- function(connection, resultsDatabaseSchema, cdmDatabaseSchema, sqlReplacements, mapping, cohortId) {
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

    return (queryJsonCohortAnalysesResults(queryMap, connection, sqlReplacements, mapping));
}

getDrugExposureDrilldown <- function(connection, resultsDatabaseSchema, cdmDatabaseSchema, sqlReplacements, mapping, cohortId) {
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

    return (queryJsonCohortAnalysesResults(queryMap, connection, sqlReplacements, mapping));
}

getProcedureDrilldown <- function(connection, resultsDatabaseSchema, cdmDatabaseSchema, sqlReplacements, mapping, cohortId) {
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

    return (queryJsonCohortAnalysesResults(queryMap, connection, sqlReplacements, mapping));
}

getVisitDrilldown <- function(connection, resultsDatabaseSchema, cdmDatabaseSchema, sqlReplacements, mapping, cohortId) {
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

    return (queryJsonCohortAnalysesResults(queryMap, connection, sqlReplacements, mapping));
}

getConditionDrilldown <- function(connection, resultsDatabaseSchema, cdmDatabaseSchema, sqlReplacements, mapping, cohortId) {
    queryMap <- list()

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

    return (queryJsonCohortAnalysesResults(queryMap, connection, sqlReplacements, mapping));
}

getConditionEraDrilldown <- function(connection, resultsDatabaseSchema, cdmDatabaseSchema, sqlReplacements, mapping, cohortId) {
    queryMap <- list()

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

    return (queryJsonCohortAnalysesResults(queryMap, connection, sqlReplacements, mapping));
}

getObservationDrilldown <- function(connection, resultsDatabaseSchema, cdmDatabaseSchema, sqlReplacements, mapping, cohortId) {
    queryMap <- list()

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

    return (queryJsonCohortAnalysesResults(queryMap, connection, sqlReplacements, mapping));
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
    sql <- do.call(renderSql, c(list(sql=sql), sqlReplacements))$sql
    sql <- translateSql(sql, targetDialect = dbms)$sql
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

getDrillDownResults <- function(result, connection, resultsDatabaseSchema, cdmDatabaseSchema, outputDirName, sqlReplacements, entityName, mapping, cohortId) {
   nameOfReport <-  paste(tolower(entityName), "s", sep ="")
   dirName <- paste(outputDirName, nameOfReport, sep="/")
   dir.create(dirName)

   for (key in names(result)) {
       query <- result[[key]]
       for (conceptId in query$CONCEPT_ID) {

           # getXXXDrilldown()
           methodName <- paste("get", entityName, "Drilldown", sep="")

           sqlReplacements$conceptId <- conceptId;
           res <- do.call(methodName, list(connection, resultsDatabaseSchema, cdmDatabaseSchema, sqlReplacements, mapping, cohortId))

           fileName <- paste(dirName, "/", toString(conceptId), ".json", sep="")
           writeToFile(fileName, res)
       }
   }
}

processReport <- function(connection, resultsDatabaseSchema, cdmDatabaseSchema, outputDirName, sqlReplacements, entityName, cohortId){

  res <- do.call("getTreemap", list(connection, outputDirName, sqlReplacements, entityName, FALSE))
  do.call("getDrillDownResults", list(res, connection, resultsDatabaseSchema, cdmDatabaseSchema, outputDirName, sqlReplacements, entityName, FALSE, cohortId))

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

  sqlReplacements <- list(
    "ohdsi_database_schema"=resultsDatabaseSchema,
    "cohortDefinitionId"=cohortId,
    "cdm_database_schema" = cdmDatabaseSchema
  )

  outputDirName <- "../output_test"
  dir.create(outputDirName)

  #res <- getCohortSpecificSummary(connection, resultsDatabaseSchema, cdmDatabaseSchema, sqlReplacements, FALSE, cohortId)
  #writeToFile(paste(outputDirName, "cohortspecific.json", sep ="/"), res)
  #
  #res <- getDeathSummary(connection, resultsDatabaseSchema, cdmDatabaseSchema, sqlReplacements, FALSE, cohortId)
  #writeToFile(paste(outputDirName, "death.json", sep ="/"), res)
  #
  #res <- getCohortObservationPeriod(connection, resultsDatabaseSchema, cdmDatabaseSchema, sqlReplacements, FALSE, cohortId)
  #writeToFile(paste(outputDirName, "cohortobservationperiod.json", sep ="/"), res)
  #
  #res <- getPersonSummary(connection, resultsDatabaseSchema, cdmDatabaseSchema, sqlReplacements, FALSE, cohortId)
  #writeToFile(paste(outputDirName, "person.json", sep ="/"), res)
  #
  #res <- getDataDensity(connection, resultsDatabaseSchema, cdmDatabaseSchema, sqlReplacements, FALSE, cohortId)
  #writeToFile(paste(outputDirName, "datadensity.json", sep ="/"), res)
  #
  #res <- getDashboard(connection, resultsDatabaseSchema, cdmDatabaseSchema, sqlReplacements, FALSE, cohortId)
  #writeToFile(paste(outputDirName, "dashboard.json", sep ="/"), res)

  ## todo
  #res <- getConditionResults(connection, resultsDatabaseSchema, cdmDatabaseSchema, cohortId)
  #writeToFile(paste(outputDirName, "conditionresults.json", sep =""/), res)

  #processReport(connection, resultsDatabaseSchema, cdmDatabaseSchema, outputDirName, sqlReplacements, "Condition", cohortId)
  #processReport(connection, resultsDatabaseSchema, cdmDatabaseSchema, outputDirName, sqlReplacements, "DrugEra", cohortId)

  #DrugExposures
  #res <- do.call("getTreemap", list(connection, outputDirName, sqlReplacements, "Drug", FALSE))
  #do.call("getDrillDownResults", list(res, connection, resultsDatabaseSchema, cdmDatabaseSchema, outputDirName, sqlReplacements, "DrugExposure", FALSE, cohortId))

  #processReport(connection, resultsDatabaseSchema, cdmDatabaseSchema, outputDirName, sqlReplacements, "Procedure", cohortId)
  #processReport(connection, resultsDatabaseSchema, cdmDatabaseSchema, outputDirName, sqlReplacements, "Visit", cohortId)
  #processReport(connection, resultsDatabaseSchema, cdmDatabaseSchema, outputDirName, sqlReplacements, "ConditionEra", cohortId)
  #processReport(connection, resultsDatabaseSchema, cdmDatabaseSchema, outputDirName, sqlReplacements, "Observation", cohortId)

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