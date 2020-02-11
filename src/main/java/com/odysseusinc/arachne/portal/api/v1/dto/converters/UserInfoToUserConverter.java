/*
 *
 * Copyright 2019 Odysseus Data Services, inc.
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
 * Authors: Pavel Grafkin
 * Created: July 15, 2019
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.portal.model.Country;
import com.odysseusinc.arachne.portal.model.Role;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.repository.CountryRepository;
import com.odysseusinc.arachne.portal.service.RoleService;
import edu.emory.mathcs.backport.java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;

@Component
public class UserInfoToUserConverter extends BaseConversionServiceAwareConverter<org.ohdsi.authenticator.model.User, User> {

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    public UserInfoToUserConverter(GenericConversionService conversionService) {

        conversionService.addConverter(this);
    }

    @Override
    public User convert(org.ohdsi.authenticator.model.User authUser) {

        User user = new User();

        user.setUsername(authUser.getUsername());
        user.setEmail(authUser.getEmail());
        user.setFirstname(authUser.getFirstName());
        user.setMiddlename(authUser.getMiddleName());
        user.setLastname(authUser.getLastName());
        user.setOrganization(authUser.getOrganization());
        user.setDepartment(authUser.getDepartment());
        user.setAffiliation(authUser.getAffiliation());
        user.setPersonalSummary(authUser.getPersonalSummary());
        user.setPhone(authUser.getPhone());
        user.setMobile(authUser.getMobile());
        user.setAddress1(authUser.getAddress1());
        user.setCity(authUser.getCity());
        user.setZipCode(authUser.getZipCode());
        user.setCountry(getCountryByCode(authUser.getCountryCode()));
        user.setRoles(getRoles(authUser));

        return user;
    }

    private List<Role> getRoles(org.ohdsi.authenticator.model.User authUser) {

        if (CollectionUtils.isEmpty(authUser.getRoles())) {
            return Collections.emptyList();
        }
        return authUser.getRoles().stream()
                .flatMap(r -> roleService.findByName(r).stream())
                .collect(Collectors.toList());
    }

    private Country getCountryByCode(String countryCode) {

        if (StringUtils.isEmpty(countryCode)) {
            return null;
        }

        List<Country> countries = countryRepository.findByCode(countryCode);
        if (CollectionUtils.isEmpty(countries)) {
            return null;
        }
        return countries.get(0);
    }
}
