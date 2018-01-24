package com.odysseusinc.arachne.portal.model.search;

import com.odysseusinc.arachne.storage.service.ContentStorageService;

public class ResultFileSearch {

    private String path;
    private String realName;

    public String getPath() {

        return path;
    }

    public void setPath(String path) {

        String relativePath = (path.length() > 1 && path.startsWith(ContentStorageService.PATH_SEPARATOR)) ? path.substring(1) : path;
        this.path = relativePath;
    }

    public String getRealName() {

        return realName;
    }

    public void setRealName(String realName) {

        this.realName = realName;
    }


}
