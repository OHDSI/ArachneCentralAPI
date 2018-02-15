/*
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
 * Authors: Anton Gackovka
 * Created: January 25, 2018
 */

package com.odysseusinc.arachne.portal.api.v1.controller;

import static com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult.ErrorCode.NO_ERROR;
import static com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult.ErrorCode.PERMISSION_DENIED;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult;
import com.odysseusinc.arachne.portal.api.v1.dto.GlobalSearchResultDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.StudyDTO;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.service.GlobalSearchService;
import com.odysseusinc.arachne.portal.service.UserService;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GlobalSearchController {

    @Autowired
    private GlobalSearchService searchService;

    @Autowired
    private UserService userService;

    @Autowired
    private ConversionService conversionService;

    @ApiOperation("Global search.")
    @RequestMapping(value = "/api/v1/search/{query}", method = GET)
    public JsonResult<GlobalSearchResultDTO> list(
            Principal principal,
            @PathVariable("query") String query)
            throws PermissionDeniedException, NotExistException, SolrServerException, NoSuchFieldException, IOException {

        final JsonResult<GlobalSearchResultDTO> result;

        final User user = userService.getByEmail(principal.getName());

        if (user == null) {
            result = new JsonResult<>(PERMISSION_DENIED);
            return result;
        }

        final GlobalSearchResultDTO globalSearchResult = searchService.search(user.getId(), query);

        result = new JsonResult<>(NO_ERROR, globalSearchResult);
        return result;
    }

}
