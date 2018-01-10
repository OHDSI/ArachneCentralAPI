package com.odysseusinc.arachne.portal.util;

import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.storage.service.ContentStorageService;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ContentStorageHelper {

    public static String RESULT_FILES_DIR = "results";

    private ContentStorageService contentStorageService;

    @Autowired
    public ContentStorageHelper(
            ContentStorageService contentStorageService
    ) {

        this.contentStorageService = contentStorageService;
    }

    public String getResultFilesDir(Submission submission, String relativeFolder) {

        return contentStorageService.getLocationForEntity(submission, Arrays.asList(RESULT_FILES_DIR, relativeFolder));
    }

    public String getResultFilesDir(Submission submission) {

        return getResultFilesDir(submission, null);
    }

    public String getResultFilesDir(Class domainClazz, Serializable identifier, String relativeFolder) {

        return contentStorageService.getLocationForEntity(domainClazz, identifier, Arrays.asList(RESULT_FILES_DIR, relativeFolder));
    }

    public String getRelativePath(String basePath, String nodePath) {

        return Paths.get(basePath).relativize(Paths.get(nodePath)).toString().replace('\\', '/');
    }
}
