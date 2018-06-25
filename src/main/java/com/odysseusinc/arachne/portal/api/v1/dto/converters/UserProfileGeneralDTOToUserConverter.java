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

import com.odysseusinc.arachne.portal.api.v1.dto.StateProvinceDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UserProfileGeneralDTO;
import com.odysseusinc.arachne.portal.model.Country;
import com.odysseusinc.arachne.portal.model.ProfessionalType;
import com.odysseusinc.arachne.portal.model.Skill;
import com.odysseusinc.arachne.portal.model.StateProvince;
import com.odysseusinc.arachne.portal.model.User;
import java.util.HashSet;
import org.springframework.stereotype.Component;


@Component
public class UserProfileGeneralDTOToUserConverter extends BaseConversionServiceAwareConverter<UserProfileGeneralDTO, User> {


    @Override
    public User convert(UserProfileGeneralDTO dto) {

        User user = new User();
        user.setMiddlename(dto.getMiddlename());
        user.setFirstname(dto.getFirstname());
        user.setLastname(dto.getLastname());
        user.setProfessionalType(conversionService.convert(dto.getProfessionalType(), ProfessionalType.class));
        HashSet<Skill> skills = new HashSet<>();
        user.setSkills(skills);
        user.setAffiliation(dto.getAffiliation());

        user.setPhone(dto.getPhone());
        user.setMobile(dto.getMobile());
        user.setAddress1(dto.getAddress1());
        user.setAddress2(dto.getAddress2());
        user.setCity(dto.getCity());
        user.setStateProvince(conversionService.convert(new StateProvinceDTO(dto.getStateProvinceId()), StateProvince.class));
        user.setZipCode(dto.getZipCode());
        user.setCountry(conversionService.convert(dto.getCountry(), Country.class));
        user.setContactEmail(dto.getContactEmail());

        user.setPersonalSummary(dto.getPersonalSummary());
        return user;
    }
}
