/**
 *
 * Copyright 2017 Observational Health Data Sciences and Informatics
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

import com.odysseusinc.arachne.commons.api.v1.dto.CommonAuthMethodDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonAuthenticationRequest;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonAuthenticationResponse;
import com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult;
import com.odysseusinc.arachne.portal.api.v1.dto.RemindPasswordDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.ResetPasswordDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UserInfoDTO;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.PasswordValidationException;
import com.odysseusinc.arachne.portal.exception.UserNotActivatedException;
import com.odysseusinc.arachne.portal.exception.UserNotFoundException;
import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.model.PasswordReset;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.model.security.ArachneUser;
import com.odysseusinc.arachne.portal.security.TokenUtils;
import com.odysseusinc.arachne.portal.service.BaseUserService;
import com.odysseusinc.arachne.portal.service.PasswordResetService;
import com.odysseusinc.arachne.portal.service.ProfessionalTypeService;
import edu.vt.middleware.password.Password;
import edu.vt.middleware.password.PasswordData;
import edu.vt.middleware.password.PasswordValidator;
import edu.vt.middleware.password.RuleResult;
import io.swagger.annotations.ApiOperation;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;

public abstract class BaseAuthenticationController extends BaseController<DataNode> {

    private static final Logger log = LoggerFactory.getLogger(BaseAuthenticationController.class);

    @Value("${arachne.token.header}")
    private String tokenHeader;
    @Value("${portal.authMethod}")
    private String userOrigin;

    private AuthenticationManager authenticationManager;
    private TokenUtils tokenUtils;
    protected BaseUserService userService;
    private UserDetailsService userDetailsService;
    private PasswordResetService passwordResetService;
    private PasswordValidator passwordValidator;
    protected ProfessionalTypeService professionalTypeService;

    public BaseAuthenticationController(AuthenticationManager authenticationManager,
                                        TokenUtils tokenUtils,
                                        BaseUserService userService,
                                        UserDetailsService userDetailsService,
                                        PasswordResetService passwordResetService,
                                        @Qualifier("passwordValidator") PasswordValidator passwordValidator,
                                        ProfessionalTypeService professionalTypeService) {

        this.authenticationManager = authenticationManager;
        this.tokenUtils = tokenUtils;
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.passwordResetService = passwordResetService;
        this.passwordValidator = passwordValidator;
        this.professionalTypeService = professionalTypeService;
    }

    @ApiOperation("Get auth method")
    @RequestMapping(value = "/api/v1/auth/method", method = RequestMethod.GET)
    public JsonResult<CommonAuthMethodDTO> authMethod() {

        final JsonResult<CommonAuthMethodDTO> result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
        result.setResult(new CommonAuthMethodDTO(userOrigin));
        return result;
    }

    @ApiOperation("Login with specified credentials.")
    @RequestMapping(value = "/api/v1/auth/login", method = RequestMethod.POST)
    public JsonResult login(@RequestBody CommonAuthenticationRequest authenticationRequest)
            throws AuthenticationException {

        JsonResult jsonResult;
        try {
            Authentication authentication = this.authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getUsername(),
                            authenticationRequest.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = getUser(authenticationRequest.getUsername());

            String token = this.tokenUtils.generateToken(user.getUsername());
            jsonResult = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
            CommonAuthenticationResponse authenticationResponse = new CommonAuthenticationResponse(token);
            jsonResult.setResult(authenticationResponse);
        } catch (Exception ex) {
            if (ex.getCause() instanceof UserNotActivatedException) {
                jsonResult = new JsonResult<>(JsonResult.ErrorCode.UNACTIVATED);
            } else {
                jsonResult = new JsonResult<>(JsonResult.ErrorCode.UNAUTHORIZED);
            }
            jsonResult.setErrorMessage(ex.getMessage());
            log.error(ex.getMessage(), ex);
        }
        // Return the token
        return jsonResult;
    }

    protected User getUser(String userName) {

        return userService.getByUsername(userName);
    }

    @ApiOperation("Logout.")
    @RequestMapping(value = "/api/v1/auth/logout", method = RequestMethod.POST)
    public JsonResult logout(HttpServletRequest request) {

        JsonResult result;
        try {
            String token = request.getHeader(tokenHeader);
            if (token != null) {
                tokenUtils.addInvalidateToken(token);
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
            String username = this.tokenUtils.getUsernameFromToken(token);
            ArachneUser user = (ArachneUser) this.userDetailsService.loadUserByUsername(username);
            if (this.tokenUtils.canTokenBeRefreshed(token, user.getLastPasswordReset())) {
                String refreshedToken = this.tokenUtils.refreshToken(token);
                result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
                result.setResult(refreshedToken);
            } else {
                result = new JsonResult<>(JsonResult.ErrorCode.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            result = new JsonResult<>(JsonResult.ErrorCode.UNAUTHORIZED);
        }
        return result;
    }

    @ApiOperation("Request password reset e-mail.")
    @RequestMapping(value = "/api/v1/auth/remind-password", method = RequestMethod.POST)
    public JsonResult remindPassword(@RequestBody @Valid RemindPasswordDTO remindPasswordDTO, BindingResult binding) {

        JsonResult result;
        if (binding.hasErrors()) {
            result = super.setValidationErrors(binding);
        } else {
            String email = remindPasswordDTO.getEmail();
            User user = userService.getByUnverifiedEmail(email);
            if (user == null) {
                result = new JsonResult<>(JsonResult.ErrorCode.VALIDATION_ERROR);
                result.setErrorMessage("No such user exists");
            } else {
                PasswordReset passwordReset = passwordResetService.generate(email);
                userService.sendRemindPasswordEmail(user, passwordReset.getToken(),
                        remindPasswordDTO.getRegistrantToken(), remindPasswordDTO.getCallbackUrl());
                result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
            }
        }
        return result;
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
            tokenUtils.addInvalidateToken(token);
        }
        JsonResult result;
        if (binding.hasErrors()) {
            result = super.setValidationErrors(binding);
        } else {
            String email = resetPasswordDTO.getEmail();
            String token = resetPasswordDTO.getToken();
            String newPassword = resetPasswordDTO.getPassword();
            PasswordData passwordData = new PasswordData(new Password(newPassword));
            RuleResult validationResult = passwordValidator.validate(passwordData);
            if (!validationResult.isValid()) {
                throw new PasswordValidationException(passwordValidator.getMessages(validationResult));
            }
            if (passwordResetService.canReset(email, token)) {
                User user = userService.getByUnverifiedEmail(email);
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
        User user = userService.getByEmail(principal.getName());
        final UserInfoDTO userInfo = conversionService.convert(user, UserInfoDTO.class);
        result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
        result.setResult(userInfo);

        return result;
    }

}
