/*
 *
 * Copyright 2018 Observational Health Data Sciences and Informatics
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Company: Odysseus Data Services, Inc.
 * Product Owner/Architecture: Gregory Klebanov
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva, Mikhail Mironov
 * Created: July 14, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto;

import com.odysseusinc.arachne.portal.model.PublishState;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PaperDTO {

    private Long id;
    private StudyMediumDTO study;
    private List<PaperFileDTO> protocols;
    private List<PaperFileDTO> papers;
    private PermissionsDTO permissions;
    private PublishState publishState;
    private Date publishedDate;
    private Boolean favourite;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public StudyMediumDTO getStudy() {

        return study;
    }

    public void setStudy(StudyMediumDTO study) {

        this.study = study;
    }

    public List<PaperFileDTO> getProtocols() {

        return protocols;
    }

    public void setProtocols(List<PaperFileDTO> protocols) {

        this.protocols = protocols;
    }

    public List<PaperFileDTO> getPapers() {

        return papers;
    }

    public void setPapers(List<PaperFileDTO> papers) {

        this.papers = papers;
    }

    public PermissionsDTO getPermissions() {

        return permissions;
    }

    public void setPermissions(PermissionsDTO permissions) {

        this.permissions = permissions;
    }

    public PublishState getPublishState() {

        return publishState;
    }

    public void setPublishState(PublishState publishState) {

        this.publishState = publishState;
    }

    public Date getPublishedDate() {

        return publishedDate;
    }

    public void setPublishedDate(Date publishedDate) {

        this.publishedDate = publishedDate;
    }

    public Boolean getFavourite() {

        return favourite;
    }

    public void setFavourite(Boolean favourite) {

        this.favourite = favourite;
    }
}
