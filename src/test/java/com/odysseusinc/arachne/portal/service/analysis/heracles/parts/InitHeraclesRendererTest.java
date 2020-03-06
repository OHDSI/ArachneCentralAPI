package com.odysseusinc.arachne.portal.service.analysis.heracles.parts;

import com.odysseusinc.arachne.portal.service.analysis.heracles.HeraclesAnalysisKind;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.odysseusinc.arachne.portal.service.analysis.heracles.parts.HeraclesTestUtils.renameToSqlParameter;
import static org.assertj.core.api.Assertions.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class InitHeraclesRendererTest {

    private InitHeraclesRenderer initHeraclesRenderer;

    @Before
    public void setUp() {
        initHeraclesRenderer = new InitHeraclesRenderer();
    }

    @Test
    public void shouldRenderInitHeraclesFragmentAndSubstituteSqlParameters() {

        final String fragment = initHeraclesRenderer.render(HeraclesAnalysisKind.FULL);

        assertThat(fragment).doesNotContain(renameToSqlParameter(initHeraclesRenderer.getParameters()));
    }

    @Test
    public void shouldHandleFollowingKeywords() {

        assertThat(initHeraclesRenderer.getParameters())
                .containsExactly("list_of_analysis_ids", "periods");

    }

}

