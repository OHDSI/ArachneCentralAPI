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
import com.odysseusinc.arachne.portal.api.v1.dto.dictionary.RoleDTO;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.NotUniqueException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.model.Role;
import com.odysseusinc.arachne.portal.service.RoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.LinkedList;
import java.util.List;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by AKrutov on 19.10.2016.
 */
@Api(hidden = true)
@Validated
@RestController
public class RoleController extends BaseController {

    Logger log = LoggerFactory.getLogger(RoleController.class);

    @Autowired
    private RoleService roleService;


    @ApiOperation(value = "Register new role.", hidden = true)
    @RequestMapping(value = "/api/v1/admin/roles", method = RequestMethod.POST)
    public JsonResult<RoleDTO> create(
            @RequestBody @Valid RoleDTO roleDTO,
            BindingResult binding)
            throws NotExistException, NotUniqueException, PermissionDeniedException {

        JsonResult<RoleDTO> result;
        if (binding.hasErrors()) {
            result = new JsonResult<>(JsonResult.ErrorCode.VALIDATION_ERROR);
            for (FieldError fieldError : binding.getFieldErrors()) {
                result.getValidatorErrors().put(fieldError.getField(), fieldError.getDefaultMessage());
            }
        } else {
            Role role = conversionService.convert(roleDTO, Role.class);
            role = roleService.create(role);
            result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
            result.setResult(conversionService.convert(role, RoleDTO.class));
        }
        return result;
    }


    @ApiOperation(value = "Get role description.", hidden = true)
    @RequestMapping(value = "/api/v1/admin/roles/{roleId}", method = RequestMethod.GET)
    public JsonResult<RoleDTO> get(@PathVariable("roleId") Long id) throws NotExistException {

        JsonResult<RoleDTO> result;
        if (id == null) {
            result = new JsonResult<>(JsonResult.ErrorCode.VALIDATION_ERROR);
            result.getValidatorErrors().put("roleId", "cannot be null");
        } else {
            Role role = roleService.getById(id);
            result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
            result.setResult(conversionService.convert(role, RoleDTO.class));
        }
        return result;
    }

    @ApiOperation(value = "Edit role.", hidden = true)
    @RequestMapping(value = "/api/v1/admin/roles/{roleId}", method = RequestMethod.PUT)
    public JsonResult<RoleDTO> update(@PathVariable("roleId") Long id, @RequestBody @Valid RoleDTO roleDTO,
                                      BindingResult binding) throws NotExistException, NotUniqueException, ValidationException {

        JsonResult<RoleDTO> result;
        if (binding.hasErrors()) {
            result = new JsonResult<>(JsonResult.ErrorCode.VALIDATION_ERROR);
            for (FieldError fieldError : binding.getFieldErrors()) {
                result.getValidatorErrors().put(fieldError.getField(), fieldError.getDefaultMessage());
            }
        } else {

            Role role = conversionService.convert(roleDTO, Role.class);
            role.setId(id);
            role = roleService.update(role);
            result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
            result.setResult(conversionService.convert(role, RoleDTO.class));

        }
        return result;
    }

    @ApiOperation(value = "Delete role.", hidden = true)
    @RequestMapping(value = "/api/v1/admin/roles/{roleId}", method = RequestMethod.DELETE)
    public JsonResult<Boolean> delete(@PathVariable("roleId") Long id) throws NotExistException {

        JsonResult<Boolean> result;
        if (id == null) {
            result = new JsonResult<>(JsonResult.ErrorCode.VALIDATION_ERROR);
            result.getValidatorErrors().put("roleId", "cannot be null");
        } else {
            roleService.delete(id);
            result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
            result.setResult(Boolean.TRUE);
        }
        return result;
    }

    @ApiOperation(value = "List roles.", hidden = true)
    @RequestMapping(value = "/api/v1/admin/roles", method = RequestMethod.GET)
    public JsonResult<List<RoleDTO>> list() {

        JsonResult<List<RoleDTO>> result;
        Iterable<Role> roles = roleService.list();
        result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
        List<RoleDTO> roleDTOs = new LinkedList<>();
        for (Role role : roles) {
            roleDTOs.add(conversionService.convert(role, RoleDTO.class));
        }
        result.setResult(roleDTOs);
        return result;
    }


}
