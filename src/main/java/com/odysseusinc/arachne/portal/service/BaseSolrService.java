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
 * Created: October 06, 2017
 *
 */

package com.odysseusinc.arachne.portal.service;

import static com.odysseusinc.arachne.portal.service.impl.solr.SolrField.META_PREFIX;

import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.model.solr.SolrCollection;
import com.odysseusinc.arachne.portal.model.solr.SolrEntity;
import com.odysseusinc.arachne.portal.service.impl.solr.FieldList;
import com.odysseusinc.arachne.portal.service.impl.solr.SolrField;
import java.util.List;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

public interface BaseSolrService<T extends SolrField> {

    String MULTI_METADATA_PREFIX = "multi_" + META_PREFIX;
    String BREADCRUMBS = "breadcrumbs";
    
    String ID = "entity_id";
    String TYPE = "entity_type";
    String TITLE = "entity_title";
    String IS_PUBLIC = "is_public";
    String PARTICIPANTS = "participants";
    String TENANTS = "tenants";
    String SYSTEM_ID = "id";

    T getSolrField(Field field);

    FieldList<T> getFieldsOfClass(Class<?> entity);

    Map<T, Object> getValuesByEntity(SolrEntity entity);

    void putDocument(
            String collection,
            Long id,
            Map<T, Object> fields
    );

    void putDocuments(
            String collection,
            List<Map<T, Object>> valuesList
    );

    QueryResponse search(
            String collection,
            SolrQuery solrQuery,
            Boolean isTenantsFilteringNeeded
    ) throws NoSuchFieldException;

    QueryResponse search(
            String collection,
            SolrQuery solrQuery
    ) throws NoSuchFieldException;

    void deleteByQuery(String collection, String query);

    void indexBySolr(SolrEntity object);

    void indexBySolr(List<? extends SolrEntity> entities);

    void delete(SolrEntity entity);

    void delete(SolrCollection collection, String id);

    void deleteAll(SolrCollection collection);
}
