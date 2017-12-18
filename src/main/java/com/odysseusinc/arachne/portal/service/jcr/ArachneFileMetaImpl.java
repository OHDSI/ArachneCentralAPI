package com.odysseusinc.arachne.portal.service.jcr;


import java.util.Date;

public class ArachneFileMetaImpl implements ArachneFileMeta {

    protected String uuid;

    protected String path;

    protected String name;

    protected Date created;

    protected Long createdBy;

    protected Date updated;

    protected String contentType;

    public ArachneFileMetaImpl() {

    }

    public ArachneFileMetaImpl(String uuid, String path, String name, Date created, Long createdBy, Date updated, String contentType) {

        this.uuid = uuid;
        this.path = path;
        this.name = name;
        this.created = created;
        this.createdBy = createdBy;
        this.updated = updated;
        this.contentType = contentType;
    }

    @Override
    public String getUuid() {

        return uuid;
    }

    public void setUuid(String uuid) {

        this.uuid = uuid;
    }

    @Override
    public String getPath() {

        return path;
    }

    public void setPath(String path) {

        this.path = path;
    }

    @Override
    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    @Override
    public Date getCreated() {

        return created;
    }

    public void setCreated(Date created) {

        this.created = created;
    }

    @Override
    public Long getCreatedBy() {

        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {

        this.createdBy = createdBy;
    }

    @Override
    public Date getUpdated() {

        return updated;
    }

    public void setUpdated(Date updated) {

        this.updated = updated;
    }

    @Override
    public String getContentType() {

        return contentType;
    }

    public void setContentType(String contentType) {

        this.contentType = contentType;
    }
}
