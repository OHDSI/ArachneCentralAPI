package com.odysseusinc.arachne.portal.service.impl;

import org.ohdsi.authenticator.service.authentication.Authenticator;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceImpl extends BaseAuthenticationServiceImpl {

    public AuthenticationServiceImpl(Authenticator authenticator, UserDetailsService userDetailsService) {

        super(authenticator, userDetailsService);
    }
}
