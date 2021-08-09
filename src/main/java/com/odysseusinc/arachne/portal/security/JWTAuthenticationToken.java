package com.odysseusinc.arachne.portal.security;

import com.odysseusinc.arachne.portal.model.security.ArachneUser;
import java.security.Principal;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.util.Assert;

public class JWTAuthenticationToken extends AbstractAuthenticationToken {
    private final String token;
    private final ArachneUser principal;

    public JWTAuthenticationToken(String token, ArachneUser principal, WebAuthenticationDetails details) {
        super(principal.getAuthorities());
        Assert.notNull(token, "token must not be null");
        this.token = token;
        this.principal = principal;
        setDetails(details);
        setAuthenticated(true);
    }

    @NotNull
    public String getToken() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public ArachneUser getPrincipal() {
        return principal;
    }

    public static Optional<JWTAuthenticationToken> ifInstance(Principal principal) {
        return (principal instanceof JWTAuthenticationToken)
                ? Optional.of((JWTAuthenticationToken) principal)
                : Optional.empty();
    }
}
