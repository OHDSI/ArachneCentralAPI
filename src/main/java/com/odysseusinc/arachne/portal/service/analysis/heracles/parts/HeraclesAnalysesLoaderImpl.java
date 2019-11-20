package com.odysseusinc.arachne.portal.service.analysis.heracles.parts;

import com.odysseusinc.arachne.portal.exception.ArachneSystemRuntimeException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.odysseusinc.arachne.portal.util.ResourcesUtils.loadStringResource;

@Component
public class HeraclesAnalysesLoaderImpl implements HeraclesAnalysesLoader {

    public static final String HERACLES_ANALYSES = "/org/ohdsi/cohortanalysis/heraclesanalyses/csv/heraclesAnalyses.csv";
    public static final String HERACLES_ANALYSES_PARAMS = "/org/ohdsi/cohortanalysis/heraclesanalyses/csv/heraclesAnalysesParams.csv";

    //Columns
    private static final String COL_ANALYSIS_ID = "analysisId";
    private static final String COL_ANALYSIS_NAME = "analysisName";
    private static final String COL_SQL_FILE_NAME = "sqlFileName";
    private static final String COL_RESULTS = "results";
    private static final String COL_DIST_RESULTS = "distResults";
    private static final String COL_PARAM_NAME = "paramName";
    private static final String COL_PARAM_VALUE = "paramValue";


    @Override
    public Map<Integer, HeraclesAnalysis> readHeraclesAnalyses() {

        return parseCSV(HERACLES_ANALYSES, record -> new HeraclesAnalysis(
                Integer.parseInt(record.get(COL_ANALYSIS_ID)),
                record.get(COL_ANALYSIS_NAME),
                record.get(COL_SQL_FILE_NAME),
                Boolean.parseBoolean(record.get(COL_RESULTS)),
                Boolean.parseBoolean(record.get(COL_DIST_RESULTS))))
                .stream()
                .collect(Collectors.toMap(HeraclesAnalysesLoader.HeraclesAnalysis::getId, analysis -> analysis));
    }

    @Override
    public Map<Integer, Set<HeraclesAnalysisParameter>> readAnalysesParams(Map<Integer, HeraclesAnalysis> heraclesAnalysisMap) {

        Map<Integer, Set<HeraclesAnalysisParameter>> analysesParamsMap = new HashMap<>();
        parseCSV(HERACLES_ANALYSES_PARAMS, record -> new HeraclesAnalysisParameter(Integer.parseInt(record.get(COL_ANALYSIS_ID)),
                record.get(COL_PARAM_NAME), record.get(COL_PARAM_VALUE)))
                .forEach(parameterRecord -> {
                    Set<HeraclesAnalysisParameter> params = analysesParamsMap.getOrDefault(parameterRecord.getAnalysisId(), new HashSet<>());
                    params.add(parameterRecord);
                    analysesParamsMap.put(parameterRecord.getAnalysisId(), params);
                });

        heraclesAnalysisMap.values().forEach(analysis -> {
            Integer id = analysis.getId();
            Set<HeraclesAnalysisParameter> params = analysesParamsMap.getOrDefault(id, new HashSet<>());
            params.add(new HeraclesAnalysisParameter(id, COL_ANALYSIS_ID, id.toString()));
            params.add(new HeraclesAnalysisParameter(id, COL_ANALYSIS_NAME, analysis.getName()));
            analysesParamsMap.put(id, params);
        });

        return analysesParamsMap;
    }

    private <T> List<T> parseCSV(String resourcePath, Function<CSVRecord, T> recordMapper) {

        String csvBody = loadStringResource(resourcePath);

        try {
            final CSVParser parser = CSVParser.parse(csvBody, CSVFormat.RFC4180.withFirstRecordAsHeader());
            return parser.getRecords().stream().map(recordMapper).collect(Collectors.toList());
        } catch (IOException ex) {
            throw new ArachneSystemRuntimeException("Cannot parse CSV: " + resourcePath, ex);
        }
    }
}
