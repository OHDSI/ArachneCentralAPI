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
 * Created: October 19, 2016
 *
 */

package com.odysseusinc.arachne.portal.model.factory;

import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.Role;
import com.odysseusinc.arachne.portal.model.security.ArachneUser;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class ArachneUserFactory {

    public static ArachneUser create(IUser user) {

        List<GrantedAuthority> authorities = new LinkedList<>();
        for (Role role : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
        }
        return new ArachneUser(
                user.getId(),
                Objects.isNull(user.getActiveTenant()) ? null : user.getActiveTenant().getId(),
                ObjectUtils.firstNonNull(user.getUsername(), user.getEmail()),
                user.getPassword(),
                user.getEmail(),
                user.getLastPasswordReset(),
                authorities
        );
    }

}
