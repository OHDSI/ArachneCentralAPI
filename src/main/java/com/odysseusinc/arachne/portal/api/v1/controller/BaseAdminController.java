/*
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
 * Created: October 03, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.controller;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonUserDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult;
import com.odysseusinc.arachne.commons.utils.UserIdUtils;
import com.odysseusinc.arachne.portal.api.v1.dto.AdminUserDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.ArachneConsts;
import com.odysseusinc.arachne.portal.api.v1.dto.BulkUsersRegistrationDTO;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.PasswordValidationException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.exception.UserNotFoundException;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.model.AbstractUserStudyListItem;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.Paper;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.model.UserOrigin;
import com.odysseusinc.arachne.portal.model.search.PaperSearch;
import com.odysseusinc.arachne.portal.model.search.StudySearch;
import com.odysseusinc.arachne.portal.model.search.UserSearch;
import com.odysseusinc.arachne.portal.model.security.Tenant;
import com.odysseusinc.arachne.portal.service.BaseAdminService;
import com.odysseusinc.arachne.portal.service.BaseDataSourceService;
import com.odysseusinc.arachne.portal.service.BasePaperService;
import com.odysseusinc.arachne.portal.service.BaseStudyService;
import com.odysseusinc.arachne.portal.service.ProfessionalTypeService;
import com.odysseusinc.arachne.portal.service.analysis.BaseAnalysisService;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final BaseDataSourceService<DS> dataSourceService;
    protected final ProfessionalTypeService professionalTypeService;
    private final BaseAdminService<S, DS, SS, SU, A, P, PS, SB> adminService;
    private final BaseStudyService<S, DS, SS, SU> studyService;
    private final BaseAnalysisService<A> analysisService;
    private final BasePaperService<P, PS, S, DS, SS, SU> paperService;

    @Autowired
    public BaseAdminController(final BaseDataSourceService<DS> dataSourceService,
                               final ProfessionalTypeService professionalTypeService,
                               final BaseAdminService<S, DS, SS, SU, A, P, PS, SB> adminService,
                               final BaseStudyService<S, DS, SS, SU> studyService,
                               final BaseAnalysisService<A> analysisService, 
                               final BasePaperService<P, PS, S, DS, SS, SU> paperService) {

        this.dataSourceService = dataSourceService;
        this.professionalTypeService = professionalTypeService;
        this.adminService = adminService;
        this.studyService = studyService;
        this.analysisService = analysisService;
        this.paperService = paperService;
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
        result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
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
    public Page<CommonUserDTO> getAll(
            @PageableDefault(page = 1)
            @SortDefault.SortDefaults({
                    @SortDefault(sort = "name", direction = Sort.Direction.ASC)
            })
                    Pageable pageable,
            UserSearch userSearch)
            throws UserNotFoundException {

        Page<U> users = userService.getAll(pageable, userSearch);
        return users.map(user -> conversionService.convert(user, CommonUserDTO.class));
    }

    @ApiOperation("Register new users")
    @RequestMapping(value = "/api/v1/admin/users/group", method = RequestMethod.POST)
    public void register(@RequestBody BulkUsersRegistrationDTO bulkUsersDto) throws PasswordValidationException, ValidationException {

        if (bulkUsersDto.getTenantIds() == null || bulkUsersDto.getTenantIds().isEmpty()) {
            throw new ValidationException("tenants: must be not empty");
        }

        boolean emailConfirmationRequired = bulkUsersDto.getEmailConfirmationRequired();

        List<U> users = convert(bulkUsersDto.getUsers());
        Set<Tenant> tenants = convertToTenants(bulkUsersDto.getTenantIds());
        updateFields(users, tenants, emailConfirmationRequired, bulkUsersDto.getPassword());

        List<U> createdUsers = userService.createAll(users);

        if (emailConfirmationRequired) {
            for (U user : createdUsers) {
                bulkUsersDto.getUsers().stream()
                        .filter(userDto -> userDto.getEmail().equals(user.getEmail()))
                        .forEach(userDto ->
                                userService.sendRegistrationEmail(user, userDto.getRegistrantToken(), userDto.getCallbackUrl(), true)
                );
            }
        }
    }

    private void updateFields(List<U> users, Set<Tenant> tenants, boolean emailConfirmationRequired, String password) {
        for (U user : users) {
            user.setPassword(password);
            user.setTenants(tenants);
            user.setOrigin(UserOrigin.NATIVE);
            if (!emailConfirmationRequired) {
                user.setEmailConfirmed(true);
            } else {
                user.setEmailConfirmed(false);
                user.setRegistrationCode(UUID.randomUUID().toString());
            }
        }
    }

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
        result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
        result.setResult(dtos);
        return result;
    }

    @RequestMapping(value = "/api/v1/admin/admins/{id}", method = RequestMethod.POST)
    public JsonResult addAdminRole(@PathVariable Long id) {

        userService.addUserToAdmins(id);
        return new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
    }

    @ApiOperation("Suggests user according to query to add admin role.")
    @RequestMapping(value = "/api/v1/admin/admins/suggest", method = RequestMethod.GET)
    public JsonResult<List<AdminUserDTO>> suggestUsers(
            @RequestParam("query") String query,
            @RequestParam(value = "limit", required = false) Integer limit
    ) {

        JsonResult<List<AdminUserDTO>> result;
        List<U> users = userService.suggestNotAdmin(query, limit == null ? 10 : limit);
        List<AdminUserDTO> userDTOs = new LinkedList<>();
        for (U user : users) {
            userDTOs.add(conversionService.convert(user, AdminUserDTO.class));
        }
        result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
        result.setResult(userDTOs);
        return result;
    }

    @RequestMapping(value = "/api/v1/admin/admins/{id}", method = RequestMethod.DELETE)
    public JsonResult removeAdminRole(@PathVariable Long id) {

        userService.removeUserFromAdmins(id);
        return new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
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

        return new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
    }
}
