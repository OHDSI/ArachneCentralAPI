/*
 *
 * Copyright 2018 Odysseus Data Services, inc.
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

import static com.odysseusinc.arachne.storage.service.ContentStorageService.PATH_SEPARATOR;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
import com.odysseusinc.arachne.commons.utils.cohortcharacterization.CohortCharacterizationDocType;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.repository.SubmissionResultFileRepository;
import com.odysseusinc.arachne.storage.model.ArachneFileMeta;
import com.odysseusinc.arachne.storage.model.QuerySpec;
import com.odysseusinc.arachne.storage.service.ContentStorageService;
import com.odysseusinc.arachne.storage.service.JcrContentStorageServiceImpl;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional
    public void updateSubmissionExtendedInfo(final Submission submission) {

        final CommonAnalysisType analysisType = submission.getSubmissionGroup().getAnalysisType();
        SubmissionExtendInfoAnalyzeStrategy strategy;
        switch (analysisType) {
            case COHORT: {
                strategy = new CohortSubmissionExtendInfoStrategy();
                break;
            }
            case COHORT_HERACLES: {
                strategy = new CohortHeraclesSubmissionExtendInfoStrategy();
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
            case COHORT_CHARACTERIZATION: {
                strategy = new CohortCharacterizationExtendInfoStrategy();
                break;
            }
            case COHORT_PATHWAY: {
                strategy = new PathwaySubmissionExtendInfoStrategy();
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

    private class CohortHeraclesSubmissionExtendInfoStrategy extends SubmissionExtendInfoAnalyzeStrategy {

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

        private static final String STUDY_SPECIFICATION_JSON = "StudySpecification.json";

        @Override
        public void updateExtendInfo(Submission submission) {

            final JsonArray result = new JsonArray();
            try {
                final String resultsDir = contentStorageHelper.getResultFilesDir(submission);
                final Map<String, String> cohortNames = new HashMap<>();
                List<ArachneFileMeta> packageFiles = searchFiles(submission, "IncidenceRate%.zip");
                if (!packageFiles.isEmpty()) {
                    Path tmpDir = Files.createTempDirectory("incidencerate");
                    try {
                        File archiveFile = tmpDir.resolve("IncidenceRate.zip").toFile();
                        try(OutputStream out = new FileOutputStream(archiveFile);
                            InputStream fileIn = contentStorageService.getContentByFilepath(packageFiles.get(0).getPath())) {
                            IOUtils.copy(fileIn, out);
                        }
                        try(FileInputStream in = new FileInputStream(archiveFile);
                            ZipInputStream zip = new ZipInputStream(in)) {
                            ZipEntry entry = zip.getNextEntry();
                            while(entry != null) {
                                if (entry.getName().endsWith(STUDY_SPECIFICATION_JSON)) {
                                    File jsonFile = tmpDir.resolve(STUDY_SPECIFICATION_JSON).toFile();
                                    try(FileOutputStream out = new FileOutputStream(jsonFile)) {
                                        IOUtils.copy(zip, out);
                                    }
                                }
                                zip.closeEntry();
                                entry = zip.getNextEntry();
                            }
                        }

                        Path specFile = tmpDir.resolve(STUDY_SPECIFICATION_JSON);
                        if (Files.exists(specFile) && Files.isRegularFile(specFile)) {
                            JsonParser parser = new JsonParser();
                            try (Reader json = new FileReader(specFile.toFile())) {
                                JsonObject spec = parser.parse(json).getAsJsonObject();
                                JsonArray targets = spec.get("targetCohorts").getAsJsonArray();
                                cohortNames.putAll(getCohortNames(targets));
                                JsonArray outcomes = spec.get("outcomeCohorts").getAsJsonArray();
                                cohortNames.putAll(getCohortNames(outcomes));
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.warn("Failed to parse cohort names, {}", e.getMessage());
                    } finally {
                        FileUtils.deleteQuietly(tmpDir.toFile());
                    }
                }

                ArachneFileMeta arachneFile = contentStorageService.getFileByPath(resultsDir
                        + PATH_SEPARATOR
                        + INCIDENCE_SUMMARY_FILENAME);
                final CSVParser parser = CSVParser.parse(contentStorageService.getContentByFilepath(arachneFile.getPath()), Charset.defaultCharset(), CSVFormat.DEFAULT.withHeader());
                final Map<String, Integer> headers = parser.getHeaderMap();

                final String targetIdHeader = "TARGET_ID";
                final String outcomeIdHeader = "OUTCOME_ID";
                final String targetNameHeader = "TARGET_NAME";
                final String outcomeNameHeader = "OUTCOME_NAME";
                final String personCountHeader = "PERSON_COUNT";
                final String timeAtRiskHeader = "TIME_AT_RISK";
                final String casesHeader = "CASES";

                Map<String, Integer> values =
                        Stream.of(targetIdHeader, outcomeIdHeader, personCountHeader, timeAtRiskHeader, casesHeader)
                                .collect(Collectors.toMap(header -> header, headers::get));
                final List<CSVRecord> records = parser.getRecords();

                if (!CollectionUtils.isEmpty(records)) {
                    records.forEach(record -> {

                        final String targetId = record.get(values.get(targetIdHeader));
                        final String targetName = cohortNames.getOrDefault(targetId, "");
                        final String outcomeId = record.get(values.get(outcomeIdHeader));
                        final String outcomeName = cohortNames.getOrDefault(outcomeId, "");
                        final String personCount = record.get(values.get(personCountHeader));
                        final String timeAtRisk = record.get(values.get(timeAtRiskHeader));
                        final String cases = record.get(values.get(casesHeader));

                        final JsonObject resultInfo = new JsonObject();
                        resultInfo.add(targetIdHeader, getJsonPrimitive(targetId));
                        resultInfo.add(targetNameHeader, getJsonPrimitive(targetName));
                        resultInfo.add(outcomeIdHeader, getJsonPrimitive(outcomeId));
                        resultInfo.add(outcomeNameHeader, getJsonPrimitive(outcomeName));
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
                            result.add(resultInfo);
                        } catch (IllegalArgumentException e) {
                            LOGGER.debug("'PERSON_COUNT' is not correct value, skipping calculate 'RATE' & 'PROPORTION' values");
                        }
                    });

                }
            } catch (IOException e) {
                LOGGER.debug(CAN_NOT_PARSE_LOG, INCIDENCE_SUMMARY_FILENAME);
            } catch (Exception e) {
                LOGGER.warn(CAN_NOT_BUILD_EXTEND_INFO_LOG, submission.getId());
                LOGGER.warn("Error: ", e);
            }
            submission.setResultInfo(result);
        }

        private Map<String, String> getCohortNames(JsonArray cohorts) {

            Map<String, String> result = new HashMap<>();
            cohorts.forEach(c -> {
                String id = c.getAsJsonObject().get("id").getAsString();
                String name = c.getAsJsonObject().get("name").getAsString();
                result.put(id, name);
            });
            return result;
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
                        + PATH_SEPARATOR
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

    private class CohortCharacterizationExtendInfoStrategy extends SubmissionExtendInfoAnalyzeStrategy {

        @Override
        public void updateExtendInfo(Submission submission) {

            JsonObject resultInfo = new JsonObject();

            final String resultsDir = contentStorageHelper.getResultFilesDir(submission)
                    + JcrContentStorageServiceImpl.PATH_SEPARATOR + "results";
            QuerySpec querySpec = new QuerySpec();
            querySpec.setPath(resultsDir);
            querySpec.setName("%.csv");
            querySpec.setNameLike(true);
            long reportCount = contentStorageService.searchFiles(querySpec)
                    .stream()
                    .filter(f -> !Objects.equals(f.getName(), "raw_data.csv"))
                    .count();
            resultInfo.add("reports", new JsonPrimitive(reportCount));
            submission.setResultInfo(resultInfo);
        }
    }

    private class PathwaySubmissionExtendInfoStrategy extends SubmissionExtendInfoAnalyzeStrategy {

        private static final int MAX_PATHWAY_STEPS = 10;

        @Override
        public void updateExtendInfo(Submission submission) {

            JsonObject resultInfo = new JsonObject();
            try {
                final String rootDir = contentStorageHelper.getResultFilesDir(submission);
                final String resultsDir = filePath(rootDir, "results");
                final String designDir = filePath(rootDir, "design");

                JsonElement design;
                try(JsonReader jsonReader = new JsonReader(new InputStreamReader(contentStorageService.getContentByFilepath(designDir
                        + PATH_SEPARATOR + "StudySpecification.json")))) {
                    JsonParser parser = new JsonParser();
                    design = parser.parse(jsonReader);
                }
                resultInfo.add("design", design);

                // Pathway codes
                String path = filePath(resultsDir, "pathway_codes.csv");
                resultInfo.add("eventCodes", getPathwayCodes(path));

                path = filePath(resultsDir, "cohort_stats.csv");
                JsonArray cohortStats = getCohortStats(path);

                // Cohort Paths
                path = filePath(resultsDir, "pathway_results.csv");
                Map<Integer, JsonArray> pathwayResults  = getPathwayResults(path);

                // Combine stats with paths
                cohortStats.forEach(cs -> {
                    JsonObject cohortStat = cs.getAsJsonObject();
                    Integer targetCohortId = cohortStat.get("targetCohortId").getAsInt();
                    JsonArray cohortPath = pathwayResults.get(targetCohortId);
                    cohortStat.add("pathways", cohortPath);
                });

                resultInfo.add("pathwayGroups", cohortStats);

            } catch (Exception e) {
                LOGGER.warn(CAN_NOT_BUILD_EXTEND_INFO_LOG, submission.getId());
                LOGGER.warn("Error: ", e);
            }

            submission.setResultInfo(resultInfo);
        }

        private Map<Integer, JsonArray> getPathwayResults(String path) throws IOException {

            return parseCsv(path, rec -> {
               JsonObject result = new JsonObject();
               result.add("targetCohortId", getJsonPrimitive(rec.get("TARGET_COHORT_ID")));
               result.add("personCount", getJsonPrimitive(rec.get("COUNT_VALUE")));
               List<String> cohortPath = new ArrayList<>();
               for(int i = 1; i <= MAX_PATHWAY_STEPS; i++) {
                   String column = "STEP_" + i;
                   String value = rec.get(column);
                   if (StringUtils.isBlank(value)) {
                       break;
                   }
                   cohortPath.add(value);
               }
               result.addProperty("path", StringUtils.join(cohortPath, "-"));
               return result;
            }, Collectors.groupingBy(pr -> pr.getAsJsonObject().get("targetCohortId").getAsInt(),
                                    ArachneCollectors.toJsonArray()));
        }

        private JsonArray getCohortStats(String path) throws IOException {

            return parseCsv(path, rec -> {
               JsonObject stat = new JsonObject();
               stat.add("targetCohortId", getJsonPrimitive(rec.get("TARGET_COHORT_ID")));
               stat.add("targetCohortCount", getJsonPrimitive(rec.get("TARGET_COHORT_COUNT")));
               stat.add("totalPathwaysCount", getJsonPrimitive(rec.get("PATHWAYS_COUNT")));
               return stat;
            }, ArachneCollectors.toJsonArray());
        }

        private JsonArray getPathwayCodes(String path) throws IOException {

            return parseCsv(path, rec -> {
                JsonObject pathwayCode = new JsonObject();
                pathwayCode.add("code", getJsonPrimitive(rec.get("CODE")));
                pathwayCode.add("name", getJsonPrimitive(rec.get("NAME")));
                pathwayCode.add("isCombo", getJsonPrimitive(rec.get("IS_COMBO")));
                return pathwayCode;
            });
        }
    }

    private JsonArray parseCsv(String csvFilePath, Mapper mapper) throws IOException {

        return parseCsv(csvFilePath, mapper, ArachneCollectors.toJsonArray());
    }

    private <R> R parseCsv(String csvFilePath, Mapper mapper, Collector<JsonElement, ?, R> collector) throws IOException {
        ArachneFileMeta arachneFile = contentStorageService.getFileByPath(csvFilePath);
        final CSVParser parser = CSVParser.parse(contentStorageService.getContentByFilepath(arachneFile.getPath()), Charset.defaultCharset(), CSVFormat.DEFAULT.withHeader());
        return StreamSupport.stream(parser.spliterator(), false).map(mapper::mapRecord).collect(collector);
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

    private String filePath(String ...path) {
        return StringUtils.join(path, PATH_SEPARATOR);
    }

    interface Mapper {
        JsonElement mapRecord(CSVRecord record);
    }
}
