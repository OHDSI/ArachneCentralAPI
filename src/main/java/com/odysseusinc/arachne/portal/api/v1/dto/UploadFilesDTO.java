package com.odysseusinc.arachne.portal.api.v1.dto;

import java.util.LinkedList;
import java.util.List;

public class UploadFilesDTO {
    private List<UploadFileDTO> files = new LinkedList<>();
    private List<UploadFileDTO> links = new LinkedList<>();

    public List<UploadFileDTO> getFiles() {

        return files;
    }

    public void setFiles(LinkedList<UploadFileDTO> files) {

        this.files = files;
    }

    public List<UploadFileDTO> getLinks() {

        return links;
    }

    public void setLinks(List<UploadFileDTO> links) {

        this.links = links;
    }
}
