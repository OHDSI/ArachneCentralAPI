package com.odysseusinc.arachne.portal.service.analysis.heracles.parts;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.odysseusinc.arachne.portal.service.analysis.heracles.HeraclesAnalysisKind;
import edu.emory.mathcs.backport.java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.sql.SqlRender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.odysseusinc.arachne.portal.service.analysis.heracles.HeraclesConstants.CDM_VERSION;
import static com.odysseusinc.arachne.portal.service.analysis.heracles.HeraclesConstants.COHORT_PERIOD_ONLY;
import static com.odysseusinc.arachne.portal.service.analysis.heracles.HeraclesConstants.INCLUDE_COST_CONCEPTS_DEFAULT;
import static com.odysseusinc.arachne.portal.service.analysis.heracles.HeraclesConstants.INCLUDE_CURRENCY_DEFAULT;
import static com.odysseusinc.arachne.portal.util.ResourcesUtils.loadStringResource;


@Component
public class AnalysesQueriesRenderer implements HeraclesRenderer {

    public static final String ANALYSES_QUERY_PREFIX = "/org/ohdsi/cohortanalysis/heraclesanalyses/sql/";
    private static final String[] parameters = new String[]{
            "CDM_version",
            "cohort_period_only",
            "condition_concept_ids",
            "drug_concept_ids",
            "includeCostConcepts",
            "includeCurrency",
            "includeDrugTypeUtilization",
            "includeVisitTypeUtilization",
            "measurement_concept_ids",
            "observation_concept_ids",
            "procedure_concept_ids",
            "rollupUtilizationDrug",
            "rollupUtilizationVisit",
            "smallcellcount",
            "analysisName",
            "analysisId"
    };
    private final Logger log = LoggerFactory.getLogger(AnalysesQueriesRenderer.class);
    private final HeraclesAnalysesLoader heraclesAnalysesLoader;

    public AnalysesQueriesRenderer(HeraclesAnalysesLoader heraclesAnalysesLoader) {

        this.heraclesAnalysesLoader = heraclesAnalysesLoader;
    }


    @Override
    public String render(HeraclesAnalysisKind analysisSpec) {

        final Map<Integer, HeraclesAnalysesLoader.HeraclesAnalysis> allAnalyses = heraclesAnalysesLoader.readHeraclesAnalyses();
        final Map<Integer, Set<HeraclesAnalysesLoader.HeraclesAnalysisParameter>> allParams = heraclesAnalysesLoader.readAnalysesParams(allAnalyses);


        StringBuilder analysesQueries = new StringBuilder();
        for (int analysisId : analysisSpec.getAnalysesIds()) {

            HeraclesAnalysesLoader.HeraclesAnalysis analysis = allAnalyses.get(analysisId);
            if (analysis != null) {
                String analysisTemplate = renderAnalysisQuery(analysisSpec, analysis, allParams.get(analysisId));
                analysesQueries.append(analysisTemplate);
                analysesQueries.append("\n");
            } else {
                log.warn("Cannot find analysis definition for the analysis Id: {}", analysisId);
            }
        }

        return analysesQueries.toString();
    }

    private String renderAnalysisQuery(HeraclesAnalysisKind analysisSpec, HeraclesAnalysesLoader.HeraclesAnalysis analysis, Set<HeraclesAnalysesLoader.HeraclesAnalysisParameter> analysisParams) {

        Preconditions.checkNotNull(analysisSpec);
        Preconditions.checkNotNull(analysis);
        Preconditions.checkNotNull(analysisParams);

        final List<String> paramNames = Lists.newArrayList(getParameters());

        final List<String> paramValues = Lists.newArrayList(
                CDM_VERSION,
                COHORT_PERIOD_ONLY,
                join(analysisSpec.getConditionConceptIds()),
                join(analysisSpec.getDrugConceptIds()),
                INCLUDE_COST_CONCEPTS_DEFAULT,
                INCLUDE_CURRENCY_DEFAULT,
                join(analysisSpec.getIncludeDrugTypeUtilization()),
                join(analysisSpec.getIncludeVisitTypeUtilization()),
                join(analysisSpec.getMeasurementConceptIds()),
                join(analysisSpec.getObservationConceptIds()),
                join(analysisSpec.getProcedureConceptIds()),
                Boolean.toString(analysisSpec.isRollupUtilizationDrug()),
                Boolean.toString(analysisSpec.isRollupUtilizationVisit()),
                Integer.toString(analysisSpec.getSmallCellCount()),
                analysis.getName(),
                Integer.toString(analysis.getId())
        );


        for (HeraclesAnalysesLoader.HeraclesAnalysisParameter analysisParam : analysisParams) {
            paramNames.add(analysisParam.getParamName());
            paramValues.add(analysisParam.getValue());
        }

        String template = loadStringResource(ANALYSES_QUERY_PREFIX + analysis.getFilename());

        return SqlRender.renderSql(template, toArray(paramNames), toArray(paramValues));
    }

    private String[] toArray(List<String> paramNames) {

        return paramNames.stream().toArray(String[]::new);
    }

    private String join(int[] conditionConceptIds) {

        if (conditionConceptIds == null || conditionConceptIds.length == 0) {
            return StringUtils.EMPTY;
        }
        return StringUtils.join(conditionConceptIds, ',');
    }


    @Override
    public List<String> getParameters() {
        return Arrays.asList(parameters);
    }

    @Override
    public String getPartName() {
        return "analysesQueries.sql";
    }
}



