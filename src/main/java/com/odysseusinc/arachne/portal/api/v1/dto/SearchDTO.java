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
 * Created: February 01, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto;

import com.odysseusinc.arachne.portal.service.SolrService;
import java.util.Map;

public class SearchDTO extends PageDTO {

    protected String sort;
    protected String order;

    protected String query;

    protected String[] collections;
    protected String[] resultFields = { SolrService.ID, SolrService.SYSTEM_ID };

    private boolean fullFacetsQuery = false;

    private Map<String, Object> filter;

    public SearchDTO() {

        this.page = 1;
        this.pageSize = 10;
    }

    public SearchDTO(boolean fullFacetsQuery) {

        this();
        this.fullFacetsQuery = fullFacetsQuery;
    }

    public String getSort() {

        return sort;
    }

    public void setSort(String sort) {

        this.sort = sort;
    }

    public String getOrder() {

        return order;
    }

    public void setOrder(String order) {

        this.order = order;
    }

    public String getQuery() {

        return query;
    }

    public void setQuery(String query) {

        this.query = query;
    }

    public Map<String, Object> getFilter() {

        return filter;
    }

    public void setFilter(Map<String, Object> filter) {

        this.filter = filter;
    }

    public boolean isFullFacetsQuery() {

        return fullFacetsQuery;
    }

    public void setFullFacetsQuery(final boolean fullFacetsQuery) {

        this.fullFacetsQuery = fullFacetsQuery;
    }

    public String[] getCollections() {

        return collections;
    }

    public void setCollections(final String... collections) {

        this.collections = collections;
    }

    public String[] getResultFields() {

        return resultFields;
    }

    public void setResultFields(final String... resultFields) {

        this.resultFields = resultFields;
    }
}
