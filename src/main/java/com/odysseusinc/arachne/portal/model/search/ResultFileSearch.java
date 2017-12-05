package com.odysseusinc.arachne.portal.model.search;

import com.odysseusinc.arachne.portal.model.Submission;
import org.apache.commons.lang3.StringUtils;

public class ResultFileSearch {

    private Submission submission;
    private String path;
    private String realName;

    private static String getFormattedPath(String path) {

        String formattedPath = path;
        if (StringUtils.isNotEmpty(path)
                && !path.equals("/")
                && !path.endsWith("/")) {
            formattedPath += "/";
        }
        return formattedPath;
    }

    public Submission getSubmission() {

        return submission;
    }

    public void setSubmission(Submission submission) {

        this.submission = submission;
    }

    public String getPath() {

        return path;
    }

    public void setPath(String path) {

        this.path = getFormattedPath(path);
    }

    public String getRealName() {

        return realName;
    }

    public void setRealName(String realName) {

        this.realName = realName;
    }


}
