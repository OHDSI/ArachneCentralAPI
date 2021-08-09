package com.odysseusinc.arachne.portal.security;

import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.model.factory.ArachneUserFactory;
import com.odysseusinc.arachne.portal.model.security.ArachneUser;
import com.odysseusinc.arachne.portal.service.ProfessionalTypeService;
import com.odysseusinc.arachne.portal.service.UserService;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.ohdsi.authenticator.service.authentication.TokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class Oauth2SuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private final static Logger log = LoggerFactory.getLogger(Oauth2SuccessHandler.class);


    private final String header;
    private final UserService userService;
    private final TokenProvider tokenProvider;
    private final ProfessionalTypeService professionalTypeService;

    public Oauth2SuccessHandler(
            @Value("${arachne.token.header}") String header,
            UserService userService,
            TokenProvider tokenProvider,
            ProfessionalTypeService professionalTypeService) {
        this.header = header;
        this.userService = userService;
        this.tokenProvider = tokenProvider;
        this.professionalTypeService = professionalTypeService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication
    ) throws IOException, ServletException {
        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            log.warn("Unsupported authentication type [{}]", authentication.getClass());
        } else {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2User principal = oauthToken.getPrincipal();

            Map<String, Object> additionalInfo = new HashMap<>();
            String method = oauthToken.getAuthorizedClientRegistrationId();
            additionalInfo.put("method", method);
            ///additionalInfo.put("token", "Do we need this???");

            Map<String, Object> attributes = principal.getAttributes();
            String username = (String) attributes.get(StandardClaimNames.SUB);

            String token = Optional.ofNullable(
                    userService.getByUsername(method, username)
            ).map(user -> {
                return tokenProvider.createToken(user.getUsername(), additionalInfo, null);
            }).orElseGet(() -> {
                ArachneUser details = createUser(attributes, username, method);
                return tokenProvider.createToken(details.getUsername(), additionalInfo, null);
            });
            // TODO Put more stuff in token???
            Cookie cookie = new Cookie(header, token);
            cookie.setSecure(true);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            response.addCookie(cookie);
        }
        super.onAuthenticationSuccess(request, response, authentication);
    }

    // TODO For some reason we are not able to get OidcUser here and use its nice getters, and only get DefaultOAuth2User ...

    @NotNull
    private ArachneUser createUser(Map<String, Object> attributes, String username, String origin) {
        log.info("User [{}] not found in DB, creating...", username);
        User user = new User();
        user.setUsername(username);
        user.setEmail(username);
        user.setEnabled(true);
        String email = (String) attributes.getOrDefault(StandardClaimNames.EMAIL, "");
        user.setContactEmail(email);
        // Elixir currently gives nothing under StandardClaimNames.EMAIL_VERIFIED
        user.setEmailConfirmed(true);
        user.setFirstname((String) attributes.getOrDefault(StandardClaimNames.GIVEN_NAME, ""));
        user.setLastname((String) attributes.getOrDefault(StandardClaimNames.FAMILY_NAME, ""));
        user.setMiddlename((String) attributes.getOrDefault(StandardClaimNames.MIDDLE_NAME, ""));
        user.setOrganization("");

        // TODO Need to make meaningful choice here
        user.setProfessionalType(professionalTypeService.list().iterator().next());

        IUser created = userService.createExternal(user, origin);
        log.info("User [{}] successfully created with id [{}]", email, created.getId());
        // Try to load again??? why do we have it as a different service???
        return ArachneUserFactory.create(created);
    }


}
