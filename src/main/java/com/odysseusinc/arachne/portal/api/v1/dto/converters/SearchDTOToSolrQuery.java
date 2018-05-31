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
 * Created: February 09, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.portal.api.v1.dto.SearchDTO;
import com.odysseusinc.arachne.portal.service.SolrService;
import com.odysseusinc.arachne.portal.service.impl.BaseSolrServiceImpl;
import com.odysseusinc.arachne.portal.service.impl.solr.FieldList;
import com.odysseusinc.arachne.portal.service.impl.solr.SolrField;
import org.apache.solr.client.solrj.SolrQuery;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

@SuppressWarnings("unused")
public abstract class SearchDTOToSolrQuery {

    protected static final String QUERY_ANY = "*";

    public static String getFacetLabel(String facetName) {

        return facetName + "s";
    }

    protected FieldList<SolrField> getSolrFields() {

        return new FieldList<>();
    }

    protected void setSorting(SearchDTO source, SolrQuery result, FieldList solrFields) {

        if (source.getSort() != null && source.getOrder() != null) {
            // Check if such field exists in Solr index and retrieve it
            SolrField sortSolrSolrField = solrFields.getByName(source.getSort());
            if (sortSolrSolrField != null) {
                String sortFieldName = sortSolrSolrField.isMultiValuesType() ?
                        sortSolrSolrField.getMultiValuesTypeFieldName() :
                        sortSolrSolrField.getSolrName();

                result.setSort(sortFieldName, SolrQuery.ORDER.valueOf(source.getOrder()));
            }
        }
    }

    protected void setOutputFields(final SolrQuery result, final String[] fields) {

        result.setFields(fields);
    }

    protected void setPagination(SearchDTO source, SolrQuery result) {

        if (source.getPage() != null && source.getPageSize() != null) {
            result.setStart((source.getPage() - 1) * source.getPageSize());
            result.setRows(source.getPageSize());
        }
    }

    protected void setQuery(SearchDTO source, SolrQuery result) {

        String queryStr = "query:*";
        if (source.getQuery() != null) {
            queryStr = "query:*" + source.getQuery() + "*";
        }
        result.setQuery(queryStr);
    }

    protected void setFilters(SearchDTO source, SolrQuery result, FieldList solrFields) {

        Map<String, Object> filterInput = source.getFilter();
        if (filterInput != null) {
            for (Map.Entry<String, Object> filterEntry : filterInput.entrySet()) {
                String filterName = filterEntry.getKey();
                Object filterValue = filterEntry.getValue();
                SolrField solrField = solrFields.getByName(filterName);
                if (solrField != null && solrField.getFaceted()) {
                    result.addFilterQuery(getExcludedTag(SolrService.fieldNameToFacet(solrField))
                            + solrField.getSolrName() + ":" + filterValue);
                }
            }
        }
    }

    // TODO
    protected void setFacets(SearchDTO source, SolrQuery result, FieldList<SolrField> solrFields) {

        JSONObject jsonFacet = new JSONObject();
        solrFields
                .stream()
                .filter(SolrField::getFaceted)
                .forEach(solrField -> {
                    result.addFacetField(SolrService.fieldNameToFacet(solrField));
                    putIntoJsonFacet(jsonFacet, SolrService.fieldNameToFacet(solrField),
                            Number.class.isAssignableFrom(solrField.getDataType()));
                });
        result.add("json.facet", jsonFacet.toString().replace("\"", ""));
    }


    protected void setCollections(SearchDTO source, SolrQuery result) {

        if (source.getCollections() != null) {
            result.setParam("collection", String.join(",", source.getCollections()));
        }
    }

    public SolrQuery convert(SearchDTO source) {

        final SolrQuery result = new SolrQuery();
        final FieldList solrFields = getSolrFields();

        setSorting(source, result, solrFields);
        setPagination(source, result);
        setOutputFields(result, source.getResultFields());
        setQuery(source, result);
        setFilters(source, result, solrFields);
        setFacets(source, result, solrFields);
        setCollections(source, result);

        return result;
    }

    /**
     * @return query for getting "full" facets for all data (without filter and query)
     */
    public SolrQuery convertToFullFacetsQuery(SearchDTO source) {

        SolrQuery result = new SolrQuery();
        FieldList solrFields = getSolrFields();
        result.setRows(0);
        result.setStart(0);
        setQuery(source, result);
        setFacets(source, result, solrFields);
        return result;
    }

    private void putIntoJsonFacet(JSONObject jsonFacet, String facetField, boolean isNumber) {

        try {
            JSONObject parameters = getJsonFacetParameters(facetField);
            if (!isNumber) {
                parameters.put("mincount", 0);
            }
            jsonFacet.put(getFacetLabel(facetField), parameters);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private JSONObject getJsonFacetParameters(String facetField) throws JSONException {

        return new JSONObject()
                .put("type", "terms")
                .put("field", facetField)
                .put("limit", 100)
                .put("missing", true)
                .put("domain",
                        new JSONObject().put("excludeTags", getTag(facetField)));
    }

    private String getTag(String facetField) {

        return facetField.toUpperCase();
    }

    private String getExcludedTag(String facetField) {

        return String.format("{!tag=%s}", facetField.toUpperCase());
    }

}
