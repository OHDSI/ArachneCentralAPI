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
 * Created: September 17, 2017
 *
 */

package com.odysseusinc.arachne.portal.service;

import com.odysseusinc.arachne.portal.exception.FieldException;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.NotUniqueException;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.model.DataSource;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.service.impl.solr.FieldList;
import com.odysseusinc.arachne.portal.service.impl.solr.SearchResult;
import java.io.IOException;
import java.util.List;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface BaseDataSourceService<T extends DataSource> {

    FieldList getSolrFields();

    T createOrRestoreDataSource(T dataSource)
            throws FieldException,
            NotExistException,
            ValidationException,
            IOException,
            SolrServerException, NoSuchFieldException, IllegalAccessException;

    SearchResult<T> search(
            SolrQuery solrQuery
    ) throws IOException, SolrServerException, NoSuchFieldException;

    SearchResult<T> search(SolrQuery solrQuery, User user) throws NoSuchFieldException, IOException, SolrServerException;

    T update(
            T dataSource
    ) throws
                    NotExistException,
                    ValidationException,
                    IOException,
                    SolrServerException,
                    NoSuchFieldException,
                    IllegalAccessException, NotUniqueException;

    T getNotDeletedById(Long id);

    T getByIdUnsecured(Long id) throws NotExistException;

    List<T> getAllNotDeletedIsNotVirtualUnsecured();

    T findByUuidUnsecured(String uuid) throws NotExistException;

    T findById(Long dataSourceId);

    Page<T> suggestDataSource(String query, Long studyId, Long userId,
                                       PageRequest pageRequest);

    void indexAllBySolr() throws IllegalAccessException, NoSuchFieldException, SolrServerException, IOException;

    void delete(Long id) throws IOException, SolrServerException;

    void unpublish(Long id) throws IOException, SolrServerException;
}
