package com.odysseusinc.arachne.portal.service.analysis.heracles.parts;

import com.odysseusinc.arachne.portal.service.analysis.heracles.HeraclesAnalysisKind;
import edu.emory.mathcs.backport.java.util.Arrays;
import org.ohdsi.sql.SqlRender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.odysseusinc.arachne.portal.util.ResourcesUtils.loadStringResource;


@Component
public class SelectResultsQueryRenderer implements HeraclesRenderer {

    public static final String INSERT_RESULT_STATEMENT = "insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3, stratum_4, count_value)\n";
    public static final String INSERT_DIST_RESULT_STATEMENT = "insert into @results_schema.heracles_results_dist (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3, stratum_4, stratum_5, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)\n";

    private static final String SELECT_RESULT_STATEMENT = "/org/ohdsi/cohortanalysis/sql/selectHeraclesResults.sql";
    private static final String SELECT_DIST_RESULT_STATEMENT = "/org/ohdsi/cohortanalysis/sql/selectHeraclesDistResults.sql";

    private static final String UNION_ALL = "\nUNION ALL\n";

    private static final String[] keywords = new String[]{"analysisId"};
    private final Logger log = LoggerFactory.getLogger(AnalysesQueriesRenderer.class);
    private final HeraclesAnalysesLoader heraclesAnalysesLoader;

    public SelectResultsQueryRenderer(HeraclesAnalysesLoader heraclesAnalysesLoader) {

        this.heraclesAnalysesLoader = heraclesAnalysesLoader;
    }

    @Override
    public List<String> getParameters() {

        return Arrays.asList(keywords);
    }

    @Override
    public String getPartName() {

        return "selectResultsQuery.sql";
    }

    public String render(HeraclesAnalysisKind analysisSpec) {

        final String select_result_fragment = loadStringResource(SELECT_RESULT_STATEMENT);
        final String select_dist_result_fragment = loadStringResource(SELECT_DIST_RESULT_STATEMENT);

        final Map<Integer, HeraclesAnalysesLoader.HeraclesAnalysis> allAnalyses = heraclesAnalysesLoader.readHeraclesAnalyses();
        List<String> resultsQueries = new ArrayList<>();
        List<String> distResultsQueries = new ArrayList<>();

        for (int analysisId : analysisSpec.getAnalysesIds()) {
            final HeraclesAnalysesLoader.HeraclesAnalysis analysis = allAnalyses.get(analysisId);
            if (analysis != null) {
                final String[] paramValues = {String.valueOf(analysisId)};
                if (analysis.hasResults()) {
                    resultsQueries.add(SqlRender.renderSql(select_result_fragment, keywords, paramValues));
                }
                if (analysis.hasDistResults()) {
                    distResultsQueries.add(SqlRender.renderSql(select_dist_result_fragment, keywords, paramValues));
                }

            } else {
                log.debug("Cannot find analysis definition for the analysis Id: {}", analysisId);
            }


        }

        StringBuilder resultQuery = new StringBuilder();
        if (!resultsQueries.isEmpty()) {
            resultQuery.append(INSERT_RESULT_STATEMENT)
                    .append(resultsQueries.stream().collect(Collectors.joining(UNION_ALL)))
                    .append(";")
                    .append("\n");
        }
        if (!distResultsQueries.isEmpty()) {
            resultQuery.append(INSERT_DIST_RESULT_STATEMENT)
                    .append(distResultsQueries.stream().collect(Collectors.joining(UNION_ALL)))
                    .append(";")
                    .append("\n");
        }

        return resultQuery.toString();
    }
}
