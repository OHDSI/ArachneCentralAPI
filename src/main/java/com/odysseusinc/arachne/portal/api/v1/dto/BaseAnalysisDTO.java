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
 * Created: May 18, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto;

import java.util.Date;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

public class BaseAnalysisDTO extends ShortBaseAnalysisDTO {

    @NotBlank
    @Length.List({
            @Length(min = 5, message = "The field must be at least 5 characters"),
            @Length(max = 255, message = "The field must be less than 255 characters")
    })
    private String title;

    private String description;

    private Date updated;

    @NotNull
    private StudyShortDTO study;

    private PermissionsDTO permissions;

    private UserInfoDTO author;

    public BaseAnalysisDTO() {

    }

    public BaseAnalysisDTO(BaseAnalysisDTO other) {

        super(other);
        this.title = other.title;
        this.description = other.description;
        this.updated = other.updated;
        this.study = other.study;
        this.permissions = other.permissions;
        this.author = other.author;
    }

    public BaseAnalysisDTO(ShortBaseAnalysisDTO other) {

        super(other);
    }

    public String getTitle() {

        return title;
    }

    public void setTitle(String title) {

        this.title = title;
    }

    public StudyShortDTO getStudy() {

        return study;
    }

    public void setStudy(StudyShortDTO study) {

        this.study = study;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public Date getUpdated() {

        return updated;
    }

    public void setUpdated(Date updated) {

        this.updated = updated;
    }

    public PermissionsDTO getPermissions() {

        return permissions;
    }

    public void setPermissions(PermissionsDTO permissions) {

        this.permissions = permissions;
    }

    public UserInfoDTO getAuthor() {

        return author;
    }

    public void setAuthor(UserInfoDTO author) {

        this.author = author;
    }

}
