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
 * Created: September 07, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.controller;

import static com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult.ErrorCode.NO_ERROR;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonBaseDataSourceDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonCDMVersionDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonDataSourceDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.OptionDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult;
import com.odysseusinc.arachne.commons.types.DBMSType;
import com.odysseusinc.arachne.portal.api.v1.dto.FacetedSearchResultDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.IDataSourceDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.PageDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.SearchDataCatalogDTO;
import com.odysseusinc.arachne.portal.exception.FieldException;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.model.DataSourceFields;
import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.service.BaseDataSourceService;
import com.odysseusinc.arachne.portal.service.StudyDataSourceService;
import com.odysseusinc.arachne.portal.service.impl.solr.SearchResult;
import com.odysseusinc.arachne.portal.util.ArachneConverterUtils;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import javax.validation.Valid;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

public abstract class BaseDataSourceController<
        DS extends IDataSource,
        DTO extends CommonBaseDataSourceDTO,
        DS_DTO extends IDataSourceDTO,
        R extends FacetedSearchResultDTO<?>> extends BaseController {

    protected final GenericConversionService conversionService;
    protected final BaseDataSourceService<DS> dataSourceService;
    protected final ArachneConverterUtils converterUtils;
    protected final StudyDataSourceService studyDataSourceService;


    public BaseDataSourceController(GenericConversionService conversionService,
                                    BaseDataSourceService<DS> dataSourceService,
                                    ArachneConverterUtils converterUtils,
                                    StudyDataSourceService studyDataSourceService) {

        this.conversionService = conversionService;
        this.dataSourceService = dataSourceService;
        this.converterUtils = converterUtils;
        this.studyDataSourceService = studyDataSourceService;
    }

    @RequestMapping(value = "/api/v1/data-sources/{id}", method = RequestMethod.PUT)
    public JsonResult<DTO> update(
            Principal principal,
            @PathVariable("id") Long dataSourceId,
            @RequestBody @Valid DTO commonDataSourceDTO,
            BindingResult bindingResult
    ) throws NotExistException,
            PermissionDeniedException,
            FieldException,
            ValidationException,
            IOException,
            SolrServerException,
            NoSuchFieldException,
            IllegalAccessException {

        return updateDataSource(principal, dataSourceId, commonDataSourceDTO, bindingResult);
    }

    private JsonResult<DTO> updateDataSource(Principal principal, Long dataSourceId,
                                             DTO commonDataSourceDTO, BindingResult bindingResult)
            throws PermissionDeniedException, IllegalAccessException, IOException, NoSuchFieldException,
            SolrServerException, ValidationException {

        JsonResult result;
        if (bindingResult.hasErrors()) {
            result = setValidationErrors(bindingResult);
        } else {
            IUser user = getUser(principal);
            final DS exist = dataSourceService.getNotDeletedByIdInAnyTenant(dataSourceId);
            DS dataSource = convertDTOToDataSource(commonDataSourceDTO);
            dataSource.setId(dataSourceId);
            dataSource.setDataNode(exist.getDataNode());
            dataSource.setPublished(true);
            dataSource = dataSourceService.updateInAnyTenant(dataSource);

            result = new JsonResult<>(NO_ERROR);
            result.setResult(convertDataSourceToDTO(dataSource));
        }
        return result;
    }

    @RequestMapping(value = "/api/v1/data-sources/{id}/from-node", method = RequestMethod.PUT)
    public JsonResult<DTO> updateFieldsDefinedAtNode(
            @PathVariable("id") Long dataSourceId,
            @RequestBody DTO commonDataSourceDTO
    ) throws IllegalAccessException, IOException, NoSuchFieldException, SolrServerException, ValidationException {

        JsonResult<DTO> result = new JsonResult<>(NO_ERROR);
        DS updating = convertDTOToDataSource(commonDataSourceDTO);
        updating.setId(dataSourceId);
        updating.setDataNode(dataSourceService.getByIdUnsecured(dataSourceId).getDataNode());
        updating = dataSourceService.updateWithoutMetadataInAnyTenant(updating);
        result.setResult(convertDataSourceToDTO(updating));
        return result;
    }

    @RequestMapping(value = "/api/v1/data-sources/search-data-source", method = RequestMethod.GET)
    public JsonResult<Page<DS_DTO>> suggestDataSource(Principal principal,
                                                      @RequestParam("studyId") Long studyId,
                                                      @RequestParam("query") String query,
                                                      @ModelAttribute PageDTO pageDTO
    ) throws PermissionDeniedException {

        if (studyId == null || query == null) {
            throw new javax.validation.ValidationException();
        }
        final IUser user = getUser(principal);
        PageRequest pageRequest = dataSourceService.getPageRequest(pageDTO);
        Page<DS> dataSources = dataSourceService.suggestDataSource(query, studyId, user.getId(), pageRequest);
        List<DS_DTO> dataSourceDTOs = converterUtils.convertList(dataSources.getContent(), getDataSourceDTOClass());
        CustomPageImpl<DS_DTO> resultPage =
                new CustomPageImpl<>(dataSourceDTOs, pageRequest, dataSources.getTotalElements());

        return new JsonResult<>(NO_ERROR, resultPage);
    }

    protected abstract Class<DS_DTO> getDataSourceDTOClass();

    @RequestMapping(value = "/api/v1/data-sources", method = RequestMethod.GET)
    public JsonResult<R> list(Principal principal,
                              @ModelAttribute SearchDataCatalogDTO searchDTO
    ) throws IOException, SolrServerException, PermissionDeniedException, NoSuchFieldException {

        final IUser user = getUser(principal);
        SolrQuery solrQuery = conversionService.convert(searchDTO, SolrQuery.class);
        SearchResult<DS> searchResult = dataSourceService.search(solrQuery, user);
        return new JsonResult<>(NO_ERROR, conversionService.convert(searchResult, getSearchResultClass()));
    }

    @RequestMapping(value = "/api/v1/data-sources/my", method = RequestMethod.GET)
    public Page<DS_DTO> getUserDataSources(Principal principal,
                                           @RequestParam(name = "query", required = false, defaultValue = "") String query,
                                           @RequestParam(name = "sort", required = false, defaultValue = DataSourceFields.UI_NAME) String sortBy,
                                           @RequestParam(name = "order", required = false, defaultValue = "asc") String order,
                                           @ModelAttribute PageDTO pageDTO
    ) throws PermissionDeniedException {

        final IUser user = getUser(principal);
        PageRequest pageRequest = dataSourceService.getPageRequest(pageDTO, sortBy, order);
        Page<DS> dataSources = dataSourceService.getUserDataSources(query, user.getId(), pageRequest);
        List<DS_DTO> dataSourceDTOs = converterUtils.convertList(dataSources.getContent(), getDataSourceDTOClass());
        return new CustomPageImpl<>(dataSourceDTOs, pageRequest, dataSources.getTotalElements());
    }

    @RequestMapping(value = "/api/v1/data-sources/byuuid/{uuid}", method = RequestMethod.GET)
    public JsonResult<DTO> getByUuid(@PathVariable("uuid") String dataSourceUuid) throws NotExistException {

        JsonResult<DTO> result = new JsonResult<>(NO_ERROR);
        DS dataSource = dataSourceService.findByUuidUnsecured(dataSourceUuid);
        result.setResult(convertDataSourceToDTO(dataSource));
        return result;
    }

    @RequestMapping(value = "/api/v1/data-sources/{id}", method = RequestMethod.GET)
    public JsonResult<DTO> get(@PathVariable("id") Long dataSourceId) throws NotExistException {

        JsonResult<DTO> result = new JsonResult<>(NO_ERROR);
        DS dataSource = dataSourceService.getNotDeletedById(dataSourceId);
        result.setResult(convertDataSourceToDTO(dataSource));
        return result;
    }

    @RequestMapping(value = "/api/v1/data-sources/commondata", method = RequestMethod.GET)
    public JsonResult<List<CommonDataSourceDTO>> getCommonBaseDataSourceDTOs(
            @RequestParam("id") List<Long> dataSourceIds) throws NotExistException {

        JsonResult<List<CommonDataSourceDTO>> result = new JsonResult<>(NO_ERROR);
        List<DS> dataSources = dataSourceService.findByIdsAndNotDeleted(dataSourceIds);
        result.setResult(this.converterUtils.convertList(dataSources, CommonDataSourceDTO.class));
        return result;
    }

    @ApiOperation("Unpublish and delete data source")
    @RequestMapping(value = "/api/v1/data-sources/{id}", method = RequestMethod.DELETE)
    public JsonResult deleteDataSource(@PathVariable("id") Long dataSourceId) throws IOException, SolrServerException {

        final DS dataSource = dataSourceService.getNotDeletedByIdInAnyTenant(dataSourceId);
        dataSourceService.unpublish(dataSourceId);

        studyDataSourceService.softDeletingDataSource(dataSource.getId());
        return new JsonResult(JsonResult.ErrorCode.NO_ERROR);
    }

    @ApiOperation("Unpublish data source")
    @RequestMapping(value = "/api/v1/data-sources/{id}/registration", method = RequestMethod.DELETE)
    public void unpublishDataSource(@PathVariable("id") Long dataSourceId) throws IOException, SolrServerException {

        dataSourceService.unpublish(dataSourceId);
    }

    @ApiOperation("Publish data source")
    @RequestMapping(value = "/api/v1/data-sources/{id}/registration", method = RequestMethod.POST)
    public JsonResult<DTO> publishDataSource(Principal principal,
                                             @PathVariable("id") Long dataSourceId,
                                             @RequestBody @Valid DTO commonDataSourceDTO,
                                             BindingResult bindingResult
    ) throws NotExistException,
            PermissionDeniedException,
            FieldException,
            ValidationException,
            IOException,
            SolrServerException,
            NoSuchFieldException,
            IllegalAccessException {

        return updateDataSource(principal, dataSourceId, commonDataSourceDTO, bindingResult);
    }

    @RequestMapping(value = "/api/v1/data-sources/cdm-versions", method = RequestMethod.GET)
    public JsonResult<List<CommonCDMVersionDTO>> getCDMVersions() {

        JsonResult<List<CommonCDMVersionDTO>> result = new JsonResult<>(NO_ERROR);
        result.setResult(Arrays.asList(CommonCDMVersionDTO.values()));
        return result;
    }

    protected abstract Class<R> getSearchResultClass();

    protected abstract Class<DTO> getDTOClass();

    protected abstract DTO convertDataSourceToDTO(DS dataSource);

    protected abstract DS convertDTOToDataSource(DTO dto);

    @RequestMapping(value = "/api/v1/data-sources/{id}/complete", method = RequestMethod.GET)
    public JsonResult<DS_DTO> getWhole(@PathVariable("id") Long dataSourceId) throws NotExistException {

        DS dataSource = dataSourceService.getNotDeletedByIdInAnyTenant(dataSourceId);
        return new JsonResult<>(NO_ERROR, conversionService.convert(dataSource, getDataSourceDTOClass()));
    }

    @RequestMapping(value = "/api/v1/data-sources/dbms-types", method = RequestMethod.GET)
    public List<OptionDTO> getDBMSTypes() {

        return converterUtils.convertList(Arrays.asList(DBMSType.values()), OptionDTO.class);
    }

}
