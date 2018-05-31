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
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva, Konstantin Yaroshovets
 * Created: February 6, 2018
 *
 */

package com.odysseusinc.arachne.portal.api.v1.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import com.odysseusinc.arachne.commons.api.v1.dto.OrganizationDTO;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.model.Organization;
import com.odysseusinc.arachne.portal.service.OrganizationService;
import com.odysseusinc.arachne.portal.service.impl.OrganizationServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api
@RestController
public class OrganizationController extends BaseController {

    private OrganizationService organizationService;

    @Autowired
    public OrganizationController(OrganizationService organizationService) {

        this.organizationService = organizationService;
    }

    @ApiOperation("Create Organization")
    @RequestMapping(value = "/api/v1/user-management/organizations", produces = APPLICATION_JSON_UTF8_VALUE, method = POST)
    public OrganizationDTO create(@RequestBody OrganizationDTO organizationDTO) throws ValidationException {

        final Organization organization = conversionService.convert(organizationDTO, Organization.class);
        final Organization saved = organizationService.create(organization);
        return conversionService.convert(saved, OrganizationDTO.class);
    }

    @ApiOperation("Get Organization")
    @RequestMapping(value = "/api/v1/user-management/organizations/{id}", produces = APPLICATION_JSON_UTF8_VALUE, method = GET)
    public OrganizationDTO get(@PathVariable Long id) {

        final Organization organization = organizationService.get(id);
        return conversionService.convert(organization, OrganizationDTO.class);
    }

    @ApiOperation("Update Organization")
    @RequestMapping(value = "/api/v1/user-management/organizations/{id}", produces = APPLICATION_JSON_UTF8_VALUE, consumes = APPLICATION_JSON_UTF8_VALUE, method = PUT)
    public OrganizationDTO update(@PathVariable Long id, @RequestBody OrganizationDTO organizationDTO) {

        organizationDTO.setId(id);
        final Organization organization = conversionService.convert(organizationDTO, Organization.class);
        final Organization updated = organizationService.update(organization);
        return conversionService.convert(updated, OrganizationDTO.class);
    }

    @ApiOperation("Delete Organization")
    @RequestMapping(value = "/api/v1/user-management/organizations/{id}", method = DELETE)
    public void delete(@PathVariable Long id) {

        organizationService.delete(id);
    }

    @ApiOperation("Suggest Organizations")
    @RequestMapping(value = "/api/v1/user-management/organizations", produces = APPLICATION_JSON_UTF8_VALUE, method = GET)
    public List<OrganizationDTO> suggest(@RequestParam(value = "query", required = false, defaultValue = "") String query,
                                         @RequestParam(value = "limit", required = false, defaultValue = "10") Integer limit) {

        final List<Organization> organizations = organizationService.suggest(query, limit);
        return organizations.stream()
                .map(o -> conversionService.convert(o, OrganizationDTO.class))
                .collect(Collectors.toList());
    }
}
