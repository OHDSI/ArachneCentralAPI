/**
 *
 * Copyright 2017 Observational Health Data Sciences and Informatics
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

package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonUserRegistrationDTO;
import com.odysseusinc.arachne.portal.model.ProfessionalType;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.api.v1.dto.converters.BaseConversionServiceAwareConverter;
import org.springframework.stereotype.Component;

@Component
public class UserDTOToUserConverter extends BaseConversionServiceAwareConverter<CommonUserRegistrationDTO, User> {


    @Override
    public User convert(CommonUserRegistrationDTO dto) {

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setMiddlename(dto.getMiddlename());
        user.setFirstname(dto.getFirstname());
        user.setLastname(dto.getLastname());
        user.setUuid(dto.getUuid());
        ProfessionalType professionalType = new ProfessionalType();
        professionalType.setId(dto.getProfessionalTypeId());
        user.setProfessionalType(professionalType);
        return user;
    }
}
