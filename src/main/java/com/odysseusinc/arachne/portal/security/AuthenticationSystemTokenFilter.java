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
 * Created: May 29, 2017
 *
 */

package com.odysseusinc.arachne.portal.security;

import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.service.BaseDataNodeService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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

    private final BaseDataNodeService<DataNode> baseDataNodeService;

    public AuthenticationSystemTokenFilter(BaseDataNodeService baseDataNodeService) {

        this.baseDataNodeService = baseDataNodeService;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String token = request.getHeader(tokenHeader);
        if (token != null) {
            DataNode dataNode = baseDataNodeService.findByToken(token)
                    .orElseThrow(() -> new BadCredentialsException("dataNode not found"));
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                GrantedAuthority dataNodeAuthority = new SimpleGrantedAuthority("ROLE_" + Roles.ROLE_DATA_NODE);
                Collection<GrantedAuthority> authorityCollection = new ArrayList<>();
                authorityCollection.add(dataNodeAuthority);
                DataNodeAuthenticationToken authentication = new DataNodeAuthenticationToken(token,
                        dataNode, authorityCollection);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
