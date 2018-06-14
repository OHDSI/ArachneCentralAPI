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
 * Created: April 11, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.portal.api.v1.dto.TenantPersonalDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UserInfoDTO;
import com.odysseusinc.arachne.portal.model.IUser;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UserToUserInfoDTOConverter<T extends UserInfoDTO> extends BaseConversionServiceAwareConverter<IUser, T> {

    @Override
    public T convert(final IUser source) {

        if (source == null) {
            return null;
        }
        final T userInfoDTO = createResultObject();
        userInfoDTO.setId(source.getUuid());
        userInfoDTO.setEmail(source.getEmail());
        userInfoDTO.setUsername(source.getUsername());
        final boolean isAdmin = source.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
        userInfoDTO.setIsAdmin(isAdmin);
        userInfoDTO.setFirstname(source.getFirstname());
        userInfoDTO.setMiddlename(source.getMiddlename());
        userInfoDTO.setLastname(source.getLastname());

        List<TenantPersonalDTO> tenantDTOs = source.getTenants()
                .stream()
                .map(t -> {
                    TenantPersonalDTO dto = conversionService.convert(t, TenantPersonalDTO.class);
                    dto.setActive(Objects.equals(source.getActiveTenant(), t));
                    return dto;
                })
                .collect(Collectors.toList());
        userInfoDTO.setTenants(tenantDTOs);

        return userInfoDTO;
    }

    @Override
    protected T createResultObject() {

        return (T)new UserInfoDTO();
    }
}
