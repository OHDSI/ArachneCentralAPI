/*
 *
 * Copyright 2018 Odysseus Data Services, inc.
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
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva
 * Created: February 05, 2018
 *
 */

package com.odysseusinc.arachne.portal.api.v1.controller;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonUserDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult;
import com.odysseusinc.arachne.commons.types.SuggestionTarget;
import com.odysseusinc.arachne.portal.api.v1.dto.ExpertListSearchResultDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.SearchExpertListDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UserProfileDTO;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.Skill;
import com.odysseusinc.arachne.portal.service.BaseUserService;
import com.odysseusinc.arachne.portal.service.impl.solr.SearchResult;
import com.odysseusinc.arachne.portal.util.UserUtils;
import io.swagger.annotations.ApiOperation;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult.ErrorCode.NO_ERROR;

public abstract class BaseExpertFinderController<U extends IUser, SK extends Skill> extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(BaseExpertFinderController.class);

    private final BaseUserService<U, SK> baseUserService;

    public BaseExpertFinderController(BaseUserService<U, SK> userService) {

        this.baseUserService = userService;
    }

    @ApiOperation("Get expert list")
    @GetMapping(value = "/api/v1/user-management/users")
    public JsonResult<ExpertListSearchResultDTO> list(
            @ModelAttribute SearchExpertListDTO searchDTO
    ) throws IOException, SolrServerException, NoSuchFieldException {

        JsonResult result = new JsonResult<ExpertListSearchResultDTO>(NO_ERROR);

        SolrQuery solrQuery = conversionService.convert(searchDTO, SolrQuery.class);
        SearchResult searchResult = baseUserService.search(solrQuery);

        result.setResult(
                this.conversionService.convert(
                        searchResult,
                        ExpertListSearchResultDTO.class
                )
        );
        return result;
    }

    @ApiOperation("View user profile.")
    @GetMapping(value = "/api/v1/user-management/users/{userId}/profile")
    public JsonResult<UserProfileDTO> viewProfile(
            Authentication principal,
            @PathVariable("userId") String userId) {
        IUser loggedInUser = userService.getById(UserUtils.getCurrentUser(principal).getId());
        IUser user = userService.getByUuidAndInitializeCollections(userId);

        UserProfileDTO userProfileDTO = conversionService.convert(user, UserProfileDTO.class);
        userProfileDTO.setIsEditable(loggedInUser.getUuid().equals(userId));
        return new JsonResult<>(JsonResult.ErrorCode.NO_ERROR, userProfileDTO);
    }

    @ApiOperation("Get user by id")
    @GetMapping(value = "/api/v1/user-management/users/{id}")
    public JsonResult<CommonUserDTO> get(
            @PathVariable("id") Long id
    ) {

        return buildResponse(baseUserService.getByIdInAnyTenant(id));
    }

    @GetMapping(value = "/api/v1/user-management/users/byusername/{username:.+}")
    public JsonResult<CommonUserDTO> getByUsername(@PathVariable("username") String username) {

        return buildResponse(baseUserService.getByUsernameInAnyTenant(username));
    }

    private JsonResult<CommonUserDTO> buildResponse(U user) {

        CommonUserDTO userDTO = conversionService.convert(user, CommonUserDTO.class);
        JsonResult<CommonUserDTO> result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
        result.setResult(userDTO);
        return result;
    }

    @ApiOperation("Suggest user according to query")
    @GetMapping(value = "/api/v1/user-management/users/suggest")
    public List<CommonUserDTO> suggest(@RequestParam(value = "target") SuggestionTarget target,
                                       @RequestParam(value = "id", required = false) Long id,
                                       @RequestParam(value = "excludeEmails", required = false) List<String> excludeEmails,
                                       @RequestParam(value = "query") String query,
                                       @RequestParam(value = "limit", defaultValue = "10") Integer limit) {

        List<U> users;
        switch (target) {
            case STUDY: {
                if (id == null) {
                    throw new javax.validation.ValidationException("Id must be specified when SuggestionTarget=STUDY");
                }
                users = baseUserService.suggestUserToStudy(query, id, limit);
                break;
            }
            case PAPER: {
                if (id == null) {
                    throw new javax.validation.ValidationException("Id must be specified when SuggestionTarget=PAPER");
                }
                users = baseUserService.suggestUserToPaper(query, id, limit);
                break;
            }
            case DATANODE: {
                if (CollectionUtils.isEmpty(excludeEmails)) {
                    throw new javax.validation.ValidationException("Emails for excluding must be specified when SuggestionTarget=DATANODE");
                }
                users = baseUserService.suggestUserFromAnyTenant(query, excludeEmails, limit);
                break;
            }
            default: {
                throw new IllegalArgumentException("Target must be specified");
            }
        }
        return users.stream()
                .map(user -> conversionService.convert(user, CommonUserDTO.class))
                .collect(Collectors.toList());
    }

}
