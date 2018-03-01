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
 * Created: October 06, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odysseusinc.arachne.portal.api.v1.dto.BreadcrumbDTO;
import com.odysseusinc.arachne.portal.config.tenancy.TenantContext;
import com.odysseusinc.arachne.portal.model.solr.SolrCollection;
import com.odysseusinc.arachne.portal.model.solr.SolrEntity;
import com.odysseusinc.arachne.portal.model.solr.SolrException;
import com.odysseusinc.arachne.portal.model.solr.SolrFieldAnno;
import com.odysseusinc.arachne.portal.model.solr.SolrValue;
import com.odysseusinc.arachne.portal.service.BaseSolrService;
import com.odysseusinc.arachne.portal.service.BreadcrumbService;
import com.odysseusinc.arachne.portal.service.impl.breadcrumb.Breadcrumb;
import com.odysseusinc.arachne.portal.service.impl.solr.FieldList;
import com.odysseusinc.arachne.portal.service.impl.solr.SolrField;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.ConversionService;

public abstract class BaseSolrServiceImpl<T extends SolrField> implements BaseSolrService<T> {

    private static final String QUERY_FIELD_PREFIX = "query_";

    @Autowired
    private SolrClient solrClient;

    @Autowired
    private BreadcrumbService breadcrumbService;

    @Autowired
    private ConversionService conversionService;

    private ObjectMapper objectMapper = new ObjectMapper();

    public T getSolrField(final Field field) {

        T solrField = null;
        if (field.isAnnotationPresent(SolrFieldAnno.class)) {
            final SolrFieldAnno solrFieldAnno = field.getAnnotation(SolrFieldAnno.class);
            solrField = extractSolrField(solrFieldAnno);
            solrField.setDataType(field.getType());
            solrField.setField(field);
            if (StringUtils.isEmpty(solrField.getName())) {
                solrField.setName(field.getName());
            }
        }
        return solrField;
    }

    private T extractSolrField(final SolrFieldAnno solrFieldAnno) {

        final T solrField = newSolrField(solrFieldAnno.name());
        solrField.setSearchable(solrFieldAnno.query());
        solrField.setFaceted(solrFieldAnno.filter());
        solrField.setPostfixNeeded(solrFieldAnno.postfix());
        final Class<? extends Function<Object, Object>>[] extractors = solrFieldAnno.extractor();
        if (extractors.length > 0) {
            solrField.setExtractor(BeanUtils.instantiate(extractors[0]));
        }
        return solrField;
    }

    protected abstract T newSolrField(String name);

    public FieldList<T> getFieldsOfClass(final Class<?> entity) {

        final FieldList<T> result = new FieldList<>();
        final List<Field> fields = getDeclaredFields(entity);

        for (final Field field : fields) {
            final T solrField = getSolrField(field);
            if (solrField != null) {
                result.add(solrField);
            }
        }

        result.addAll(gatherClassSolrAnnotations(entity));

        return result;
    }

    private List<T> gatherClassSolrAnnotations(final Class<?> entity) {

        return AnnotationUtils.getRepeatableAnnotations(entity, SolrFieldAnno.class)
                .stream()
                .map(this::extractSolrField)
                .collect(Collectors.toList());
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
    public Map<T, Object> getValuesByEntity(final Object entity) throws IllegalAccessException {

        final Map<T, Object> values = new HashMap<>();
        final FieldList<T> fieldList = getFieldsOfClass(entity.getClass());
        for (final T solrField : fieldList) {
            Object fieldValue = null;
            final Field field = solrField.getField();
            if (field != null) {
                field.setAccessible(true);
                fieldValue = field.get(entity);
            }
            final Function<Object, Object> fieldConverter = solrField.getExtractor();
            if (fieldConverter != null) {
                fieldValue = fieldConverter.apply(entity);
            }
            values.put(solrField, fieldValue);
        }

        addBreadcrumbsIfNeeded(entity, values);

        return values;
    }

    private void addBreadcrumbsIfNeeded(final Object entity, final Map<T, Object> values) {

        if (entity instanceof Breadcrumb) {
            final Breadcrumb bc = (Breadcrumb)entity;
            try {
                final List<BreadcrumbDTO> breadcrumbs = breadcrumbService.getBreadcrumbs(bc.getCrumbType(), bc.getId()).stream().map(v -> conversionService.convert(v, BreadcrumbDTO.class)).collect(Collectors.toList());
                final T field = newSolrField(BREADCRUMBS);
                field.setPostfixNeeded(Boolean.FALSE);
                field.setSearchable(Boolean.FALSE);
                values.put(field, objectMapper.writeValueAsString(breadcrumbs));
            } catch (final JsonProcessingException e) {
                throw new UnsupportedOperationException(e);
            }
        }
    }

    @Override
    public void putDocument(
            final String collection,
            final Long id,
            final Map<T, Object> values
    ) throws IOException, SolrServerException {

        SolrInputDocument document = new SolrInputDocument();

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

            if (solrField.isMultiValuesType()) {
                String valueForSort = null;
                if (!StringUtils.isEmpty((String) queryValue)) {
                    List<String> list = Arrays.asList(StringUtils.split(((String) queryValue)));
                    Collections.sort(list);
                    valueForSort = String.join(" ", list);
                }
                document.addField(solrField.getMultiValuesTypeFieldName(), valueForSort);
            }
        }

        // these two fields will be concatenated into solr document id
        document.addField(ID, id);
        document.addField(TYPE, collection);

        // Example of calling RequestProcessorChain for update:
        // UpdateRequest updateRequest = new UpdateRequest();
        // updateRequest.setCommitWithin(1000);
        // updateRequest.setParam("update.chain", "query_agg");
        // updateRequest.add(document);
        // UpdateResponse updateResponse = updateRequest.process(solrClient);

        UpdateResponse updateResponse = solrClient.add(collection, document);
        UpdateResponse commitResponse = solrClient.commit(collection);

        if (commitResponse.getStatus() != 0 || updateResponse.getStatus() != 0) {
            throw new SolrServerException("Cannot index by Solr");
        }
    }

    private SolrQuery addTenantFilter(SolrQuery solrQuery, Field tenantDiscriminatorField) throws NoSuchFieldException {

        String tenancyFilter = getSolrField(tenantDiscriminatorField).getSolrName() + ":" + TenantContext.getCurrentTenant().toString();
        return solrQuery.addFilterQuery(tenancyFilter);
    }

    @Override
    public QueryResponse search(
            String collection,
            SolrQuery solrQuery,
            Field tenantDiscriminatorField
    ) throws IOException, SolrServerException, NoSuchFieldException {

        if (tenantDiscriminatorField != null) {
            solrQuery = addTenantFilter(solrQuery, tenantDiscriminatorField);
        }
        return solrClient.query(collection, solrQuery);
    }

    @Override
    public QueryResponse search(
            String collection,
            SolrQuery solrQuery
    ) throws IOException, SolrServerException, NoSuchFieldException {

        return search(collection, solrQuery, null);
    }

    @Override
    public void deleteByQuery(String collection, String query) throws IOException, SolrServerException {

        solrClient.deleteByQuery(collection, query);
        solrClient.commit(collection);
    }

    @Override
    public void indexBySolr(final SolrEntity entity) {
        try {
            final Map<T, Object> values = getValuesByEntity(entity);

            putDocument(
                    entity.getCollection().getName(),
                    entity.getId(),
                    values
            );
        } catch (final Exception e) {
            throw new SolrException(e);
        }
    }

    @Override
    public void delete(final SolrEntity entity) throws IOException, SolrServerException {

        delete(entity.getCollection(), entity.getSolrId());
    }
    
    @Override
    public void delete(final SolrCollection collection, final String id) throws IOException, SolrServerException {
        
        deleteByQuery(collection.getName(), "id:" + id);
    }

    @Override
    public void deleteAll(final SolrCollection collection) throws IOException, SolrServerException {

        deleteByQuery(collection.getName(), "*:*");
    }
}
