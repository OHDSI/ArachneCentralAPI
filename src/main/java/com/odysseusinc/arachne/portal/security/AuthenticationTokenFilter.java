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
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.authenticator.exception.AuthenticationException;
import org.ohdsi.authenticator.service.authentication.Authenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.GenericFilterBean;

public class AuthenticationTokenFilter extends GenericFilterBean {

    Logger log = LoggerFactory.getLogger(AuthenticationTokenFilter.class);

    public static final String USER_REQUEST_HEADER = "Arachne-User-Request";
    @Value("${arachne.token.header}")
    private String tokenHeader;

    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private Authenticator authenticator;


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException, AuthenticationException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        this.getAuthToken(httpRequest)
                .ifPresent(authToken -> {
                    try {
                        String username = authenticator.resolveUsername(authToken);
                        if (username == null) {
                            return;
                        }
                        String requestedUsername = httpRequest.getHeader(USER_REQUEST_HEADER);

                        if (requestedUsername != null && !Objects.equals(username, requestedUsername)) {
                            throw new BadCredentialsException("forced logout");
                        }
                        if (SecurityContextHolder.getContext().getAuthentication() == null) {
                            this.putUserInSecurityContext(httpRequest, username);
                        }
                    } catch (AuthenticationException | org.springframework.security.core.AuthenticationException ex) {
                        String method = httpRequest.getMethod();
                        if (!HttpMethod.OPTIONS.matches(method)) {
                            if (log.isDebugEnabled()) {
                                log.debug("Authentication failed", ex);
                            } else {
                                log.error("Authentication failed: {}, requested: {} {}", ex.getMessage(), method, httpRequest.getRequestURI());
                            }
                        }
                    }
                });
        chain.doFilter(request, response);
    }

    private void putUserInSecurityContext(HttpServletRequest httpRequest, String username) {

        UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        TenantContext.setCurrentTenant(((ArachneUser) userDetails).getActiveTenantId());
    }

    private Optional<String> getAuthToken(HttpServletRequest httpRequest) {

        if (httpRequest == null) {
            return Optional.empty();
        }

        String authToken = httpRequest.getHeader(tokenHeader);
        if (authToken != null) {
            return Optional.of(authToken);
        }
        if (httpRequest.getCookies() != null) {
            return Arrays.stream(httpRequest.getCookies())
                    .filter(cookie -> StringUtils.isNotEmpty(cookie.getName()))
                    .filter(cookie -> cookie.getName().equalsIgnoreCase(tokenHeader))
                    .map(Cookie::getValue)
                    .findAny();
        }
        return Optional.empty();
    }

}
