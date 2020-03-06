package com.odysseusinc.arachne.portal.service.submission.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.odysseusinc.arachne.portal.repository.ResultFileRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionFileRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionGroupRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionInsightRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionResultFileRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionStatusHistoryRepository;
import com.odysseusinc.arachne.portal.repository.submission.BaseSubmissionRepository;
import com.odysseusinc.arachne.portal.service.BaseDataSourceService;
import com.odysseusinc.arachne.portal.service.UserService;
import com.odysseusinc.arachne.portal.service.mail.ArachneMailSender;
import com.odysseusinc.arachne.portal.util.AnalysisHelper;
import com.odysseusinc.arachne.portal.util.ContentStorageHelper;
import com.odysseusinc.arachne.portal.util.LegacyAnalysisHelper;
import com.odysseusinc.arachne.portal.util.SubmissionHelper;
import com.odysseusinc.arachne.storage.service.ContentStorageService;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@RunWith(MockitoJUnitRunner.class)
public class SubmissionServiceImplTest {

    @InjectMocks
    @Spy
    private SubmissionServiceImpl submissionService;

    @Mock
    private BaseSubmissionRepository<?> submissionRepository;
    @Mock
    private BaseDataSourceService<?> dataSourceService;
    @Mock
    private ArachneMailSender mailSender;
    @Mock
    private AnalysisHelper analysisHelper;
    @Mock
    private SimpMessagingTemplate wsTemplate;
    @Mock
    private LegacyAnalysisHelper legacyAnalysisHelper;
    @Mock
    private SubmissionResultFileRepository submissionResultFileRepository;
    @Mock
    private SubmissionGroupRepository submissionGroupRepository;
    @Mock
    private SubmissionInsightRepository submissionInsightRepository;
    @Mock
    private SubmissionFileRepository submissionFileRepository;
    @Mock
    private ResultFileRepository resultFileRepository;
    @Mock
    private SubmissionStatusHistoryRepository submissionStatusHistoryRepository;
    @Mock
    private EntityManager entityManager;
    @Mock
    private SubmissionHelper submissionHelper;
    @Mock
    private ContentStorageService contentStorageService;
    @Mock
    private UserService userService;
    @Mock
    private ContentStorageHelper contentStorageHelper;

    @Captor
    private ArgumentCaptor<File> fileCaptor;



    @Test
    public void uploadResultsByDataOwner_zipFile() throws IOException {

        doReturn(null)
                .when(submissionService)
                .uploadResultFileByDataOwner(any(),  any(File.class));

        URL zipFileUrl = getClass().getClassLoader().getResource("submission/test.zip");

        submissionService.uploadCompressedResultsByDataOwner(1L, new File(zipFileUrl.getPath()));

        verify(submissionService, times(2))
                .uploadResultFileByDataOwner(any(), fileCaptor.capture());

        final List<String> capturedFileNames = fileCaptor.getAllValues().stream().map(file -> file.getName()).collect(Collectors.toList());

        assertThat(capturedFileNames).containsExactly("test2.txt", "test3.txt");

    }

}