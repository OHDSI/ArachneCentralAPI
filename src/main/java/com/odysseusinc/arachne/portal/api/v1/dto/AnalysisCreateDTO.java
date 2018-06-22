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
 * Created: May 17, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto;

import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotBlank;

public class AnalysisCreateDTO extends DTO {

    @NotNull
    private Long studyId;

    @NotBlank
    private String title;

    @NotNull
    private String typeId;

    private String description;

    private Boolean attachDefaultCodeFiles;

    public Long getStudyId() {

        return studyId;
    }

    public void setStudyId(Long studyId) {

        this.studyId = studyId;
    }

    public String getTitle() {

        return title;
    }

    public void setTitle(String title) {

        this.title = title;
    }

    public String getTypeId() {

        return typeId;
    }

    public void setTypeId(String typeId) {

        this.typeId = typeId;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public Boolean getAttachDefaultCodeFiles() {

        return attachDefaultCodeFiles;
    }

    public void setAttachDefaultCodeFiles(Boolean attachDefaultCodeFiles) {

        this.attachDefaultCodeFiles = attachDefaultCodeFiles;
    }
}
