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

package com.odysseusinc.arachne.portal.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult;
import com.odysseusinc.arachne.portal.config.tenancy.TenantContext;
import com.odysseusinc.arachne.portal.model.security.ArachneUser;
import java.io.IOException;
import java.util.Objects;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

public class AuthenticationTokenFilter extends UsernamePasswordAuthenticationFilter {

    public static final String USER_REQUEST_HEADER = "Arachne-User-Request";
    @Value("${arachne.token.header}")
    private String tokenHeader;

    @Autowired
    private TokenUtils tokenUtils;


    @Autowired
    private UserDetailsService userDetailsService;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException, AuthenticationException {

        try {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String authToken = httpRequest.getHeader(tokenHeader);
            if (authToken == null && httpRequest.getCookies() != null) {
                for (Cookie cookie : httpRequest.getCookies()) {
                    if (cookie.getName().equalsIgnoreCase(tokenHeader)) {
                        authToken = cookie.getValue();
                    }
                }
            }
            if (authToken != null) {
                String username = this.tokenUtils.getUsernameFromToken(authToken);
                String requested = httpRequest.getHeader(USER_REQUEST_HEADER);
                if (requested != null && username != null && !Objects.equals(username, requested)){
                    throw new BadCredentialsException("forced logout");
                }
                if (tokenUtils.isExpired(authToken)) {
                    if (((HttpServletRequest) request).getRequestURI().startsWith("/api")) {
                        if (username != null) {
                            throw new BadCredentialsException("token expired");
                        }
                    }
                }

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                    if (this.tokenUtils.validateToken(authToken, userDetails)) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        TenantContext.setCurrentTenant(((ArachneUser) userDetails).getActiveTenantId());
                    }
                }
            }
            chain.doFilter(request, response);
        } catch (AuthenticationException ex) {
            logger.debug(ex.getMessage(), ex);
            ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            JsonResult<Boolean> result = new JsonResult<>(JsonResult.ErrorCode.UNAUTHORIZED);
            result.setResult(Boolean.FALSE);

            response.getOutputStream().write(objectMapper.writeValueAsString(result).getBytes());
            response.setContentType("application/json");
        }
    }

}
