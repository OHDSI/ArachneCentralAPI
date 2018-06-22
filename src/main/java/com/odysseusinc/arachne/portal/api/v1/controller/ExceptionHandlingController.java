/*
 *
 * Copyright 2018 Observational Health Data Sciences and Informatics
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
 * Created: January 13, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.controller;

import static com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult.ErrorCode.ALREADY_EXIST;
import static com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult.ErrorCode.DEPENDENCY_EXISTS;
import static com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult.ErrorCode.PERMISSION_DENIED;
import static com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult.ErrorCode.SYSTEM_ERROR;
import static com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult.ErrorCode.VALIDATION_ERROR;

import com.odysseusinc.arachne.commons.api.v1.dto.ArachnePasswordInfoDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult;
import com.odysseusinc.arachne.nohandlerfoundexception.NoHandlerFoundExceptionUtils;
import com.odysseusinc.arachne.portal.exception.AlreadyExistException;
import com.odysseusinc.arachne.portal.exception.EmailNotUniqueException;
import com.odysseusinc.arachne.portal.exception.FieldException;
import com.odysseusinc.arachne.portal.exception.IORuntimeException;
import com.odysseusinc.arachne.portal.exception.NoExecutableFileException;
import com.odysseusinc.arachne.portal.exception.NotEmptyException;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.PasswordValidationException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.exception.ServiceNotAvailableException;
import com.odysseusinc.arachne.portal.exception.UserNotFoundException;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.exception.ValidationRuntimeException;
import com.odysseusinc.arachne.portal.exception.WrongFileFormatException;
import com.odysseusinc.arachne.portal.security.LoginRequestContext;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;
import javax.servlet.http.Cookie;
import java.util.Collection;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class ExceptionHandlingController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandlingController.class);
    private static final String COOKIE_USER_REQUEST = "Arachne-User-Request";

    private static final String ERROR_MESSAGE = "An error has occurred. Please contact system administrator";

    private static final Consumer<HttpServletResponse> ADD_COOKIE_FUNCTION = response -> {

        if (Objects.nonNull(LoginRequestContext.getUserName())) {
            Cookie cookie = new Cookie(COOKIE_USER_REQUEST, LoginRequestContext.getUserName());
            cookie.setPath("/");
            response.addCookie(cookie);
        }
    };

    private NoHandlerFoundExceptionUtils noHandlerFoundExceptionUtils;

    public ExceptionHandlingController(NoHandlerFoundExceptionUtils noHandlerFoundExceptionUtils) {

        this.noHandlerFoundExceptionUtils = noHandlerFoundExceptionUtils;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<JsonResult> exceptionHandler(Exception ex) {

        LOGGER.error(ex.getMessage(), ex);
        JsonResult result = new JsonResult<>(SYSTEM_ERROR);
        result.setErrorMessage(ex.getMessage());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<JsonResult> exceptionHandler(ValidationException ex) {

        LOGGER.warn(ex.getMessage());
        JsonResult result = new JsonResult<>(VALIDATION_ERROR);
        result.setErrorMessage(ex.getMessage());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ExceptionHandler(ValidationRuntimeException.class)
    public ResponseEntity<JsonResult> exceptionHandler(ValidationRuntimeException ex) {

        LOGGER.error(ex.getMessage() + ": " +
                ex.getValidatorErrors().values()
                        .stream()
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList())
                        .toString());
        JsonResult result = new JsonResult<>(VALIDATION_ERROR);
        result.setValidatorErrors(ex.getValidatorErrors());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<JsonResult> exceptionHandler(MethodArgumentNotValidException ex) {

        LOGGER.warn(ex.getMessage());
        JsonResult result = new JsonResult<>(VALIDATION_ERROR);
        if (ex.getBindingResult().hasErrors()) {
            result = setValidationErrors(ex.getBindingResult());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<JsonResult> exceptionHandler(ConstraintViolationException ex) {

        LOGGER.warn(ex.getMessage());
        JsonResult result = new JsonResult<>(VALIDATION_ERROR);
        if (!ex.getConstraintViolations().isEmpty()) {
            result = setValidationErrors(ex.getConstraintViolations());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ExceptionHandler(EmailNotUniqueException.class)
    public ResponseEntity<JsonResult> exceptionHandler(EmailNotUniqueException ex) {

        LOGGER.warn(ex.getMessage());
        JsonResult result = new JsonResult<>(VALIDATION_ERROR);
        if (!ex.getEmailNotUniqueErrors().isEmpty()) {
            result = setEmailValidationErrors(ex.getEmailNotUniqueErrors());
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<JsonResult> exceptionHandler(final DataAccessException ex) {

        LOGGER.error(ex.getMessage(), ex);
        final JsonResult result = new JsonResult<>(SYSTEM_ERROR);
        result.setErrorMessage(ERROR_MESSAGE);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<JsonResult> exceptionHandler(IOException ex) {

        LOGGER.error(ex.getMessage(), ex);
        JsonResult result = new JsonResult<>(SYSTEM_ERROR);
        result.setErrorMessage(ex.getMessage());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ExceptionHandler(IORuntimeException.class)
    public ResponseEntity<JsonResult> exceptionHandler(IORuntimeException ex) {

        LOGGER.error(ex.getMessage(), ex);
        JsonResult result = new JsonResult<>(SYSTEM_ERROR);
        result.setErrorMessage(ex.getMessage());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ExceptionHandler(PermissionDeniedException.class)
    public ResponseEntity<JsonResult> exceptionHandler(PermissionDeniedException ex) {

        LOGGER.error(ex.getMessage(), ex);
        JsonResult result = new JsonResult<>(PERMISSION_DENIED);
        result.setErrorMessage(ex.getMessage());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<JsonResult> exceptionHandler(AccessDeniedException ex) {

        LOGGER.error(ex.getMessage());
        JsonResult result = new JsonResult<>(PERMISSION_DENIED);
        result.setErrorMessage(ex.getMessage());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ExceptionHandler(NotEmptyException.class)
    public ResponseEntity<JsonResult> exceptionHandler(NotEmptyException ex) {

        LOGGER.error(ex.getMessage(), ex);
        JsonResult result = new JsonResult<>(DEPENDENCY_EXISTS);
        result.setErrorMessage(ex.getMessage());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ExceptionHandler(FieldException.class)
    public ResponseEntity<JsonResult> exceptionHandler(FieldException ex) {

        LOGGER.error(ex.getMessage(), ex);
        JsonResult result = new JsonResult<>(VALIDATION_ERROR);
        result.setErrorMessage("Incorrect data");
        result.getValidatorErrors().put(ex.getField(), ex.getMessage());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ExceptionHandler(PasswordValidationException.class)
    public ResponseEntity<JsonResult> exceptionHandler(PasswordValidationException ex) {

        LOGGER.error("User tried to register with weak password");
        JsonResult result = new JsonResult<>(VALIDATION_ERROR);
        result.setErrorMessage("You have provided a weak password");
        final ArachnePasswordInfoDTO passwordInfoDTO = conversionService.convert(ex.getPasswordInfo(), ArachnePasswordInfoDTO.class);
        result.getValidatorErrors().put("password", passwordInfoDTO);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ExceptionHandler(WrongFileFormatException.class)
    public ResponseEntity<JsonResult> exceptionHandler(WrongFileFormatException ex) {

        LOGGER.error(ex.getMessage(), ex);
        JsonResult result = new JsonResult<>(VALIDATION_ERROR);
        result.setErrorMessage(ex.getMessage());
        result.getValidatorErrors().put(ex.getField(), ex.getMessage());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<JsonResult> exceptionHandler(UserNotFoundException ex,
                                                       HttpServletResponse response) throws IOException {

        LOGGER.error(ex.getMessage(), ex);
        JsonResult result = new JsonResult<>(VALIDATION_ERROR);
        result.setErrorMessage(ex.getMessage());
        result.getValidatorErrors().put(ex.getField(), ex.getMessage());
        response.sendRedirect("/auth/login?message=email-not-confirmed");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ExceptionHandler(NotExistException.class)
    public ResponseEntity<JsonResult> exceptionHandler(NotExistException ex) {

        LOGGER.error(ex.getMessage());
        JsonResult result = new JsonResult<>(VALIDATION_ERROR);
        result.setErrorMessage(ex.getMessage());
        result.getValidatorErrors().put(ex.getEntity().getSimpleName(), ex.getMessage());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ExceptionHandler(AlreadyExistException.class)
    public ResponseEntity<JsonResult> exceptionHandler(AlreadyExistException ex) {

        LOGGER.error(ex.getMessage(), ex);
        JsonResult result = new JsonResult<>(ALREADY_EXIST);
        result.setErrorMessage(ex.getMessage());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ExceptionHandler(NoExecutableFileException.class)
    public ResponseEntity<JsonResult> exceptionHandler(NoExecutableFileException ex) {

        LOGGER.error(ex.getMessage(), ex);
        JsonResult result = new JsonResult(VALIDATION_ERROR);
        result.setErrorMessage(ex.getMessage());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ServiceNotAvailableException.class)
    @ResponseBody
    public JsonResult exceptionHandler(ServiceNotAvailableException ex) {

        LOGGER.warn(ex.getMessage());
        JsonResult result = new JsonResult(SYSTEM_ERROR);
        result.setErrorMessage(ex.getMessage());
        return result;
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<JsonResult> exceptionHandler(BindException ex) {

        LOGGER.warn(ex.getMessage());
        JsonResult result = new JsonResult<>(VALIDATION_ERROR);
        result.setErrorMessage("Incorrect data");
        if (ex.hasErrors()) {
            ex.getFieldErrors().forEach(er -> result.getValidatorErrors().put(er.getField(), er.getDefaultMessage()));
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ExceptionHandler({NoHandlerFoundException.class})
    public void handleNotFoundError(HttpServletRequest request, HttpServletResponse response) throws Exception {

        noHandlerFoundExceptionUtils.handleNotFoundError(request, response, ADD_COOKIE_FUNCTION);
    }
}
