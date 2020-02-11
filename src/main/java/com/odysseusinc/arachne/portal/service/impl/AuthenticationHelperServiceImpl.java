package com.odysseusinc.arachne.portal.service.impl;

import com.odysseusinc.arachne.portal.service.AuthenticationHelperService;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.authenticator.exception.MethodNotSupportedAuthenticationException;
import org.ohdsi.authenticator.service.AuthService;
import org.ohdsi.authenticator.service.authentication.AuthServiceProvider;
import org.ohdsi.authenticator.service.jdbc.JdbcAuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationHelperServiceImpl implements AuthenticationHelperService {

    private List<String> nativeAuthMethodTypes = Collections.singletonList(JdbcAuthService.AUTH_METHOD_NAME);

    @Value("${security.method}")
    protected String authMethodName;

    private AuthServiceProvider authServiceProvider;

    public AuthenticationHelperServiceImpl(AuthServiceProvider authServiceProvider) {

        this.authServiceProvider = authServiceProvider;
    }

    @Override
    public String getCurrentMethodType() {

        AuthService authService = authServiceProvider.getByMethod(authMethodName)
                .orElseThrow(MethodNotSupportedAuthenticationException::new);
        return authService.getMethodType();
    }

    @Override
    public String getCurrentMethodName() {

        return StringUtils.lowerCase(authMethodName);
    }

    @Override
    public AuthService getCurrentAuthService() {

        return authServiceProvider.getByMethod(authMethodName)
                .orElseThrow(MethodNotSupportedAuthenticationException::new);

    }

    @Override
    public boolean isNative() {

        AuthService authService = authServiceProvider.getByMethod(authMethodName)
                .orElseThrow(MethodNotSupportedAuthenticationException::new);

        return nativeAuthMethodTypes.contains(authService.getMethodType());
    }

}
