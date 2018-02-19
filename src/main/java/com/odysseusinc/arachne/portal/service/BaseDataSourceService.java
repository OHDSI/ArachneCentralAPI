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

import com.odysseusinc.arachne.portal.exception.FieldException;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.NotUniqueException;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.service.impl.solr.FieldList;
import com.odysseusinc.arachne.portal.service.impl.solr.SearchResult;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.io.IOException;
import java.util.List;

public interface BaseDataSourceService<BDS extends IDataSource, RDS extends IDataSource, DS extends IDataSource> {

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

    DS update(
            DS dataSource
    ) throws
                    NotExistException,
                    ValidationException,
                    IOException,
                    SolrServerException,
                    NoSuchFieldException,
                    IllegalAccessException, NotUniqueException;

    RDS updateInAnyTenant(
            RDS dataSource
    ) throws
            NotExistException,
            ValidationException,
            IOException,
            SolrServerException,
            NoSuchFieldException,
            IllegalAccessException, NotUniqueException;

    DS getNotDeletedById(Long id);

    RDS getNotDeletedByIdInAnyTenant(Long id);

    DS getByIdUnsecured(Long id) throws NotExistException;

    List<DS> getAllNotDeletedIsNotVirtualUnsecured();

    DS findByUuidUnsecured(String uuid) throws NotExistException;

    void indexBySolr(BDS dataSource)
            throws IOException, SolrServerException, NoSuchFieldException, IllegalAccessException;

    DS findById(Long dataSourceId);

    Page<DS> suggestDataSource(String query, Long studyId, Long userId,
                                       PageRequest pageRequest);

    void indexAllBySolr() throws IllegalAccessException, NoSuchFieldException, SolrServerException, IOException;

    void delete(Long id) throws IOException, SolrServerException;
}
