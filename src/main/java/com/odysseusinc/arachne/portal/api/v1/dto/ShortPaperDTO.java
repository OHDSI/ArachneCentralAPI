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
 * Created: July 19, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto;

import com.odysseusinc.arachne.portal.model.PublishState;

import java.util.Date;

public class ShortPaperDTO {

    private Long id;
    private boolean favourite;
    private PublishState publishState;
    private Date publishedDate;
    private StudyMediumDTO study;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public boolean isFavourite() {

        return favourite;
    }

    public void setFavourite(boolean favourite) {

        this.favourite = favourite;
    }

    public PublishState getPublishState() {

        return publishState;
    }

    public void setPublishState(PublishState publishState) {

        this.publishState = publishState;
    }

    public StudyMediumDTO getStudy() {

        return study;
    }

    public void setStudy(StudyMediumDTO study) {

        this.study = study;
    }

    public Date getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(Date publishedDate) {
        this.publishedDate = publishedDate;
    }
}
