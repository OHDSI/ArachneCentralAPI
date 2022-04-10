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
 * WIDSHOUDS WARRANDSIES OR CONDIDSIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Company: Odysseus Data Services, Inc.
 * Product Owner/Architecture: Gregory Klebanov
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva, Mikhail Mironov
 * Created: September 17, 2017
 *
 */

package com.odysseusinc.arachne.portal.service;

import com.odysseusinc.arachne.portal.api.v1.dto.PageDTO;
import com.odysseusinc.arachne.portal.exception.FieldException;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.service.impl.solr.FieldList;
import com.odysseusinc.arachne.portal.service.impl.solr.SearchResult;
import java.io.IOException;
import java.util.List;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface BaseDataSourceService<DS extends IDataSource> extends Indexable {

    FieldList getSolrFields();

    DS createOrRestoreDataSource(DS dataSource)
            throws FieldException,
            NotExistException,
            ValidationException,
            IOException,
            SolrServerException, NoSuchFieldException, IllegalAccessException;

    SearchResult<DS> search(
            SolrQuery solrQuery
    ) throws IOException, SolrServerException, NoSuchFieldException;

    SearchResult<DS> search(SolrQuery solrQuery, IUser user) throws NoSuchFieldException, IOException, SolrServerException;

    DS updateInAnyTenant(DS dataSource) throws IllegalAccessException, NoSuchFieldException, SolrServerException, IOException;

    DS updateWithoutMetadataInAnyTenant(DS dataSource) throws IllegalAccessException, NoSuchFieldException, SolrServerException, IOException;

    DS getNotDeletedByIdInAnyTenant(Long id);

    DS getByIdInAnyTenant(Long id);

    DS getByIdUnsecured(Long id) throws NotExistException;

    List<DS> getAllNotDeletedAndIsNotVirtualFromAllTenants(boolean withManual);

    List<Long> getTenantsForDatanode(Long nodeId);

    DS findByUuidUnsecured(String uuid) throws NotExistException;

    void indexBySolr(DS dataSource)
            throws IOException, SolrServerException, NoSuchFieldException, IllegalAccessException;

    DS getNotDeletedById(Long dataSourceId);

    List<DS> findByIdsAndNotDeleted(List<Long> dataSourceIds);

    List<DS> getByDataNodeId(Long id);

    List<DS> getNotDeletedByDataNodeId(Long dataNodeId);

    Page<DS> suggestDataSource(String query, Long studyId, Long userId,
                                       PageRequest pageRequest);

    void indexBySolr(List<DS> dataSources);

    void delete(Long id) throws IOException, SolrServerException;

    void unpublish(Long id) throws IOException, SolrServerException;

    Page<DS> getUserDataSources(final String query, final Long userId, PageRequest pageRequest);

    /**
     * Makes links between Studies from the given tenant and DataSource deleted
     */
    void makeLinksWithStudiesDeleted(Long tenantId, Long dataSourceId);

    void makeLinksWithTenantsNotDeleted(Long dataSourceId);

    PageRequest getPageRequest(PageDTO pageDTO, String sortBy, String order) throws PermissionDeniedException;

    PageRequest getPageRequest(PageDTO pageDTO) throws PermissionDeniedException;
}
