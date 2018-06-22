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
 * Created: April 19, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonHealthStatus;
import com.odysseusinc.arachne.commons.api.v1.dto.OrganizationDTO;
import java.util.List;

public class DataNodeDTO {
    private Long id;
    private String name;
    private String description;
    private Boolean virtual;
    private String atlasVerion;
    private CommonHealthStatus healthStatus;
    private String healthStatusTitle;
    private List<ShortUserDTO> dataOwners;
    private Boolean currentUserDataOwner;
    private Boolean published;
    private OrganizationDTO organization;

    public DataNodeDTO() {

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

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public Boolean getVirtual() {

        return virtual;
    }

    public void setVirtual(Boolean virtual) {

        this.virtual = virtual;
    }

    public String getAtlasVerion() {

        return atlasVerion;
    }

    public void setAtlasVerion(String atlasVerion) {

        this.atlasVerion = atlasVerion;
    }

    public CommonHealthStatus getHealthStatus() {

        return healthStatus;
    }

    public void setHealthStatus(CommonHealthStatus healthStatus) {

        this.healthStatus = healthStatus;
    }

    public String getHealthStatusTitle() {

        return healthStatusTitle;
    }

    public void setHealthStatusTitle(String healthStatusTitle) {

        this.healthStatusTitle = healthStatusTitle;
    }

    public List<ShortUserDTO> getDataOwners() {

        return dataOwners;
    }

    public void setDataOwners(List<ShortUserDTO> dataOwners) {

        this.dataOwners = dataOwners;
    }

    public Boolean getCurrentUserDataOwner() {

        return currentUserDataOwner;
    }

    public void setCurrentUserDataOwner(Boolean currentUserDataOwner) {

        this.currentUserDataOwner = currentUserDataOwner;
    }

    public Boolean getPublished() {
        return published;
    }

    public void setPublished(Boolean published) {
        this.published = published;
    }

    public OrganizationDTO getOrganization() {

        return organization;
    }

    public void setOrganization(OrganizationDTO organization) {

        this.organization = organization;
    }
}
