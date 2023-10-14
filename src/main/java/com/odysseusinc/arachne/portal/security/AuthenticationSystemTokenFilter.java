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
import com.odysseusinc.arachne.portal.service.BaseDataNodeService;
import com.odysseusinc.arachne.portal.service.BaseDataSourceService;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

public class AuthenticationSystemTokenFilter extends GenericFilterBean {

    @Value("${arachne.systemToken.header}")
    private String tokenHeader;

    private final BaseDataNodeService<?> baseDataNodeService;
    private final BaseDataSourceService<?> dataSourceService;

    public AuthenticationSystemTokenFilter(BaseDataNodeService<?> baseDataNodeService, BaseDataSourceService<?> dataSourceService) {
        this.baseDataNodeService = baseDataNodeService;
        this.dataSourceService = dataSourceService;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String token = request.getHeader(tokenHeader);
        if (token != null) {
            DataNode dataNode = baseDataNodeService.findByToken(token)
                    .orElseThrow(() -> new BadCredentialsException("dataNode not found"));
            // TODO The above call to com.odysseusinc.arachne.portal.service.BaseDataNodeService.findByToken
            //  doesn't load datasources, because there are no tenants in security context yet!
            //  And even when it does so (once we switch Datasources to HasTenants), there is no transaction
            //  here to access lazily loaded many-to-many relation.
            List<Long> tenantIds = dataSourceService.getTenantsForDatanode(dataNode.getId());
            GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + Roles.ROLE_DATA_NODE);
            Collection<GrantedAuthority> authorities = Arrays.asList(authority);
            DataNodeAuthenticationToken authentication = new DataNodeAuthenticationToken(
                    token, dataNode, authorities, tenantIds
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
