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
import com.odysseusinc.arachne.portal.api.v1.dto.dictionary.CreateStudyStatusDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.dictionary.StudyStatusDTO;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.NotUniqueException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.model.StudyStatus;
import com.odysseusinc.arachne.portal.service.StudyStatusService;
import io.swagger.annotations.ApiOperation;
import java.util.LinkedList;
import java.util.List;
import javax.validation.Valid;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StudyStatusController {

    private StudyStatusService studyStatusService;
    private GenericConversionService conversionService;

    public StudyStatusController(StudyStatusService studyStatusService, GenericConversionService conversionService) {

        this.studyStatusService = studyStatusService;
        this.conversionService = conversionService;

    }

    @ApiOperation(value = "Register new study status.", hidden = true)
    @RequestMapping(value = "/api/v1/admin/study-statuses", method = RequestMethod.POST)
    public JsonResult<StudyStatusDTO> create(
            @RequestBody @Valid CreateStudyStatusDTO studyStatusDTO,
            BindingResult binding)
            throws NotExistException, NotUniqueException, PermissionDeniedException {

        JsonResult<StudyStatusDTO> result;
        if (binding.hasErrors()) {
            result = new JsonResult<>(JsonResult.ErrorCode.VALIDATION_ERROR);
            for (FieldError fieldError : binding.getFieldErrors()) {
                result.getValidatorErrors().put(fieldError.getField(), fieldError.getDefaultMessage());
            }
        } else {
            StudyStatus studyStatus = conversionService.convert(studyStatusDTO, StudyStatus.class);
            studyStatus = studyStatusService.create(studyStatus);
            result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
            result.setResult(conversionService.convert(studyStatus, StudyStatusDTO.class));
        }
        return result;
    }

    @ApiOperation("Get study status.")
    @RequestMapping(value = "/api/v1/study-management/study-statuses/{studyStatusId}", method = RequestMethod.GET)
    public JsonResult<StudyStatusDTO> get(@PathVariable("studyStatusId") Long id) throws NotExistException {

        StudyStatus studyStatus = studyStatusService.getById(id);
        JsonResult<StudyStatusDTO> result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
        result.setResult(conversionService.convert(studyStatus, StudyStatusDTO.class));
        return result;
    }

    @ApiOperation(value = "Edit study status.", hidden = true)
    @RequestMapping(value = "/api/v1/admin/study-statuses/{studyStatusId}", method = RequestMethod.PUT)
    public JsonResult<StudyStatusDTO> update(
            @PathVariable("studyStatusId") Long id,
            @RequestBody @Valid StudyStatusDTO studyStatusDTO,
            BindingResult binding)
            throws NotExistException, NotUniqueException, ValidationException {

        JsonResult<StudyStatusDTO> result;
        if (binding.hasErrors()) {
            result = new JsonResult<>(JsonResult.ErrorCode.VALIDATION_ERROR);
            for (FieldError fieldError : binding.getFieldErrors()) {
                result.getValidatorErrors().put(fieldError.getField(), fieldError.getDefaultMessage());
            }
        } else {
            StudyStatus studyStatus = conversionService.convert(studyStatusDTO, StudyStatus.class);
            studyStatus = studyStatusService.update(studyStatus);
            result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
            result.setResult(conversionService.convert(studyStatus, StudyStatusDTO.class));
        }
        return result;
    }

    @ApiOperation(value = "Delete study status.", hidden = true)
    @RequestMapping(value = "/api/v1/admin/study-statuses/{studyStatusId}", method = RequestMethod.DELETE)
    public JsonResult<Boolean> delete(@PathVariable("studyStatusId") Long id) throws NotExistException {

        JsonResult<Boolean> result;
        if (id == null) {
            result = new JsonResult<>(JsonResult.ErrorCode.VALIDATION_ERROR);
            result.getValidatorErrors().put("studyStatusId", "cannot be null");
        } else {
            studyStatusService.delete(id);
            result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
            result.setResult(Boolean.TRUE);
        }
        return result;
    }

    @ApiOperation("List study statuses.")
    @RequestMapping(value = "/api/v1/study-management/study-statuses", method = RequestMethod.GET)
    public JsonResult<List<StudyStatusDTO>> list() {

        JsonResult<List<StudyStatusDTO>> result;
        Iterable<StudyStatus> studyStatuses = studyStatusService.list();
        result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
        List<StudyStatusDTO> studyStatusDTOs = new LinkedList<>();
        for (StudyStatus studyStatus : studyStatuses) {
            studyStatusDTOs.add(conversionService.convert(studyStatus, StudyStatusDTO.class));
        }
        result.setResult(studyStatusDTOs);
        return result;
    }

}
