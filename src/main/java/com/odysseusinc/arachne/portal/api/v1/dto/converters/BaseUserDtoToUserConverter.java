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
 * Authors: Sergey Maletsky
 * Created: May 31, 2018
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonAddressDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonUserRegistrationDTO;
import com.odysseusinc.arachne.portal.model.Country;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.ProfessionalType;
import com.odysseusinc.arachne.portal.model.StateProvince;
import java.util.Objects;

public abstract class BaseUserDtoToUserConverter<CU extends CommonUserRegistrationDTO, U extends IUser> extends BaseConversionServiceAwareConverter<CU, U> {

    @Override
    public U convert(CU dto) {

        U user = newUser();
        //user.setEmail(dto.getEmail());
        user.setEmail(dto.getEmail().toLowerCase());
        user.setPassword(dto.getPassword());
        user.setMiddlename(dto.getMiddlename());
        user.setFirstname(dto.getFirstname());
        user.setLastname(dto.getLastname());
        user.setOrganization(dto.getOrganization());
        user.setDepartment(dto.getDepartment());
        ProfessionalType professionalType = new ProfessionalType();
        professionalType.setId(dto.getProfessionalTypeId());
        user.setProfessionalType(professionalType);
        CommonAddressDTO address = dto.getAddress();
        if (Objects.nonNull(address)) {
            user.setZipCode(address.getZipCode());
            user.setCity(address.getCity());
            user.setAddress1(address.getAddress1());
            user.setAddress2(address.getAddress2());
            user.setPhone(address.getPhone());
            user.setMobile(address.getMobile());
            user.setContactEmail(address.getContactEmail());
            if (Objects.nonNull(address.getCountry())) {
                user.setCountry(conversionService.convert(address.getCountry(), Country.class));
            }
            if (Objects.nonNull(address.getStateProvince())) {
                user.setStateProvince(conversionService.convert(address.getStateProvince(), StateProvince.class));
            }
        }
        return user;
    }

    protected abstract U newUser();
}
