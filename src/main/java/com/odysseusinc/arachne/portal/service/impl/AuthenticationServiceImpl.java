/*
 *
 * Copyright 2021 Odysseus Data Services, inc.
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
 * Authors: Yaroslav Molodkov, Alexandr Cumarav, Vitaliy Kulakov
 * Created: March 17, 2021
 *
 */

package com.odysseusinc.arachne.portal.service.impl;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonAuthenticationRequest;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.model.factory.ArachneUserFactory;
import com.odysseusinc.arachne.portal.model.security.ArachneUser;
import com.odysseusinc.arachne.portal.service.AuthenticationService;
import com.odysseusinc.arachne.portal.service.UserService;
import java.util.Collections;
import java.util.Optional;
import org.ohdsi.authenticator.model.UserInfo;
import org.ohdsi.authenticator.service.authentication.Authenticator;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final Authenticator authenticator;
    private final UserDetailsService userDetailsService;
    private final UserImportService userImportService;


    @Value("${security.method}")
    protected String authMethodName;
    @Autowired
    private UserService userService;

    public AuthenticationServiceImpl(Authenticator authenticator, UserDetailsService userDetailsService, UserImportService userImportService) {

        this.authenticator = authenticator;
        this.userDetailsService = userDetailsService;
        this.userImportService = userImportService;
    }

    @Transactional(rollbackFor = Exception.class, readOnly = false)
    public String authenticateAndGetAuthToken(CommonAuthenticationRequest authenticationRequest) {

        String username = authenticationRequest.getUsername();
        String password = authenticationRequest.getPassword();
        try {

            UserInfo userInfo = authenticator.authenticate(
                    authMethodName,
                    new UsernamePasswordCredentials(username, password)
            );
            authenticate(userInfo.getUsername(), password);
            return userInfo.getToken();
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            throw e;
        }
    }

    @Override
    public void authenticate(String username, String password) {
        authenticateBaseOnExternalUser(username, password);
        userImportService.synchronizeUserWithExternalSourceIfPossible(username);

        UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
        authentication.setDetails(userDetails);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void authenticateBaseOnExternalUser(String username, String password) {

        User user = new User();
        user.setUsername(username);
        user.setRoles(Collections.emptyList());

        ArachneUser arachneUser = ArachneUserFactory.create(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(arachneUser, password, arachneUser.getAuthorities());
        authentication.setDetails(arachneUser);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ArachneUser> findUser(String origin, String username) {
        return Optional.ofNullable(userService.getByUsername(origin, username)).map(ArachneUserFactory::create);
    }

}
