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
 * Created: October 03, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.controller;

import static com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult.ErrorCode.NO_ERROR;

import com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult;
import com.odysseusinc.arachne.commons.utils.ConverterUtils;
import com.odysseusinc.arachne.commons.utils.UserIdUtils;
import com.odysseusinc.arachne.portal.api.v1.dto.AdminUserDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.ArachneConsts;
import com.odysseusinc.arachne.portal.api.v1.dto.BatchOperationDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.BulkUsersRegistrationDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UserWithTenantsDTO;
import com.odysseusinc.arachne.portal.exception.EmailNotUniqueException;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.exception.UserNotFoundException;
import com.odysseusinc.arachne.portal.model.AbstractUserStudyListItem;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.Paper;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.model.search.PaperSearch;
import com.odysseusinc.arachne.portal.model.search.StudySearch;
import com.odysseusinc.arachne.portal.model.search.UserSearch;
import com.odysseusinc.arachne.portal.model.security.Tenant;
import com.odysseusinc.arachne.portal.service.BaseAdminService;
import com.odysseusinc.arachne.portal.service.BaseDataSourceService;
import com.odysseusinc.arachne.portal.service.BasePaperService;
import com.odysseusinc.arachne.portal.service.BaseStudyService;
import com.odysseusinc.arachne.portal.service.BaseTenantService;
import com.odysseusinc.arachne.portal.service.ProfessionalTypeService;
import com.odysseusinc.arachne.portal.service.analysis.BaseAnalysisService;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Secured("ROLE_ADMIN")
public abstract class BaseAdminController<
        U extends IUser,
        S extends Study,
        DS extends IDataSource,
        SS extends StudySearch,
        SU extends AbstractUserStudyListItem,
        A extends Analysis,
        P extends Paper,
        PS extends PaperSearch,
        SB extends Submission> extends BaseController<DataNode, U> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminController.class);

    private final BaseDataSourceService<DS> dataSourceService;
    protected final ProfessionalTypeService professionalTypeService;
    private final BaseAdminService<S, DS, SS, SU, A, P, PS, SB> adminService;
    private final BaseStudyService<S, DS, SS, SU> studyService;
    private final BaseAnalysisService<A> analysisService;
    private final BasePaperService<P, PS, S, DS, SS, SU> paperService;
    private final BaseTenantService tenantService;
    private final ConverterUtils converterUtils;
    private final Validator validator;
    private final MessageSource messageSource;

    @Autowired
    public BaseAdminController(final BaseDataSourceService<DS> dataSourceService,
                               final ProfessionalTypeService professionalTypeService,
                               final BaseAdminService<S, DS, SS, SU, A, P, PS, SB> adminService,
                               final BaseStudyService<S, DS, SS, SU> studyService,
                               final BaseAnalysisService<A> analysisService, 
                               final BasePaperService<P, PS, S, DS, SS, SU> paperService,
                               final BaseTenantService tenantService,
                               final ConverterUtils converterUtils,
                               final Validator validator,
                               final MessageSource messageSource) {

        this.dataSourceService = dataSourceService;
        this.professionalTypeService = professionalTypeService;
        this.adminService = adminService;
        this.studyService = studyService;
        this.analysisService = analysisService;
        this.paperService = paperService;
        this.tenantService = tenantService;
        this.converterUtils = converterUtils;
        this.validator = validator;
        this.messageSource = messageSource;
    }

    @ApiOperation(value = "Enable user.", hidden = true)
    @RequestMapping(value = "/api/v1/admin/users/{userUuid}/enable/{isEnabled}", method = RequestMethod.POST)
    public JsonResult<Boolean> enableUser(@PathVariable("userUuid") String uuid,
                                          @PathVariable("isEnabled") Boolean isEnabled)
            throws
            PermissionDeniedException,
            UserNotFoundException,
            IllegalAccessException,
            SolrServerException,
            IOException,
            NotExistException,
            NoSuchFieldException {

        JsonResult<Boolean> result;
        U user = userService.getByIdInAnyTenant(UserIdUtils.uuidToId(uuid));
        user.setEnabled(isEnabled);
        userService.updateInAnyTenant(user);
        result = new JsonResult<>(NO_ERROR);
        result.setResult(isEnabled);
        return result;
    }

    @ApiOperation(value = "Delete study by id with all links", hidden = true)
    @RequestMapping(value = "/api/v1/admin/study", method = RequestMethod.DELETE)
    public void fullDeleteStudy(Long[] ids) {

        adminService.cascadeDeleteStudiesByIds(Arrays.asList(ids));
    }

    @ApiOperation(value = "Get all users.", hidden = true)
    @RequestMapping(value = "/api/v1/admin/users", method = RequestMethod.GET)
    public Page<UserWithTenantsDTO> getAll(
            @PageableDefault(page = 1)
            @SortDefault.SortDefaults({
                    @SortDefault(sort = "firstname", direction = Sort.Direction.ASC)
            })
                    Pageable pageable,
            UserSearch userSearch)
            throws UserNotFoundException {
      
        final Page<U> users = userService.getPage(pageable, userSearch);
        return users.map(user -> conversionService.convert(user, UserWithTenantsDTO.class));
    }

    @ApiOperation("Register new users")
    @RequestMapping(value = "/api/v1/admin/users/group", method = RequestMethod.POST)
    public void register(
            @RequestBody BulkUsersRegistrationDTO bulkUsersDto
    ) {

        bulkUsersDto.getUsers().forEach(u -> u.setPassword(bulkUsersDto.getPassword()));

        Set<ConstraintViolation<BulkUsersRegistrationDTO>> constraintViolations = validator.validate(bulkUsersDto);
        if (!constraintViolations.isEmpty()) {
            throw new ConstraintViolationException(constraintViolations);
        }

        Set<Tenant> tenants = new HashSet<>(tenantService.findByIdsIn(bulkUsersDto.getTenantIds()));
        List<U> users = converterUtils.convertList(bulkUsersDto.getUsers(), getUser());

        Map<String, String> emailValidationErrors = getEmailValidationErrors(users);
        if (!emailValidationErrors.isEmpty()) {
            String message = emailValidationErrors.entrySet().stream()
                    .map(entry -> entry.getKey() + " " + entry.getValue())
                    .collect(Collectors.joining("; "));
            throw new EmailNotUniqueException(message, emailValidationErrors);
        }

        userService.saveUsers(users, tenants, bulkUsersDto.getEmailConfirmationRequired());
    }

    protected abstract Class getUser();

    @ApiOperation(value = "Get user ids.", hidden = true)
    @RequestMapping(value = "/api/v1/admin/users/ids", method = RequestMethod.GET)
    public List<String> getListOfUserIdsByFilter(final UserSearch userSearch)
            throws UserNotFoundException {
  
        final List<U> users = userService.getList(userSearch);
        return users.stream().map(IUser::getId).map(UserIdUtils::idToUuid).collect(Collectors.toList());
    }

    @ApiOperation(value = "Get all users.", hidden = true)
    @RequestMapping(value = "/api/v1/admin/admins", method = RequestMethod.GET)
    public JsonResult<List<AdminUserDTO>> getAdmins(
            Principal principal,
            @RequestParam(name = "sortBy", required = false) String sortBy,
            @RequestParam(name = "sortAsc", required = false) Boolean sortAsc
    ) throws PermissionDeniedException {

        JsonResult<List<AdminUserDTO>> result;
        List<U> users = userService.getAllAdmins(sortBy, sortAsc);
        List<AdminUserDTO> dtos = users.stream()
                .map(user -> conversionService.convert(user, AdminUserDTO.class))
                .collect(Collectors.toList());
        result = new JsonResult<>(NO_ERROR);
        result.setResult(dtos);
        return result;
    }

    @RequestMapping(value = "/api/v1/admin/admins/{id}", method = RequestMethod.POST)
    public JsonResult addAdminRole(@PathVariable Long id) {

        userService.addUserToAdmins(id);
        return new JsonResult<>(NO_ERROR);
    }

    @ApiOperation("Suggests user according to query to add admin role.")
    @RequestMapping(value = "/api/v1/admin/admins/suggest", method = RequestMethod.GET)
    public JsonResult<List<AdminUserDTO>> suggestUsers(
            @RequestParam("query") String query,
            @RequestParam(value = "limit", required = false) Integer limit
    ) {

        JsonResult<List<AdminUserDTO>> result;
        List<U> users = userService.suggestNotAdmin(query, limit == null ? 10 : limit);
        List<AdminUserDTO> userDTOs = users.stream()
                .map(user -> conversionService
                        .convert(user, AdminUserDTO.class))
                .collect(Collectors.toList());
        result = new JsonResult<>(NO_ERROR);
        result.setResult(userDTOs);
        return result;
    }

    @RequestMapping(value = "/api/v1/admin/admins/{id}", method = RequestMethod.DELETE)
    public JsonResult removeAdminRole(@PathVariable Long id) {

        userService.removeUserFromAdmins(id);
        return new JsonResult<>(NO_ERROR);
    }

    @RequestMapping(value = "/api/v1/admin/{domain}/reindex-solr", method = RequestMethod.POST)
    public JsonResult reindexSolr(@PathVariable("domain") final String domain)
            throws IllegalAccessException, NotExistException, NoSuchFieldException, SolrServerException, IOException {

        switch(domain) {
            case ArachneConsts.Domains.DATA_SOURCES:
                dataSourceService.indexAllBySolr();
                break;
            case ArachneConsts.Domains.USERS:
                userService.indexAllBySolr();
                break;
            case ArachneConsts.Domains.STUDIES:
                studyService.indexAllBySolr();
                break;
            case ArachneConsts.Domains.ANALYISES:
                analysisService.indexAllBySolr();
                break;
            case ArachneConsts.Domains.PAPERS:
                paperService.indexAllBySolr();
                break;
            default:
                throw new UnsupportedOperationException("Reindex isn't allowed for domain: " + domain);
        }

        return new JsonResult<>(NO_ERROR);
    }
    
    @RequestMapping(value = "/api/v1/admin/users/batch", method = RequestMethod.POST)
    public JsonResult doBatchOperation(@RequestBody BatchOperationDTO dto) {

        userService.performBatchOperation(dto.getIds(), dto.getType());
        return new JsonResult<>(NO_ERROR);
    }

    private Map<String, String> getEmailValidationErrors(List<U> users) {

        List<U> persistentUsers = userService.findUsersInAnyTenantByEmailIn(users.stream()
                .map(user -> user.getEmail())
                .collect(Collectors.toList()));
        Map<String, U> mailUserMap = persistentUsers.stream()
                .collect(Collectors.toMap(U::getEmail, Function.identity()));
        Map<String, String> emailValidationErrors = new HashMap<>();
        for (int i = 0; i < users.size(); i++) {
            if (!Objects.isNull(mailUserMap.get(users.get(i).getEmail()))) {
                emailValidationErrors.put("users[" + i + "].email", messageSource.getMessage("validation.email.already.used", null, null));
            }
        }

        return emailValidationErrors;
    }
}
