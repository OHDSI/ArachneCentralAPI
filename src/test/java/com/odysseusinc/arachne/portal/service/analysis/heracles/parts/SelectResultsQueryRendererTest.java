package com.odysseusinc.arachne.portal.service.analysis.heracles.parts;

import com.google.common.collect.ImmutableMap;
import com.odysseusinc.arachne.portal.service.analysis.heracles.HeraclesAnalysisKind;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class SelectResultsQueryRendererTest {

    @Mock
    private HeraclesAnalysesLoader.HeraclesAnalysis analysis;
    @Mock
    private HeraclesAnalysesLoader heraclesAnalysesLoader;
    @InjectMocks
    private SelectResultsQueryRenderer selectResultsQueryRenderer;

    @Test
    public void shouldRenderEmptyStringIfNoAnalysesWithTheResults() {

        when(heraclesAnalysesLoader.readHeraclesAnalyses()).thenReturn(ImmutableMap.of(1, analysis));
        when(analysis.hasResults()).thenReturn(false);
        when(analysis.hasDistResults()).thenReturn(false);

        final String fragment = selectResultsQueryRenderer.render(HeraclesAnalysisKind.FULL);

        assertThat(fragment).isBlank();
    }

    @Test
    public void shouldRenderResultsSection() {

        when(heraclesAnalysesLoader.readHeraclesAnalyses()).thenReturn(ImmutableMap.of(1, analysis));
        when(analysis.hasResults()).thenReturn(true);
        when(analysis.hasDistResults()).thenReturn(false);

        final String fragment = selectResultsQueryRenderer.render(HeraclesAnalysisKind.FULL);

        assertThat(fragment).contains(SelectResultsQueryRenderer.INSERT_RESULT_STATEMENT);
        assertThat(fragment).doesNotContain(SelectResultsQueryRenderer.INSERT_DIST_RESULT_STATEMENT);
    }

    @Test
    public void shouldRenderDistinctResultsSection() {

        when(heraclesAnalysesLoader.readHeraclesAnalyses()).thenReturn(ImmutableMap.of(1, analysis));
        when(analysis.hasResults()).thenReturn(false);
        when(analysis.hasDistResults()).thenReturn(true);

        final String fragment = selectResultsQueryRenderer.render(HeraclesAnalysisKind.FULL);

        assertThat(fragment).doesNotContain(SelectResultsQueryRenderer.INSERT_RESULT_STATEMENT);
        assertThat(fragment).contains(SelectResultsQueryRenderer.INSERT_DIST_RESULT_STATEMENT);
    }
}