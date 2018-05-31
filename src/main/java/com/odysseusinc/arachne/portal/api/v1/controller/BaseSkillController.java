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
 * Created: September 19, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.controller;

import com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult;
import com.odysseusinc.arachne.portal.api.v1.dto.dictionary.SkillDTO;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.NotUniqueException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.Skill;
import com.odysseusinc.arachne.portal.service.BaseSkillService;
import com.odysseusinc.arachne.portal.service.BaseUserService;
import io.swagger.annotations.ApiOperation;
import java.security.Principal;
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
import org.springframework.web.bind.annotation.RequestParam;

public abstract class BaseSkillController<S extends Skill, U extends IUser> {

    protected final BaseSkillService<S> skillService;
    protected final GenericConversionService conversionService;
    protected final BaseUserService<U, S> userService;

    public BaseSkillController(BaseSkillService<S> skillService, GenericConversionService conversionService, BaseUserService<U, S> userService) {

        this.skillService = skillService;
        this.conversionService = conversionService;
        this.userService = userService;
    }

    @ApiOperation("Register new skill.")
    @RequestMapping(value = "/api/v1/user-management/skills", method = RequestMethod.POST)
    public JsonResult<SkillDTO> create(@RequestBody @Valid SkillDTO skillDTO, BindingResult binding)
            throws NotExistException, NotUniqueException, PermissionDeniedException {

        JsonResult<SkillDTO> result;
        if (binding.hasErrors()) {
            result = new JsonResult<>(JsonResult.ErrorCode.VALIDATION_ERROR);
            for (FieldError fieldError : binding.getFieldErrors()) {
                result.getValidatorErrors().put(fieldError.getField(), fieldError.getDefaultMessage());
            }
        } else {
            S skill = convertDtoToSkill(skillDTO);
            skill = skillService.create(skill);
            result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
            result.setResult(conversionService.convert(skill, SkillDTO.class));
        }
        return result;
    }

    @ApiOperation("Get skill description.")
    @RequestMapping(value = "/api/v1/user-management/skills/{skillId}", method = RequestMethod.GET)
    public JsonResult<SkillDTO> get(@PathVariable("skillId") Long id) throws NotExistException {

        S skill = skillService.getById(id);
        JsonResult<SkillDTO> result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
        result.setResult(conversionService.convert(skill, SkillDTO.class));
        return result;
    }

    @ApiOperation(value = "Update skill description.", hidden = true)
    @RequestMapping(value = "/api/v1/admin/skills/{skillId}", method = RequestMethod.PUT)
    public JsonResult<SkillDTO> update(@PathVariable("skillId") Long id, @RequestBody @Valid SkillDTO skillDTO,
                                       BindingResult binding) throws NotExistException, NotUniqueException, ValidationException {

        JsonResult<SkillDTO> result;
        if (binding.hasErrors()) {
            result = new JsonResult<>(JsonResult.ErrorCode.VALIDATION_ERROR);
            for (FieldError fieldError : binding.getFieldErrors()) {
                result.getValidatorErrors().put(fieldError.getField(), fieldError.getDefaultMessage());
            }
        } else {
            skillDTO.setId(id);
            S skill = convertDtoToSkill(skillDTO);
            skill = skillService.update(skill);
            result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
            result.setResult(conversionService.convert(skill, SkillDTO.class));
        }
        return result;
    }

    protected abstract S convertDtoToSkill(SkillDTO skillDTO);

    @ApiOperation(value = "Delete skill.", hidden = true)
    @RequestMapping(value = "/api/v1/admin/skills/{skillId}", method = RequestMethod.DELETE)
    public JsonResult<Boolean> delete(@PathVariable("skillId") Long id) throws NotExistException {

        JsonResult<Boolean> result;
        if (id == null) {
            result = new JsonResult<>(JsonResult.ErrorCode.VALIDATION_ERROR);
            result.getValidatorErrors().put("skillId", "cannot be null");
        } else {
            skillService.delete(id);
            result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
            result.setResult(Boolean.TRUE);
        }
        return result;
    }

    @ApiOperation("List of skills.")
    @RequestMapping(value = "/api/v1/user-management/skills", method = RequestMethod.GET)
    public JsonResult<List<SkillDTO>> list(Principal principal) {

        Long userId = userService.getByEmail(principal.getName()).getId();
        JsonResult<List<SkillDTO>> result;
        List<S> skills = skillService.getAllExpectOfUserSkills(userId);
        result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
        List<SkillDTO> skillDTOs = new LinkedList<>();
        skills.forEach(skill -> skillDTOs.add(conversionService.convert(skill, SkillDTO.class)));
        result.setResult(skillDTOs);
        return result;
    }

    @ApiOperation("Suggest skills.")
    @RequestMapping(value = "/api/v1/user-management/skills/search", method = RequestMethod.GET)
    public JsonResult<List<SkillDTO>> suggest(
            @RequestParam("query") String query,
            @RequestParam("limit") Integer limit
    ) {

        JsonResult<List<SkillDTO>> result;
        Iterable<S> skills = skillService.suggestSkill(query, limit);
        result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
        List<SkillDTO> skillDTOs = new LinkedList<>();
        for (S skill : skills) {
            skillDTOs.add(conversionService.convert(skill, SkillDTO.class));
        }
        result.setResult(skillDTOs);
        return result;
    }

}
