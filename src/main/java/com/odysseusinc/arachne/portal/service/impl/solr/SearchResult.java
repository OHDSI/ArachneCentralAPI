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
 * Created: February 03, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.impl.solr;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;

public class SearchResult<T> {

    private SolrQuery solrQuery;
    private QueryResponse solrResponse;
    private List<T> entityList;
    private Map<String, List<String>> excludedOptions = new HashMap<>();

    public SearchResult(SolrQuery solrQuery, QueryResponse solrResponse, List<T> entityList) {

        this.solrQuery = solrQuery;
        this.solrResponse = solrResponse;
        this.entityList = entityList;
    }

    public SolrQuery getSolrQuery() {

        return solrQuery;
    }

    public void setSolrQuery(SolrQuery solrQuery) {

        this.solrQuery = solrQuery;
    }

    public QueryResponse getSolrResponse() {

        return solrResponse;
    }

    public void setSolrResponse(QueryResponse solrResponse) {

        this.solrResponse = solrResponse;
    }

    public List<T> getEntityList() {

        return entityList;
    }

    public void setEntityList(List<T> entityList) {

        this.entityList = entityList;
    }

    public Map<String, List<String>> getExcludedOptions() {

        return excludedOptions;
    }

    public void setExcludedOptions(Map<String, List<String>> excludedOptions) {

        this.excludedOptions = excludedOptions;
    }

    public Map<String, List<String>> excludedOptions() {

        Map<String, List<String>> excludedOptions = new HashMap<>();
        Optional<List<FacetField>> facetFields = Optional.ofNullable(this.getSolrResponse().getFacetFields());
        facetFields.ifPresent(fields -> {
            fields.forEach(e -> {
                List<String> emptyValues = e.getValues().stream()
                        .filter(v -> v.getCount() == 0)
                        .map(FacetField.Count::getName)
                        .collect(Collectors.toList());
                excludedOptions.put(e.getName(), emptyValues);
            });
        });
        return excludedOptions;
    }
}
