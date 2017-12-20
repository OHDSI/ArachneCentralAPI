package com.odysseusinc.arachne.portal.util;

import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.storage.service.ContentStorageService;
import java.nio.file.Path;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ContentStorageHelper {

    public static String RESULT_FILES_DIR = "results";

    private ContentStorageService contentStorageService;
    private AnalysisHelper analysisHelper;

    @Autowired
    public ContentStorageHelper(
            ContentStorageService contentStorageService,
            AnalysisHelper analysisHelper
    ) {

        this.contentStorageService = contentStorageService;
        this.analysisHelper = analysisHelper;
    }

    public String getResultFilesDir(Submission submission, String relativeFolder) {

        return contentStorageService.getLocationForEntity(submission, Arrays.asList(RESULT_FILES_DIR, relativeFolder));
    }

    public String getResultFilesDir(Submission submission) {

        return getResultFilesDir(submission, null);
    }
}
