package com.odysseusinc.arachne.portal.service;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonAuthenticationRequest;
import com.odysseusinc.arachne.portal.model.security.ArachneUser;
import java.util.Optional;

public interface AuthenticationService {

    String authenticateAndGetAuthToken(CommonAuthenticationRequest authenticationRequest);

    void authenticate(String username, String password);

    Optional<ArachneUser> findUser(String origin, String username);
}
