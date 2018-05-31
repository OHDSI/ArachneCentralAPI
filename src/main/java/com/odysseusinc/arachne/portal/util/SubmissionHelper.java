/*
 *
 * Copyright 2018 Observational Health Data Sciences and Informatics
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Company: Odysseus Data Services, Inc.
 * Product Owner/Architecture: Gregory Klebanov
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva, Mikhail Mironov
 * Created: January 13, 2017
 *
 */

package com.odysseusinc.arachne.portal.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
import com.odysseusinc.arachne.commons.utils.cohortcharacterization.CohortCharacterizationDocType;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.repository.SubmissionResultFileRepository;
import com.odysseusinc.arachne.storage.model.ArachneFileMeta;
import com.odysseusinc.arachne.storage.model.QuerySpec;
import com.odysseusinc.arachne.storage.service.ContentStorageService;
import com.odysseusinc.arachne.storage.service.JcrContentStorageServiceImpl;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class SubmissionHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubmissionHelper.class);
    private static final String PLE_SUMMARY_FILENAME = "PLE_summary.csv";
    private static final String PLP_SUMMARY_FILENAME = "PLP_summary.csv";
    private static final String INCIDENCE_SUMMARY_FILENAME = "ir_summary.csv";
    private static final String CAN_NOT_PARSE_LOG = "Can not parse '{}'";
    private static final String CAN_NOT_BUILD_EXTEND_INFO_LOG = "Can not build extendInfo for submission with id='{}'";

    private final SubmissionResultFileRepository submissionResultFileRepository;
    private final AnalysisHelper analysisHelper;
    private final ContentStorageService contentStorageService;
    private final ContentStorageHelper contentStorageHelper;

    public SubmissionHelper(SubmissionResultFileRepository submissionResultFileRepository, AnalysisHelper analysisHelper, ContentStorageService contentStorageService, ContentStorageHelper contentStorageHelper) {

        this.submissionResultFileRepository = submissionResultFileRepository;
        this.analysisHelper = analysisHelper;
        this.contentStorageService = contentStorageService;
        this.contentStorageHelper = contentStorageHelper;
    }

    public void updateSubmissionExtendedInfo(final Submission submission) {

        final CommonAnalysisType analysisType = submission.getSubmissionGroup().getAnalysisType();
        SubmissionExtendInfoAnalyzeStrategy strategy;
        switch (analysisType) {
            case COHORT: {
                strategy = new CohortSubmissionExtendInfoStrategy();
                break;
            }
            case COHORT_CHARACTERIZATION: {
                strategy = new CohortCharacterizationSubmissionExtendInfoStrategy();
                break;
            }
            case INCIDENCE: {
                strategy = new IncidenceSubmissionExtendInfoStrategy();
                break;
            }
            case ESTIMATION: {
                strategy = new EstimationSubmissionExtendInfoStrategy();
                break;
            }
            case PREDICTION: {
                strategy = new PredictionSubmissionExtendInfoStrategy();
                break;
            }
            default: {
                strategy = new DefaultSubmissionExtendInfoStrategy();
            }
        }
        strategy.updateExtendInfo(submission);
    }

    private static abstract class SubmissionExtendInfoAnalyzeStrategy {

        public abstract void updateExtendInfo(final Submission submission);
    }

    private static class DefaultSubmissionExtendInfoStrategy extends SubmissionExtendInfoAnalyzeStrategy {

        @Override
        public void updateExtendInfo(final Submission submission) {

        }
    }

    private class CohortSubmissionExtendInfoStrategy extends SubmissionExtendInfoAnalyzeStrategy {

        public final String COUNT_KEY = "count";
        public String personCountColName = "persons";

        public long getPersonCount(final Submission submission) {

            final List<ArachneFileMeta> files = searchFiles(submission, "%count%.csv");

            long sum = 0;

            for (ArachneFileMeta f : files) {
                try {
                    final CSVParser parser = CSVParser.parse(contentStorageService.getContentByFilepath(f.getPath()), Charset.defaultCharset(), CSVFormat.DEFAULT.withHeader());
                    final Integer countColumnNumber = parser.getHeaderMap().containsKey(COUNT_KEY.toLowerCase())
                            ? parser.getHeaderMap().get(COUNT_KEY.toLowerCase())
                            : parser.getHeaderMap().get(COUNT_KEY.toUpperCase());
                    if (countColumnNumber != null) {
                        final List<CSVRecord> records = parser.getRecords();
                        if (!CollectionUtils.isEmpty(records)) {
                            final long count = Long.parseLong(records.get(0).get(countColumnNumber));
                            sum += count;
                        }
                    }
                } catch (IOException e) {
                    LOGGER.warn("Can not open \"count\" file, ResultFile={}. Error={}", f, e.getMessage());
                }
            }

            return sum;
        }

        @Override
        public void updateExtendInfo(final Submission submission) {

            final JsonObject resultInfo = new JsonObject();
            resultInfo.add(personCountColName, new JsonPrimitive(getPersonCount(submission)));
            submission.setResultInfo(resultInfo);
        }
    }

    private class CohortCharacterizationSubmissionExtendInfoStrategy extends SubmissionExtendInfoAnalyzeStrategy {

        @Override
        public void updateExtendInfo(final Submission submission) {

            final JsonObject resultInfo = new JsonObject();

            // Set person count
            CohortSubmissionExtendInfoStrategy cohortStrategy = new CohortSubmissionExtendInfoStrategy();
            resultInfo.add(cohortStrategy.personCountColName, new JsonPrimitive(cohortStrategy.getPersonCount(submission)));

            // Set amount of reports
            try {
                final Set<String> docTypes = Arrays.stream(CohortCharacterizationDocType.values())
                        .filter(docType -> CohortCharacterizationDocType.UNKNOWN != docType)
                        .map(docType -> docType.getTitle())
                        .collect(Collectors.toSet());

                final String resultsDir = contentStorageHelper.getResultFilesDir(submission);

                final QuerySpec querySpec = new QuerySpec();
                querySpec.setPath(resultsDir);
                querySpec.setSearchSubfolders(true);
                querySpec.setContentTypes(docTypes);

                final int count = contentStorageService.searchFiles(querySpec).size();

                final JsonElement element = new JsonPrimitive(count);
                resultInfo.add("reports", element);
            } catch (Exception e) {
                LOGGER.warn(CAN_NOT_BUILD_EXTEND_INFO_LOG, submission.getId());
                LOGGER.warn("Error: ", e);
            }
            submission.setResultInfo(resultInfo);
        }
    }

    private class IncidenceSubmissionExtendInfoStrategy extends SubmissionExtendInfoAnalyzeStrategy {

        @Override
        public void updateExtendInfo(Submission submission) {

            final JsonObject resultInfo = new JsonObject();
            try {
                final String resultsDir = contentStorageHelper.getResultFilesDir(submission);
                ArachneFileMeta arachneFile = contentStorageService.getFileByPath(resultsDir
                        + JcrContentStorageServiceImpl.PATH_SEPARATOR
                        + INCIDENCE_SUMMARY_FILENAME);
                final CSVParser parser = CSVParser.parse(contentStorageService.getContentByFilepath(arachneFile.getPath()), Charset.defaultCharset(), CSVFormat.DEFAULT.withHeader());
                final Map<String, Integer> headers = parser.getHeaderMap();

                final String personCountHeader = "PERSON_COUNT";
                final String timeAtRiskHeader = "TIME_AT_RISK";
                final String casesHeader = "CASES";

                Map<String, Integer> values =
                        Arrays.asList(personCountHeader, timeAtRiskHeader, casesHeader)
                                .stream()
                                .collect(Collectors.toMap(header -> header, headers::get));
                final List<CSVRecord> records = parser.getRecords();

                if (!CollectionUtils.isEmpty(records)) {
                    final CSVRecord firstRecord = records.get(0);
                    final String personCount = firstRecord.get(values.get(personCountHeader));
                    final String timeAtRisk = firstRecord.get(values.get(timeAtRiskHeader));
                    final String cases = firstRecord.get(values.get(casesHeader));

                    resultInfo.add(personCountHeader, getJsonPrimitive(personCount));
                    resultInfo.add(timeAtRiskHeader, getJsonPrimitive(timeAtRisk));
                    resultInfo.add(casesHeader, getJsonPrimitive(cases));
                    try {
                        final float casesFloat = cast(cases).floatValue();
                        try {
                            final float timeAtRiskFloat = cast(timeAtRisk).floatValue();
                            final float rate = timeAtRiskFloat > 0 ? casesFloat / timeAtRiskFloat * 1000 : 0F;
                            resultInfo.add("RATE", new JsonPrimitive(rate));
                        } catch (IllegalArgumentException e) {
                            LOGGER.debug("'TIME_AT_RISK' is not correct value, skipping calculate 'RATE' value");
                        }
                        try {
                            final float personsFloat = cast(personCount).floatValue();
                            final float proportion = personsFloat > 0 ? casesFloat / personsFloat * 1000 : 0F;
                            resultInfo.add("PROPORTION", new JsonPrimitive(proportion));
                        } catch (IllegalArgumentException e) {
                            LOGGER.debug("'TIME_AT_RISK' is not correct value, skipping calculate 'PROPORTION' value");
                        }
                    } catch (IllegalArgumentException e) {
                        LOGGER.debug("'PERSON_COUNT' is not correct value, skipping calculate 'RATE' & 'PROPORTION' values");
                    }

                }
            } catch (IOException e) {
                LOGGER.debug(CAN_NOT_PARSE_LOG, INCIDENCE_SUMMARY_FILENAME);
            } catch (Exception e) {
                LOGGER.warn(CAN_NOT_BUILD_EXTEND_INFO_LOG, submission.getId());
                LOGGER.warn("Error: ", e);
            }
            submission.setResultInfo(resultInfo);
        }
    }

    private class EstimationSubmissionExtendInfoStrategy extends SubmissionExtendInfoAnalyzeStrategy {

        @Override
        public void updateExtendInfo(final Submission submission) {

            JsonObject resultInfo = new JsonObject();
            try {
                final List<ArachneFileMeta> resultFiles = searchFiles(submission, PLE_SUMMARY_FILENAME);
                if (resultFiles.size() > 0) {
                    resultInfo = parseCsvDataframeToJson(resultFiles.get(0).getPath());
                }
            } catch (IOException e) {
                LOGGER.warn(CAN_NOT_PARSE_LOG, PLE_SUMMARY_FILENAME);
            } catch (Exception e) {
                LOGGER.warn(CAN_NOT_BUILD_EXTEND_INFO_LOG, submission.getId());
                LOGGER.warn("Error: ", e);
            }
            submission.setResultInfo(resultInfo);
        }
    }

    private class PredictionSubmissionExtendInfoStrategy extends SubmissionExtendInfoAnalyzeStrategy {

        @Override
        public void updateExtendInfo(final Submission submission) {

            JsonObject resultInfo = new JsonObject();

            try {
                final String resultsDir = contentStorageHelper.getResultFilesDir(submission);
                final ArachneFileMeta arachneFile = contentStorageService.getFileByPath(resultsDir
                        + JcrContentStorageServiceImpl.PATH_SEPARATOR
                        + PLP_SUMMARY_FILENAME);
                resultInfo = parseCsvDataframeToJson(arachneFile.getPath());
            } catch (IOException e) {
                LOGGER.warn(CAN_NOT_PARSE_LOG, PLP_SUMMARY_FILENAME);
            } catch (Exception e) {
                LOGGER.warn(CAN_NOT_BUILD_EXTEND_INFO_LOG, submission.getId());
                LOGGER.warn("Error: ", e);
            }
            submission.setResultInfo(resultInfo);
        }
    }

    private List<ArachneFileMeta> searchFiles(Submission submission, String fileNameLike) {

        QuerySpec querySpec = new QuerySpec();

        querySpec.setPath(contentStorageHelper.getResultFilesDir(submission));
        querySpec.setName(fileNameLike);
        querySpec.setNameLike(true);
        querySpec.setSearchSubfolders(true);

        return contentStorageService.searchFiles(querySpec);
    }

    private JsonObject parseCsvDataframeToJson(String filepath) throws IOException {

        final JsonObject resultInfo = new JsonObject();

        final CSVParser parser = CSVParser.parse(contentStorageService.getContentByFilepath(filepath), Charset.defaultCharset(), CSVFormat.DEFAULT.withHeader());
        final Map<String, Integer> headerMap = parser.getHeaderMap();
        final List<CSVRecord> csvRecordList = parser.getRecords();

        JsonArray jsonHeaders = new JsonArray();
        headerMap.forEach((key, value) -> jsonHeaders.add(key));
        resultInfo.add("headers", jsonHeaders);

        JsonArray jsonRecords = new JsonArray();
        csvRecordList.forEach(record -> {
            final JsonObject jsonRecord = new JsonObject();
            for (Map.Entry<String, Integer> entry : headerMap.entrySet()) {
                final String key = entry.getKey();
                final String value = record.get(entry.getValue());
                if (NumberUtils.isCreatable(value)) {
                    jsonRecord.addProperty(key, Float.parseFloat(value));
                } else {
                    jsonRecord.addProperty(key, value);
                }
            }
            jsonRecords.add(jsonRecord);
        });
        resultInfo.add("records", jsonRecords);

        return resultInfo;
    }

    private static JsonElement getJsonPrimitive(final String value) {

        if (value == null) {
            return JsonNull.INSTANCE;
        }
        try {
            return new JsonPrimitive(cast(value));
        } catch (IllegalArgumentException e) {
            return new JsonPrimitive(value);
        }
    }

    private static Number cast(final String value) throws IllegalArgumentException {

        if (value != null && value.matches("^\\d+(\\.)\\d*$")) {
            return Float.valueOf(value);
        } else if (value != null && value.matches("^\\d+$")) {
            return Long.valueOf(value);
        } else {
            throw new IllegalArgumentException();
        }
    }

}
