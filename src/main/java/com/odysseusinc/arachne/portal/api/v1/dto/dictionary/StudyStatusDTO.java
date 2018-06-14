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
 * Created: January 13, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.dictionary;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.odysseusinc.arachne.portal.api.v1.dto.DTO;
import javax.validation.constraints.NotNull;

public class StudyStatusDTO extends DTO {

    @NotNull
    private Long id;
    private String name;
    private String[] availableActions;

    @JsonCreator
    public StudyStatusDTO(
            @JsonProperty(value = "id") @NotNull Long studyStatusId,
            @JsonProperty("name") @NotNull String studyStatusName) {

        id = studyStatusId;
        name = studyStatusName;
    }

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String[] getAvailableActions() {

        return availableActions;
    }

    public void setAvailableActions(String[] availableActions) {

        this.availableActions = availableActions;
    }
}
