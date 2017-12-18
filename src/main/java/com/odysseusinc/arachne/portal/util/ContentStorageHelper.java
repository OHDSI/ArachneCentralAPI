package com.odysseusinc.arachne.portal.util;

import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.service.ContentStorageService;
import java.util.Arrays;

public class ContentStorageHelper {

    public static String RESULT_FILES_DIR = "results";

    private ContentStorageService contentStorageService;

    public ContentStorageHelper(ContentStorageService contentStorageService) {

        this.contentStorageService = contentStorageService;
    }

    public String getResultFilesDir(Submission submission, String relativeFolder) {

        return contentStorageService.getJcrLocationForEntity(submission, Arrays.asList(RESULT_FILES_DIR, relativeFolder));
    }

    public String getResultFilesDir(Submission submission) {

        return getResultFilesDir(submission, null);
    }
}
