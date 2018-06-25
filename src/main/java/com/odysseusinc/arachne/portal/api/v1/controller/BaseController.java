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

import static com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult.ErrorCode.VALIDATION_ERROR;

import com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult;
import com.odysseusinc.arachne.portal.exception.FieldException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.Skill;
import com.odysseusinc.arachne.portal.security.DataNodeAuthenticationToken;
import com.odysseusinc.arachne.portal.service.BaseUserService;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import javax.validation.ConstraintViolation;

public abstract class BaseController<DN extends DataNode, U extends IUser> {

    @Autowired
    protected BaseUserService<U, Skill> userService;

    @Autowired
    protected GenericConversionService conversionService;

    protected U getUser(Principal principal) throws PermissionDeniedException {

        return userService.getUser(principal);
    }

    protected DN getDatanode(Principal principal) throws PermissionDeniedException {

        if (principal == null || !(principal instanceof DataNodeAuthenticationToken)) {
            throw new PermissionDeniedException();
        }
        return (DN) ((DataNodeAuthenticationToken) principal).getPrincipal();
    }


    protected <T> JsonResult<T> setValidationErrors(BindingResult binding) {

        JsonResult<T> result;
        result = new JsonResult<>(VALIDATION_ERROR);
        for (FieldError fieldError : binding.getFieldErrors()) {
            result.getValidatorErrors().put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return result;
    }

    protected <T> JsonResult<T> setValidationErrors(Set<ConstraintViolation> violations) {

        JsonResult<T> result;
        result = new JsonResult<>(VALIDATION_ERROR);
        violations.forEach(v -> result.getValidatorErrors().put(v.getPropertyPath().toString(), v.getMessage()));
        return result;
    }

    protected <T> JsonResult<T> setEmailValidationErrors(Map<String, String> emailErrors) {

        JsonResult<T> result;
        result = new JsonResult<>(VALIDATION_ERROR);
        result.getValidatorErrors().putAll(emailErrors);

        return result;
    }

    protected <T> JsonResult<T> setValidationErrors(FieldException ex, String scope) {

        JsonResult<T> result = new JsonResult<>(VALIDATION_ERROR);

        Map<String, Object> errors = new HashMap<>();
        errors.put(ex.getField(), ex.getMessage());

        if (scope == null) {
            result.setValidatorErrors(errors);
        } else {
            result.getValidatorErrors().put(scope, errors);
        }

        return result;
    }
}
