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
 * Created: September 04, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.controller;

import com.google.common.base.Strings;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonDataNodeCreationResponseDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonDataNodeDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonDataNodeRegisterDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonDataSourceDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.OrganizationDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult;
import com.odysseusinc.arachne.portal.exception.AlreadyExistException;
import com.odysseusinc.arachne.portal.exception.FieldException;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.model.DataSource;
import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.Organization;
import com.odysseusinc.arachne.portal.service.BaseDataNodeService;
import com.odysseusinc.arachne.portal.service.BaseDataSourceService;
import com.odysseusinc.arachne.portal.service.BaseUserService;
import com.odysseusinc.arachne.portal.service.OrganizationService;
import com.odysseusinc.arachne.portal.service.StudyDataSourceService;
import com.odysseusinc.arachne.portal.service.analysis.BaseAnalysisService;
import com.odysseusinc.arachne.portal.util.ArachneConverterUtils;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@SuppressWarnings("unused")
public abstract class BaseDataNodeController<
        DS extends IDataSource,
        C_DS_DTO extends CommonDataSourceDTO,
        DN extends DataNode> extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(BaseDataNodeController.class);
    protected final BaseAnalysisService analysisService;
    protected final BaseDataSourceService<DS> dataSourceService;
    protected final BaseDataNodeService<DN> baseDataNodeService;
    protected final GenericConversionService genericConversionService;
    protected final BaseUserService userService;
    protected final StudyDataSourceService studyDataSourceService;
    protected final ArachneConverterUtils converterUtils;
    protected final OrganizationService organizationService;

    @Autowired
    public BaseDataNodeController(BaseAnalysisService analysisService,
                                  BaseDataNodeService<DN> dataNodeService,
                                  BaseDataSourceService<DS> dataSourceService,
                                  GenericConversionService genericConversionService,
                                  BaseUserService userService,
                                  StudyDataSourceService studyDataSourceService,
                                  ArachneConverterUtils converterUtils,
                                  OrganizationService organizationService) {

        this.analysisService = analysisService;
        this.baseDataNodeService = dataNodeService;
        this.dataSourceService = dataSourceService;
        this.genericConversionService = genericConversionService;
        this.userService = userService;
        this.studyDataSourceService = studyDataSourceService;
        this.converterUtils = converterUtils;
        this.organizationService = organizationService;
    }

    @ApiOperation("Create new data node.")
    @RequestMapping(value = "/api/v1/data-nodes", method = RequestMethod.POST)
    public JsonResult<CommonDataNodeCreationResponseDTO> createDataNode(
            Principal principal
    ) throws PermissionDeniedException, AlreadyExistException {

        final IUser user = getUser(principal);
        final DN dataNode = buildEmptyDN();
        CommonDataNodeCreationResponseDTO responseDTO = createDataNode(dataNode, principal);
        final JsonResult<CommonDataNodeCreationResponseDTO> result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
        result.setResult(responseDTO);
        return result;
    }

    @ApiOperation("Create new manual data node.")
    @RequestMapping(value = "/api/v1/data-nodes/manual", method = RequestMethod.POST)
    public CommonDataNodeCreationResponseDTO createManualDataNode(
            @RequestBody @Valid CommonDataNodeRegisterDTO commonDataNodeRegisterDTO,
            Principal principal
    ) throws PermissionDeniedException, AlreadyExistException, ValidationException, BindException {

        validateOrganization(commonDataNodeRegisterDTO.getOrganization());
        commonDataNodeRegisterDTO.setId(null);
        final DN dataNode = conversionService.convert(commonDataNodeRegisterDTO, getDataNodeDNClass());
        final Organization organization = conversionService.convert(commonDataNodeRegisterDTO.getOrganization(), Organization.class);
        dataNode.setOrganization(organizationService.getOrCreate(organization));
        return createDataNode(dataNode, principal);
    }

    private CommonDataNodeCreationResponseDTO createDataNode(DN dataNode, Principal principal)
            throws PermissionDeniedException, AlreadyExistException {

        final IUser user = getUser(principal);
        final DN registeredDataNode = baseDataNodeService.create(dataNode);
        baseDataNodeService.linkUserToDataNodeUnsafe(registeredDataNode, user);
        return conversionService.convert(registeredDataNode, CommonDataNodeCreationResponseDTO.class);
    }

    protected abstract Class<DN> getDataNodeDNClass();

    protected abstract DN buildEmptyDN();

    protected DataNode buildEmptyDataNode() {

        DataNode dataNode = new DataNode();
        dataNode.setVirtual(false);
        dataNode.setName(null);
        dataNode.setPublished(false);
        return dataNode;
    }

    @ApiOperation("Update data node info")
    @RequestMapping(value = "/api/v1/data-nodes/{dataNodeId}", method = RequestMethod.PUT)
    public JsonResult<CommonDataNodeDTO> updateDataNode(
            @PathVariable("dataNodeId") Long dataNodeId,
            @RequestBody @Valid CommonDataNodeRegisterDTO commonDataNodeRegisterDTO,
            Principal principal
    ) throws PermissionDeniedException, NotExistException, AlreadyExistException, BindException, ValidationException {

        validateOrganization(commonDataNodeRegisterDTO.getOrganization());

        final IUser user = getUser(principal);
        final DN dataNode = conversionService.convert(commonDataNodeRegisterDTO, getDataNodeDNClass());
        dataNode.setId(dataNodeId);
        final Organization organization = conversionService.convert(commonDataNodeRegisterDTO.getOrganization(), Organization.class);
        dataNode.setOrganization(organizationService.getOrCreate(organization));
        final DN updatedDataNode = baseDataNodeService.update(dataNode);
        final CommonDataNodeDTO dataNodeRegisterResponseDTO
                = conversionService.convert(updatedDataNode, CommonDataNodeDTO.class);
        final JsonResult<CommonDataNodeDTO> result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
        result.setResult(dataNodeRegisterResponseDTO);
        return result;
    }

    private void validateOrganization(final OrganizationDTO organizationDTO) throws BindException {

        BindException bindException = new BindException("organization", "not null");
        if (Objects.isNull(organizationDTO.getId()) && Objects.isNull(organizationDTO.getName()) ) {
            bindException.addError(new FieldError("organization", "organization", "May not be empty"));
        }
        if (bindException.hasErrors()) {
            throw bindException;
        }
    }

    @ApiOperation("Create new data source of datanode.")
    @RequestMapping(value = "/api/v1/data-nodes/{dataNodeId}/data-sources", method = RequestMethod.POST)
    public JsonResult createDataSource(@PathVariable("dataNodeId") Long id,
                                       @RequestBody C_DS_DTO commonDataSourceDTO
    ) throws FieldException,
            NotExistException,
            ValidationException,
            IOException,
            SolrServerException, NoSuchFieldException, IllegalAccessException, BindException {

        // we validate only two fields, because we don't want to validate another fields, because they always are null
        validate(commonDataSourceDTO);

        JsonResult<CommonDataSourceDTO> result;
        DataNode dataNode = baseDataNodeService.getById(id);

        if (dataNode == null) {
            throw new IllegalArgumentException("Unable to find datanode by ID " + id);
        }

        DS dataSource = convertCommonDataSourceDtoToDataSource(commonDataSourceDTO);
        dataSource.setDataNode(dataNode);
        dataSource = dataSourceService.createOrRestoreDataSource(dataSource);
        result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
        result.setResult(genericConversionService.convert(dataSource, CommonDataSourceDTO.class));
        return result;
    }

    private void validate(final C_DS_DTO commonDataSourceDTO) throws BindException {

        BindException bindException = new BindException("ds", "not null");

        if (Objects.isNull(commonDataSourceDTO.getDbmsType())) {
            bindException.addError(new FieldError("ds", "dbmsType", "May not be empty"));
        }
        if (Strings.isNullOrEmpty(commonDataSourceDTO.getName())) {
            bindException.addError(new FieldError("ds", "name", "May not be empty"));
        }
        if (bindException.hasErrors()) {
            throw bindException;
        }
    }

    @GetMapping(value = "/api/v1/data-nodes/{dataNodeId}/data-sources")
    public List<CommonDataSourceDTO> getDataSourcesForDataNode(@PathVariable("dataNodeId") Long dataNodeId) {

        DataNode dataNode = baseDataNodeService.getById(dataNodeId);
        List<DataSource> dataSources = dataNode.getDataSources().stream().filter(ds -> Boolean.TRUE.equals(ds.getPublished())).collect(Collectors.toList());
        return converterUtils.convertList(dataSources, CommonDataSourceDTO.class);
    }

    @RequestMapping(value = "/api/v1/data-nodes/{dataNodeId}", method = RequestMethod.GET)
    public JsonResult<CommonDataNodeDTO> getDataNode(@PathVariable("dataNodeId") Long dataNodeId) {

        DataNode dataNode = baseDataNodeService.getById(dataNodeId);
        return new JsonResult<>(JsonResult.ErrorCode.NO_ERROR, getDataNode(dataNode));
    }

    @RequestMapping(value = "/api/v1/data-nodes", method = RequestMethod.GET)
    public List<CommonDataNodeDTO> getDataNodes() {

        List<DN> dataNodes = baseDataNodeService.findAllIsNotVirtual();
        return converterUtils.convertList(dataNodes, CommonDataNodeDTO.class);
    }

    @RequestMapping(value = "/api/v1/data-nodes/suggest", method = RequestMethod.GET)
    public List<CommonDataNodeDTO> suggestDataNodes(Principal principal) throws PermissionDeniedException {

        IUser user = getUser(principal);
        List<DN> dataNodes = baseDataNodeService.suggestDataNode(user.getId());
        return converterUtils.convertList(dataNodes, CommonDataNodeDTO.class);
    }

    @RequestMapping(value = "/api/v1/data-nodes/byuuid/{dataNodeUuid}", method = RequestMethod.GET)
    public JsonResult<CommonDataNodeDTO> getDataNode(@PathVariable("dataNodeUuid") String dataNodeUuid) {

        DataNode dataNode = baseDataNodeService.getBySid(dataNodeUuid);
        return new JsonResult<>(JsonResult.ErrorCode.NO_ERROR, getDataNode(dataNode));
    }

    protected CommonDataNodeDTO getDataNode(DataNode dataNode) {

        if (dataNode == null) {
            throw new NotExistException(DataNode.class);
        }
        return conversionService.convert(dataNode, CommonDataNodeDTO.class);
    }

    protected abstract DS convertCommonDataSourceDtoToDataSource(C_DS_DTO commonDataSourceDTO);

    @ApiOperation("Unregister data source of datanode")
    @RequestMapping(value = "/api/v1/data-nodes/{dataNodeId}/data-sources/{dataSourceId}", method = RequestMethod.DELETE)
    public JsonResult unregisterDataSource(@PathVariable("dataNodeId") Long dataNodeId,
                                           @PathVariable("dataSourceId") Long dataSourceId)
            throws PermissionDeniedException, IOException, SolrServerException {

        final DS dataSource = dataSourceService.getNotDeletedByIdInAnyTenant(dataSourceId);
        studyDataSourceService.softDeletingDataSource(dataSource.getId());
        return new JsonResult(JsonResult.ErrorCode.NO_ERROR);
    }

}
