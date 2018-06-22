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

package com.odysseusinc.arachne.portal.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odysseusinc.arachne.portal.api.v1.dto.BreadcrumbDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.converters.SolrFieldExtractor;
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
import com.odysseusinc.arachne.portal.util.EntityUtils;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.ConversionService;

public abstract class BaseSolrServiceImpl<T extends SolrField> implements BaseSolrService<T> {

    private static final String QUERY_FIELD_PREFIX = "query_";

    @Value("${arachne.solrBatchSize}")
    private int solrBatchSize;
    
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
            solrField.setDataType(solrFieldAnno.clazz() == String.class ? field.getType() : solrFieldAnno.clazz());
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
        solrField.setSortNeeded(solrFieldAnno.sort());
        solrField.setDataType(solrFieldAnno.clazz());
        final Class<? extends SolrFieldExtractor>[] extractors = solrFieldAnno.extractor();
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

        final List<T> fields = new LinkedList<>();
        AnnotationUtils.getRepeatableAnnotations(entity, SolrFieldAnno.class).forEach(
                anno -> fields.add(extractSolrField(anno))
        );
        
        if (entity.getSuperclass() != null) {
            fields.addAll(gatherClassSolrAnnotations(entity.getSuperclass()));
        }
        return fields;
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
    public Map<T, Object> getValuesByEntity(final SolrEntity entity)  {

        final Map<T, Object> values = new HashMap<>();
        final FieldList<T> fieldList = getFieldsOfClass(entity.getClass());
        for (final T solrField : fieldList) {
            final Object fieldValue;
            final Field field = solrField.getField();
            final SolrFieldExtractor extractor = solrField.getExtractor();
            
            if (extractor != null) {
                fieldValue = extractor.extract(entity);
            } else if (field != null) {
                field.setAccessible(true);
                fieldValue = getFieldValue(entity, field);
            } else {
                throw new NullPointerException("FieldValue cannot be null");
            }
            
            values.put(solrField, fieldValue);
        }

        if (entity.getId() != null) {
            final T idField = newSolrField(ID);
            idField.setDataType(Long.class);
            idField.setPostfixNeeded(false);
            values.put(idField, entity.getId());
        }
        
        addBreadcrumbsIfNeeded(entity, values);

        return values;
    }

    private Object getFieldValue(final Object entity, final Field field) {
        
        try {
            return field.get(entity);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void addBreadcrumbsIfNeeded(final Object entity, final Map<T, Object> values) {

        if (entity instanceof Breadcrumb) {
            final Breadcrumb bc = (Breadcrumb)entity;
            try {
                final List<BreadcrumbDTO> breadcrumbs = breadcrumbService.getBreadcrumbs(bc)
                        .stream()
                        .map(v -> conversionService.convert(v, BreadcrumbDTO.class))
                        .collect(Collectors.toList());
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
    ) {

        try {
            final SolrInputDocument document = createSolrDocument(collection, id, values);

            final UpdateResponse updateResponse = solrClient.add(collection, document);
            final UpdateResponse commitResponse = solrClient.commit(collection);

            if (commitResponse.getStatus() != 0 || updateResponse.getStatus() != 0) {
                throw new SolrServerException("Cannot index by Solr");
            }    
        } catch( IOException | SolrServerException e) {
            throw new SolrException(e);
        }
        
    }
    
    @Override
    public void putDocuments(
            final String collection,
            final List<Map<T, Object>> valuesList
    ) {

        EntityUtils.splitAndApplyBatchFunction(sublist -> {
            try {
                final List<SolrInputDocument> documents = sublist.stream()
                        .map(v -> createSolrDocument(collection, v))
                        .collect(Collectors.toList());

                final UpdateResponse updateResponse = solrClient.add(collection, documents);
                final UpdateResponse commitResponse = solrClient.commit(collection);

                if (commitResponse.getStatus() != 0 || updateResponse.getStatus() != 0) {
                    throw new SolrServerException("Cannot index by Solr");
                }
            } catch( IOException | SolrServerException e) {
                throw new SolrException(e);
            }
        }, valuesList, solrBatchSize);
    }
        
    
    private SolrInputDocument createSolrDocument(final String entityType, final Map<T, Object> values) {
        
        // in this case ID will be taken from values
        return this.createSolrDocument(entityType, null, values);
    }
    
    private SolrInputDocument createSolrDocument(final String entityType, final Long id, final Map<T, Object> values) {

        final SolrInputDocument document = new SolrInputDocument();

        for (final Map.Entry<T, Object> field : values.entrySet()) {
            final T solrField = field.getKey();
            final Object rawValue = field.getValue();
            // Note: value for filtering and value for full-text search can differ.
            // E.g. main value may be an id of object,
            // and full-text search should be done over human-readable representation of such object
            Object value = null;
            Object queryValue = null;

            // Parse saved value
            if (rawValue != null) {
                if (rawValue instanceof Collection<?>) {
                    final Collection rawValueList = (Collection) rawValue;
                    final int arrSize = rawValueList.size();
                    final Object[] valArray = new Object[arrSize];
                    final Object[] queryValArray = new Object[arrSize];
                    int index = 0;
                    for (final Object rawValueEntry : rawValueList) {
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

            if (solrField.isSortNeeded() && solrField.isMultiValuesType()) {
                String valueForSort = null;
                if (!StringUtils.isEmpty((String) queryValue)) {
                    final List<String> list = Arrays.asList(StringUtils.split(((String) queryValue)));
                    Collections.sort(list);
                    valueForSort = String.join(" ", list);
                }
                document.addField(solrField.getMultiValuesTypeFieldName(), valueForSort);
            }
            
            
        }

        // these two fields will be concatenated into solr document id
        if (document.getField(ID) == null) {
            if (id == null) {
                throw new IllegalArgumentException("Id cannot be null");
            } else {
                document.addField(ID, id);
            }
        }
        document.addField(TYPE, entityType);
        return document;
    }

    private SolrQuery addTenantFilter(final SolrQuery solrQuery) {

        final String tenancyFilter = BaseSolrService.TENANTS + ":" + TenantContext.getCurrentTenant().toString();
        return solrQuery.addFilterQuery(tenancyFilter);
    }

    @Override
    public QueryResponse search(
            final String collection,
            SolrQuery solrQuery,
            final Boolean isTenantsFilteringNeeded
    ) {

        if (isTenantsFilteringNeeded) {
            solrQuery = addTenantFilter(solrQuery);
        }
        try {
            return solrClient.query(collection, solrQuery);
        } catch (SolrServerException | IOException e) {
            throw new SolrException(e);
        }
    }

    @Override
    public QueryResponse search(
            final String collection,
            final SolrQuery solrQuery
    ) {

        return search(collection, solrQuery, false);
    }

    @Override
    public void deleteByQuery(final String collection, final String query) {

        try {
            solrClient.deleteByQuery(collection, query);
            solrClient.commit(collection);       
        } catch( IOException | SolrServerException e) {
            throw new SolrException(e);
        }
    }

    @Override
    public void indexBySolr(final SolrEntity entity) {
        
        final Map<T, Object> values = getValuesByEntity(entity);
        putDocument(
                entity.getCollection().getName(),
                entity.getId(),
                values
        );
    }



    @Override
    public void indexBySolr(final List<? extends SolrEntity> entities) {

        try {
            final Map<SolrCollection, List<SolrEntity>> entitiesGroupByCollection = entities
                    .stream()
                    .collect(Collectors.groupingBy(SolrEntity::getCollection));
            entitiesGroupByCollection.forEach((key, value) -> putDocuments(
                    key.getName(),
                    value.stream().map(this::getValuesByEntity).collect(Collectors.toList())
            ));
        } catch (final Exception e) {
            throw new SolrException(e);
        }
    }

    @Override
    public void delete(final SolrEntity entity) {

        delete(entity.getCollection(), entity.getSolrId());
    }
    
    @Override
    public void delete(final SolrCollection collection, final String id) {
        
        deleteByQuery(collection.getName(), "id:" + id);
    }

    @Override
    public void deleteAll(final SolrCollection collection) {

        deleteByQuery(collection.getName(), "*:*");
    }
}
