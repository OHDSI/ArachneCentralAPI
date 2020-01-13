package com.odysseusinc.arachne.portal.service.impl;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonAuthenticationRequest;
import com.odysseusinc.arachne.portal.api.v1.dto.converters.UserInfoToUserConverter;
import com.odysseusinc.arachne.portal.service.AuthenticationService;
import org.ohdsi.authenticator.model.UserInfo;
import org.ohdsi.authenticator.service.authentication.Authenticator;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.transaction.annotation.Transactional;

public class BaseAuthenticationServiceImpl implements AuthenticationService {

    protected Authenticator authenticator;
    private UserInfoToUserConverter userConverter;
    private UserDetailsService userDetailsService;

    @Value("${security.method}")
    private String authMethod;


    public BaseAuthenticationServiceImpl(Authenticator authenticator, UserInfoToUserConverter userConverter, UserDetailsService userDetailsService) {

        this.authenticator = authenticator;
        this.userConverter = userConverter;
        this.userDetailsService = userDetailsService;
    }

    @Transactional(rollbackFor = Exception.class, readOnly = false)
    public String authenticateAndGetAuthToken(CommonAuthenticationRequest authenticationRequest) {

        String username = authenticationRequest.getUsername();
        String password = authenticationRequest.getPassword();
        try {

            UserInfo userInfo = authenticator.authenticate(
                    authMethod,
                    new UsernamePasswordCredentials(username, password)
            );
            authenticate(userInfo.getUsername(), password);
            return userInfo.getToken();
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            throw e;
        }
    }

    public void authenticate(String username, String password) {
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
        authentication.setDetails(userDetails);
        SecurityContextHolder.getContext().setAuthentication(authentication);

    }

}
