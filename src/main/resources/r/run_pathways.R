library(DatabaseConnector)
library(SqlRender)
library(rlist)
library(purrr)
library(dplyr)

getEventCohortCodes <- function(pathwayAnalysis) {
  
  eventCohorts <- pathwayAnalysis$eventCohorts
  return(list.order(eventCohorts, name))
  #return(map(orderedCohorts, function(ec){ return(ec) }))
}

indexOf <- function(list, val) {
  vv <- match(list, val)
  for(i in 1:length(vv)) {
    v <- vv[[i]]
    if (!is.na(v)) {
      return(i)
    }
  }
  return(-1)
}

getEventCohortIdIndexSql <- function(design) {
  
  eventCohortCodes <- getEventCohortCodes(design)
  sqlList <- imap(eventCohortCodes, function(id, index){
    sql <- SqlRender::render(sql = "SELECT @cohort_definition_id AS cohort_definition_id, @event_cohort_index AS cohort_index", 
                             cohort_definition_id = id, event_cohort_index = index)
    invisible(sql)
  })
  sql <- paste(sqlList, collapse = ' UNION ALL ')
  return(sql)
}

getEventCohortsByComboCode <- function(design, comboCode) {
  
  eventCohorts <- design$eventCohorts
  eventCodes <- getEventCohortCodes(design)
  filtered <- keep(eventCohorts, ~ (2 ^ indexOf(eventCodes, .x$id) & comboCode) > 0 )
  return(filtered)
}

run_pathways <- function(workDir,
                         connectionDetails,
                         cdmDatabaseSchema,
                         vocabularySchema = cdmDatabaseSchema,
                         resultsSchema,
                         cohortTable = "cohort",
                         cohortDefinitions,
                         designPath = "pathways.json",
                         analysisId,
                         outputFolder) {
  start.time <- Sys.time()
  
  design <- list.load(file.path(workDir, designPath))
  if (is.null(design) || length(design$targetCohorts) == 0) {
    stop("No target cohorts in Pathways analysis!")
  }
  
  dbms = connectionDetails$dbms
  connection <- DatabaseConnector::connect(connectionDetails = connectionDetails)
  
  # Cohort definitions
  writeLines("Executing cohort definitions")
  for(cohortFile in cohortDefinitions){
    writeLines(paste("Executing cohort", cohortFile$file, cohortFile$id))
    cf <- file.path(workDir, cohortFile$file)
    sql <- readSql(cf)
    sql <- render(sql,
                     cdm_database_schema = cdmDatabaseSchema,
                     vocabulary_database_schema = cdmDatabaseSchema,
                     target_database_schema = resultsSchema,
                     target_cohort_table = cohortTable,
                     target_cohort_id = cohortFile$id,
                     output = "output")
    sql <- translate(sql, targetDialect = connectionDetails$dbms)
    executeSql(connection, sql)
  }
  
  # Build analysis query
  eventCohortSql <- getEventCohortIdIndexSql(design)
  
  targetCohorts <- design$targetCohorts
  analysisSqlTmpl <- readSql("runPathwayAnalysis.sql")
  analysisSqlList <- map(targetCohorts, function(tc) {
    sql <- render(sql = analysisSqlTmpl, generation_id = analysisId, event_cohort_id_index_map = eventCohortSql, temp_database_schema = resultsSchema, 
                  target_database_schema = resultsSchema, target_cohort_table = paste0(resultsSchema,'.',cohortTable), pathway_target_cohort_id = tc$id,
                  max_depth = design$maxDepth, combo_window = first(design$combinationWindow, default = 1), allow_repeats = design$allowRepeats)
    translatedSql <- translate(sql, targetDialect = dbms)
    return(translatedSql)
  })
 
  analysisSql <- paste(analysisSqlList, collapse = "")
  
  # Run analysis
  writeLines("Running Pathway Analysis")
  executeSql(connection, sql = analysisSql)
  
  # Generate statistics
  writeLines("Gathering statistics")
  sql <- render(sql = "select distinct combo_id FROM @target_database_schema.pathway_analysis_events where pathway_analysis_generation_id = @generation_id;",
         target_database_schema = resultsSchema, generation_id = analysisId)
  comboCodes <- querySql(connection, sql = translate(sql, targetDialect = dbms))
  names(comboCodes) <- snakeCaseToCamelCase(names(comboCodes))
  comboCodes <- comboCodes$comboId
  eventCodes <- getEventCohortCodes(design)

  # Get pathway codes
  pathwayCodes <- map(comboCodes, function(code) {
    eventCohorts <- getEventCohortsByComboCode(design, code)
    names <- paste(map(eventCohorts, function(ec){ return(ec$name) }), collapse = ",")
    return(list(code = code, names = names, isCombo = length(eventCohorts) > 1))
  })
  
  if (length(pathwayCodes) == 0) {
    stop('No pathway codes found')
  }
  
  sqlList <- map(pathwayCodes, function(pc) {
    sql <- render(sql = "INSERT INTO @target_database_schema.pathway_analysis_codes (pathway_analysis_generation_id, code, name, is_combo)
        VALUES (@generation_id, @code, '@name', @is_combo);", target_database_schema = resultsSchema, generation_id = analysisId,
                  code = as.character(pc$code), name = pc$names, is_combo = ifelse(pc$isCombo, 1, 0))
    return(translate(sql, dbms))
  })
  executeSql(connection, sql = paste(sqlList, collapse = ""))
  
  #Save paths
  
  savePathsSql <- readSql("savePaths.sql")
  sql <- render(sql = savePathsSql, target_database_schema = resultsSchema, generation_id = analysisId)
  sql <- translate(sql = sql, targetDialect = dbms)
  executeSql(connection = connection, sql = sql)
    
  # Results
  dir.create(file.path(workDir, outputFolder), showWarnings = FALSE)
  outputFolder <- file.path(workDir, outputFolder)
  
  codeLookupSql <- readSql("getPathwayCodeLookup.sql")
  sql <- render(sql = codeLookupSql, target_database_schema = resultsSchema, generation_id = analysisId)
  sql <- translate(sql = sql, targetDialect = dbms)
  pathwayCodes <- querySql(connection = connection, sql = sql)
  write.csv(pathwayCodes, file.path(outputFolder, "pathway_codes.csv"), na = "")
  
  statsSql <- readSql("getPathwayStats.sql")
  sql <- render(sql = statsSql, target_database_schema = resultsSchema, generation_id = analysisId)
  sql <- translate(sql = sql, targetDialect = dbms)
  cohortStats <- querySql(connection = connection, sql = sql)
  write.csv(cohortStats, file.path(outputFolder, "cohort_stats.csv"), na = "")
  
  resultsSql <- readSql("getPathwayResults.sql")
  sql <- render(sql = resultsSql, target_database_schema = resultsSchema, generation_id = analysisId)
  sql <- translate(sql = sql, targetDialect = dbms)
  pathwayResults <- querySql(connection = connection, sql = sql)
  write.csv(pathwayResults, file.path(outputFolder, "pathway_results.csv"), na = "")
  
  disconnect(connection)
  
  end.time <- Sys.time()
  time.taken <- end.time - start.time
  writeLines(paste("Pathway analysis execution took",signif(time.taken, 3),attr(time.taken, "units")))
}