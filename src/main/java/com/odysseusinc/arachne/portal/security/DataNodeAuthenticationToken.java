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
 * Created: May 29, 2017
 *
 */

package com.odysseusinc.arachne.portal.security;

import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.model.security.HasTenants;
import java.util.Collection;
import java.util.List;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class DataNodeAuthenticationToken extends AbstractAuthenticationToken implements HasTenants {

    private String token;
    private DataNode dataNode;
    private final List<Long> activeTenantIds;

    public DataNodeAuthenticationToken(String token, DataNode dataNode, Collection<? extends GrantedAuthority> authorities, List<Long> activeTenantIds) {

        super(authorities);
        this.token = token;
        this.dataNode = dataNode;
        this.activeTenantIds = activeTenantIds;
    }

    @Override
    public Object getCredentials() {

        return token;
    }

    @Override
    public Object getPrincipal() {

        return dataNode;
    }

    @Override
    public List<Long> getActiveTenantIds() {
        return activeTenantIds;
    }
}
