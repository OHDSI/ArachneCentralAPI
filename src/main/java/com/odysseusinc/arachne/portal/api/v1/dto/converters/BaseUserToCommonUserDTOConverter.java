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
 * Created: April 28, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonProfessionalTypeDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonUserDTO;
import com.odysseusinc.arachne.commons.utils.UserIdUtils;
import com.odysseusinc.arachne.portal.exception.NoDTOConverterException;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.ProfessionalType;
import org.springframework.stereotype.Component;

public abstract class BaseUserToCommonUserDTOConverter<DTO extends CommonUserDTO> extends BaseConversionServiceAwareConverter<IUser, DTO> {

    @Override
    public DTO convert(IUser user) {

        final DTO dto = createResultObject();
        dto.setId(UserIdUtils.idToUuid(user.getId()));
        dto.setPassword("");
        dto.setEmail(user.getEmail());
        dto.setFirstname(user.getFirstname());
        dto.setMiddlename(user.getMiddlename());
        dto.setLastname(user.getLastname());
        dto.setUsername(user.getUsername());
        dto.setEmailConfirmed(user.getEmailConfirmed());
        dto.setEnabled(user.getEnabled());
        dto.setCreated(user.getCreated());
        dto.setUpdated(user.getUpdated());
        if (user.getProfessionalType() != null) {
            if (conversionService.canConvert(ProfessionalType.class, CommonProfessionalTypeDTO.class)) {
                dto.setProfessionalType(conversionService.convert(user.getProfessionalType(), CommonProfessionalTypeDTO.class));
            } else {
                throw new NoDTOConverterException("cannot convert ProfessionalType to ProfessionaltypeDTO");
            }
        }
        return dto;
    }
}
