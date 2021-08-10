package com.odysseusinc.arachne.portal.service.analysis;

import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.service.analysis.impl.AnalysisFilesSavingServiceImpl;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.odysseusinc.arachne.commons.utils.CommonFileUtils.ANALYSIS_INFO_FILE_DESCRIPTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AnalysisFilesSavingServiceImplTest {

    private static final String ANALYSIS_DESCRIPTION_TEST_VALUE = "new analysis description value";
    private final MultipartFile description_file;
    @Mock
    private Analysis analysis;
    @InjectMocks
    private AnalysisFilesSavingServiceImpl analysisFilesSavingService;

    {
        description_file = new MockMultipartFile(ANALYSIS_INFO_FILE_DESCRIPTION, ANALYSIS_DESCRIPTION_TEST_VALUE.getBytes());
    }

    @Test
    public void shouldSkipUpdateIfDescriptionFileNotProvided() throws IOException {

        final String newDescription = analysisFilesSavingService.updateAnalysisFromMetaFiles(analysis, Lists.emptyList());

        verify(analysis, never()).setDescription(anyString());
        assertThat(newDescription).isNull();
    }

    @Test
    public void shouldAutomaticallyUpdateEmptyDescriptionAndReturnNull() throws IOException {

        final String newDescription = analysisFilesSavingService.updateAnalysisFromMetaFiles(analysis, Lists.list(description_file));

        verify(analysis).setDescription(ANALYSIS_DESCRIPTION_TEST_VALUE);
        assertThat(newDescription).isNull();
    }

    @Test
    public void shouldNotUpdateOldDescriptionButReturnNewDescription() throws IOException {

        when(analysis.getDescription()).thenReturn("old description");

        final String newDescription = analysisFilesSavingService.updateAnalysisFromMetaFiles(analysis, Lists.list(description_file));

        verify(analysis, never()).setDescription(anyString());
        assertThat(newDescription).isEqualTo(ANALYSIS_DESCRIPTION_TEST_VALUE);
    }

    @Test
    public void shouldNotUpdateTheSameDescriptionAndReturnNull() throws IOException {

        when(analysis.getDescription()).thenReturn(ANALYSIS_DESCRIPTION_TEST_VALUE);

        final String newDescription = analysisFilesSavingService.updateAnalysisFromMetaFiles(analysis, Lists.list(description_file));

        verify(analysis, never()).setDescription(anyString());
        assertThat(newDescription).isNull();
    }
}