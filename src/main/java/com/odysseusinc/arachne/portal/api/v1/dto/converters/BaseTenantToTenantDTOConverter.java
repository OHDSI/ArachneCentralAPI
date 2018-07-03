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
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva
 * Created: February 15, 2018
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.portal.api.v1.dto.TenantBaseDTO;
import com.odysseusinc.arachne.portal.model.security.Tenant;

public abstract class BaseTenantToTenantDTOConverter<DTO extends TenantBaseDTO> extends BaseConversionServiceAwareConverter<Tenant, DTO> {

    @Override
    public DTO convert(Tenant source) {

        DTO tenantDTO = createResultObject();
        tenantDTO.setId(source.getId());
        tenantDTO.setName(source.getName());
        tenantDTO.setDefault(source.getDefault());
        return tenantDTO;
    }
}