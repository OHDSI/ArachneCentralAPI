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
 * Created: August 21, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import static com.odysseusinc.arachne.portal.api.v1.dto.converters.SearchDTOToSolrQuery.getFacetLabel;
import static java.lang.Long.parseLong;
import static java.util.Comparator.comparing;

import com.odysseusinc.arachne.portal.api.v1.dto.FacetOptionList;
import com.odysseusinc.arachne.portal.service.SolrService;
import com.odysseusinc.arachne.portal.service.impl.solr.FieldList;
import com.odysseusinc.arachne.portal.service.impl.solr.SearchResult;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("unused")
public abstract class SearchResultToFacetedSearchResultDTOConverter {

    protected FieldList getSolrFields() {

        return new FieldList();
    }

    protected Map<String, FacetOptionList> buildFacets(SearchResult source, FieldList solrFields) {

        Map<String, List<String>> excludedOptions = source.getExcludedOptions();
        NamedList response = source.getSolrResponse().getResponse();
        Map facetsMap = (Map) response.asMap(10).get("facets");
        Map<String, FacetOptionList> facets = new HashMap<>();

        // Facets for option list and numeric type
        List<FacetField> facetFieldList = source.getSolrResponse().getFacetFields();
        if (facetFieldList != null) {
            for (FacetField facetField : facetFieldList) {

                FacetOptionList facetOptionList = getFacetOptionList(facetsMap, facetField.getName(),
                        excludedOptions.get(facetField.getName()));
                facets.put(solrFields.getBySolrName(SolrService.facetToFieldName(facetField)).getName(), facetOptionList);
            }
        }
        return facets;
    }

    @SuppressWarnings(value = "unchecked")
    private FacetOptionList getFacetOptionList(Map facets, String facetName, List<String> excludedOptions) {

        Map facetLabel = (Map) facets.get(getFacetLabel(facetName));
        List<SimpleOrderedMap> buckets = (List<SimpleOrderedMap>) facetLabel.get("buckets");
        FacetOptionList facetOptionList = new FacetOptionList();

        boolean isNumberType = false;
        for (SimpleOrderedMap each : buckets) {

            Map entry = each.asMap(2);
            if (!isNumberType && entry.get("val") instanceof Long) {
                isNumberType = true;
            }
            final String val = entry.get("val").toString();
            if (excludedOptions != null && excludedOptions.contains(val)){
                continue;
            }
            final Object count = entry.get("count");
            facetOptionList.put(val, count);
        }
        if (isNumberType) {
            Long min = parseLong(facetOptionList.entrySet().stream()
                    .min(comparing(o -> parseLong(o.getKey()))).get().getKey());
            Long max = parseLong(facetOptionList.entrySet().stream()
                    .max(comparing(o -> parseLong(o.getKey()))).get().getKey());
            facetOptionList.setMin(min);
            facetOptionList.setMax(max);
        }
        return facetOptionList;
    }

    protected List buildContent(SearchResult source) {

        return new ArrayList();
    }

    protected PageRequest buildPageRequest(SearchResult source) {

        Integer itemsOnPage = source.getSolrQuery().getRows();
        Integer pageNum = 1;
        if (source.getSolrQuery().getStart() > 0) {
            pageNum += (source.getSolrQuery().getStart() / source.getSolrQuery().getRows());
        }
        return new PageRequest(pageNum, itemsOnPage);
    }

    protected long getTotal(SearchResult source) {

        return source.getSolrResponse().getResults().getNumFound();
    }
}
