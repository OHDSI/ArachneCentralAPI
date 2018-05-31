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
 * Created: January 16, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonProfessionalTypeDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.CountryDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UserProfileGeneralDTO;
import com.odysseusinc.arachne.portal.exception.NoDTOConverterException;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.ProfessionalType;
import org.springframework.stereotype.Component;


@Component
@SuppressWarnings("unused")
public class UserToUserProfileGeneralDTOConverter extends BaseConversionServiceAwareConverter<IUser, UserProfileGeneralDTO> {


    @Override
    public UserProfileGeneralDTO convert(IUser user) {

        UserProfileGeneralDTO dto = new UserProfileGeneralDTO();
        dto.setFirstname(user.getFirstname());
        dto.setMiddlename(user.getMiddlename());
        dto.setLastname(user.getLastname());
        dto.setPersonalSummary(user.getPersonalSummary());
        if (user.getProfessionalType() != null) {
            if (conversionService.canConvert(ProfessionalType.class, CommonProfessionalTypeDTO.class)) {
                dto.setProfessionalType(conversionService.convert(user.getProfessionalType(), CommonProfessionalTypeDTO.class));
            } else {
                throw new NoDTOConverterException("cannot convert ProfessionalType to ProfessionaltypeDTO");
            }
        }
        dto.setPhone(user.getPhone());
        dto.setMobile(user.getMobile());
        dto.setAddress1(user.getAddress1());
        dto.setAddress2(user.getAddress2());
        dto.setCity(user.getCity());
        if (user.getStateProvince() != null) {
            dto.setStateProvinceId(user.getStateProvince().getId());
        }
        dto.setZipCode(user.getZipCode());
        if (user.getCountry() != null) {
            dto.setCountry(conversionService.convert(user.getCountry(), CountryDTO.class));
        }
        dto.setAffiliation(user.getAffiliation());
        dto.setContactEmail(user.getContactEmail());
        return dto;
    }


}
