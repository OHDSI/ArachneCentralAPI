package com.odysseusinc.arachne.portal.service;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonAuthenticationRequest;
import org.ohdsi.authenticator.model.UserInfo;

public interface AuthenticationService {

    String authenticateAndGetAuthToken(CommonAuthenticationRequest authenticationRequest);

    void authenticateBaseOnExternalUser(CommonAuthenticationRequest authenticationRequest, UserInfo userInfo);

    void authenticateBaseOnInternalUser(CommonAuthenticationRequest authenticationRequest, String username);

}
