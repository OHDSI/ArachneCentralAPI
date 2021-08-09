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
 * Created: September 16, 2021
 *
 */

package com.odysseusinc.arachne.portal.config;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.boot.autoconfigure.security.oauth2.client.ClientsConfiguredCondition;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Order(150)
@Configuration
@Conditional(ClientsConfiguredCondition.class)
@EnableWebSecurity
public class Oauth2SecurityConfig extends WebSecurityConfigurerAdapter {
    private final AuthenticationSuccessHandler authenticationSuccessHandler;

    public Oauth2SecurityConfig(AuthenticationSuccessHandler authenticationSuccessHandler) {
        this.authenticationSuccessHandler = authenticationSuccessHandler;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.oauth2Login(oauth -> oauth
                .successHandler(authenticationSuccessHandler)
                .userInfoEndpoint(endpoint ->
                        endpoint.oidcUserService(oidcUserService())
                )
        );
    }

    private OidcUserService oidcUserService() {
        Set<String> scopes = Stream.of(
                "openid", "profile", "email", "address"
        ).collect(Collectors.toSet());
        OidcUserService userService = new OidcUserService();
        userService.setAccessibleScopes(scopes);
        return userService;
    }

}
