package com.odysseusinc.arachne.portal.service.impl;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonAuthenticationRequest;
import com.odysseusinc.arachne.portal.api.v1.dto.converters.UserInfoToUserConverter;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.model.factory.ArachneUserFactory;
import com.odysseusinc.arachne.portal.model.security.ArachneUser;
import com.odysseusinc.arachne.portal.service.AuthenticationService;
import java.util.Collections;
import org.ohdsi.authenticator.model.UserInfo;
import org.ohdsi.authenticator.service.authentication.Authenticator;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class, readOnly = false)
public class AuthenticationServiceImpl implements AuthenticationService {

    protected Authenticator authenticator;
    private UserInfoToUserConverter userConverter;
    private UserDetailsService userDetailsService;

    @Value("${security.method}")
    private String authMethod;


    public AuthenticationServiceImpl(Authenticator authenticator, UserInfoToUserConverter userConverter, UserDetailsService userDetailsService) {

        this.authenticator = authenticator;
        this.userConverter = userConverter;
        this.userDetailsService = userDetailsService;
    }

    @Transactional
    public String authenticateAndGetAuthToken(CommonAuthenticationRequest authenticationRequest) {

        String username = authenticationRequest.getUsername();
        String password = authenticationRequest.getPassword();
        try {
            UserInfo userInfo = authenticator.authenticate(
                    authMethod,
                    new UsernamePasswordCredentials(username, password)
            );
            authenticateBaseOnExternalUser(authenticationRequest, userInfo);
            synchronizeExternalAndInternalUserAccounts(username);
            authenticateBaseOnInternalUser(authenticationRequest, username);
            if (userInfo == null || userInfo.getAdditionalInfo() == null || userInfo.getToken() == null) {
                throw new AuthenticationServiceException("Cannot refresh token user info is either null or does not contain token");
            }
            return userInfo.getToken();
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            throw e;
        }
    }

    public void authenticateBaseOnExternalUser(CommonAuthenticationRequest authenticationRequest, UserInfo userInfo) {

        User user = new User();
        user.setUsername(userInfo.getUsername());
        user.setRoles(Collections.emptyList());

        ArachneUser arachneUser = ArachneUserFactory.create(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(arachneUser, authenticationRequest.getPassword(), ((UserDetails) arachneUser).getAuthorities());
        authentication.setDetails(arachneUser);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public void authenticateBaseOnInternalUser(CommonAuthenticationRequest authenticationRequest, String username) {

        UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, authenticationRequest.getPassword(), userDetails.getAuthorities());
        authentication.setDetails(userDetails);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    protected void synchronizeExternalAndInternalUserAccounts(String username) {

    }

}
