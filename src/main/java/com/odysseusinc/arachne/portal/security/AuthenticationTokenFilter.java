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

package com.odysseusinc.arachne.portal.security;

import com.odysseusinc.arachne.portal.config.tenancy.TenantContext;
import com.odysseusinc.arachne.portal.model.security.ArachneUser;
import java.util.Objects;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.ohdsi.authenticator.filter.JWTAuthenticationFilter;
import org.ohdsi.authenticator.service.authentication.Authenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

public class AuthenticationTokenFilter extends JWTAuthenticationFilter {

    public static final String USER_REQUEST_HEADER = "Arachne-User-Request";
    @Value("${arachne.token.header}")
    private String tokenHeader;

    @Autowired
    private UserDetailsService userDetailsService;

    public AuthenticationTokenFilter(Authenticator authenticator) {

        super(authenticator);
    }

    @Override
    protected String getToken(HttpServletRequest httpRequest) {

        String authToken = httpRequest.getHeader(tokenHeader);
        if (Objects.isNull(authToken) && httpRequest.getCookies() != null) {
            for (Cookie cookie : httpRequest.getCookies()) {
                if (cookie.getName().equalsIgnoreCase(tokenHeader)) {
                    authToken = cookie.getValue();
                }
            }
        }
        return authToken;
    }

    @Override
    protected void onSuccessAuthentication(HttpServletRequest httpRequest, UserDetails userDetails, AbstractAuthenticationToken authentication) {

        String requested = httpRequest.getHeader(USER_REQUEST_HEADER);
        String username = userDetails.getUsername();
        if (requested != null && username != null && !Objects.equals(username, requested)) {
            throw new BadCredentialsException("forced logout");
        }
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));
        TenantContext.setCurrentTenant(((ArachneUser) userDetails).getActiveTenantId());
    }

    @Override
    protected UserDetails getUserDetails(String username) {

        return this.userDetailsService.loadUserByUsername(username);
    }
}
