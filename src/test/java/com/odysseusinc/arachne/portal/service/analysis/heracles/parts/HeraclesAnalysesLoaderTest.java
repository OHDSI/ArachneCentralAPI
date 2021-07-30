package com.odysseusinc.arachne.portal.service.analysis.heracles.parts;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class HeraclesAnalysesLoaderTest {

    private final int TOTAL_ANALYSES_COUNT = 231;
    private HeraclesAnalysesLoaderImpl heraclesAnalysesLoader;

    @Before
    public void setUp() {

        heraclesAnalysesLoader = new HeraclesAnalysesLoaderImpl();
    }


    @Test
    public void shouldLoadHeraclesAnalyses() {

        final Map<Integer, HeraclesAnalysesLoader.HeraclesAnalysis> allAnalyses = heraclesAnalysesLoader.readHeraclesAnalyses();

        assertThat(allAnalyses.size()).isEqualTo(TOTAL_ANALYSES_COUNT);
        final HeraclesAnalysesLoader.HeraclesAnalysis actualAnalysis = allAnalyses.get(101);
        assertThat(actualAnalysis.getName()).isEqualTo("Number of persons by age, with age at first observation period");
        assertThat(actualAnalysis.getFilename()).isEqualTo("101_numberOfPersonsByAge.sql");
        assertThat(actualAnalysis.hasDistResults()).isFalse();
        assertThat(actualAnalysis.hasResults()).isTrue();
    }


    @Test
    public void shouldLoadHeraclesAnalysesParams() {

        final Map<Integer, HeraclesAnalysesLoader.HeraclesAnalysis> allAnalyses = heraclesAnalysesLoader.readHeraclesAnalyses();
        final Map<Integer, Set<HeraclesAnalysesLoader.HeraclesAnalysisParameter>> analysesParams = heraclesAnalysesLoader.readAnalysesParams(allAnalyses);

        final Set<HeraclesAnalysesLoader.HeraclesAnalysisParameter> analysisParams = analysesParams.get(101);
        assertThat(analysisParams).containsExactlyInAnyOrder(
                new HeraclesAnalysesLoader.HeraclesAnalysisParameter(101, "analysisName", "Number of persons by age, with age at first observation period"),
                new HeraclesAnalysesLoader.HeraclesAnalysisParameter(101, "analysisId", "101")
        );


    }

}