/**
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
 * Created: September 17, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.impl;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonModelType;
import com.odysseusinc.arachne.portal.api.v1.dto.SearchDataCatalogDTO;
import com.odysseusinc.arachne.portal.exception.FieldException;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.model.DataSource;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.repository.BaseDataSourceRepository;
import com.odysseusinc.arachne.portal.service.BaseDataSourceService;
import com.odysseusinc.arachne.portal.service.BaseSolrService;
import com.odysseusinc.arachne.portal.service.impl.solr.FieldList;
import com.odysseusinc.arachne.portal.service.impl.solr.SearchResult;
import com.odysseusinc.arachne.portal.service.impl.solr.SolrField;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Transactional(rollbackFor = Exception.class)
public abstract class BaseDataSourceServiceImpl<DS extends DataSource, SF extends SolrField> implements BaseDataSourceService<DS> {
    private static final Logger log = LoggerFactory.getLogger(DataSourceServiceImpl.class);
    protected BaseDataSourceRepository<DS> dataSourceRepository;
    protected BaseSolrService<SF> solrService;
    protected GenericConversionService conversionService;

    public BaseDataSourceServiceImpl(BaseSolrService<SF> solrService,
                                     BaseDataSourceRepository<DS> dataSourceRepository,
                                     GenericConversionService conversionService) {

        this.solrService = solrService;
        this.dataSourceRepository = dataSourceRepository;
        this.conversionService = conversionService;
    }

    @Override
    @PreAuthorize("hasPermission(#dataSource, "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).CREATE_DATASOURCE)")
    @PostAuthorize("@ArachnePermissionEvaluator.addPermissions(principal, returnObject )")
    public DS createOrRestoreDataSource(DS dataSource)
            throws FieldException,
            NotExistException,
            ValidationException,
            IOException,
            SolrServerException, NoSuchFieldException, IllegalAccessException {

        final Boolean virtual = dataSource.getDataNode().getVirtual();
        beforeCreate(dataSource, virtual);

        final DS exist = dataSourceRepository.findByUuid(dataSource.getUuid());
        final boolean isRestore = exist != null && exist.getDeleted() != null
                && Objects.equals(exist.getDataNode().getId(), dataSource.getDataNode().getId());

        if (isRestore) {
            dataSource.setId(exist.getId());
        }

        if (!dataSource.getModelType().equals(CommonModelType.CDM)) {
            dataSource.setCdmVersion(null);
        }
        DS savedDataSource = dataSourceRepository.save(dataSource);
        afterCreate(savedDataSource, virtual);
        return savedDataSource;
    }

    protected void beforeCreate(DS dataSource, boolean virtual) {

        dataSource.setCreated(new Date());
    }

    protected void afterCreate(DS dataSource, boolean virtual)
            throws IllegalAccessException, NoSuchFieldException, SolrServerException, IOException {

        if (!virtual) {
            indexBySolr(dataSource);
        }
    }

    public SearchResult<DS> search(
            SolrQuery solrQuery
    ) throws IOException, SolrServerException {

        List<DS> dataSourceList;

        QueryResponse solrResponse = solrService.search(
                SolrServiceImpl.DATA_SOURCE_COLLECTION,
                solrQuery
        );

        List<Long> docIdList = solrResponse.getResults()
                .stream()
                .map(solrDoc -> Long.parseLong(solrDoc.get("id").toString()))
                .collect(Collectors.toList());

        // We need to repeat sorting, because repository doesn't prevent order of passed ids
        dataSourceList = dataSourceRepository.findByIdInAndDeletedIsNull(docIdList);
        dataSourceList.sort(Comparator.comparing(item -> docIdList.indexOf(item.getId())));

        return new SearchResult<>(solrQuery, solrResponse, dataSourceList);
    }

    @Override
    public SearchResult<DS> search(SolrQuery solrQuery, User user)
            throws NoSuchFieldException, IOException, SolrServerException {

        solrQuery = addFilterQuery(solrQuery, user);
        SearchResult<DS> result = search(solrQuery);
        result.setExcludedOptions(getExcludedOptions(user));
        return result;
    }

    @Transactional
    @PreAuthorize("hasPermission(#dataSource, "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).CREATE_DATASOURCE)")
    @PostAuthorize("@ArachnePermissionEvaluator.addPermissions(principal, returnObject )")
    @Override
    public DS update(DS dataSource)
            throws IllegalAccessException, NoSuchFieldException, SolrServerException, IOException {

        DS exist = dataSourceRepository.findByUuidAndDeletedIsNull(dataSource.getUuid())
                .orElseThrow(() -> new NotExistException(DataSource.class));

        if (dataSource.getName() != null) {
            exist.setName(dataSource.getName());
        }

        if (dataSource.getModelType() != null) {
            exist.setModelType(dataSource.getModelType());
        }

        if (dataSource.getCdmVersion() != null) {
            exist.setCdmVersion(dataSource.getModelType().equals(CommonModelType.CDM) ? dataSource.getCdmVersion() : null);
        }

        beforeUpdate(exist, dataSource);

        DS savedDataSource = dataSourceRepository.save(exist);
        afterUpdate(savedDataSource);

        return savedDataSource;
    }

    protected void beforeUpdate(DS target, DS dataSource) {

    }

    protected void afterUpdate(DS dataSource)
            throws IllegalAccessException, NoSuchFieldException, SolrServerException, IOException {

        if (!dataSource.getDataNode().getVirtual()) {
            indexBySolr(dataSource);
        }
    }

    @PreAuthorize("hasPermission(#id, 'DataSource', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).ACCESS_DATASOURCE)")
    @Override
    public DS getNotDeletedById(Long id) {

        return dataSourceRepository.findByIdAndDeletedIsNull(id)
                .orElseThrow(() -> new NotExistException(DataSource.class));
    }

    @Override
    public DS getByIdUnsecured(Long id) {

        if (id == null) {
            throw new NotExistException("id is null", getType());
        }
        DS dataSource = dataSourceRepository.findOne(id);
        if (dataSource == null) {
            throw new NotExistException(getType());
        }
        return dataSource;
    }

    protected abstract Class<?> getType();

    @Override
    public List<DS> getAllNotDeletedIsNotVirtualUnsecured() {

        return dataSourceRepository.getByDataNodeVirtualAndDeletedIsNull(false);
    }

    @PreAuthorize("hasPermission(#uuid, 'DataSource', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).ACCESS_DATASOURCE)")
    @PostAuthorize("@ArachnePermissionEvaluator.addPermissions(principal, returnObject )")
    @Override
    public DS findByUuid(String uuid) throws NotExistException {

        return findByUuidUnsecured(uuid);
    }

    @Override
    public DS findByUuidUnsecured(String uuid) throws NotExistException {

        if (uuid == null) {
            throw new NotExistException("uuid is null", getType());
        }
        DS dataSource = dataSourceRepository.findByUuid(uuid);
        if (dataSource == null) {
            throw new NotExistException(getType());
        }
        return dataSource;
    }

    @Override
    public Page<DS> suggestDataSource(final String query, final Long studyId, final Long userId,
                                      PageRequest pageRequest) {

        final String[] split = query.trim().split(" ");
        String suggestRequest = "%(" + String.join("|", split) + ")%";
        return doSuggestDataSource(suggestRequest, userId, studyId, pageRequest);
    }

    protected Page<DS> doSuggestDataSource(String query, Long userId, Long studyId, PageRequest pageRequest) {

        return dataSourceRepository.suggest(query, studyId, pageRequest);
    }

    @PreAuthorize("hasPermission(#uuid, 'DataSource', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).DELETE_DATASOURCE)")
    @Transactional
    @Override
    public void delete(String uuid) throws IOException, SolrServerException {

        log.info("Deleting datasource with uuid={}", uuid);
        if (dataSourceRepository.deleteByUuidAndDeletedIsNull(uuid) == 0) {
            throw new NotExistException(getType());
        }
    }

    public FieldList<SF> getSolrFields() {

        FieldList<SF> fieldList = new FieldList<>();

        fieldList.addAll(solrService.getFieldsOfClass(getType()));
        fieldList.addAll(getExtraSolrFilelds());
        return fieldList;
    }

    protected List<SF> getExtraSolrFilelds() {

        return Collections.emptyList();
    }

    public void indexAllBySolr() throws IllegalAccessException, NoSuchFieldException, SolrServerException, IOException {

        solrService.deleteByQuery(SolrServiceImpl.DATA_SOURCE_COLLECTION, "*:*");
        List<DS> dataSourceList = getAllNotDeletedIsNotVirtualUnsecured();
        for (DS dataSource : dataSourceList) {
            indexBySolr(dataSource);
        }
    }

    protected SolrQuery addFilterQuery(SolrQuery solrQuery, User user) throws NoSuchFieldException {

        return solrQuery;
    }

    private Map<String, List<String>> getExcludedOptions(User user) throws NoSuchFieldException,
            IOException, SolrServerException {

        SolrQuery solrQuery = conversionService.convert(new SearchDataCatalogDTO(true), SolrQuery.class);
        solrQuery = addFilterQuery(solrQuery, user);

        QueryResponse solrResponse = solrService.search(
                SolrServiceImpl.DATA_SOURCE_COLLECTION,
                solrQuery
        );
        SearchResult<Long> searchResult = new SearchResult<>(solrQuery, solrResponse, Collections.<Long>emptyList());
        return searchResult.excludedOptions();
    }

    protected void indexBySolr(DS dataSource)
            throws IOException, SolrServerException, NoSuchFieldException, IllegalAccessException {

        Map<SF, Object> values = solrService.getValuesByEntity(dataSource);

        solrService.putDocument(
                SolrServiceImpl.DATA_SOURCE_COLLECTION,
                dataSource.getId(),
                values
        );
    }

    public abstract List<DS> getAllByUserId(Long userId);
}
