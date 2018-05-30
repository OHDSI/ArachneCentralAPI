/*
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
 * Authors: Anton Gackovka
 * Created: May 24, 2018
 */

package com.odysseusinc.arachne.portal.api.v1.dto;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonUserDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.TenantBaseDTO;
import java.util.ArrayList;
import java.util.List;

public class UserWithTenantsDTO extends CommonUserDTO {
    
    private TenantBaseDTO activeTenant;
    private List<TenantBaseDTO> tenants = new ArrayList<>();

    public List<TenantBaseDTO> getTenants() {

        return tenants;
    }

    public void setTenants(final List<TenantBaseDTO> tenants) {

        this.tenants = tenants;
    }

    public TenantBaseDTO getActiveTenant() {

        return activeTenant;
    }

    public void setActiveTenant(final TenantBaseDTO activeTenant) {

        this.activeTenant = activeTenant;
    }
}
