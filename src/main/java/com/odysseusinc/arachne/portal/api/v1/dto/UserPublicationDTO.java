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
 * Created: January 17, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto;

import java.util.Date;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

/**
 * Created by AKrutov on 11.01.2017.
 */
public class UserPublicationDTO {

    private Long id;

    @Length(max = 1024, message = "The field must be less than 1024 characters")
    private String description;
    @NotNull
    @Length(max = 255, message = "The field must be less than 255 characters")
    private String title;
    @NotNull
    @Length(max = 255, message = "The field must be less than 255 characters")
    private String publisher;
    @NotNull
    @Length(max = 255, message = "The field must be less than 255 characters")
    private String url;
    private Date date;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public String getTitle() {

        return title;
    }

    public void setTitle(String title) {

        this.title = title;
    }

    public String getPublisher() {

        return publisher;
    }

    public void setPublisher(String publisher) {

        this.publisher = publisher;
    }

    public Date getDate() {

        return date;
    }

    public void setDate(Date date) {

        this.date = date;
    }

    public String getUrl() {

        return url;
    }

    public void setUrl(String url) {

        this.url = url;
    }
}
