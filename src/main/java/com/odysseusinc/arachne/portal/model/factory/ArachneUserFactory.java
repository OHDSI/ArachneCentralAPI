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
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva, Mikhail Mironov
 * Created: October 19, 2016
 *
 */

package com.odysseusinc.arachne.portal.model.factory;

import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.security.ArachneUser;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class ArachneUserFactory {

    public static ArachneUser create(IUser user) {

        List<GrantedAuthority> authorities = Optional.ofNullable(user.getRoles()).map(roles ->
                roles.stream().map(role -> new SimpleGrantedAuthority(role.getName()))
        ).orElseGet(Stream::empty).collect(Collectors.toList());
        List<Long> tenantIds = Optional.ofNullable(user.getActiveTenant()).map(tenant ->
                Collections.singletonList(tenant.getId())
        ).orElseGet(Collections::emptyList);
        return new ArachneUser(
                user.getId(),
                tenantIds,
                ObjectUtils.firstNonNull(user.getUsername(), user.getEmail()),
                user.getPassword(),
                user.getEmail(),
                user.getLastPasswordReset(),
                authorities
        );
    }

}
