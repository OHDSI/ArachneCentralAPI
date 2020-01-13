package com.odysseusinc.arachne.portal.service.impl;

import com.odysseusinc.arachne.portal.api.v1.dto.converters.UserInfoToUserConverter;
import org.ohdsi.authenticator.service.authentication.Authenticator;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceImpl extends BaseAuthenticationServiceImpl {

    public AuthenticationServiceImpl(Authenticator authenticator, UserInfoToUserConverter userConverter, UserDetailsService userDetailsService) {

        super(authenticator, userConverter, userDetailsService);
    }
}
