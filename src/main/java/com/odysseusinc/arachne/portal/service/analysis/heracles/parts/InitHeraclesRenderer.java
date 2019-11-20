package com.odysseusinc.arachne.portal.service.analysis.heracles.parts;

import com.odysseusinc.arachne.portal.service.analysis.heracles.HeraclesAnalysisKind;
import edu.emory.mathcs.backport.java.util.Arrays;
import org.ohdsi.sql.SqlRender;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.odysseusinc.arachne.portal.util.ResourcesUtils.loadStringResource;
import static org.apache.commons.lang3.StringUtils.join;

@Component
public class InitHeraclesRenderer implements HeraclesRenderer {

    private static final String INIT_ANALYSES_SQL = "/org/ohdsi/cohortanalysis/sql/initHeraclesAnalyses.sql";
    private static final String[] parameters = new String[]{"list_of_analysis_ids", "periods"};

    @Override
    public String render(HeraclesAnalysisKind analysisSpec) {

        final String template = loadStringResource(INIT_ANALYSES_SQL);
        final String listOfAnalyses = join(analysisSpec.getAnalysesIds(), ',');
        return SqlRender.renderSql(template, parameters, new String[]{listOfAnalyses, analysisSpec.getPeriods()});
    }

    @Override
    public List<String> getParameters() {

        return Arrays.asList(parameters);
    }

    @Override
    public String getPartName() {
        return "initHeracles.sql";
    }
}
