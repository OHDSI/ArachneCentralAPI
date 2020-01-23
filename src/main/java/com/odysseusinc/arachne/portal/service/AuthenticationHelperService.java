package com.odysseusinc.arachne.portal.service;

import org.ohdsi.authenticator.service.AuthService;

public interface AuthenticationHelperService {

    String getCurrentMethodType();

    String getCurrentMethodName();

    AuthService getCurrentAuthService();

    boolean isNative();

}
