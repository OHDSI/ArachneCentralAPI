package com.odysseusinc.arachne.portal.service.analysis.heracles.parts;

import com.odysseusinc.arachne.portal.service.analysis.heracles.HeraclesAnalysisKind;
import edu.emory.mathcs.backport.java.util.Arrays;
import org.ohdsi.sql.SqlRender;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.odysseusinc.arachne.portal.util.ResourcesUtils.loadStringResource;

@Component
public class FinalizeAnalysesRenderer implements HeraclesRenderer {

    public static final String FINALIZE_HERACLES_ANALYSES_SQL = "/org/ohdsi/cohortanalysis/sql/finalizeHeraclesAnalyses.sql";

    private static final String[] parameters = new String[]{"refreshStats", "runHERACLESHeel", "smallcellcount"};

    @Override
    public String render(HeraclesAnalysisKind analysisSpec) {

        final String template = loadStringResource(FINALIZE_HERACLES_ANALYSES_SQL);
        return SqlRender.renderSql(template, parameters, new String[]{
                Boolean.toString(analysisSpec.getRefreshStats()),
                Boolean.toString(analysisSpec.getRunAhillesHeel()),
                Integer.toString(analysisSpec.getSmallCellCount())});
    }

    @Override
    public List<String> getParameters() {

        return Arrays.asList(parameters);
    }

    @Override
    public String getPartName() {
        return "finalizeAnalyses.sql";
    }
}
