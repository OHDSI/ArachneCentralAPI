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
 * Created: October 06, 2017
 *
 */

package com.odysseusinc.arachne.portal.service;

import com.odysseusinc.arachne.portal.service.impl.solr.FieldList;
import com.odysseusinc.arachne.portal.service.impl.solr.SolrField;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

public interface BaseSolrService<T extends SolrField> {
    T getSolrField(Field field);

    FieldList<T> getFieldsOfClass(Class entity);

    Map<T, Object> getValuesByEntity(Object entity) throws IllegalAccessException, NoSuchFieldException;

    void putDocument(
            String collection,
            Long id,
            Map<T, Object> fields
    ) throws IOException, SolrServerException;

    QueryResponse search(
            String collection,
            SolrQuery solrQuery
    ) throws IOException, SolrServerException;

    void deleteByQuery(String collection, String query) throws IOException, SolrServerException;
}
