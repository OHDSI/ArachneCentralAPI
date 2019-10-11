package com.odysseusinc.arachne.portal.api.v1.dto;

import java.util.Date;
import java.util.Map;

public class BaseSubmissionDTO extends DTO {

    private Long id;
    private DataSourceDTO dataSource;
    private SubmissionStatusDTO status;
    private Integer resultFilesCount;
    private PermissionsDTO permissions;
    private Date createdAt;
    private ShortUserDTO author;
    private Object resultInfo;
    private Boolean hidden;

    public BaseSubmissionDTO() {
    }

    public BaseSubmissionDTO(BaseSubmissionDTO other) {

        this.id = other.id;
        this.dataSource = other.dataSource;
        this.status = other.status;
        this.resultFilesCount = other.resultFilesCount;
        this.permissions = other.permissions;
        this.createdAt = other.createdAt;
        this.author = other.author;
        this.resultInfo = other.resultInfo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DataSourceDTO getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSourceDTO dataSource) {
        this.dataSource = dataSource;
    }

    public SubmissionStatusDTO getStatus() {
        return status;
    }

    public void setStatus(SubmissionStatusDTO status) {
        this.status = status;
    }

    public Integer getResultFilesCount() {
        return resultFilesCount;
    }

    public void setResultFilesCount(Integer resultFilesCount) {
        this.resultFilesCount = resultFilesCount;
    }

    public PermissionsDTO getPermissions() {
        return permissions;
    }

    public void setPermissions(PermissionsDTO permissions) {
        this.permissions = permissions;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public ShortUserDTO getAuthor() {
        return author;
    }

    public void setAuthor(ShortUserDTO author) {
        this.author = author;
    }

    public Object getResultInfo() {
        return resultInfo;
    }

    public void setResultInfo(Object resultInfo) {
        this.resultInfo = resultInfo;
    }

    public void setHidden(Boolean hidden) {

        this.hidden = hidden;
    }

    public Boolean getHidden() {

        return hidden;
    }
}
