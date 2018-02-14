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
 * Created: September 04, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.controller;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonDataNodeCreationResponseDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonDataNodeDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonDataNodeRegisterDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonDataSourceDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult;
import com.odysseusinc.arachne.portal.exception.AlreadyExistException;
import com.odysseusinc.arachne.portal.exception.FieldException;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.model.DataNodeRole;
import com.odysseusinc.arachne.portal.model.DataSource;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.service.BaseDataNodeService;
import com.odysseusinc.arachne.portal.service.BaseDataSourceService;
import com.odysseusinc.arachne.portal.service.BaseUserService;
import com.odysseusinc.arachne.portal.service.StudyDataSourceService;
import com.odysseusinc.arachne.portal.service.analysis.BaseAnalysisService;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import javax.validation.Valid;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@SuppressWarnings("unused")
public abstract class BaseDataNodeController
        <DS extends DataSource,
                C_DS_DTO extends CommonDataSourceDTO,
                DN extends DataNode> extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(BaseDataNodeController.class);
    protected final BaseAnalysisService analysisService;
    protected final BaseDataSourceService<DS> dataSourceService;
    protected final BaseDataNodeService<DN> baseDataNodeService;
    protected final GenericConversionService genericConversionService;
    protected final BaseUserService userService;
    protected final StudyDataSourceService studyDataSourceService;

    @Autowired
    public BaseDataNodeController(BaseAnalysisService analysisService,
                                  BaseDataNodeService<DN> dataNodeService,
                                  BaseDataSourceService<DS> dataSourceService,
                                  GenericConversionService genericConversionService,
                                  BaseUserService userService,
                                  StudyDataSourceService studyDataSourceService) {

        this.analysisService = analysisService;
        this.baseDataNodeService = dataNodeService;
        this.dataSourceService = dataSourceService;
        this.genericConversionService = genericConversionService;
        this.userService = userService;
        this.studyDataSourceService = studyDataSourceService;
    }

    @ApiOperation("Create new data node.")
    @RequestMapping(value = "/api/v1/data-nodes", method = RequestMethod.POST)
    public JsonResult<CommonDataNodeCreationResponseDTO> createDataNode(
            Principal principal
    ) throws PermissionDeniedException, AlreadyExistException {

        final User user = getUser(principal);
        final DN dataNode = createEmptyDataNode();
        final DN registeredDataNode = baseDataNodeService.create(dataNode);
        baseDataNodeService.linkUserToDataNodeUnsafe(registeredDataNode, user, Collections.singleton(DataNodeRole.ADMIN));

        final CommonDataNodeCreationResponseDTO dataNodeRegisterResponseDTO
                = conversionService.convert(registeredDataNode, CommonDataNodeCreationResponseDTO.class);
        final JsonResult<CommonDataNodeCreationResponseDTO> result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
        result.setResult(dataNodeRegisterResponseDTO);
        return result;
    }

    protected abstract DN createEmptyDataNode();

    @ApiOperation("Update data node info")
    @RequestMapping(value = "/api/v1/data-nodes/{dataNodeId}", method = RequestMethod.PUT)
    public JsonResult<CommonDataNodeDTO> updateDataNode(
            @PathVariable("dataNodeId") Long dataNodeId,
            @RequestBody @Valid CommonDataNodeRegisterDTO commonDataNodeRegisterDTO,
            Principal principal
    ) throws PermissionDeniedException, NotExistException {

        final User user = getUser(principal);
        final DN dataNode = createEmptyDataNode();
        dataNode.setId(dataNodeId);
        dataNode.setName(commonDataNodeRegisterDTO.getName());
        dataNode.setDescription(commonDataNodeRegisterDTO.getDescription());
        final DN updatedDataNode = baseDataNodeService.update(dataNode);
        final CommonDataNodeDTO dataNodeRegisterResponseDTO
                = conversionService.convert(updatedDataNode, CommonDataNodeDTO.class);
        final JsonResult<CommonDataNodeDTO> result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
        result.setResult(dataNodeRegisterResponseDTO);
        return result;
    }

    @ApiOperation("Create new data source of datanode.")
    @RequestMapping(value = "/api/v1/data-nodes/{dataNodeId}/data-sources", method = RequestMethod.POST)
    public JsonResult createDataSource(@PathVariable("dataNodeId") Long id,
                                       @RequestBody C_DS_DTO commonDataSourceDTO
    ) throws FieldException,
            NotExistException,
            ValidationException,
            IOException,
            SolrServerException, NoSuchFieldException, IllegalAccessException {

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

    @RequestMapping(value = "/api/v1/data-nodes/{dataNodeId}", method = RequestMethod.GET)
    public JsonResult<CommonDataNodeDTO> getDataNode(@PathVariable("dataNodeId") Long dataNodeId) {

        DataNode dataNode = baseDataNodeService.getById(dataNodeId);
        return new JsonResult<>(JsonResult.ErrorCode.NO_ERROR, getDataNode(dataNode));
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
}
