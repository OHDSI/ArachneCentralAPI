run_ple_analysis <- function(analysiDescriptionFile, outputFolder, targetCohortDefinitionPath, comparatorCohortDefinitionPath, outcomeCohortDefinitionPath, dbms,  connectionString, user, password, cdmDatabaseSchema, resultsDatabaseSchema, exposureTable = "cohort", outcomeTable = "cohort", cdmVersion = 5, maxCores = 1){
  
  # This function read description of Population Level Estimation Analysis from json file execute code with approparate settings and saves results to file system
  
  #Inputs:
  # analysiDescriptionFile - path to json file with analysis description
  # outputFolder - folder used to store analysis results
  # targetCohortDefinitionPath - path to sql file with target cohort description
  # comparatorCohortDefinitionPath - path to sql file with comparator cohort description 
  # outcomeCohortDefinitionPath - path to sql sql with outcome cohort description

  # Outputs: 
  # None
  
  
  library(packrat)
  packrat::status()
  # Load the Cohort Method library
  library(CohortMethod) 
  library(SqlRender)
  library(EmpiricalCalibration)
  
  # Data extraction ----
  connectionDetails <- createConnectionDetails(dbms=dbms,
                                               connectionString=connectionString,
                                               user=user,
                                               password=password)
  connection <- connect(connectionDetails) 
  
  analysisSettings <- CohortMethod::loadCmAnalysisList(analysiDescriptionFile)
  
  randId <- sample(1e6, 3) # generating array of random integers in range from 0 to 1e6
  targetCohortId <- randId[1]
  comparatorCohortId <- randId[2]
  outcomeCohortId <- randId[3]
  
  outcomeList <- c(outcomeCohortId)
  
  sql <- readSql(targetCohortDefinitionPath)
  sql <- renderSql(sql,
                   cdm_database_schema = cdmDatabaseSchema,
                   target_database_schema = resultsDatabaseSchema,
                   target_cohort_table = "cohort",
                   vocabulary_database_schema = cdmDatabaseSchema,
                   target_cohort_id = targetCohortId)$sql
  sql <- translateSql(sql, targetDialect = connectionDetails$dbms)$sql
  executeSql(connection, sql)
  
  sql <- readSql(comparatorCohortDefinitionPath)
  sql <- renderSql(sql,
                   cdm_database_schema = cdmDatabaseSchema,
                   target_database_schema = resultsDatabaseSchema,
                   target_cohort_table = "cohort",
                   vocabulary_database_schema = cdmDatabaseSchema,
                   target_cohort_id = comparatorCohortId)$sql
  sql <- translateSql(sql, targetDialect = connectionDetails$dbms)$sql
  executeSql(connection, sql)
  
  sql <- readSql(outcomeCohortDefinitionPath)
  sql <- renderSql(sql,
                   cdm_database_schema = cdmDatabaseSchema,
                   target_database_schema = resultsDatabaseSchema,
                   target_cohort_table = "cohort",
                   vocabulary_database_schema = cdmDatabaseSchema,
                   target_cohort_id = outcomeCohortId)$sql
  sql <- translateSql(sql, targetDialect = connectionDetails$dbms)$sql
  executeSql(connection, sql)
  
  
  # Default Prior & Control settings ----
  defaultPrior <- createPrior("laplace", 
                              exclude = c(0),
                              useCrossValidation = TRUE)
  
  defaultControl <- createControl(cvType = "auto",
                                  startingVariance = 0.01,
                                  noiseLevel = "quiet",
                                  tolerance  = 2e-07,
                                  cvRepetitions = 10,
                                  threads = 1)
  
  # PLEASE NOTE ----
  # If you want to use your code in a distributed network study
  # you will need to create a temporary cohort table with common cohort IDs.
  # The code below ASSUMES you are only running in your local network 
  # where common cohort IDs have already been assigned in the cohort table.
  
  # Get all  Concept IDs for exclusion ----
  if (length(analysisSettings$psExclusionConceptSet) == 0){
    excludedConcepts <- c()
  }else{
    sql <- analysisSettings$psExclusionConceptSetSQL
    sql <- SqlRender::renderSql(sql, cdm_database_schema = cdmDatabaseSchema, vocabulary_database_schema = cdmDatabaseSchema)$sql
    sql <- SqlRender::translateSql(sql, targetDialect = connectionDetails$dbms)$sql
    excludedConcepts <- querySql(connection, sql)
    excludedConcepts <- excludedConcepts$CONCEPT_ID
  }
  
  
  # Get all  Concept IDs for inclusion ----
  if (length(analysisSettings$psInclusionConceptSet) == 0){
    includedConcepts <- c()
  }else{
    sql <- analysisSettings$psInclusionConceptSetSQL
    sql <- SqlRender::renderSql(sql, cdm_database_schema = cdmDatabaseSchema, vocabulary_database_schema = cdmDatabaseSchema)$sql
    sql <- SqlRender::translateSql(sql, targetDialect = connectionDetails$dbms)$sql
    includedConcepts <- querySql(connection, sql)
    includedConcepts <- includedConcepts$CONCEPT_ID
  }
  
  # Get all  Concept IDs for exclusion in the outcome model ----
  if (length(analysisSettings$omExclusionConceptSet) == 0){
    omExcludedConcepts <- c()
  }else{
    sql <- analysisSettings$omExclusionConceptSetSQL
    sql <- SqlRender::renderSql(sql, cdm_database_schema = cdmDatabaseSchema, vocabulary_database_schema = cdmDatabaseSchema)$sql
    sql <- SqlRender::translateSql(sql, targetDialect = connectionDetails$dbms)$sql
    omExcludedConcepts <- querySql(connection, sql)
    omExcludedConcepts <- omExcludedConcepts$CONCEPT_ID
  }
  # Get all  Concept IDs for inclusion exclusion in the outcome model ----
  if (length(analysisSettings$omInclusionConceptSet) == 0){
    omIncludedConcepts <- c()
  }else{
    sql <- analysisSettings$omInclusionConceptSetSQL
    sql <- SqlRender::renderSql(sql, cdm_database_schema = cdmDatabaseSchema, vocabulary_database_schema = cdmDatabaseSchema)$sql
    sql <- SqlRender::translateSql(sql, targetDialect = connectionDetails$dbms)$sql
    omIncludedConcepts <- querySql(connection, sql)
    omIncludedConcepts <- omIncludedConcepts$CONCEPT_ID
  }
  
  
  # Get all  Concept IDs for empirical calibration ----
  
  if (length(analysisSettings$negativeControlConceptSet) == 0){
    negativeControlConcepts <- c()
  }else{
    sql <- analysisSettings$negativeControlConceptSetSQL
    sql <- SqlRender::renderSql(sql, cdm_database_schema = cdmDatabaseSchema, vocabulary_database_schema = cdmDatabaseSchema)$sql
    sql <- SqlRender::translateSql(sql, targetDialect = connectionDetails$dbms)$sql
    negativeControlConcepts <- querySql(connection, sql)
    negativeControlConcepts <- negativeControlConcepts$CONCEPT_ID
  } 
  
  # Create drug comparator and outcome arguments by combining target + comparitor + outcome + negative controls ----
  dcos <- createDrugComparatorOutcomes(targetId = targetCohortId,
                                       comparatorId = comparatorCohortId,
                                       excludedCovariateConceptIds = excludedConcepts,
                                       includedCovariateConceptIds = includedConcepts,
                                       outcomeIds = c(outcomeList, negativeControlConcepts))
  
  drugComparatorOutcomesList <- list(dcos)
  
  
  
  # Define which types of covariates must be constructed ----
  covariateSettings <- createCovariateSettings(useDemographicsGender = analysisSettings$psDemographicsGender,
                                               useDemographicsRace = analysisSettings$psDemographicsRace,
                                               useDemographicsEthnicity = analysisSettings$psDemographicsEthnicity,
                                               useDemographicsAge = analysisSettings$psDemographicsAge,
                                               useDemographicsIndexYear = analysisSettings$psDemographicsYear,
                                               useDemographicsIndexMonth = analysisSettings$psDemographicsMonth,
                                               useConditionOccurrenceLongTerm = analysisSettings$psConditionOcc365d,
                                               useConditionOccurrenceShortTerm = analysisSettings$psConditionOcc30d,
                                               useConditionOccurrenceInpatientMediumTerm = analysisSettings$psConditionOccInpt180d,
                                               useConditionEraOverlap = analysisSettings$psConditionEraOverlap,
                                               useDrugExposureLongTerm = analysisSettings$psDrugExposure365d,
                                               useDrugExposureShortTerm = analysisSettings$psDrugExposure30d,
                                               useDrugEraLongTerm = analysisSettings$psDrugEra365d,
                                               useDrugEraShortTerm = analysisSettings$psDrugEra30d,
                                               useDrugEraOverlap = analysisSettings$psDrugEraOverlap,
                                               useProcedureOccurrenceLongTerm = analysisSettings$psProcedureOcc365d,
                                               useProcedureOccurrenceShortTerm = analysisSettings$psProcedureOcc30d,
                                               useObservationLongTerm = analysisSettings$psObservation365d,
                                               useObservationShortTerm = analysisSettings$psObservation30d,
                                               useMeasurementLongTerm = analysisSettings$psMeasurement365d,
                                               useMeasurementShortTerm = analysisSettings$psMeasurement30d,
                                               useCharlsonIndex = analysisSettings$psRiskScoresCharlson,
                                               useDcsi = analysisSettings$psRiskScoresDcsi,
                                               useChads2 = analysisSettings$psRiskScoresChads2,
                                               useChads2Vasc = analysisSettings$psRiskScoresChads2vasc,
                                               excludedCovariateConceptIds = excludedConcepts,
                                               includedCovariateConceptIds = includedConcepts)


  getDbCmDataArgs <- createGetDbCohortMethodDataArgs(washoutPeriod = as.numeric(analysisSettings$minimumWashoutPeriod),
                                                     firstExposureOnly = FALSE,
                                                     removeDuplicateSubjects = analysisSettings$rmSubjectsInBothCohortsFormatted,
                                                     studyStartDate = "",
                                                     studyEndDate = "",
                                                     excludeDrugsFromCovariates = FALSE,
                                                     covariateSettings = covariateSettings)
  
  createStudyPopArgs <- createCreateStudyPopulationArgs(removeSubjectsWithPriorOutcome = analysisSettings$rmPriorOutcomesFormatted,
                                                        firstExposureOnly = FALSE,
                                                        washoutPeriod = as.numeric(analysisSettings$minimumWashoutPeriod),
                                                        removeDuplicateSubjects = analysisSettings$rmSubjectsInBothCohortsFormatted,
                                                        minDaysAtRisk = as.numeric(analysisSettings$minimumDaysAtRisk),
                                                        riskWindowStart = as.numeric(analysisSettings$timeAtRiskStart),
                                                        addExposureDaysToStart = FALSE,
                                                        riskWindowEnd = as.numeric(analysisSettings$timeAtRiskEnd),
                                                        addExposureDaysToEnd = as.logical(analysisSettings$addExposureDaysToEnd))
  
  
  if (analysisSettings$modelType == 1){
    modelTypeName = "logistic"
  }else if(analysisSettings$modelType == 2){
    modelTypeName = "poisson"
  }else if(analysisSettings$modelType == 3){
    modelTypeName = "cox"
  }else{
    stop("undefined model type")    
  }
  
  fitOutcomeModelArgs1 <- createFitOutcomeModelArgs(useCovariates = FALSE,
                                                    modelType = modelTypeName,
                                                    stratified = TRUE,
                                                    includeCovariateIds = omIncludedConcepts,
                                                    excludeCovariateIds = omExcludedConcepts,
                                                    prior = defaultPrior,
                                                    control = defaultControl)
  
  createPsArgs1 <- createCreatePsArgs(control = defaultControl) # Using only defaults
  trimByPsArgs1 <- createTrimByPsArgs() # Using only defaults 
  trimByPsToEquipoiseArgs1 <- createTrimByPsToEquipoiseArgs() # Using only defaults 
  trimByPs = FALSE
  trimByPsToEquipoise = FALSE
  if (analysisSettings$psTrim == 1){
    trimByPs = TRUE
    trimByPsArgs1 <- createTrimByPsArgs(trimFraction = analysisSettings$psTrimFractionFormatted)
  }else if (analysisSettings$psTrim == 2){
    trimByPsToEquipoise = TRUE
    trimByPsToEquipoiseArgs1 <- createTrimByPsToEquipoiseArgs(bounds = as.numeric(unlist(strsplit(analysisSettings$psTrimFractionFormatted, split=", "))))
  }
  
  matchOnPsArgs1 <- createMatchOnPsArgs() # Using only defaults 
  stratifyByPsArgs1 <- createStratifyByPsArgs() # Using only defaults 
  matchOnPs = FALSE
  stratifyByPs = FALSE
  if (analysisSettings$psMatch == 1){
    matchOnPs = TRUE
    matchOnPsArgs1 <- createMatchOnPsArgs(caliper = 0.25, caliperScale = "standardized", maxRatio = as.numeric(analysisSettings$psMatchMaxRatio)) 
  }else if (analysisSettings$psMatch == 2){
    stratifyByPs = TRUE
    stratifyByPsArgs1 <- createStratifyByPsArgs(numberOfStrata = analysisSettings$psStratNumStrata) 
  }
  
  cmAnalysis1 <- createCmAnalysis(analysisId = 1,
                                  description = analysisSettings$nameMultiLine,
                                  getDbCohortMethodDataArgs = getDbCmDataArgs,
                                  createStudyPopArgs = createStudyPopArgs,
                                  createPs = TRUE,
                                  createPsArgs = createPsArgs1,
                                  trimByPs = trimByPs,
                                  trimByPsArgs = trimByPsArgs1,
                                  trimByPsToEquipoise = trimByPsToEquipoise,
                                  trimByPsToEquipoiseArgs = trimByPsToEquipoiseArgs1,
                                  matchOnPs = matchOnPs,
                                  matchOnPsArgs = matchOnPsArgs1,
                                  stratifyByPs = stratifyByPs,
                                  stratifyByPsArgs = stratifyByPsArgs1,
                                  computeCovariateBalance = TRUE,
                                  fitOutcomeModel = TRUE,
                                  fitOutcomeModelArgs = fitOutcomeModelArgs1)
  
  
  cmAnalysisList <- list(cmAnalysis1)
  
  
  
  # Run the analysis ----
  result <- runCmAnalyses(connectionDetails = connectionDetails,
                          cdmDatabaseSchema = cdmDatabaseSchema,
                          exposureDatabaseSchema = resultsDatabaseSchema,
                          exposureTable = exposureTable,
                          outcomeDatabaseSchema = resultsDatabaseSchema,
                          outcomeTable = outcomeTable,
                          cdmVersion = cdmVersion,
                          outputFolder = outputFolder,
                          cmAnalysisList = cmAnalysisList,
                          drugComparatorOutcomesList = drugComparatorOutcomesList,
                          getDbCohortMethodDataThreads = 1,
                          createPsThreads = 1,
                          psCvThreads = min(16, maxCores),
                          computeCovarBalThreads = min(3, maxCores),
                          createStudyPopThreads = min(3, maxCores),
                          trimMatchStratifyThreads = min(10, maxCores),
                          fitOutcomeModelThreads = max(1, round(maxCores/4)),
                          outcomeCvThreads = min(4, maxCores),
                          outcomeIdsOfInterest = outcomeList,
                          refitPsForEveryOutcome = FALSE)
  
  ## Summarize the results
  analysisSummary <- summarizeAnalyses(result)
  head(analysisSummary)
  
  # Perform Empirical Calibration ----
  newSummary <- data.frame()
  # Calibrate p-values:
  drugComparatorOutcome <- drugComparatorOutcomesList[[1]]
  for (drugComparatorOutcome in drugComparatorOutcomesList) {
    for (analysisId in unique(analysisSummary$analysisId)) {
      subset <- analysisSummary[analysisSummary$analysisId == analysisId &
                                  analysisSummary$targetId == drugComparatorOutcome$targetId &
                                  analysisSummary$comparatorId == drugComparatorOutcome$comparatorId, ]
      
      negControlSubset <- subset[analysisSummary$outcomeId %in% negativeControlConcepts, ]
      negControlSubset <- negControlSubset[!is.na(negControlSubset$logRr) & negControlSubset$logRr != 0, ]
      
      hoiSubset <- subset[!(analysisSummary$outcomeId %in% negativeControlConcepts), ]
      hoiSubset <- hoiSubset[!is.na(hoiSubset$logRr) & hoiSubset$logRr != 0, ]
      
      if (nrow(negControlSubset) > 10) {
        null <- fitMcmcNull(negControlSubset$logRr, negControlSubset$seLogRr)
        
        # View the empirical calibration plot with only negative controls
        plotCalibrationEffect(negControlSubset$logRr,
                              negControlSubset$seLogRr)
        
        # Save the empirical calibration plot with only negative controls
        plotName <- paste("calEffectNoHois_a",analysisId, "_t", drugComparatorOutcome$targetId, "_c", drugComparatorOutcome$comparatorId, ".png", sep = "")
        plotCalibrationEffect(negControlSubset$logRr,
                              negControlSubset$seLogRr,
                              fileName = file.path(outputFolder, plotName))
        
        # View the empirical calibration plot with  negative controls and HOIs plotted
        plotCalibrationEffect(negControlSubset$logRr,
                              negControlSubset$seLogRr,
                              hoiSubset$logRr, 
                              hoiSubset$seLogRr)
        
        # Save the empirical calibration plot with  negative controls and HOIs plotted
        plotName <- paste("calEffect_a",analysisId, "_t", drugComparatorOutcome$targetId, "_c", drugComparatorOutcome$comparatorId, ".png", sep = "")
        plotCalibrationEffect(negControlSubset$logRr,
                              negControlSubset$seLogRr,
                              hoiSubset$logRr, 
                              hoiSubset$seLogRr,
                              fileName = file.path(outputFolder, plotName))
        
        calibratedP <- calibrateP(null, subset$logRr, subset$seLogRr)
        subset$calibratedP <- calibratedP$p
        subset$calibratedP_lb95ci <- calibratedP$lb95ci
        subset$calibratedP_ub95ci <- calibratedP$ub95ci
        mcmc <- attr(null, "mcmc")
        subset$null_mean <- mean(mcmc$chain[, 1])
        subset$null_sd <- 1/sqrt(mean(mcmc$chain[, 2]))
      } else {
        subset$calibratedP <- NA
        subset$calibratedP_lb95ci <- NA
        subset$calibratedP_ub95ci <- NA
        subset$null_mean <- NA
        subset$null_sd <- NA
      }
      newSummary <- rbind(newSummary, subset)
    }
  }
  
  # Results ----
  drugComparatorOutcome <- drugComparatorOutcomesList[[1]]
  for (drugComparatorOutcome in drugComparatorOutcomesList) {
    for (analysisId in unique(analysisSummary$analysisId)) {
      currentAnalysisSubset <- analysisSummary[analysisSummary$analysisId == analysisId &
                                                 analysisSummary$targetId == drugComparatorOutcome$targetId &
                                                 analysisSummary$comparatorId == drugComparatorOutcome$comparatorId &
                                                 analysisSummary$outcomeId %in% outcomeList, ]
      
      for(currentOutcomeId in unique(currentAnalysisSubset$outcomeId)) {
        outputImageSuffix <- paste0("_a",analysisId, "_t", currentAnalysisSubset$targetId, "_c", currentAnalysisSubset$comparatorId, "_o", currentOutcomeId, ".png")
        
        cohortMethodFile <- result$cohortMethodDataFolder[result$target == currentAnalysisSubset$targetId &
                                                            result$comparatorId == currentAnalysisSubset$comparatorId &
                                                            result$outcomeId == currentOutcomeId &
                                                            result$analysisId == analysisId]
        
        cohortMethodData <- loadCohortMethodData(cohortMethodFile)
        
        studyPopFile <- result$studyPopFile[result$target == currentAnalysisSubset$targetId &
                                              result$comparatorId == currentAnalysisSubset$comparatorId &
                                              result$outcomeId == currentOutcomeId &
                                              result$analysisId == analysisId]
        
        # Return the attrition table for the study population ----
        studyPop <- readRDS(studyPopFile)
        getAttritionTable(studyPop)
        
        # View the attrition diagram
        drawAttritionDiagram(studyPop, 
                             treatmentLabel = "Target", 
                             comparatorLabel = "Comparator")
        
        # Save the attrition diagram ----
        plotName <- paste0("attritionDiagram", outputImageSuffix);
        drawAttritionDiagram(studyPop, 
                             treatmentLabel = "Target", 
                             comparatorLabel = "Comparator", 
                             fileName = file.path(outputFolder, plotName))
        
        
        psFile <- result$psFile[result$target == currentAnalysisSubset$targetId &
                                  result$comparatorId == currentAnalysisSubset$comparatorId &
                                  result$outcomeId == currentOutcomeId &
                                  result$analysisId == analysisId]
        
        ps <- readRDS(psFile)
        
        # Compute the area under the receiver-operator curve (AUC) for the propensity score model ----
        computePsAuc(ps)
        
        # Plot the propensity score distribution ----
        plotPs(ps, 
               scale = "preference")
        
        # Save the propensity score distribution ----
        plotName <- paste0("propensityScorePlot", outputImageSuffix);
        plotPs(ps, 
               scale = "preference",
               fileName = file.path(outputFolder, plotName))
        
        
        # Inspect the propensity model ----
        propensityModel <- getPsModel(ps, cohortMethodData)
        head(propensityModel)
        
        
        strataFile <- result$strataFile[result$target == currentAnalysisSubset$targetId &
                                          result$comparatorId == currentAnalysisSubset$comparatorId &
                                          result$outcomeId == currentOutcomeId &
                                          result$analysisId == analysisId]
        strataPop <- readRDS(strataFile)
        
        # View PS With Population Trimmed By Percentile ----
        plotPs(strataPop, 
               ps, 
               scale = "preference")
        
        # Save PS With Population Trimmed By Percentile ----
        plotName <- paste0("propensityScorePlotStrata", outputImageSuffix);
        plotPs(strataPop, 
               ps, 
               scale = "preference",
               fileName = file.path(outputFolder, plotName))
        
        
        # Get the attrition table and diagram for the strata pop ----
        getAttritionTable(strataPop)
        
        # View the attrition diagram for the strata pop ----
        drawAttritionDiagram(strataPop)
        
        # Save the attrition diagram for the strata pop ----
        plotName <- paste0("attritionDiagramStrata", outputImageSuffix);
        drawAttritionDiagram(strataPop,
                             fileName = file.path(outputFolder, plotName))
        
        
        # Plot the covariate balance ----
        balanceFile <- result$covariateBalanceFile[result$target == currentAnalysisSubset$targetId &
                                                     result$comparatorId == currentAnalysisSubset$comparatorId &
                                                     result$outcomeId == currentOutcomeId &
                                                     result$analysisId == analysisId]
        balance <- readRDS(balanceFile)
        
        # View the covariate balance scatter plot ----
        plotCovariateBalanceScatterPlot(balance)
        
        # Save the covariate balance scatter plot ----
        plotName <- paste0("covBalScatter", outputImageSuffix);
        plotCovariateBalanceScatterPlot(balance,
                                        fileName = file.path(outputFolder, plotName))
        
        # View the plot of top variables ----
        plotCovariateBalanceOfTopVariables(balance)
        
        # Save the plot of top variables ----
        plotName <- paste0("covBalTop", outputImageSuffix);
        plotCovariateBalanceOfTopVariables(balance,
                                           fileName = file.path(outputFolder, plotName))
        
        
        # Outcome Model ----
        
        outcomeFile <- result$outcomeModelFile[result$target == currentAnalysisSubset$targetId &
                                                 result$comparatorId == currentAnalysisSubset$comparatorId &
                                                 result$outcomeId == currentOutcomeId &
                                                 result$analysisId == analysisId]
        outcomeModel <- readRDS(outcomeFile)
        
        # Calibrated results -----
        outcomeSummary <- newSummary[newSummary$targetId == currentAnalysisSubset$targetId & 
                                       newSummary$comparatorId == currentAnalysisSubset$comparatorId & 
                                       newSummary$outcomeId == currentOutcomeId & 
                                       newSummary$analysisId == analysisId, ]  
        
        outcomeSummaryOutput <- data.frame(outcomeSummary$rr, 
                                           outcomeSummary$ci95lb, 
                                           outcomeSummary$ci95ub, 
                                           outcomeSummary$logRr, 
                                           outcomeSummary$seLogRr,
                                           outcomeSummary$p,
                                           outcomeSummary$calibratedP, 
                                           outcomeSummary$calibratedP_lb95ci,
                                           outcomeSummary$calibratedP_ub95ci,
                                           outcomeSummary$null_mean,
                                           outcomeSummary$null_sd)
        
        colnames(outcomeSummaryOutput) <- c("Estimate", 
                                            "lower .95", 
                                            "upper .95", 
                                            "logRr", 
                                            "seLogRr", 
                                            "p", 
                                            "cal p",  
                                            "cal p - lower .95",  
                                            "cal p - upper .95", 
                                            "null mean",  
                                            "null sd")
        
        rownames(outcomeSummaryOutput) <- "treatment"
        
        # View the outcome model -----
        outcomeModelOutput <- capture.output(outcomeModel)
        outcomeModelOutput <- head(outcomeModelOutput,n=length(outcomeModelOutput)-2)
        write.table(outcomeSummaryOutput, file = file.path(outputFolder,"PLE_summary.csv"), row.names = FALSE, col.names = TRUE, sep = ",")
        
        outcomeSummaryOutput <- capture.output(printCoefmat(outcomeSummaryOutput))
        outcomeModelOutput <- c(outcomeModelOutput, outcomeSummaryOutput)
        writeLines(outcomeModelOutput)
      }
    }
  }
}