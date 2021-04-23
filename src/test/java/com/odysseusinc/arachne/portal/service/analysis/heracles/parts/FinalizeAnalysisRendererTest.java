package com.odysseusinc.arachne.portal.service.analysis.heracles.parts;

import com.odysseusinc.arachne.portal.service.analysis.heracles.HeraclesAnalysisKind;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static com.odysseusinc.arachne.portal.service.analysis.heracles.parts.HeraclesTestUtils.renameToSqlParameter;
import static org.assertj.core.api.Assertions.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class FinalizeAnalysisRendererTest {

    private FinalizeAnalysesRenderer finalizeAnalysisRenderer;

    @Before
    public void setUp(){

        finalizeAnalysisRenderer = new FinalizeAnalysesRenderer();
    }


    @Test
    public void shouldRenderFinalizeAnalysisFragmentAndSubstituteKeywords() {

        final String fragment = finalizeAnalysisRenderer.render(HeraclesAnalysisKind.FULL);

        assertThat(fragment).doesNotContain(renameToSqlParameter(finalizeAnalysisRenderer.getParameters()));
    }


    @Test
    public void shouldHandleFollowingKeywords(){

        assertThat(finalizeAnalysisRenderer.getParameters())
                .containsExactly("refreshStats", "runHERACLESHeel" , "smallcellcount");
    }
}