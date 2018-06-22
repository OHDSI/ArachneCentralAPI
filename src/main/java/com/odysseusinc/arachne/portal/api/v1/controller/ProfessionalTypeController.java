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

import com.odysseusinc.arachne.commons.api.v1.dto.CommonProfessionalTypeDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.NotUniqueException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.model.ProfessionalType;
import com.odysseusinc.arachne.portal.service.ProfessionalTypeService;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
public class ProfessionalTypeController {

    Logger log = LoggerFactory.getLogger(ProfessionalTypeController.class);

    @Autowired
    private ProfessionalTypeService professionalTypeService;

    @Autowired
    private GenericConversionService conversionService;


    @ApiOperation(value = "Register new professional type.", hidden = true)
    @RequestMapping(value = "/api/v1/admin/professional-types", method = RequestMethod.POST)
    public JsonResult<CommonProfessionalTypeDTO> create(
            @RequestBody @Valid CommonProfessionalTypeDTO professionalTypeDTO,
            BindingResult binding)
            throws NotExistException, NotUniqueException, PermissionDeniedException {

        JsonResult result;
        if (binding.hasErrors()) {
            result = new JsonResult<>(JsonResult.ErrorCode.VALIDATION_ERROR);
            for (FieldError fieldError : binding.getFieldErrors()) {
                result.getValidatorErrors().put(fieldError.getField(), fieldError.getDefaultMessage());
            }
        } else {
            ProfessionalType professionalType = conversionService.convert(professionalTypeDTO, ProfessionalType.class);
            professionalType = professionalTypeService.create(professionalType);
            result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
            result.setResult(conversionService.convert(professionalType, professionalTypeDTO.getClass()));
        }
        return result;
    }

    @ApiOperation("Get professional type.")
    @RequestMapping(value = "/api/v1/user-management/professional-types/{professionalTypeId}", method = RequestMethod.GET)
    public JsonResult<CommonProfessionalTypeDTO> get(@PathVariable("professionalTypeId") Long id) throws NotExistException {

        JsonResult result;
        if (id == null) {
            result = new JsonResult<>(JsonResult.ErrorCode.VALIDATION_ERROR);
            result.getValidatorErrors().put("professionalTypeId", "cannot be null");
        } else {
            ProfessionalType skill = professionalTypeService.getById(id);
            result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
            result.setResult(skill);
        }
        return result;
    }

    @ApiOperation(value = "Update professional type.", hidden = true)
    @RequestMapping(value = "/api/v1/admin/professional-types/{professionalTypeId}", method = RequestMethod.PUT)
    public JsonResult<CommonProfessionalTypeDTO> update(
            @PathVariable("professionalTypeId") Long id,
            @RequestBody @Valid CommonProfessionalTypeDTO professionalTypeDTO,
            BindingResult binding)
            throws NotExistException, NotUniqueException, ValidationException {

        JsonResult result = null;
        if (binding.hasErrors()) {
            result = new JsonResult<>(JsonResult.ErrorCode.VALIDATION_ERROR);
            for (FieldError fieldError : binding.getFieldErrors()) {
                result.getValidatorErrors().put(fieldError.getField(), fieldError.getDefaultMessage());
            }
        } else {
            ProfessionalType professionalType = conversionService.convert(professionalTypeDTO, ProfessionalType.class);
            professionalType.setId(id);
            professionalType = professionalTypeService.update(professionalType);
            result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
            result.setResult(professionalType);
        }
        return result;
    }

    @ApiOperation(value = "Delete professional type.", hidden = true)
    @RequestMapping(value = "/api/v1/admin/professional-types/{professionalTypeId}", method = RequestMethod.DELETE)
    public JsonResult<Boolean> remove(@PathVariable("professionalTypeId") Long id) throws NotExistException {

        JsonResult result = null;
        if (id == null) {
            result = new JsonResult<>(JsonResult.ErrorCode.VALIDATION_ERROR);
            result.getValidatorErrors().put("professionalTypeId", "cannot be null");
        } else {
            professionalTypeService.delete(id);
            result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
            result.setResult(Boolean.TRUE);
        }
        return result;
    }

    @ApiOperation("List professional types.")
    @RequestMapping(value = "/api/v1/user-management/professional-types", method = RequestMethod.GET)
    public JsonResult<List<CommonProfessionalTypeDTO>> list() {

        JsonResult result = null;
        Iterable<ProfessionalType> professionalTypes = professionalTypeService.list();
        result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
        result.setResult(professionalTypes);
        return result;
    }

}
