/*
 *
 * Copyright 2018 Odysseus Data Services, inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Company: Odysseus Data Services, Inc.
 * Product Owner/Architecture: Gregory Klebanov
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva, Mikhail Mironov
 * Created: October 09, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.controller;

import static com.odysseusinc.arachne.portal.api.v1.controller.util.ControllerUtils.emulateEmailSent;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonAuthMethodDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonAuthenticationRequest;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonAuthenticationResponse;
import com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult;
import com.odysseusinc.arachne.commons.utils.ErrorMessages;
import com.odysseusinc.arachne.portal.api.v1.dto.RemindPasswordDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.ResetPasswordDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UserInfoDTO;
import com.odysseusinc.arachne.portal.exception.NoDefaultTenantException;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.PasswordValidationException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.exception.UserNotActivatedException;
import com.odysseusinc.arachne.portal.exception.UserNotFoundException;
import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.PasswordReset;
import com.odysseusinc.arachne.portal.security.passwordvalidator.ArachnePasswordData;
import com.odysseusinc.arachne.portal.security.passwordvalidator.ArachnePasswordValidationResult;
import com.odysseusinc.arachne.portal.security.passwordvalidator.ArachnePasswordValidator;
import com.odysseusinc.arachne.portal.service.AuthenticationService;
import com.odysseusinc.arachne.portal.service.BaseUserService;
import com.odysseusinc.arachne.portal.service.LoginAttemptService;
import com.odysseusinc.arachne.portal.service.PasswordResetService;
import com.odysseusinc.arachne.portal.service.ProfessionalTypeService;
import com.odysseusinc.arachne.portal.service.AuthenticationHelperService;
import edu.vt.middleware.password.Password;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.security.Principal;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.apache.solr.client.solrj.SolrServerException;
import org.ohdsi.authenticator.model.UserInfo;
import org.ohdsi.authenticator.service.authentication.Authenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public abstract class BaseAuthenticationController extends BaseController<DataNode, IUser> {

    private static final Logger log = LoggerFactory.getLogger(BaseAuthenticationController.class);

    @Value("${arachne.token.header}")
    private String tokenHeader;

    protected Authenticator authenticator;
    protected BaseUserService userService;
    private PasswordResetService passwordResetService;
    private ArachnePasswordValidator passwordValidator;
    protected ProfessionalTypeService professionalTypeService;
    protected LoginAttemptService loginAttemptService;
    private AuthenticationService authenticationService;
    protected AuthenticationHelperService authenticationHelperService;

    public BaseAuthenticationController(Authenticator authenticator,
                                        BaseUserService userService,
                                        PasswordResetService passwordResetService,
                                        @Qualifier("passwordValidator") ArachnePasswordValidator passwordValidator,
                                        ProfessionalTypeService professionalTypeService,
                                        LoginAttemptService loginAttemptService,
                                        AuthenticationService authenticationService, AuthenticationHelperService authenticationHelperService) {

        this.authenticator = authenticator;
        this.userService = userService;
        this.passwordResetService = passwordResetService;
        this.passwordValidator = passwordValidator;
        this.professionalTypeService = professionalTypeService;
        this.loginAttemptService = loginAttemptService;
        this.authenticationService = authenticationService;
        this.authenticationHelperService = authenticationHelperService;
    }

    @ApiOperation("Get auth method")
    @RequestMapping(value = "/api/v1/auth/method", method = RequestMethod.GET)
    public JsonResult<CommonAuthMethodDTO> authMethod() {

        final JsonResult<CommonAuthMethodDTO> result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
        result.setResult(new CommonAuthMethodDTO(authenticationHelperService.getCurrentMethodType()));
        return result;
    }

    @ApiOperation("Login with specified credentials.")
    @RequestMapping(value = "/api/v1/auth/login", method = RequestMethod.POST)
    public JsonResult<CommonAuthenticationResponse> login(@Valid @RequestBody CommonAuthenticationRequest authenticationRequest)
            throws AuthenticationException {

        JsonResult<CommonAuthenticationResponse> jsonResult;
        authenticationRequest.normalizeLogin();
        String username = authenticationRequest.getUsername();
        try {
            checkIfUserBlocked(username);
            checkIfUserHasTenant(username);

            String authToken = authenticationService.authenticateAndGetAuthToken(authenticationRequest);

            CommonAuthenticationResponse authenticationResponse = new CommonAuthenticationResponse(authToken);
            jsonResult = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR, authenticationResponse);
            loginAttemptService.loginSucceeded(username);
        } catch (Exception ex) {
            jsonResult = getJsonResultForUnsuccessfulLogin(username, ex);
        }
        // Return the token
        return jsonResult;
    }

    private JsonResult<CommonAuthenticationResponse> getJsonResultForUnsuccessfulLogin(String username, Exception ex) {

        JsonResult<CommonAuthenticationResponse> jsonResult;
        String errorMessage = ex.getMessage();
        if (ex.getCause() instanceof UserNotActivatedException) {
            jsonResult = new JsonResult<>(JsonResult.ErrorCode.UNACTIVATED);
        } else {
            loginAttemptService.loginFailed(username);
            jsonResult = new JsonResult<>(JsonResult.ErrorCode.UNAUTHORIZED);
            errorMessage = "Unsuccessful attempt to login for user: {}";
        }
        jsonResult.setErrorMessage(ex.getMessage());
        log.error(errorMessage, username);
        return jsonResult;
    }

    private void checkIfUserBlocked(String username) throws PermissionDeniedException {

        final Long remainingAccountLockPeriodSeconds = loginAttemptService.getRemainingAccountLockPeriod(username);
        if (remainingAccountLockPeriodSeconds != null) {
            final String errorMessage = String.format("You have exceeded the number of allowed login attempts. Please try again in %s seconds.", remainingAccountLockPeriodSeconds);
            throw new PermissionDeniedException(errorMessage);
        }
    }

    protected void checkIfUserHasTenant(String username) throws AuthenticationException {

        IUser user = userService.getByUsernameInAnyTenant(username);
        if (user == null) {
            throw new BadCredentialsException(ErrorMessages.BAD_CREDENTIALS.getMessage());
        }
        if (user.getTenants() == null || user.getTenants().isEmpty()) {
            throw new NoDefaultTenantException("Request admin to add you into a tenant.");
        }
    }

    @ApiOperation("Logout.")
    @RequestMapping(value = "/api/v1/auth/logout", method = RequestMethod.POST)
    public JsonResult logout(HttpServletRequest request) {

        JsonResult result;
        try {
            String token = request.getHeader(tokenHeader);
            if (token != null) {
                authenticator.invalidateToken(token);
            }
            result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
            result.setResult(true);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            result = new JsonResult<>(JsonResult.ErrorCode.SYSTEM_ERROR);
            result.setResult(false);
            result.setErrorMessage(ex.getMessage());
        }
        return result;
    }

    @ApiOperation("Refresh session token.")
    @RequestMapping(value = "/api/v1/auth/refresh", method = RequestMethod.POST)
    public JsonResult<String> refresh(HttpServletRequest request) {

        JsonResult<String> result;
        try {
            String token = request.getHeader(this.tokenHeader);
            UserInfo userInfo = authenticator.refreshToken(token);
            result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
            if (userInfo == null || userInfo.getToken() == null) {
                throw new AuthenticationServiceException("Cannot refresh token user info is either null or does not contain token");
            }
            result.setResult(userInfo.getToken());
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            result = new JsonResult<>(JsonResult.ErrorCode.UNAUTHORIZED);
        }
        return result;
    }

    @ApiOperation("Request password reset e-mail.")
    @RequestMapping(value = "/api/v1/auth/remind-password", method = RequestMethod.POST)
    public void remindPassword(@RequestBody @Valid RemindPasswordDTO remindPasswordDTO) throws InterruptedException {

        IUser user = userService.getByUnverifiedEmailIgnoreCaseInAnyTenant(remindPasswordDTO.getEmail());
        // If user was not found,
        // do not throw exception to prevent "Unauthenticated Email Address Enumeration" security issue
        if (user != null) {
            PasswordReset passwordReset = passwordResetService.generate(user.getEmail());
            userService.sendRemindPasswordEmail(user, passwordReset.getToken(),
                    remindPasswordDTO.getRegistrantToken(), remindPasswordDTO.getCallbackUrl());
        } else {
            emulateEmailSent();
        }
    }

    @ApiOperation("Reset password for specified e-mail.")
    @RequestMapping(value = "/api/v1/auth/reset-password", method = RequestMethod.POST)
    public JsonResult resetPassword(
            Principal principal,
            HttpServletRequest request,
            @RequestBody @Valid ResetPasswordDTO resetPasswordDTO,
            BindingResult binding)
            throws PasswordValidationException, UserNotFoundException, NotExistException,
            NoSuchFieldException, IOException, SolrServerException, IllegalAccessException {

        if (principal != null) {
            String token = request.getHeader(tokenHeader);
            authenticator.invalidateToken(token);
        }
        JsonResult result;
        if (binding.hasErrors()) {
            result = super.setValidationErrors(binding);
        } else {
            String email = resetPasswordDTO.getEmail();
            String token = resetPasswordDTO.getToken();
            String newPassword = resetPasswordDTO.getPassword();
            final ArachnePasswordData passwordData = new ArachnePasswordData(new Password(newPassword));
            final ArachnePasswordValidationResult validationResult = passwordValidator.validate(passwordData);
            if (!validationResult.isValid()) {
                throw new PasswordValidationException(passwordValidator.getMessages(validationResult));
            }
            if (passwordResetService.canReset(email, token)) {
                IUser user = userService.getByUnverifiedEmailInAnyTenant(email);
                user.setPassword(newPassword);
                userService.resetPassword(user);
                result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
            } else {
                result = new JsonResult<>(JsonResult.ErrorCode.VALIDATION_ERROR);
                result.setErrorMessage("Token expired. Please, request new reset password link.");
            }
        }
        return result;
    }

    @ApiOperation("Get information about current user.")
    @RequestMapping(value = "/api/v1/auth/me", method = RequestMethod.GET)
    public JsonResult<UserInfoDTO> info(Principal principal) {

        final JsonResult<UserInfoDTO> result;
        IUser user = userService.getByUsernameInAnyTenant(principal.getName());
        final UserInfoDTO userInfo = conversionService.convert(user, UserInfoDTO.class);
        result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
        result.setResult(userInfo);

        return result;
    }

}
