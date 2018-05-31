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

package com.odysseusinc.arachne.portal.api.v1.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonBaseDataSourceDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonHealthStatus;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataSourceDTO extends CommonBaseDataSourceDTO implements IDataSourceDTO {

    private String status;
    private DataNodeDTO dataNode;
    private CommonHealthStatus healthStatus;
    private String healthStatusTitle;
    private Date deleted;
    private PermissionsDTO permissions;
    private boolean canBeRecreated = Boolean.TRUE;

    public String getStatus() {

        return status;
    }

    public void setStatus(String status) {

        this.status = status;
    }

    public DataNodeDTO getDataNode() {

        return dataNode;
    }

    public void setDataNode(DataNodeDTO dataNode) {

        this.dataNode = dataNode;
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

    public Date getDeleted() {

        return deleted;
    }

    public void setDeleted(Date deleted) {

        this.deleted = deleted;
    }

    public PermissionsDTO getPermissions() {

        return permissions;
    }

    public void setPermissions(PermissionsDTO permissions) {

        this.permissions = permissions;
    }

    public boolean isCanBeRecreated() {

        return canBeRecreated;
    }

    public void setCanBeRecreated(final boolean canBeRecreated) {

        this.canBeRecreated = canBeRecreated;
    }
}
