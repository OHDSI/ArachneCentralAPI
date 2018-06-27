run_plp_analysis <-function(basicDir, analysisDescriptionFile, cohortDefinitionPath, outcomeDefinitionPath, dbms, connectionString, user, password, cdmDatabaseSchema, cohortsDatabaseSchema, cohortTable = "cohort", outcomeTable = "cohort", cdmVersion="5"){
  #Inputs:
  # basicDir - folder where packrat was unbundled and will be used to store analysis results
  # cohortDefinitionPath - path to sql file with target cohort description
  # outcomeDefinitionPath - path to sql file with outcome cohort description
  # dbms - type of DBMS server (possible string values ""mysql", "oracle", "postgresql", "redshift", "sql server", "pdw", "netezza", "bigquery")
  # connectionString - JDBC connection string ()
  # user - user name
  # password - user password
  # cdmDatabaseSchema - name of the schema with CDM data
  # cohortsDatabaseSchema - name of schema to store cohorts
  # cohortTable - name of table with exposure cohort (default value "cohort"), 
  # outcomeTable - name of table with outcome cohort (default value "cohort") 
  # cdmVersion - OMOP CDM version: currently support "4" an "5" (default value "5")
  
  # Outputs: 
  # None
  
  start.time <- Sys.time()
  
  
  # Load the PatientLevelPrediction library
  library(PatientLevelPrediction) 
  library(CohortMethod)
  library(SqlRender)
  # Data extraction ----
  
  # TODO: Insert your connection details here
  if ("impala" == dbms){
    driverPath <- Sys.getenv("IMPALA_DRIVER_PATH")
    if (missing(driverPath) || is.null(driverPath) || driverPath == ''){
      driverPath <- "/impala"
    }
    connectionDetails <- DatabaseConnector::createConnectionDetails(dbms = dbms,
    connectionString = connectionString,
    user = user,
    password = password,
    pathToDriver = driverPath)
  } else {
    connectionDetails <- DatabaseConnector::createConnectionDetails(dbms = dbms,
                                                                  connectionString = connectionString,
                                                                  user = user,
                                                                  password = password)
  }
  connection <- DatabaseConnector::connect(connectionDetails) 
  
  analysisSettings <- CohortMethod::loadCmAnalysisList(analysisDescriptionFile)
  plpDataPath <- file.path(basicDir , "plp_data")
  plpModelPath = file.path(basicDir, "plp_model")
  plpResultsPath = file.path(basicDir, "plp_results")

  randId <- sample(1e6, 2) # generating array of random integers in range from 0 to 1e6
  targetCohortId <- randId[1]
  outcomeCohortId <- randId[2]
  outcomeList <- c(outcomeCohortId)
  
  # PLEASE NOTE ----
  # If you want to use your code in a distributed network study
  # you will need to create a temporary cohort table with common cohort IDs.
  # The code below ASSUMES you are only running in your local network 
  # where common cohort IDs have already been assigned in the cohort table.
  
  # Get all  Concept IDs for exclusion ----
  if (length(analysisSettings$cvExclusionConceptSet) == 0){
    excludedConcepts <- c()
  }else{
    sql <- analysisSettings$cvExclusionConceptSetSQL
    sql <- SqlRender::renderSql(sql, cdm_database_schema = cdmDatabaseSchema)$sql
    sql <- SqlRender::translateSql(sql, targetDialect = connectionDetails$dbms)$sql
    excludedConcepts <- querySql(connection, sql)
    excludedConcepts <- excludedConcepts$CONCEPT_ID
  }
  
  
  # Get all  Concept IDs for inclusion ----
  if (length(analysisSettings$cvInclusionConceptSet) == 0){
    includedConcepts <- c()
  }else{
    sql <- analysisSettings$cvInclusionConceptSetSQL
    sql <- SqlRender::renderSql(sql, cdm_database_schema = cdmDatabaseSchema)$sql
    sql <- SqlRender::translateSql(sql, targetDialect = connectionDetails$dbms)$sql
    includedConcepts <- querySql(connection, sql)
    includedConcepts <- includedConcepts$CONCEPT_ID
  }
  
  sql <- readSql(cohortDefinitionPath)
  sql <- renderSql(sql,
                   cdm_database_schema = cdmDatabaseSchema,
                   target_database_schema = cohortsDatabaseSchema,
                   target_cohort_table = cohortTable,
                   target_cohort_id = targetCohortId,
                   output = "output")$sql
  sql <- translateSql(sql, targetDialect = connectionDetails$dbms)$sql
  executeSql(connection, sql)
  
  
  sql <- readSql(outcomeDefinitionPath)
  sql <- renderSql(sql,
                   cdm_database_schema = cdmDatabaseSchema,
                   target_database_schema = cohortsDatabaseSchema,
                   target_cohort_table = outcomeTable,
                   target_cohort_id = outcomeCohortId,
                   output = "output")$sql
  sql <- translateSql(sql, targetDialect = connectionDetails$dbms)$sql
  executeSql(connection, sql)
  
  
  if (!is.null(analysisSettings$sampleSize)){
    analysisSettings$sampleSize = as.numeric(analysisSettings$sampleSize)
  }
  if(analysisSettings$moSeed == "NULL"){
    analysisSettings$moSeed = NULL   
  }
  if (!is.null(analysisSettings$nFold)){
    analysisSettings$nFold=as.numeric(analysisSettings$nFold)
  }else{
    analysisSettings$nFold = 3
  }
  
  # Define which types of covariates must be constructed ----
  covariateSettings <- FeatureExtraction::createCovariateSettings(useCovariateDemographics = analysisSettings$cvDemographics,
                                               useCovariateDemographicsGender = analysisSettings$cvDemographicsGender,
                                               useCovariateDemographicsRace = analysisSettings$cvDemographicsRace,
                                               useCovariateDemographicsEthnicity = analysisSettings$cvDemographicsEthnicity,
                                               useCovariateDemographicsAge = analysisSettings$cvDemographicsAge, 
                                               useCovariateDemographicsYear = analysisSettings$cvDemographicsYear,
                                               useCovariateDemographicsMonth = analysisSettings$cvDemographicsMonth,
                                               useCovariateConditionOccurrence = analysisSettings$cvConditionOcc,    
                                               useCovariateConditionOccurrence365d = analysisSettings$cvConditionOcc365d,
                                               useCovariateConditionOccurrence30d = analysisSettings$cvConditionOcc30d,
                                               useCovariateConditionOccurrenceInpt180d = analysisSettings$cvConditionOccInpt180d,
                                               useCovariateConditionEra = analysisSettings$cvConditionEra, 
                                               useCovariateConditionEraEver = analysisSettings$cvConditionEraEver,
                                               useCovariateConditionEraOverlap = analysisSettings$cvConditionEraOverlap,
                                               useCovariateConditionGroup = analysisSettings$cvConditionGroup,
                                               useCovariateConditionGroupMeddra = analysisSettings$cvConditionGroupMeddra,
                                               useCovariateConditionGroupSnomed = analysisSettings$cvConditionGroupSnomed,
                                               useCovariateDrugExposure = analysisSettings$cvDrugExposure, 
                                               useCovariateDrugExposure365d = analysisSettings$cvDrugExposure365d,
                                               useCovariateDrugExposure30d = analysisSettings$cvDrugExposure30d, 
                                               useCovariateDrugEra = analysisSettings$cvDrugEra,
                                               useCovariateDrugEra365d = analysisSettings$cvDrugEra365d, 
                                               useCovariateDrugEra30d = analysisSettings$cvDrugEra30d,
                                               useCovariateDrugEraOverlap = analysisSettings$cvDrugEraOverlap, 
                                               useCovariateDrugEraEver = analysisSettings$cvDrugEraEver,
                                               useCovariateDrugGroup = analysisSettings$cvDrugGroup, 
                                               useCovariateProcedureOccurrence = analysisSettings$cvProcedureOcc,
                                               useCovariateProcedureOccurrence365d = analysisSettings$cvProcedureOcc365d,
                                               useCovariateProcedureOccurrence30d = analysisSettings$cvProcedureOcc30d,
                                               useCovariateProcedureGroup = analysisSettings$cvProcedureGroup, 
                                               useCovariateObservation = analysisSettings$cvObservation,
                                               useCovariateObservation365d = analysisSettings$cvObservation365d, 
                                               useCovariateObservation30d = analysisSettings$cvObservation30d,
                                               useCovariateObservationCount365d = analysisSettings$cvObservationCount365d, 
                                               useCovariateMeasurement = analysisSettings$cvMeasurement,
                                               useCovariateMeasurement365d = analysisSettings$cvMeasurement365d, 
                                               useCovariateMeasurement30d = analysisSettings$cvMeasurement30d,
                                               useCovariateMeasurementCount365d = analysisSettings$cvMeasurementCount365d,
                                               useCovariateMeasurementBelow = analysisSettings$cvMeasurementBelow,
                                               useCovariateMeasurementAbove = analysisSettings$cvMeasurementAbove, 
                                               useCovariateConceptCounts = analysisSettings$cvConceptCounts,
                                               useCovariateRiskScores = analysisSettings$cvRiskScores, 
                                               useCovariateRiskScoresCharlson = analysisSettings$cvRiskScoresCharlson,
                                               useCovariateRiskScoresDCSI = analysisSettings$cvRiskScoresDcsi, 
                                               useCovariateRiskScoresCHADS2 = analysisSettings$cvRiskScoresChads2,
                                               useCovariateRiskScoresCHADS2VASc = analysisSettings$cvRiskScoresChads2vasc,
                                               useCovariateInteractionYear = analysisSettings$cvInteractionYear, 
                                               useCovariateInteractionMonth = analysisSettings$cvInteractionMonth,
                                               excludedCovariateConceptIds = excludedConcepts,
                                               includedCovariateConceptIds = includedConcepts,
                                               deleteCovariatesSmallCount = analysisSettings$delCovariatesSmallCount,
                                               longTermDays = 365,
                                               mediumTermDays = 180,
                                               shortTermDays = 30,
                                               windowEndDays = 0)


  plpData <- PatientLevelPrediction::getPlpData(connectionDetails = connectionDetails,
                                                cdmDatabaseSchema = cdmDatabaseSchema,
                                                cohortId = targetCohortId,
                                                outcomeIds = outcomeList,
                                                studyStartDate = "",
                                                studyEndDate = "",
                                                cohortDatabaseSchema = cohortsDatabaseSchema,
                                                cohortTable = cohortTable,
                                                outcomeDatabaseSchema = cohortsDatabaseSchema,
                                                outcomeTable = outcomeTable,
                                                cdmVersion = cdmVersion,
                                                firstExposureOnly = as.logical(analysisSettings$firstExposureOnly),
                                                washoutPeriod = as.numeric(analysisSettings$minimumWashoutPeriod),
                                                sampleSize = ,
                                                covariateSettings = covariateSettings)


  PatientLevelPrediction::savePlpData(plpData, plpDataPath)

  # Create study population ----
  population <- PatientLevelPrediction::createStudyPopulation(plpData = plpData,
                                                              outcomeId = outcomeCohortId,
                                                              binary = TRUE,
                                                              includeAllOutcomes = as.logical(analysisSettings$includeAllOutcomes),
                                                              firstExposureOnly = as.logical(analysisSettings$firstExposureOnly),
                                                              washoutPeriod = as.numeric(analysisSettings$minimumWashoutPeriod),
                                                              removeSubjectsWithPriorOutcome = as.logical(analysisSettings$rmPriorOutcomes),
                                                              priorOutcomeLookback = as.numeric(analysisSettings$priorOutcomeLookback),
                                                              requireTimeAtRisk = as.logical(analysisSettings$requireTimeAtRisk),
                                                              minTimeAtRisk = as.numeric(analysisSettings$minimumTimeAtRisk),
                                                              riskWindowStart = as.numeric(analysisSettings$timeAtRiskStart),
                                                              addExposureDaysToStart = FALSE,
                                                              riskWindowEnd = as.numeric(analysisSettings$timeAtRiskEnd),
                                                              addExposureDaysToEnd = as.logical(analysisSettings$addExposureDaysToEnd))
  
  

  # Create the model settings ----
  if (analysisSettings$modelType == 1){
    modelSettings <- PatientLevelPrediction::setRandomForest(mtries = c(as.numeric(analysisSettings$moMTries)),
                                                             ntrees = c(as.numeric(unlist(strsplit(analysisSettings$moNTrees, ",")))),
                                                             max_depth = c(as.numeric(analysisSettings$moMaxDepth)),
                                                             varImp = as.logical(analysisSettings$moVarImp),
                                                             seed = analysisSettings$moSeed)
  }else if(analysisSettings$modelType == 2){
    modelSettings <- PatientLevelPrediction::setNaiveBayes()
  }else if(analysisSettings$modelType == 3){
    modelSettings <- PatientLevelPrediction::setMLP(size = c(as.numeric(analysisSettings$moSize)),
                                                    alpha = c(as.numeric(analysisSettings$moAlpha)),
                                                    seed = analysisSettings$moSeed)
  }else if(analysisSettings$modelType == 4){
    modelSettings <- PatientLevelPrediction::setKNN(k = as.numeric(analysisSettings$moK),
                                                    indexFolder = basicDir)
  }else if(analysisSettings$modelType == 5){
    modelSettings <- PatientLevelPrediction::setGradientBoostingMachine(ntrees = c(as.numeric(unlist(strsplit(analysisSettings$moNTrees, ",")))),
                                                                        nthread = as.numeric(analysisSettings$moNThread),
                                                                        max_depth = c(as.numeric(analysisSettings$moMaxDepth)),
                                                                        min_rows = c(as.numeric(analysisSettings$moMinRows)),
                                                                        learn_rate = c(as.numeric(analysisSettings$moLearnRate)),
                                                                        seed = analysisSettings$moSeed)
  }else if(analysisSettings$modelType == 6){
    modelSettings <- PatientLevelPrediction::setDecisionTree(max_depth = c(as.numeric(analysisSettings$moMaxDepth)),
                                                             min_samples_split = c(as.numeric(analysisSettings$moMinSamplesSplit)),
                                                             min_samples_leaf = c(as.numeric(analysisSettings$moMinSamplesLeaf)),
                                                             min_impurity_split = c(as.numeric(analysisSettings$moMinSamplesSplit)),
                                                             seed = analysisSettings$moSeed,
                                                             class_weight = analysisSettings$moClassWeight,
                                                             plot = F)
  }else if(analysisSettings$modelType == 7){
    modelSettings <- PatientLevelPrediction::setAdaBoost(n_estimators = c(as.numeric(analysisSettings$moNEstimators)),
                                                         learning_rate = c(as.numeric(analysisSettings$moLearningRate)),
                                                         seed = analysisSettings$moSeed)
  }else if(analysisSettings$modelType == 8){
    modelSettings <- PatientLevelPrediction::setLassoLogisticRegression(variance = as.numeric(analysisSettings$moVariance),
                                                                        seed = analysisSettings$moSeed)
  }
  else{
    stop("undefined model type")    
  }

  
  
  # Run the model ----
  if(analysisSettings$testSplit == 0){
    testSplit = 'time'
  }else if(analysisSettings$testSplit == 1){
    testSplit = 'person'
  }else{
    stop("undefined test split value")
  }

  results <- PatientLevelPrediction::runPlp(population = population,
                                            plpData = plpData, 
                                            modelSettings = modelSettings, 
                                            testSplit = testSplit,
                                            testFraction = as.numeric(analysisSettings$testFraction)/100, 
                                            nfold = analysisSettings$nFold,
                                            save  = file.path(basicDir, "analysisInfo"),
                                            saveModel = FALSE)
  

  PatientLevelPrediction::savePlpModel(results$model, dirPath = plpModelPath)
  PatientLevelPrediction::savePlpResult(results, dirPath = plpResultsPath)
  
  plp_summary <- data.frame(results$performanceEvaluation$evaluationStatistics)
  plp_summary <- subset(plp_summary, select = c(Metric, Eval, Value))
  write.table(plp_summary, file = file.path(basicDir, "PLP_summary.csv"), row.names = FALSE, sep = ",")
  
  end.time <- Sys.time()
  time.taken <- end.time - start.time
  time.taken  
  }