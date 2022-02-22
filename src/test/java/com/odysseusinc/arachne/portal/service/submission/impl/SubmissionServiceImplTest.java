package com.odysseusinc.arachne.portal.service.submission.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.model.security.ArachneUser;
import com.odysseusinc.arachne.portal.repository.SubmissionResultFileRepository;
import com.odysseusinc.arachne.portal.repository.submission.SubmissionRepository;
import com.odysseusinc.arachne.portal.security.JWTAuthenticationToken;
import com.odysseusinc.arachne.portal.service.UserService;
import com.odysseusinc.arachne.portal.util.ContentStorageHelper;
import com.odysseusinc.arachne.storage.model.ArachneFileMeta;
import com.odysseusinc.arachne.storage.service.ContentStorageService;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.context.SecurityContextHolder;

@RunWith(MockitoJUnitRunner.class)
public class SubmissionServiceImplTest {

    @Mock
    private Submission submission;
    @Mock
    private IUser user;
    @Mock
    private ArachneFileMeta arachneFileMeta;
    @Mock
    private ContentStorageService contentStorageService;
    @Mock
    private UserService userService;
    @Mock
    private SubmissionRepository submissionRepository;
    @Mock
    private ContentStorageHelper contentStorageHelper;
    @Mock
    private SubmissionResultFileRepository submissionResultFileRepository;
    @InjectMocks
    private SubmissionServiceImpl submissionService;
    @Captor
    private ArgumentCaptor<File> fileCaptor;
    @Captor
    private ArgumentCaptor<String> stringCaptor;
    @Captor
    private ArgumentCaptor<Long> longCaptor;

    @Before
    public void setUp() {
        SecurityContextHolder.getContext().setAuthentication(new JWTAuthenticationToken("", new ArachneUser(1L), null));
        when(submissionRepository.getOne(1L)).thenReturn(submission);
        when(contentStorageService.saveFile(any(), any(), any())).thenReturn(arachneFileMeta);
    }

    @Test
    public void shouldUploadFlatZipArchive() throws IOException {

        URL zipFileUrl = getClass().getClassLoader().getResource("submission/test.zip");

        submissionService.uploadCompressedResultsByDataOwner(1L, new File(zipFileUrl.getPath()));

        verify(contentStorageService, times(2)).saveFile(fileCaptor.capture(), stringCaptor.capture(), longCaptor.capture());
        verify(submissionResultFileRepository).saveAll(anyList());
        final List<String> capturedFileNames = fileCaptor.getAllValues().stream().map(File::getName).collect(Collectors.toList());
        assertThat(capturedFileNames).containsExactly("test2.txt", "test3.txt");
    }

    @Test
    public void shouldUploadZipArchivePreservingSubFolder() throws IOException {

        URL zipFileUrl = getClass().getClassLoader().getResource("submission/test_with_folders.zip");

        submissionService.uploadCompressedResultsByDataOwner(1L, new File(zipFileUrl.getPath()));

        verify(contentStorageHelper).getResultFilesDir(submission, "output/test2.txt");
        verify(contentStorageHelper).getResultFilesDir(submission, "output/test3.txt");

        verify(contentStorageService, times(2)).saveFile(fileCaptor.capture(), stringCaptor.capture(), longCaptor.capture());
        final List<String> capturedFileNames = fileCaptor.getAllValues().stream().map(File::getName).collect(Collectors.toList());
        assertThat(capturedFileNames).containsExactly("test2.txt", "test3.txt");
    }
}
