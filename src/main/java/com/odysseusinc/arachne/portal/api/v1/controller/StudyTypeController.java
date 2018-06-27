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
import com.odysseusinc.arachne.portal.api.v1.dto.dictionary.CreateStudyTypeDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.dictionary.StudyTypeDTO;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.NotUniqueException;
import com.odysseusinc.arachne.portal.model.StudyType;
import com.odysseusinc.arachne.portal.service.StudyTypeService;
import io.swagger.annotations.ApiOperation;
import java.util.LinkedList;
import java.util.List;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConverterNotFoundException;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StudyTypeController {

    private final Logger log = LoggerFactory.getLogger(StudyStatusController.class);

    private StudyTypeService studyTypeService;
    private GenericConversionService conversionService;

    public StudyTypeController(StudyTypeService studyTypeService, GenericConversionService conversionService) {

        this.studyTypeService = studyTypeService;
        this.conversionService = conversionService;
    }

    @ApiOperation(value = "Register new study type.", hidden = true)
    @RequestMapping(value = "/api/v1/admin/study-types", method = RequestMethod.POST)
    public JsonResult create(@RequestBody @Valid CreateStudyTypeDTO studyTypeDTO, BindingResult binding) {

        JsonResult result;
        if (binding.hasErrors()) {
            result = new JsonResult<>(JsonResult.ErrorCode.VALIDATION_ERROR);
            for (FieldError fieldError : binding.getFieldErrors()) {
                result.getValidatorErrors().put(fieldError.getField(), fieldError.getDefaultMessage());
            }
        } else {
            try {
                StudyType studyType = conversionService.convert(studyTypeDTO, StudyType.class);
                studyType = studyTypeService.create(studyType);
                result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
                result.setResult(studyType);
            } catch (ConverterNotFoundException ex) {
                log.error(ex.getMessage(), ex);
                result = new JsonResult<>(JsonResult.ErrorCode.SYSTEM_ERROR);
                result.setErrorMessage(ex.getMessage());
            } catch (NotUniqueException ex) {
                log.error(ex.getMessage(), ex);
                result = new JsonResult<>(JsonResult.ErrorCode.VALIDATION_ERROR);
                result.getValidatorErrors().put(ex.getField(), ex.getMessage());
                result.setErrorMessage(ex.getMessage());
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                result = new JsonResult<>(JsonResult.ErrorCode.SYSTEM_ERROR);
                result.setErrorMessage(ex.getMessage());

            }
        }
        return result;
    }

    @ApiOperation("Get study type.")
    @RequestMapping(value = "/api/v1/study-management/study-types/{studyTypeId}", method = RequestMethod.GET)
    public JsonResult get(@PathVariable("studyTypeId") Long id) {

        JsonResult result = null;
        try {
            StudyType studyType = studyTypeService.getById(id);
            result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
            result.setResult(studyType);
        } catch (NotExistException ex) {
            log.error(ex.getMessage(), ex);
            result = new JsonResult<>(JsonResult.ErrorCode.VALIDATION_ERROR);
            result.getValidatorErrors().put("studyTypeId", "Status with id=" + id + " not found");
            result.setErrorMessage(ex.getMessage());
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            result = new JsonResult<>(JsonResult.ErrorCode.SYSTEM_ERROR);
            result.setErrorMessage(ex.getMessage());
        }
        return result;
    }

    @ApiOperation(value = "Edit study type.", hidden = true)
    @RequestMapping(value = "/api/v1/admin/study-types/{studyTypeId}", method = RequestMethod.PUT)
    public JsonResult update(
            @PathVariable("studyTypeId") Long id,
            @RequestBody @Valid StudyTypeDTO studyTypeDTO,
            BindingResult binding) {

        JsonResult result = null;
        if (binding.hasErrors()) {
            result = new JsonResult<>(JsonResult.ErrorCode.VALIDATION_ERROR);
            for (FieldError fieldError : binding.getFieldErrors()) {
                result.getValidatorErrors().put(fieldError.getField(), fieldError.getDefaultMessage());
            }
        } else {
            try {
                StudyType studyType = conversionService.convert(studyTypeDTO, StudyType.class);
                studyType = studyTypeService.update(studyType);
                result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
                result.setResult(studyType);
            } catch (ConverterNotFoundException ex) {
                log.error(ex.getMessage(), ex);
                result = new JsonResult<>(JsonResult.ErrorCode.SYSTEM_ERROR);
                result.setErrorMessage(ex.getMessage());
            } catch (NotExistException ex) {
                log.error(ex.getMessage(), ex);
                result = new JsonResult<>(JsonResult.ErrorCode.VALIDATION_ERROR);
                result.getValidatorErrors().put("id", "Status with id=" + id + " not found");
                result.setErrorMessage(ex.getMessage());
            } catch (NotUniqueException ex) {
                log.error(ex.getMessage(), ex);
                result = new JsonResult<>(JsonResult.ErrorCode.VALIDATION_ERROR);
                result.getValidatorErrors().put(ex.getField(), ex.getMessage());
                result.setErrorMessage(ex.getMessage());
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                result = new JsonResult<>(JsonResult.ErrorCode.SYSTEM_ERROR);
                result.setErrorMessage(ex.getMessage());

            }
        }
        return result;
    }

    @ApiOperation(value = "Delete study type.", hidden = true)
    @RequestMapping(value = "/api/v1/admin/study-types/{studyTypeId}", method = RequestMethod.DELETE)
    public JsonResult delete(@PathVariable("studyTypeId") Long id) {

        JsonResult result = null;
        if (id == null) {
            result = new JsonResult<>(JsonResult.ErrorCode.VALIDATION_ERROR);
            result.getValidatorErrors().put("studyTypeId", "cannot be null");
        } else {
            try {
                studyTypeService.delete(id);
                result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
                result.setResult(Boolean.TRUE);
            } catch (NotExistException ex) {
                log.error(ex.getMessage(), ex);
                result = new JsonResult<>(JsonResult.ErrorCode.VALIDATION_ERROR);
                result.getValidatorErrors().put("studyTypeId", "Status with id=" + id + " not found");
                result.setErrorMessage(ex.getMessage());
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                result = new JsonResult<>(JsonResult.ErrorCode.SYSTEM_ERROR);
                result.setErrorMessage(ex.getMessage());

            }
        }
        return result;
    }

    @ApiOperation("List study types.")
    @RequestMapping(value = "/api/v1/study-management/study-types", method = RequestMethod.GET)
    public JsonResult<List<StudyTypeDTO>> list() {

        JsonResult result;
        try {
            Iterable<StudyType> studyTypees = studyTypeService.list();
            result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
            List<StudyTypeDTO> studyTypeDTOs = new LinkedList<>();
            for (StudyType studyType : studyTypees) {
                studyTypeDTOs.add(conversionService.convert(studyType, StudyTypeDTO.class));
            }
            result.setResult(studyTypeDTOs);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            result = new JsonResult<>(JsonResult.ErrorCode.SYSTEM_ERROR);
            result.setErrorMessage(ex.getMessage());

        }
        return result;
    }

}
