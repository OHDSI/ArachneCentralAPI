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
 * Created: November 01, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto;


import org.hibernate.validator.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

public class UploadFileDTO {
    private MultipartFile file;
    @NotBlank
    private String label;
    private Boolean isExecutable;
    private String link;

    public MultipartFile getFile() {

        return file;
    }

    public void setFile(MultipartFile file) {

        this.file = file;
    }

    public String getLabel() {

        return label;
    }

    public void setLabel(String label) {

        this.label = label;
    }

    public Boolean getExecutable() {

        return isExecutable;
    }

    public void setExecutable(Boolean executable) {

        isExecutable = executable;
    }

    public String getLink() {

        return link;
    }

    public void setLink(String link) {

        this.link = link;
    }
}
