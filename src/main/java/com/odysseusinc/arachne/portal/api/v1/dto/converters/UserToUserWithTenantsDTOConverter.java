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

package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.portal.api.v1.dto.TenantBaseDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UserWithTenantsDTO;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.security.Tenant;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UserToUserWithTenantsDTOConverter extends BaseUserToCommonUserDTOConverter<UserWithTenantsDTO> {

    @Override
    public UserWithTenantsDTO convert(final IUser user) {

        final UserWithTenantsDTO dto = super.convert(user);

        final List<TenantBaseDTO> convertedTenantList = user.getTenants()
                .stream()
                .map(this::convertTenant)
                .collect(Collectors.toList());

        dto.setTenants(convertedTenantList);

        dto.setActiveTenant(convertTenant(user.getActiveTenant()));
        
        return dto;
    }
    
    private TenantBaseDTO convertTenant(final Tenant tenant) {
        
        return conversionService.convert(tenant, TenantBaseDTO.class);
    }

    @Override
    protected UserWithTenantsDTO createResultObject() {

        return new UserWithTenantsDTO();
    }
}
