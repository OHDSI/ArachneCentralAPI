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

package com.odysseusinc.arachne.portal.service.impl;

import com.odysseusinc.arachne.portal.model.solr.SolrFieldAnno;
import com.odysseusinc.arachne.portal.model.solr.SolrValue;
import com.odysseusinc.arachne.portal.service.BaseSolrService;
import com.odysseusinc.arachne.portal.service.impl.solr.FieldList;
import com.odysseusinc.arachne.portal.service.impl.solr.SolrField;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class BaseSolrServiceImpl<T extends SolrField> implements BaseSolrService<T> {
    public static final String DATA_SOURCE_COLLECTION = "data-sources";
    public static final String USER_COLLECTION = "users";
    private static final String QUERY_FIELD_PREFIX = "query_";
    private static final String ID = "id";

    @Value("${arachne.solrServerUrl}")
    private String serverUrl;

    public T getSolrField(Field field) {

        T solrField = null;
        if (field.isAnnotationPresent(SolrFieldAnno.class)) {
            SolrFieldAnno solrFieldAnno = field.getAnnotation(SolrFieldAnno.class);
            solrField = newSolrField();
            solrField.setName(field.getName());
            solrField.setDataType(field.getType());
            solrField.setSearchable(solrFieldAnno.query());
            solrField.setFaceted(solrFieldAnno.filter());
        }
        return solrField;
    }

    protected abstract T newSolrField();

    public FieldList<T> getFieldsOfClass(Class entity) {

        FieldList<T> result = new FieldList<>();
        List<Field> fields = getDeclaredFields(entity);

        for (Field field : fields) {
            T solrField = getSolrField(field);
            if (solrField != null) {
                result.add(solrField);
            }
        }

        return result;
    }

    private List<Field> getDeclaredFields(Class<?> entity) {

        List<Field> fields = new LinkedList<>();
        fields.addAll(Arrays.asList(entity.getDeclaredFields()));
        if (entity.getSuperclass() != null) {
            fields.addAll(getDeclaredFields(entity.getSuperclass()));
        }
        return fields;
    }

    private Optional<Field> getDeclaredField(Class<?> entity, final String fieldName) {

        try {
            Field field = entity.getDeclaredField(fieldName);
            return Optional.of(field);
        } catch (NoSuchFieldException e) {
            if (entity.getSuperclass() != null) {
                return getDeclaredField(entity.getSuperclass(), fieldName);
            }
        }
        return Optional.empty();
    }

    @Override
    public Map<T, Object> getValuesByEntity(Object entity) throws IllegalAccessException, NoSuchFieldException {

        Map<T, Object> values = new HashMap<>();

        FieldList<T> fieldList = getFieldsOfClass(entity.getClass());
        for (T solrField : fieldList) {
            Optional<Field> fieldOptional = getDeclaredField(entity.getClass(), solrField.getName());
            if (fieldOptional.isPresent()) {
                Field field = fieldOptional.get();
                field.setAccessible(true);
                values.put(solrField, field.get(entity));
            }
        }

        return values;
    }

    @Override
    public void putDocument(
            String collection,
            Long id,
            Map<T, Object> values
    ) throws IOException, SolrServerException {

        SolrClient solr = new HttpSolrClient.Builder(serverUrl + "/" + collection).build();
        SolrInputDocument document = new SolrInputDocument();

        List<String> queryString = new ArrayList<>();

        for (Map.Entry<T, Object> field : values.entrySet()) {
            SolrField solrField = field.getKey();
            Object rawValue = field.getValue();
            // Note: value for filtering and value for full-text search can differ.
            // E.g. main value may be an id of object,
            // and full-text search should be done over human-readable representation of such object
            Object value = null;
            Object queryValue = null;

            // Parse saved value
            if (rawValue != null) {
                if (rawValue instanceof Collection<?>) {
                    Collection rawValueList = (Collection) rawValue;
                    int arrSize = rawValueList.size();
                    Object[] valArray = new Object[arrSize];
                    Object[] queryValArray = new Object[arrSize];
                    int index = 0;
                    for (Object rawValueEntry : rawValueList) {
                        if (rawValueEntry instanceof SolrValue) {
                            valArray[index] = ((SolrValue) rawValueEntry).getSolrValue();
                            queryValArray[index] = ((SolrValue) rawValueEntry).getSolrQueryValue();
                        } else {
                            valArray[index] = rawValueEntry.toString();
                            queryValArray[index] = rawValueEntry.toString();
                        }
                        index++;
                    }
                    value = valArray;
                    queryValue = Arrays.toString(queryValArray);
                } else {
                    if (rawValue instanceof SolrValue) {
                        value = ((SolrValue) rawValue).getSolrValue();
                        queryValue = ((SolrValue) rawValue).getSolrQueryValue();
                    } else {
                        value = rawValue.toString();
                        queryValue = rawValue.toString();
                    }
                }

                if (solrField.getSearchable()) {
                    document.addField(QUERY_FIELD_PREFIX + solrField.getName(), queryValue);
                }
            }

            document.addField(solrField.getSolrName(), value);
        }

        document.addField(ID, id);

        // Example of calling RequestProcessorChain for update:
        // UpdateRequest updateRequest = new UpdateRequest();
        // updateRequest.setCommitWithin(1000);
        // updateRequest.setParam("update.chain", "query_agg");
        // updateRequest.add(document);
        // UpdateResponse updateResponse = updateRequest.process(solr);

        UpdateResponse updateResponse = solr.add(document);
        UpdateResponse commitResponse = solr.commit();

        if (commitResponse.getStatus() != 0 || updateResponse.getStatus() != 0) {
            throw new SolrServerException("Cannot index by Solr");
        }
    }

    @Override
    public QueryResponse search(
            String collection,
            SolrQuery solrQuery
    ) throws IOException, SolrServerException {

        SolrClient solr = new HttpSolrClient.Builder(serverUrl + "/" + collection).build();
        return solr.query(solrQuery);
    }

    @Override
    public void deleteByQuery(String collection, String query) throws IOException, SolrServerException {

        SolrClient solrClient = new HttpSolrClient.Builder(serverUrl + "/" + collection).build();
        solrClient.deleteByQuery(query);
        solrClient.commit();
    }
}
