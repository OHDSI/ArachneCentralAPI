package com.odysseusinc.arachne.portal.service.analysis.heracles.parts;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.odysseusinc.arachne.portal.service.analysis.heracles.HeraclesAnalysisKind;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static com.odysseusinc.arachne.portal.service.analysis.heracles.parts.HeraclesTestUtils.renameToSqlParameter;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class AnalysesQueriesRendererTest {

    private HeraclesAnalysesLoader.HeraclesAnalysis analysis;
    private HeraclesAnalysesLoader.HeraclesAnalysisParameter analysisParam;
    @Mock
    private HeraclesAnalysesLoader heraclesAnalysesLoader;
    @InjectMocks
    private AnalysesQueriesRenderer analysesQueriesRenderer;
    private static int analysis_111_id = 111;

    @Before
    public void setUp() {

        analysis = new HeraclesAnalysesLoader.HeraclesAnalysis(analysis_111_id, "Number of persons by observation period start month", "111_112_numberOfPersosnByObservationPeriod.sql", true, false);
        analysisParam = new HeraclesAnalysesLoader.HeraclesAnalysisParameter(analysis_111_id, "fieldName", "observation_period_start_date");
    }

    @Test
    public void itShouldRenderAnalysisAndSubstituteParameters() {

        when(heraclesAnalysesLoader.readHeraclesAnalyses()).thenReturn(ImmutableMap.of(analysis_111_id, analysis));
        when(heraclesAnalysesLoader.readAnalysesParams(any())).thenReturn(ImmutableMap.of(analysis_111_id, ImmutableSet.of(analysisParam)));

        final String fragment = analysesQueriesRenderer.render(HeraclesAnalysisKind.FULL);

        final List<String> sqlParametersList = renameToSqlParameter(analysesQueriesRenderer.getParameters());
        assertThat(fragment).doesNotContain(sqlParametersList);
        assertThat(fragment).contains("@results_schema", "@CDM_schema");
    }
}