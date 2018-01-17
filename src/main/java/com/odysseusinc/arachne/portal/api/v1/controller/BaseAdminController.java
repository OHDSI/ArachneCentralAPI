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
import com.odysseusinc.arachne.portal.api.v1.dto.AdminUserDTO;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.exception.UserNotFoundException;
import com.odysseusinc.arachne.portal.model.AbstractUserStudyListItem;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.model.DataSource;
import com.odysseusinc.arachne.portal.model.Paper;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.model.search.PaperSearch;
import com.odysseusinc.arachne.portal.model.search.StudySearch;
import com.odysseusinc.arachne.portal.model.search.UserSearch;
import com.odysseusinc.arachne.portal.service.BaseAdminService;
import com.odysseusinc.arachne.portal.service.BaseDataSourceService;
import com.odysseusinc.arachne.portal.service.ProfessionalTypeService;
import io.swagger.annotations.ApiOperation;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


@Secured("ROLE_ADMIN")
public abstract class BaseAdminController<
        S extends Study,
        DS extends DataSource,
        SS extends StudySearch,
        SU extends AbstractUserStudyListItem,
        A extends Analysis,
        P extends Paper,
        PS extends PaperSearch,
        SB extends Submission> extends BaseController<DataNode> {

    private final BaseDataSourceService<DS> dataSourceService;
    protected final ProfessionalTypeService professionalTypeService;
    private final BaseAdminService<S, DS, SS, SU, A, P, PS, SB> adminService;

    @Autowired
    public BaseAdminController(BaseDataSourceService<DS> dataSourceService,
                               ProfessionalTypeService professionalTypeService,
                               BaseAdminService<S, DS, SS, SU, A, P, PS, SB> adminService) {

        this.dataSourceService = dataSourceService;
        this.professionalTypeService = professionalTypeService;
        this.adminService = adminService;
    }

    @ApiOperation(value = "Enable user.", hidden = true)
    @RequestMapping(value = "/api/v1/admin/users/{userId}/enable/{isEnabled}", method = RequestMethod.POST)
    public JsonResult<Boolean> enableUser(@PathVariable("userId") Long id,
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
        User user = userService.getByIdAndInitializeCollections(id);
        user.setEnabled(isEnabled);
        userService.update(user);
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
            throws PermissionDeniedException, UserNotFoundException {

        Pageable search = new PageRequest(pageable.getPageNumber() - 1, pageable.getPageSize(), pageable.getSort());
        Iterator<Sort.Order> pageIt = pageable.getSort().iterator();
        Stream<Sort.Order> pageStream = StreamSupport.stream(Spliterators.spliteratorUnknownSize(pageIt, Spliterator.ORDERED), false);
        if (pageStream.anyMatch(order -> order.getProperty().equals("name"))) {
            search = new PageRequest(pageable.getPageNumber() - 1, pageable.getPageSize(),
                    pageable.getSort().getOrderFor("name").getDirection(),
                    "firstname", "middlename", "lastname");
        }
        Page<User> users = userService.getAll(search, userSearch);
        return users
                .map(user -> conversionService.convert(user, CommonUserDTO.class));
    }


    @ApiOperation(value = "Get all users.", hidden = true)
    @RequestMapping(value = "/api/v1/admin/admins", method = RequestMethod.GET)
    public JsonResult<List<AdminUserDTO>> getAdmins(
            Principal principal,
            @RequestParam(name = "sortBy", required = false) String sortBy,
            @RequestParam(name = "sortAsc", required = false) Boolean sortAsc
    ) throws PermissionDeniedException {

        JsonResult<List<AdminUserDTO>> result;
        List<User> users = userService.getAllAdmins(sortBy, sortAsc);
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
        List<User> users = userService.suggestNotAdmin(query, limit == null ? 10 : limit);
        List<AdminUserDTO> userDTOs = new LinkedList<>();
        for (User user : users) {
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


    @RequestMapping(value = "/api/v1/admin/data-sources/reindex-solr", method = RequestMethod.POST)
    public JsonResult reindexDataSourcesBySolr()
            throws IllegalAccessException, NotExistException, NoSuchFieldException, SolrServerException, IOException {

        dataSourceService.indexAllBySolr();
        return new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
    }

    @RequestMapping(value = "/api/v1/admin/users/reindex-solr", method = RequestMethod.POST)
    public JsonResult reindexUsersBySolr()
            throws IllegalAccessException, NotExistException, NoSuchFieldException, SolrServerException, IOException {

        userService.indexAllBySolr();
        return new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
    }
}
