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

import com.odysseusinc.arachne.portal.service.AuthenticationService;
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
import org.ohdsi.authenticator.service.AuthService;
import org.ohdsi.authenticator.service.authentication.AuthServiceProvider;
import org.ohdsi.authenticator.service.authentication.TokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.web.filter.GenericFilterBean;

public class AuthenticationTokenFilter extends GenericFilterBean {

    Logger log = LoggerFactory.getLogger(AuthenticationTokenFilter.class);

    public static final String USER_REQUEST_HEADER = "Arachne-User-Request";
    @Value("${arachne.token.header}")
    private String tokenHeader;

    @Autowired
    private TokenProvider tokenProvider;
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private AuthServiceProvider authServiceProvider;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        try {

            SecurityContext context = SecurityContextHolder.getContext();
            Authentication authentication = context.getAuthentication();
            if (authentication == null) {
                getAuthToken(httpRequest).ifPresent(authToken -> {
                    String username = tokenProvider.resolveValue(authToken, "sub", String.class);

                    if (username == null) {
                        return;
                    }
                    String requestedUsername = httpRequest.getHeader(USER_REQUEST_HEADER);

                    if (requestedUsername != null && !Objects.equals(username, requestedUsername)) {
                        throw new BadCredentialsException("forced logout");
                    }
                    // TODO Extract constant for "method"?
                    String method = tokenProvider.resolveValue(authToken, "method", String.class);
                    String origin = authServiceProvider.getByMethod(method).map(AuthService::getMethodType).orElse(method);
                    authenticationService.findUser(origin, username).ifPresent(user ->
                            context.setAuthentication(new JWTAuthenticationToken(authToken, user, new WebAuthenticationDetails(httpRequest)))
                    );
                });

            } else if (authentication instanceof DataNodeAuthenticationToken) {
                // Do nothing to ensure datanode auth is passed untouched
            } else {
                String username = authentication.getName();
                authenticationService.findUser("JDBC", username).ifPresent(user -> {
                    // For users authenticated differently, we ensure that list of authorities set during authentication is retained
                    // This ensures tests using such SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor.roles remail operational
                    user.setAuthorities(authentication.getAuthorities());
                    context.setAuthentication(new JWTAuthenticationToken(username, user, new WebAuthenticationDetails(httpRequest)));
                });
            }
        } catch (org.ohdsi.authenticator.exception.AuthenticationException | AuthenticationException ex) {
            String method = httpRequest.getMethod();
            if (!HttpMethod.OPTIONS.matches(method)) {
                log.debug("Authentication failed", ex);
            }
        }
        chain.doFilter(request, response);
    }

    private Optional<String> getAuthToken(HttpServletRequest httpRequest) {
        return Optional.ofNullable(httpRequest).flatMap(request -> {
                Optional<String> fromHeader = Optional.ofNullable(request.getHeader(tokenHeader));
                return fromHeader.isPresent() ? fromHeader : getAuthTokenFromCookies(httpRequest, tokenHeader);
        });
    }

    public static Optional<String> getAuthTokenFromCookies(HttpServletRequest httpRequest, String tokenHeader) {
        return Optional.of(httpRequest.getCookies()).flatMap(cookies ->
                Arrays.stream(cookies)
                .filter(cookie -> StringUtils.isNotEmpty(cookie.getName()))
                .filter(cookie -> cookie.getName().equalsIgnoreCase(tokenHeader))
                .map(Cookie::getValue)
                .findAny()
        );
    }

}
