library(rJava)

getCorelatedCriteriaQuery <- function(corelatedCriteria, eventTable, dbms){
  sql <- SqlRender::readSql("additionalQuery.sql")
  sql <- SqlRender::renderSql(sql)$sql
  sql <- SqlRender::translateSql(sql, targetDialect = dbms)$sql
  return(sql)
}

getCriteriaGroupQuery <- function(group, eventTable, dbms){
  sql <- SqlRender::readSql("groupQuery.sql")
  
  additionalCriteriaQueries <- c()
  for(i in seq_along(group$CriteriaList)){
    cc <- group$CriteriaList[i]
    sql <- getCorelatedCriteriaQuery(cc, eventTable, dbms)
    sql <- gsub("@indexId", i, sql)
    additionalCriteriaQueries[[i]] <- sql
  }
  n <- length(additionalCriteriaQueries)
  for(i in seq_along(group$DemographicCriteriaList)){
    dc <- group$DemographicCriteriaList[[i]]
    sql <- getDemographicCriteriaQuery(dc, eventTable, dbms)
    sql <- gsub("@indexId", i + n, sql)
    additionalCriteriaQueries[[i + n]] <- sql
  }
  n <- length(additionalCriteriaQueries)
  for(i in seq_along(group$Groups)){
    g <- group$Groups[[i]]
    sql <- getCriteriaGroupQuery(g, eventTable, dbms)
    sql <- gsub("@indexId", i + n, sql)
    additionalCriteriaQueries[[i + n]] <- sql
  }
  
  if (length(additionalCriteriaQueries) > 0){
    sql <- gsub("@criteriaQueries", paste(additionalCriteriaQueries, collapse = "\nUNION ALL\n"))
  }
  
  sql <- SqlRender::renderSql(sql)$sql
  sql <- SqlRender::translateSql(sql, targetDialect = dbms)$sql
  return(sql)
}

convertWindowEndpoint <- function(endpoint){
  w <- .jnew("org/ohdsi/circe/cohortdefinition/Endpoint")
  if (!is.null(endpoint$Days)){
    days <- .jnew("java/lang/Integer", toString(endpoint$Days))
    `.jfield<-`(w, 'days', days)
  }
  if (!is.null(endpoint$Coeff)){
    `.jfield<-`(w, 'coeff', as.integer(endpoint$Coeff))
  }
  return(w)
}

convertWindow <- function(window){
  w <- .jnew("org/ohdsi/circe/cohortdefinition/Window")
  start <- convertWindowEndpoint(window$Start)
  `.jfield<-`(w, 'start', start)
  end <- convertWindowEndpoint(window$End)
  `.jfield<-`(w, 'end', end)
  return(w)
}

convertConcept <- function(concept){
  c <- .jnew("org/ohdsi/circe/vocabulary/Concept")
  conceptId <- .jnew("java/lang/Long", toString(concept$CONCEPT_ID))
  `.jfield<-`(c, 'conceptId', conceptId)
  `.jfield<-`(c, 'conceptName', concept$CONCEPT_NAME)
  if (!is.null(concept$STANDARD_CONCEPT)){
    `.jfield<-`(c, 'standardConcept', concept$STANDARD_CONCEPT)
  }
  if (!is.null(concept$INVALID_REASON)){
    `.jfield<-`(c, 'invalidReason', concept$INVALID_REASON)
  }
  `.jfield<-`(c, 'conceptCode', concept$CONCEPT_CODE)
  `.jfield<-`(c, 'domainId', toString(concept$DOMAIN_ID))
  `.jfield<-`(c, 'vocabularyId', toString(concept$VOCABULARY_ID))
  if (!is.null(concept$CONCEPT_CLASS_ID)){
    `.jfield<-`(c, 'conceptClassId', toString(concept$CONCEPT_CLASS_ID))
  }
  return(c)
}

convertConceptArray <- function(concepts){
  cc <- list()
  for(i in seq_along(concepts)){
    concept <- concepts[[i]]
    cc[[i]] <- convertConcept(concept)
  }
  return(.jarray(cc, contents.class = 'org/ohdsi/circe/vocabulary/Concept'))
}

convertDateRange <- function(dateRange){
  dr <- .jnew("org/ohdsi/circe/cohortdefinition/DateRange")
  `.jfield<-`(dr, 'value', dateRange$Value)
  `.jfield<-`(dr, 'op', dateRange$Op)
  `.jfield<-`(dr, 'extent', dateRange$Extent)
  return(dr)
}

convertPeriod <- function(period){
  p <- .jnew("org/ohdsi/circe/cohortdefinition/Period")
  if (!is.null(period$StartDate)){
    `.jfield<-`(p, 'startDate', period$StartDate)
  }
  if (!is.null(period$EndDate)){
    `.jfield<-`(p, 'periodEndDate', period$EndDate)
  }
}

convertNumericRange <- function(range){
  r <- .jnew("org/ohdsi/circe/cohortdefinition/NumericRange")
  if (!is.null(range$Value)){
    value <- .jcast(.jnew("java/lang/Integer", toString(range$Value)), new.class = "java/lang/Number")
    `.jfield<-`(r, 'value', value)
  }
  if (!is.null(range$Op)){
    `.jfield<-`(r, 'op', range$Op)
  }
  if (!is.null(range$Extent)){
    extent <- .jcast(.jnew("java/lang/Integer", toString(range$Extent)), new.class = "java/lang/Number")
    `.jfield<-`(r, 'extent', extent)
  }
  return(r)
}

convertTextFilter <- function(filter){
  tf <- .jnew("org/ohdsi/circe/cohortdefinition/TextFilter")
  if (!is.null(filter$Text)){
    `.jfield<-`(tf, 'text', filter$Text)
  }
  if (!is.null(filter$Op)){
    `.jfield<-`(tf, 'op', filter$Op)
  }
  return(tf)
}

convertCriteria <- function(criteria){
  c <- NULL
  if (!is.null(criteria$ConditionEra)){
    c = .jnew("org/ohdsi/circe/cohortdefinition/ConditionEra")
    if (!is.null(criteria$ConditionEra$CodesetId)){
      codesetId <- .jnew("java/lang/Integer", toString(criteria$ConditionEra$CodesetId))
      `.jfield<-`(c, 'codesetId', codesetId)
    }
    first <- .jnew("java/lang/Boolean", toString(isTRUE(criteria$ConditionEra$First)))
    `.jfield<-`(c, 'first', first)
    if (!is.null(criteria$ConditionEra$EraStartDate)){
      eraStartDate <- convertDateRange(criteria$ConditionEra$EraStartDate)
      `.jfield<-`(c, 'eraStartDate', eraStartDate)
    }
    if (!is.null(criteria$ConditionEra$EraEndDate)){
      eraEndDate <- convertDateRange(criteria$ConditionEra$EraEndDate)
      `.jfield<-`(c, 'eraEndDate', eraEndDate)
    }
    if (!is.null(criteria$ConditionEra$OccurrenceCount)){
      occurrenceCount <- convertNumericRange(criteria$ConditionEra$OccurrenceCount)
      `.jfield<-`(c, 'occurrenceCount', occurrenceCount)
    }
    if (!is.null(criteria$ConditionEra$EraLength)){
      eraLength <- convertNumericRange(criteria$ConditionEra$EraLength)
      `.jfield<-`(c, 'eraLength', eraLength)
    }
    if (!is.null(criteria$ConditionEra$AgeAtStart)){
      ageAtStart <- convertNumericRange(criteria$ConditionEra$AgeAtStart)
      `.jfield<-`(c, 'ageAtStart', ageAtStart)
    }
    if (!is.null(criteria$ConditionEra$AgeAtEnd)){
      ageAtEnd <- convertNumericRange(criteria$ConditionEra$AgeAtEnd)
      `.jfield<-`(c, 'ageAtEnd', ageAtEnd)
    }
    if (!is.null(critera$ConditionEra$Gender)){
      jgArray = convertConceptArray(criteria$ConditionEra$Gender)
      `.jfield<-`(c, 'gender', jgArray)
    }
  } else if (!is.null(criteria$ConditionOccurrence)){
    c = .jnew("org/ohdsi/circe/cohortdefinition/ConditionOccurrence")
    conditionOccurrence <- criteria$ConditionOccurrence
    if (!is.null(conditionOccurrence$CodesetId)){
      codesetId <- .jnew("java/lang/Integer", toString(conditionOccurrence$CodesetId))
      `.jfield<-`(c, 'codesetId', codesetId)
    }
    first <- .jnew("java/lang/Boolean", toString(isTRUE(conditionOccurrence$First)))
    `.jfield<-`(c, 'first', first)
    if (!is.null(conditionOccurrence$OccurrenceStartDate)){
      occurrenceStartDate <- convertDateRange(conditionOccurrence$OccurrenceStartDate)
      `.jfield<-`(c, 'occurrenceStartDate', occurrenceStartDate)
    }
    if (!is.null(conditionOccurrence$OccurrenceEndDate)){
      occurrenceEndDate <- convertDateRange(conditionOccurrence$OccurrenceEndDate)
      `.jfield<-`(c, 'occurrenceEndDate', occurrenceEndDate)
    }
    if (!is.null(conditionOccurrence$ConditionType)){
      conditionTypes <- list()
      for(i in seq_along(conditionOccurrence$ConditionType)){
        type <- conditionOccurrence$ConditionType[[i]]
        jtype <- convertConcept(type)
        conditionTypes[[i]] <- jtype
      }
      jArray <- .jarray(conditionTypes, contents.class = "org/ohdsi/circe/vocabulary/Concept")
      `.jfield<-`(c, 'conditionType', jArray)
    }
    if (!is.null(conditionOccurrence$StopReason)){
      stopReason <- convertTextFilter(conditionOccurrence$StopReason)
      `.jfield<-`(c, 'stopReason', stopReason)
    }
    if (!is.null(conditionOccurrence$ConditionSourceConcept)){
      conditionSourceConcept <- .jnew("java/lang/Integer", toString(conditionOccurrence$ConditionSourceConcept))
      `.jfield<-`(c, 'conditionSourceConcept', conditionSourceConcept)
    }
    if (!is.null(conditionOccurrence$Age)){
      age <- convertNumericRange(conditionOccurrence$Age)
      `.jfield<-`(c, 'age', age)
    }
    if (!is.null(conditionOccurrence$Gender)){
      genders <- list()
      for(i in seq_along(conditionOccurrence$Gender)){
        g <- conditionOccurrence$Gender[[i]]
        jg <- convertConcept(g)
        genders[[i]] <- g
      }
      jgArray = .jarray(genders, contents.class = "org/ohdsi/circe/vocabulary/Concept")
      `.jfield<-`(c, 'gender', jgArray)
    }
    if (!is.null(conditionOccurrence$ProviderSpecialty)){
      jArray <- convertConceptArray(conditionOccurrence$ProviderSpeciality)
      `.jfield<-`(c, 'providerSpecialty', jArray)
    }
    if (!is.null(conditionOccurrence$VisitType)){
      jArray <- convertConceptArray(conditionOccurrence$VisitType)
      `.jfield<-`(c, 'visitType', jArray)
    }
  } else if (!is.null(criteria$Death)){
    c = .jnew("org/ohdsi/circe/cohortdefinition/Death")
    death <- criteria$Death
    if (!is.null(death$CodesetId)){
      codesetId <- .jnew("java/lang/Integer", toString(death$CodesetId))
      `.jfield<-`(c, 'codesetId', codesetId)
    }
    if (!is.null(death$OccurrenceStartDate)){
      occurrenceStartDate <- convertDateRange(death$OccurrenceStartDate)
      `.jfield<-`(c, "occurrenceStartDate", occurrenceStartDate)
    }
    if (!is.null(death$DeathType)){
      jArray <- convertConceptArray(death$DeathType)
      `.jfield<-`(c, 'deathType', jArray)
    }
    if (!is.null(death$DeathSourceConcept)){
      sourceConcept <- .jnew("java/lang/Integer", toString(death$DeathSourceConcept))
      `.jfield<-`(c, 'deathSourceConcept', sourceConcept)
    }
    if (!is.null(death$Age)){
      age <- convertNumericRange(death$Age)
      `.jfield<-`(c, 'age', age)
    }
    if (!is.null(death$Gender)){
      jArray <- convertConceptArray(death$Gender)
      `.jfield<-`(c, 'gender', jArray)
    }
  } else if (!is.null(criteria$DeviceExposure)){
    c = .jnew("org/ohdsi/circe/cohortdefinition/DeviceExposure")
    deviceExposure <- criteria$DeviceExposure
    if (!is.null(deviceExposure$CodesetId)){
      codesetId <- .jnew("java/lang/Integer", toString(deviceExposure$CodesetId))
      `.jfield<-`(c, 'codesetId', codesetId)
    }
    first <- .jnew("java/lang/Boolean", toString(isTRUE(deviceExposure$First)))
    `.jfield<-`(c, 'first', first)
    if (!is.null(deviceExposure$OccurrenceStartDate)){
      occurrenceStartDate <- convertDateRange(deviceExposure$OccurrenceStartDate)
      `.jfield<-`(c, 'occurrenceStartDate', occurrenceStartDate)
    }
    if (!is.null(deviceExposure$OccurrenceEndDate)){
      occurrenceEndDate <- convertDateRange(deviceExposure$OccurrenceEndDate)
      `.jfield<-`(c, 'occurrenceEndDate', occurrenceEndDate)
    }
    if (!is.null(deviceExposure$DeviceType)){
      jArray <- convertConceptArray(deviceExposure$DeviceType)
      `.jfield<-`(c, 'deviceType', jArray)
    }
    if (!is.null(deviceExposure$UniqueDeviceId)){
      uniqueDeviceId <- convertTextFilter(deviceExposure$UniqueDeviceId)
      `.jfield<-`(c, 'uniqueDeviceId', uniqueDeviceId)
    }
    if (!is.null(deviceExposure$Quantity)){
      quantity <- convertNumericRange(deviceExposure$Quantity)
      `.jfield<-`(c, 'quantity', quantity)
    }
    if (!is.null(deviceExposure$DeviceSourceConcept)){
      deviceSourceConcept <- .jnew("java/lang/Integer", toString(deviceExposure$DeviceSourceConcept))
      `.jfield<-`(c, 'deviceSourceConcept', deviceSourceConcept)
    }
    if (!is.null(deviceExposure$Age)){
      age <- convertNumericRange(deviceExposure$Age)
      `.jfield<-`(c, 'age', age)
    }
    if (!is.null(deviceExposure$Gender)){
      jArray <- convertConceptArray(deviceExposure$Gender)
      `.jfield<-`(c, 'gender', jArray)
    }
    if (!is.null(deviceExposure$ProviderSpecialty)){
      jArray <- convertConceptArray(deviceExposure$ProviderSpecialty)
      `.jfield<-`(c, 'providerSpeciality', jArray)
    }
    if (!is.null(deviceExposure$VisitType)){
      jArray <- convertConceptArray(deviceExposure$VisitType)
      `.jfield<-`(c, 'visitType', jArray)
    }
  } else if (!is.null(criteria$DoseEra)){
    c = .jnew("org/ohdsi/circe/cohortdefinition/DoseEra")
    doseEra <- criteria$DoseEra
    if (!is.null(doseEra$CodesetId)){
      codesetId <- .jnew("java/lang/Integer", toString(doseEra$CodesetId))
      `.jfield<-`(c, 'codesetId', codesetId)
    }
    first <- .jnew("java/lang/Boolean", toString(isTRUE(doseEra$First)))
    `.jfield<-`(c, 'first', first)
    if (!is.null(doseEra$EraStartDate)){
      eraStartDate <- convertDateRange(doseEra$EraStartDate)
      `.jfield<-`(c, 'eraStartDate', eraStartDate)
    }
    if (!is.null(doseEra$EraEndDate)){
      eraEndDate <- convertDateRange(doseEra$EraStartDate)
      `.jfield<-`(c, 'eraEndDate', eraEndDate)
    }
    if (!is.null(doseEra$Unit)){
      jArray <- convertConceptArray(doseEra$Unit)
      `.jfield<-`(c, 'unit', jArray)
    }
    if (!is.null(doseEra$DoseValue)){
      doseValue <- convertNumericRange(doseEra$DoseValue)
      `.jfield<-`(c, 'doseValue', doseValue)
    }
    if (!is.null(doseEra$EraLength)){
      eraLength <- convertNumericRange(doseEra$EraLength)
      `.jfield<-`(c, 'eraLength', eraLength)
    }
    if (!is.null(doseEra$AgeAtStart)){
      ageAtStart <- convertNumericRange(doseEra$AgeAtStart)
      `.jfield<-`(c, 'ageAtStart', ageAtStart)
    }
    if (!is.null(doseEra$AgeAtEnd)){
      ageAtEnd <- convertNumericRange(doseEra$AgeAtEnd)
      `.jfield<-`(c, 'ageAtEnd', ageAtEnd)
    }
    if (!is.null(doseEra$Gender)){
      jArray <- convertConceptArray(doseEra$Gender)
      `.jfield<-`(c, 'gender', jArray)
    }
  } else if (!is.null(criteria$DrugEra)){
    c = .jnew("org/ohdsi/circe/cohortdefinition/DrugEra")
    drugEra <- criteria$DrugEra
    if (!is.null(doseEra$CodesetId)){
      codesetId <- .jnew("java/lang/Integer", toString(drugEra$CodesetId))
      `.jfield<-`(c, 'codesetId', codesetId)
    }
    first <- .jnew("java/lang/Boolean", toString(isTRUE(drugEra$First)))
    `.jfield<-`(c, 'first', first)
    if (!is.null(drugEra$EraStartDate)){
      eraStartDate <- convertDateRange(drugEra$EraStartDate)
      `.jfield<-`(c, 'eraStartDate', eraStartDate)
    }
    if (!is.null(drugEra$EraEndDate)){
      eraEndDate <- convertDateRange(drugEra$EraEndDate)
      `.jfield<-`(c, "eraEndDate", eraEndDate)
    }
    if (!is.null(drugEra$OccurrenceCount)){
      occurrenceCount <- convertNumericRange(drugEra$OccurrenceCount)
      `.jfield<-`(c, 'occurrenceCount', occurrenceCount)
    }
    if (!is.null(drugEra$GapDays)){
      gapDays <- convertNumericRange(drugEra$GapDays)
      `.jfield<-`(c, 'gapDays', gapDays)
    }
    if (!is.null(drugEra$EraLength)){
      eraLength <- convertNumericRange(drugEra$EraLength)
      `.jfield<-`(c, 'eraLength', eraLength)
    }
    if (!is.null(drugEra$AgeAtStart)){
      ageAtStart <- convertNumericRange(drugEra$AgeAtStart)
      `.jfield<-`(c, 'ageAtStart', ageAtStart)
    }
    if (!is.null(drugEra$AgeAtEnd)){
      ageAtEnd <- convertNumericRange(drugEra$AgeAtEnd)
      `.jfield<-`(c, 'ageAtEnd', ageAtEnd)
    }
    if (!is.null(drugEra$Gender)){
      jArray <- convertConceptArray(drugEra$Gender)
      `.jfield<-`(c, 'gender', jArray)
    }
  } else if (!is.null(criteria$DrugExposure)){
    c = .jnew("org/ohdsi/circe/cohortdefinition/DrugExposure")
    drugExposure <- criteria$DrugExposure
    if (!is.null(drugExposure$CodesetId)){
      codesetId <- .jnew("java/lang/Integer", toString(drugExposure$CodesetId))
      `.jfield<-`(c, 'codesetId', codesetId)
    }
    first <- .jnew("java/lang/Boolean", toString(isTRUE(drugExposure$First)))
    `.jfield<-`(c, 'first', first)
    if (!is.null(drugExposure$OccurrenceStartDate)){
      occurrenceStartDate <- convertDateRange(drugExposure$OccurrenceStartDate)
      `.jfield<-`(c, 'occurrenceStartDate', occurrenceStartDate)
    }
    if (!is.null(drugExposure$OccurrenceEndDate)){
      occurrenceEndDate <- convertDateRange(drugExposure$OccurrenceEndDate)
      `.jfield<-`(c, 'occurrenceEndDate', occurrenceEndDate)
    }
    if (!is.null(drugExposure$StopReason)){
      stopReason <- convertTextFilter(drugExposure$StopReason)
      `.jfield<-`(c, 'stopReason', stopReason)
    }
    if (!is.null(drugExposure$Refills)){
      refills <- convertNumericRange(drugExposure$Refills)
      `.jfield<-`(c, 'refills', refills)
    }
    if (!is.null(drugExposure$Quantity)){
      quantity <- convertNumericRange(drugExposure$Quantity)
      `.jfield<-`(c, 'quantity', quantity)
    }
    if (!is.null(drugExposure$DaysSupply)){
      daysSupply <- convertNumericRange(drugExposure$daysSupply)
      `.jfield<-`(c, 'daysSupply', daysSupply)
    }
    if (!is.null(drugExposure$RouteConcept)){
      jArray <- convertConceptArray(drugExposure$RouteConcept)
      `.jfield<-`(c, 'routeConcept', jArray)
    }
    if (!is.null(drugExposure$EffectiveDrugDose)){
      effectiveDrugDose <- convertNumericRange(drugExposure$EffectiveDrugDose)
      `.jfield<-`(c, 'effectiveDrugDose', effectiveDrugDose)
    }
    if (!is.null(drugExposure$DoseUnit)){
      jArray <- convertConceptArray(drugExposure$DoseUnit)
      `.jfield<-`(c, 'doseUnit', doseUnit)
    }
    if (!is.null(drugExposure$LotNumber)){
      lotNumber <- convertTextFilter(drugExposure$LotNumber)
      `.jfield<-`(c, 'lotNumber', lotNumber)
    }
    if (!is.null(drugExposure$DrugSourceConcept)){
      drugSourceConcept <- .jnew("java/lang/Integer", toString(drugExposure$DrugSourceConcept))
      `.jfield<-`(c, 'drugSourceConcept', drugSourceConcept)
    }
    if (!is.null(drugExposure$Age)){
      age <- convertNumericRange(drugExposure$Age)
      `.jfield<-`(c, 'age', age)
    }
    if (!is.null(drugExposure$Gender)){
      jArray <- convertConceptArray(drugExposure$Gender)
      `.jfield<-`(c, 'gender', gender)
    }
    if (!is.null(drugExposure$ProviderSpecialty)){
      jArray <- convertConceptArray(drugExposure$ProviderSpecialty)
      `.jfield<-`(c, 'providerSpecialty', drugExposure$ProviderSpecialty)
    }
    if (!is.null(drugExposure$VistType)){
      jArray <- convertConceptArray(drugExposure$VisitType)
      `.jfield<-`(c, 'visitType', jArray)
    }
  } else if (!is.null(criteria$Measurement)){
    c = .jnew("org/ohdsi/circe/cohortdefinition/Measurement")
    measurement <- criteria$Measurement
    if (!is.null(measurement$CodesetId)){
      codesetId <- .jnew("java/lang/Integer", toString(measurement$CodesetId))
      `.jfield<-`(c, 'codesetId', codesetId)
    }
    first <- .jnew("java/lang/Boolean", toString(isTRUE(measurement$First)))
    `.jfield<-`(c, 'first', first)
    if (!is.null(measurement$OccurrenceStartDate)){
      occurrenceStartDate <- convertDateRange(measurement$OccurrenceStartDate)
      `.jfield<-`(c, 'occurrenceStartDate', occurrenceStartDate)
    }
    if (!is.null(measurement$MeasurementType)){
      jArray <- convertConceptArray(measurement$MeasurementType)
      `.jfield<-`(c, 'measurementType', jArray)
    }
    if (!is.null(measurement$Operator)){
      jArray <- convertConceptArray(measurement$Operator)
      `.jfield<-`(c, 'operator', jArray)
    }
    if (!is.null(measurement$ValueAsNumber)){
      valueAsNumber <- convertNumericRange(measurement$ValueAsNumber)
      `.jfield<-`(c, 'valueAsNumber', valueAsNumber)
    }
    if (!is.null(measurement$ValueAsConcept)){
      jArray <- convertConceptArray(measurement$ValueAsConcept)
      `.jfield<-`(c, 'valueAsConcept', jArray)
    }
    if (!is.null(measurement$Unit)){
      jArray <- convertConceptArray(measurement$Unit)
      `.jfield<-`(c, 'unit', jArray)
    }
    if (!is.null(measurement$RangeLow)){
      rangeLow <- convertNumericRange(measurement$RangeLow)
      `.jfield<-`(c, 'rangeLow', rangeLow)
    }
    if (!is.null(measurement$RangeHigh)){
      rangeHigh <- convertNumericRange(measurement$RangeHigh)
      `.jfield<-`(c, 'rangeHigh', rangeHigh)
    }
    if (!is.null(measurement$RangeLowRatio)){
      rangeLowRatio <- convertNumericRange(measurement$RangeLowRatio)
      `.jfield<-`(c, 'rangeLowRatio', rangeLowRatio)
    }
    if (!is.null(measurement$RangeHighRatio)){
      rangeHighRatio <- convertNumericRange(measurement$RangeHighRatio)
      `.jfield<-`(c, 'rangeHighRatio', rangeHighRatio)
    }
    abnormal <- .jnew("java/lang/Boolean", toString(isTRUE(measurement$Abnormal)))
    .jfield(c, 'abnormal', abnormal)
    if (!is.null(measurement$MeasurementSourceConcept)){
      measurementSourceConcept <- .jnew("java/lang/Integer", toString(measurement$MeasurementSourceConcept))
      `.jfield<-`(c, 'measurementSourceConcept', measurementSourceConcept)
    }
    if (!is.null(measurement$Age)){
      age <- convertNumericRange(measurement$Age)
      `.jfield<-`(c, 'age', age)
    }
    if (!is.null(measurement$Gender)){
      jArray <- convertConceptArray(measurement$Gender)
      `.jfield<-`(c, 'gender', jArray)
    }
    if (!is.null(measurement$ProviderSpecialty)){
      jArray <- convertConceptArray(measurement$ProviderSpecialty)
      `.jfield<-`(c, 'providerSpecialty', jArray)
    }
    if (!is.null(measurement$VisitType)){
      jArray <- convertConceptArray(measurement$VisitType)
      `.jfield<-`(c, 'visitType', jArray)
    }
  } else if (!is.null(criteria$Observation)){
    c = .jnew("org/ohdsi/circe/cohortdefinition/Observation")
    observation <- criteria$Observation
    if (!is.null(observation$CodesetId)){
      codesetId <- .jnew("java/lang/Integer", toString(observation$CodesetId))
      `.jfield<-`(c, 'codesetId', codesetId)
    }
    first <- .jnew("java/lang/Boolean", toString(isTRUE(observation$First)))
    if (!is.null(observation$OccurrenceStartDate)){
      occurrenceStartDate <- observation$OccurrenceStartDate
      `.jfield<-`(c, 'occurrenceStartDate', occurrenceStartDate)
    }
    if (!is.null(observation$ObservationType)){
      jArray <- convertConceptArray(observation$ObservationType)
      `.jfield<-`(c, 'observationType', jArray)
    }
    if (!is.null(observation$ValueAsNumber)){
      valueAsNumber <- convertNumericRange(observation$ValueAsNumber)
      `.jfield<-`(c, 'valueAsNumber', valueAsNumber)
    }
    if (!is.null(observation$ValueAsString)){
      valueAsString <- convertTextFilter(observation$ValueAsString)
      `.jfield<-`(c, 'valueAsString', valueAsString)
    }
    if (!is.null(observation$ValueAsConcept)){
      jArray <- convertConceptArray(observation$ValueAsConcept)
      `.jfield<-`(c, 'valueAsConcept', jArray)
    }
    if (!is.null(observation$Qualifier)){
      qualifier <- convertConceptArray(observation$qualifier)
      `.jfield<-`(c, 'qualifier', qualifier)
    }
    if (!is.null(observation$Unit)){
      jArray <- convertConceptArray(observation$Unit)
      `.jfield<-`(c, 'unit', jArray)
    }
    if (!is.null(observation$ObservationSourceConcept)){
      conceptId <- .jnew("java/lang/Integer", toString(observation$ObservationSourceConcept))
      `.jfield<-`(c, 'observationSourceConcept', conceptId)
    }
    if (!is.null(observation$Age)){
      age <- convertNumericRange(observation$Age)
      `.jfield<-`(c, 'age', age)
    }
    if (!is.null(observation$Gender)){
      jArray <- convertConceptArray(observation$Gender)
      `.jfield<-`(c, 'gender', jArray)
    }
    if (!is.null(measurement$ProviderSpecialty)){
      jArray <- convertConceptArray(measurement$ProviderSpecialty)
      `.jfield<-`(c, 'providerSpecialty', jArray)
    }
    if (!is.null(measurement$VisitType)){
      jArray <- convertConceptArray(measurement$VisitType)
      `.jfield<-`(c, 'visitType', jArray)
    }
  } else if (!is.null(criteria$ObservationPeriod)){
    c = .jnew("org/ohdsi/circe/cohortdefinition/ObservationPeriod")
    observationPeriod <- criteria$ObservationPeriod
    first <- .jnew("java/lang/Boolean", toString(isTRUE(observationPeriod$First)))
    if (!is.null(observationPeriod$PeriodStartDate)){
      periodStartDate <- convertDateRange(observationPeriod$PeriodStartDate)
      `.jfield<-`(c, 'periodStartDate', periodStartDate)
    }
    if (!is.null(observationPeriod$PeriodEndDate)){
      periodEndDate <- convertDateRange(observationPeriod$PeriodEndDate)
      `.jfield<-`(c, 'periodEndDate', periodEndDate)
    }
    if (!is.null(observationPeriod$UserDefinedPeriod)){
      udp <- convertPeriod(observationPeriod$UserDefinedPeriod)
      `.jfield<-`(c, 'userDefinedPeriod', udp)
    }
    if (!is.null(observationPeriod$PeriodType)){
      jArray <- convertConceptArray(observationPeriod$PeriodType)
      `.jfield<-`(c, 'periodType', jArray)
    }
    if (!is.null(observationPeriod$PeriodLength)){
      periodLength <- convertNumericRange(observationPeriod$PeriodLength)
      `.jfield<-`(c, 'periodLength', periodLength)
    }
    if (!is.null(observationPeriod$AgeAtStart)){
      ageAtStart <- convertNumericRange(observationPeriod$AgeAtStart)
      `.jfield<-`(c, 'ageAtStart', ageAtStart)
    }
    if (!is.null(observationPeriod$AgeAtEnd)){
      ageAtEnd <- convertNumericRange(observationPeriod$AgeAtEnd)
      `.jfield<-`(c, 'ageAtEnd', ageAtEnd)
    }
  } else if (!is.null(criteria$ProcedureOccurrence)){
    c = .jnew("org/ohdsi/circe/cohortdefinition/ProcedureOccurrence")
    procedureOccurrence <- criteria$ProcedureOccurrence
    first <- .jnew("java/lang/Boolean", toString(isTRUE(procedureOccurrence$First)))
    if (!is.null(procedureOccurrence$PeriodStartDate)){
      periodStartDate <- convertDateRange(procedureOccurrence$PeriodStartDate)
      `.jfield<-`(c, 'periodStartDate', periodStartDate)
    }
    if (!is.null(procedureOccurrence$PeriodEndDate)){
      periodEndDate <- convertDateRange(procedureOccurrence$PeriodEndDate)
      `.jfield<-`(c, 'periodEndDate', periodEndDate)
    }
    if (!is.null(procedureOccurrence$PeriodType)){
      jArray <- convertConceptArray(procedureOccurrence$PeriodType)
      `.jfield<-`(c, 'periodType', jArray)
    }
    if (!is.null(procedureOccurrence$PeriodLength)){
      periodLength <- convertNumericRange(procedureOccurrence$PeriodLength)
      `.jfield<-`(c, 'periodLength', periodLength)
    }
    if (!is.null(procedureOccurrence$AgeAtStart)){
      ageAtStart <- convertNumericRange(procedureOccurrence$AgeAtStart)
      `.jfield<-`(c, 'ageAtStart', ageAtStart)
    }
    if (!is.null(procedureOccurrence$AgeAtEnd)){
      ageAtEnd <- convertNumericRange(procedureOccurrence$AgeAtEnd)
      `.jfield<-`(c, 'ageAtEnd', ageAtEnd)
    }
  } else if (!is.null(criteria$Specimen)){
    c = .jnew("org/ohdsi/circe/cohortdefinition/Specimen")
    specimen <- criteria$Specimen
    if (!is.null(specimen$CodesetId)){
      codesetId <- .jnew("java/lang/Integer", toString(specimen$CodesetId))
      `.jfield<-`(c, 'codesetId', codesetId)
    }
    first <- .jnew("java/lang/Boolean", toString(isTRUE(specimen$First)))
    if (!is.null(specimen$OccurrenceStartDate)){
      occurrenceStartDate <- convertDateRange(specimen$OccurrenceStartDate)
      `.jfield<-`(c, 'occurrenceStartDate', occurrenceStartDate)
    }
    if (!is.null(specimen$SpecimenType)){
      jArray <- convertConceptArray(specimen$SpecimenType)
      `.jfield<-`(c, 'specimenType', jArray)
    }
    if (!is.null(specimen$Quantity)){
      quantity <- convertNumericRange(specimen$Quantity)
      `.jfield<-`(c, 'quantity', quantity)
    }
    if (!is.null(specimen$Unit)){
      jArray <- convertConceptArray(specimen$Unit)
      `.jfield<-`(c, 'unit', jArray)
    }
    if (!is.null(specimen$AnatomicSite)){
      jArray <- convertConceptArray(specimen$AnatomicSite)
      `.jfield<-`(c, 'anatomicSite', specimen$AnatomicSite)
    }
    if (!is.null(specimen$DiseaseStatus)){
      jArray <- convertConceptArray(specimen$DiseaseStatus)
      `.jfield<-`(c, 'diseaseStatus', jArray)
    }
    if (!is.null(specimen$SourceId)){
      sourceId <- convertTextFilter(specimen$SourceId)
      `.jfield<-`(c, 'sourceId', sourceId)
    }
    if (!is.null(specimen$SpecimenSourceConcept)){
      specimenSourceConcept <- .jnew("java/lang/Integer", toString(specimen$SpecimenSourceConcept))
      `.jfield<-`(c, 'specimenSourceConcept', specimenSourceConcept)
    }
    if (!is.null(specimen$Age)){
      age <- convertNumericRange(specimen$Age)
      `.jfield<-`(c, 'age', age)
    }
    if (!is.null(specimen$Gender)){
      jArray <- convertConceptArray(specimen$Gender)
      `.jfield<-`(c, 'gender', jArray)
    }
  } else if (!is.null(criteria$VisitOccurrence)){
    c = .jnew("org/ohdsi/circe/cohortdefinition/VisitOccurrence")
    visitOccurrence <- criteria$VisitOccurrence
    if (!is.null(visitOccurrence$CodesetId)){
      codesetId <- .jnew("java/lang/Integer", toString(visitOccurrence$SourceId))
      `.jfield<-`(c, 'codesetId', codesetId)
    }
    first <- .jnew("java/lang/Boolean", isTRUE(visitOccurrence$First))
    if (!is.null(visitOccurrence$OccurrenceStartDate)){
      occurrenceStartDate <- convertDateRange(visitOccurrence$OccurrenceStartDate)
      `.jfield<-`(c, 'occurrenceStartDate', occurrenceStartDate)
    }
    if (!is.null(visitOccurrence$OccurrenceEndDate)){
      occurrenceEndDate <- convertDateRange(visitOccurrence$OccurrenceEndDate)
      `.jfield<-`(c, 'occurrenceEndDate', occurrenceEndDate)
    }
    if (!is.null(visitOccurrence$VisitType)){
      jArray <- convertConceptArray(visitOccurrence$VisitType)
      `.jfield<-`(c, 'visitType', jArray)
    }
    if (!is.null(visitOccurrence$VisitSourceConcept)){
      visitSourceConcept <- .jnew("java/lang/Integer", toString(visitOccurrence$VisitSourceConcept))
      `.jfield<-`(c, 'visitSourceConcept', visitSourceConcept)
    }
    if (!is.null(visitOccurrence$VisitLength)){
      visitLength <- convertNumericRange(visitOccurrence$VisitLength)
      `.jfield<-`(c, 'visitLength', visitLength)
    }
    if (!is.null(visitOccurrence$Age)){
      age <- convertNumericRange(visitOccurrence$age)
      `.jfield<-`(c, 'age', age)
    }
    if (!is.null(visitOccurrence$Gender)){
      jArray <- convertConceptArray(visitOccurrence$Gender)
      `.jfield<-`(c, 'gender', jArray)
    }
    if (!is.null(visitOccurrence$ProviderSpecialty)){
      jArray <- convertConceptArray(visitOccurrence$ProviderSpecialty)
      `.jfield<-`(c, 'providerSpecialty', jArray)
    }
    if (!is.null(visitOccurrence$PlaceOfService)){
      jArray <- convertConceptArray(visitOccurrence$PlaceOfService)
      `.jfield<-`(c, 'placeOfService', jArray)
    }
  }
  return(c)
}

convertStrata <- function(strata){
  group <- .jnew("org/ohdsi/circe/cohortdefinition/CriteriaGroup")
  `.jfield<-`(group, "type", strata$Type)
  if (!is.null(strata$Count)){
    count <- .jnew("java/lang/Integer", as.integer(strata$Count))
    `.jfield<-`(group, "count", count)
  }
  # CriteriaList
  criteriaList <- list()
  for(i in seq_along(strata$CriteriaList)){
    criteria <- strata$CriteriaList[[i]]
    cc <- .jnew("org/ohdsi/circe/cohortdefinition/CorelatedCriteria")

    # --- CRITERIA ---
    jcc <- convertCriteria(criteria$Criteria)
    `.jfield<-`(cc, 'criteria', .jcast(jcc, new.class = "org/ohdsi/circe/cohortdefinition/Criteria"))

    startWindow <- convertWindow(criteria$StartWindow)
    `.jfield<-`(cc, 'startWindow', startWindow)
    endWindow <- convertWindow(criteria$EndWindow)
    `.jfield<-`(cc, 'endWindow', endWindow)

    occurrence <- .jnew("org/ohdsi/circe/cohortdefinition/Occurrence")
    type <- as.integer(criteria$Occurrence$Type)
    `.jfield<-`(occurrence, 'type', type)
    count <- as.integer(criteria$Occurrence$Count)
    `.jfield<-`(occurrence, 'count', count)
    `.jfield<-`(occurrence, 'isDistinct', isTRUE(criteria$Occurrence$IsDistinct[1]))
    `.jfield<-`(cc, 'occurrence', occurrence)

    `.jfield<-`(cc, 'restrictVisit', isTRUE(criteria$RestrictVisit))

    criteriaList[[i]] <- cc
  }
  `.jfield<-`(group, 'criteriaList', .jarray(criteriaList, contents.class = "org/ohdsi/circe/cohortdefinition/CorelatedCriteria"))

  # DemographicCriteriaList
  demographicCriteria <- list()
  for(i in seq_along(strata$DemographicCriteriaList)){
    criteria <- strata$DemographicCriteria[[i]]
    dc <- .jnew("org/ohdsi/circe/cohortdefinition/DemographicCriteria")
    if (!is.null(criteria$Age)){
      age <- convertNumericRange(criteria$Age)
      `.jfield<-`(dc, 'age', age)
    }
    if (!is.null(criteria$Gender)){
      `.jfield<-`(dc, 'gender', convertConceptArray(criteria$Gender))
    }
    if (!is.null(criteria$Race)){
      `.jfield<-`(dc, 'race', convertConceptArray(criteria$Race))
    }
    if (!is.null(criteria$Ethnicity)){
      `.jfield<-`(dc, 'ethnicity', convertConceptArray(criteria$Ethnicity))
    }
    if (!is.null(criteria$OccurenceStartDate)){
      `.jfield<-`(dc, 'occurenceStartDate', convertDateRange(criteria$OccurenceStartDate))
    }
    if (!is.null(criteria$OccurenceEndDate)){
      `.jfield<-`(dc, 'occurenceEndDate', convertDateRange(criteria$OccurenceEndDate))
    }
    demographicCriteria[[i]] <- dc
  }
  `.jfield<-`(group, 'demographicCriteriaList', .jarray(demographicCriteria, contents.class = "org/ohdsi/circe/cohortdefinition/DemographicCriteria"))

  # Groups
  groups <- list()
  for(i in seq_along(strata$Groups)){
    gr <- strata$Groups[[i]]
    g <- convertStrata(gr)
    groups[[i]] <- g
  }
  `.jfield<-`(group, 'groups', .jarray(groups, contents.class = "org/ohdsi/circe/cohortdefinition/CriteriaGroup"))
  
  return(group);
}

getStrataQuery <- function(strataCriteria, dbms){

  builder <- .jnew("org/ohdsi/circe/cohortdefinition/CohortExpressionQueryBuilder")
  jStrataCriteria <- convertStrata(strataCriteria)
  tryCatch(criteria <- .jcall(builder, returnSig = 'S', 'getCriteriaGroupQuery', jStrataCriteria, "#analysis_events"),
           NullPointerException = function(e){
             print(e)
             e$jobj$printStackTrace()
             stop()
           })
  additionalCriteriaQuery <- paste("\nJOIN (\n", criteria, ") AC on AC.person_id = pe.person_id AND AC.event_id = pe.event_id")
  indexId <- 0
  sql <- SqlRender::readSql("strata.sql")
  sql <- SqlRender::renderSql(sql,
                              additionalCriteriaQuery = gsub("@indexId", "0", additionalCriteriaQuery),
                              indexId = indexId)$sql
  sql <- SqlRender::translateSql(sql, targetDialect = dbms)$sql
  return (sql)
}

convertExpression <- function(expression){
  cse <- .jnew("org/ohdsi/circe/vocabulary/ConceptSetExpression")
  items <- list()
  for(i in seq_along(expression$items)){
    expr <- expression$items[[i]]
    item <- .jnew("org/ohdsi/circe/vocabulary/ConceptSetExpression$ConceptSetItem")
    concept <- convertConcept(expr$concept)
    `.jfield<-`(item, 'concept', concept)
    `.jfield<-`(item, 'isExcluded', isTRUE(expr$isExcluded))
    `.jfield<-`(item, 'includeDescendants', isTRUE(expr$includeDescendants))
    `.jfield<-`(item, 'includeMapped', isTRUE(expr$includeMapped))
    items[[i]] <- item
  }
  `.jfield<-`(cse, 'items', .jarray(items, contents.class = "org/ohdsi/circe/vocabulary/ConceptSetExpression$ConceptSetItem"))
  return(cse)
}

convertConceptSet <- function(conceptSet){
  cs <- .jnew("org/ohdsi/circe/cohortdefinition/ConceptSet")
  `.jfield<-`(cs, 'id', as.integer(conceptSet$id))
  `.jfield<-`(cs, 'name', conceptSet$name)
  expr <- convertExpression(conceptSet$expression)
  `.jfield<-`(cs, 'expression', expr)
  return(cs)
}

convertConceptSetArray <- function(conceptSets){
  cs <- list()
  for(i in seq_along(conceptSets)){
    conceptSet <- conceptSets[[i]]
    cs[[i]] <- convertConceptSet(conceptSet)
  }
  return(.jarray(cs, contents.class = "org/ohdsi/circe/cohortdefinition/ConceptSet"))
}

getCodesetQuery <- function(conceptSets){
  jInit = NULL
  builder <- .jnew("org/ohdsi/circe/cohortdefinition/CohortExpressionQueryBuilder", jInit)
  arg <- convertConceptSetArray(conceptSets)
  sql <- .jcall(builder, returnSig = "S", 'getCodesetQuery', arg)
  return(sql)
}

buildAnalysisQuery <- function(analysisExpression, analysisId, dbms, cdmSchema, resultsDatabaseSchema){
  
  cohortIdStatements <- list()
  for(i in seq_along(analysisExpression$targetIds)){
    id <- analysisExpression$targetIds[[i]]
    stmt <- paste("SELECT ", id, " as cohort_id, 0 as is_outcome")
    cohortIdStatements[[i]] <- stmt
  }
  outcomeIdStatements <- list()
  for(i in seq_along(analysisExpression$outcomeIds)){
    id <- analysisExpression$outcomeIds[[i]]
    stmt <- paste("SELECT ", id, " as cohort_id, 1 as is_outcome")
    outcomeIdStatements[[i]] <- stmt
  }
  targets <- paste(cohortIdStatements, collapse = " UNION ")
  outcomes <- paste(outcomeIdStatements, collapse = " UNION ")
  cohortInserts <- paste(targets, " UNION ", outcomes)
  write(paste("Cohort inserts: ", cohortInserts), stdout())
  
  dateField <- analysisExpression$timeAtRisk$start$DateField
  if (!is.null(dateField) && "StartDate" == dateField) {
     startDay <- "cohort_start_date"
  } else { 
     startDay <- "cohort_end_date"
  }
  adjustedStart <- paste("DATEADD(day,", analysisExpression$timeAtRisk$start$Offset, ",", startDay, ")")
  dateField <- analysisExpression$timeAtRisk$end$DateField
  if (!is.null(dateField) && dateField == "StartDate") {
    endDay <- "cohort_start_date"
  } else {
    endDay <- "cohort_end_date"
  }
  adjustedEnd <- paste("DATEADD(day,", analysisExpression$timeAtRisk$end$Offset, ",", endDay, ")")
  
  studyWindowClauses <- list()
  if (!is.null(analysisExpression$studyWindow)){
    i <- 1
    if (!is.null(analysisExpression$studyWindow$startDate) && length(analysisExpression$studyWindow$startDate) > 0){
      studyWindowClauses[[i]] <- paste("t.cohort_start_date >= '", analysisExpression$studyWindow$startDate, "'", collapse = "")
      i <- i + 1
    }
    if (!is.null(analysisExpression$studyWindow$endDate) && length(analysisExpression$studyWindow$endDate) > 0){
      studyWindowClauses[[i]] <- paste("t.cohort_start_date <= '", analysisExpression$studyWindow$endDate, "'", collapse = "")
    }
  }
  cohortDataFilter <- ""
  if (length(studyWindowClauses) > 0){
    cohortDataFilter <- paste("AND ", paste(studyWindowClauses, collapse = " AND "))
  }
  
  endDateUnions <- ""
  if (!is.null(analysisExpression$studyWindow) && !is.null(analysisExpression$studyWindow$endDate) && length(analysisExpression$studyWindow$endDate) > 0){
    endDateUnions <- paste("UNION\nselect combos.target_id, combos.outcome_id, t.subject_id, t.cohort_start_date, '", analysisExpression$studyWindow$endDate, "' as followup_end, 0 as is_case\n FROM cteCohortCombos combos\n JOIN  cteCohortData t on combos.target_id = t.target_id and combos.outcome_id = t.outcome_id")
  }
  
  codesetQuery = getCodesetQuery(analysisExpression$ConceptSets)
#  write(paste("Codeset Query: ", codesetQuery), stdout())
  
  strataInsert <- list()
  for(i in seq_along(analysisExpression$strata)){
    strata <- analysisExpression$strata[[i]]
    cg <- strata$expression
    st <- getStrataQuery(cg, dbms)
    stratumInsert <- gsub("@strata_sequence", i, st)
    strataInsert[[i]] <- stratumInsert
  }
  strataCohortInserts <- paste(strataInsert, collapse = "\n")
#  write(paste("Strata Cohort Inserts: ", strataCohortInserts), stdout())
  
  sql <- SqlRender::readSql("performAnalysis.sql")
  sql <- gsub("@cohortInserts", cohortInserts, sql)
  sql <- gsub("@strataCohortInserts", strataCohortInserts, sql)
  sql <- gsub("@cohortDataFilter", cohortDataFilter, sql)
  sql <- gsub("@codesetQuery", codesetQuery, sql)
  sql <- gsub("@EndDateUnions", endDateUnions, sql)
  sql <- SqlRender::renderSql(sql,
                              results_database_schema = resultsDatabaseSchema,
                              adjustedStart = adjustedStart,
                              adjustedEnd = adjustedEnd,
                              cdm_database_schema = cdmSchema,
                              results_database_schema = resultsDatabaseSchema)$sql
  sql = gsub("@cdm_database_schema", cdmSchema, sql)
  sql = gsub("@results_database_schema", resultsDatabaseSchema, sql)
  sql = gsub("@analysisId", toString(analysisId), sql)
#  sql <- SqlRender::translateSql(sql, targetDialect = dbms)$sql
  return(sql)
}