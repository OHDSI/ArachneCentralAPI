package com.odysseusinc.arachne.portal.model.search;

import com.odysseusinc.arachne.portal.model.Submission;

public class ResultFileSearch {

    private Submission submission;
    private String path;
    private String realName;

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

        this.path = path;
    }

    public String getRealName() {

        return realName;
    }

    public void setRealName(String realName) {

        this.realName = realName;
    }
}
