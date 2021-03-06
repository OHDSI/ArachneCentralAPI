/*
 *
 * Copyright 2018 Odysseus Data Services, inc.
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
 * Created: July 03, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

public class DataReferenceDTO {

    @NotNull
    private Long dataNodeId;
    @NotEmpty
    private String entityGuid;


    public DataReferenceDTO() {

    }

    public DataReferenceDTO(Long datanodeId, String entityGuid) {

        this.dataNodeId = datanodeId;
        this.entityGuid = entityGuid;
    }

    public Long getDataNodeId() {

        return dataNodeId;
    }

    public void setDataNodeId(Long dataNodeId) {

        this.dataNodeId = dataNodeId;
    }

    public String getEntityGuid() {

        return entityGuid;
    }

    public void setEntityGuid(String entityGuid) {

        this.entityGuid = entityGuid;
    }
}
