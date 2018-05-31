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

import com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.NotUniqueException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.service.CRUDLService;
import javax.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Created by AKrutov on 12.12.2016.
 */
public abstract class CRUDLController<T, D> extends BaseController {
    private Class<T> entityType;
    private Class<D> dtoType;

    public abstract CRUDLService<T> getBaseService();

    protected JsonResult<D> create(@RequestBody @Valid D dto, BindingResult binding) throws NotUniqueException,
            PermissionDeniedException, NotExistException {

        JsonResult<D> result;
        if (binding.hasErrors()) {
            result = new JsonResult<>(JsonResult.ErrorCode.VALIDATION_ERROR);
            for (FieldError fieldError : binding.getFieldErrors()) {
                result.getValidatorErrors().put(fieldError.getField(), fieldError.getDefaultMessage());
            }
        } else {
            T entity = conversionService.convert(dto, entityType);
            entity = getBaseService().create(entity);
            result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
            result.setResult(conversionService.convert(entity, dtoType));
        }
        return result;
    }
}
