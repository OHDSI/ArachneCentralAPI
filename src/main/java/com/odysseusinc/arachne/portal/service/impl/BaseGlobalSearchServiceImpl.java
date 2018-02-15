/*
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
 * Authors: Anton Gackovka
 * Created: February 12, 2018
 */

package com.odysseusinc.arachne.portal.service.impl;

import static com.odysseusinc.arachne.portal.service.BaseSolrService.*;

import com.odysseusinc.arachne.portal.api.v1.dto.GlobalSearchResultDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.SearchExpertListDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.SearchGlobalDTO;
import com.odysseusinc.arachne.portal.service.BaseGlobalSearchService;
import com.odysseusinc.arachne.portal.service.BaseSolrService;
import com.odysseusinc.arachne.portal.service.impl.solr.SearchResult;
import com.odysseusinc.arachne.portal.service.impl.solr.SolrField;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;

public abstract class BaseGlobalSearchServiceImpl<SF extends SolrField> implements BaseGlobalSearchService<SF> {

    private final BaseSolrService<SF> solrService;

    @Autowired
    private ConversionService conversionService;

    protected BaseGlobalSearchServiceImpl(
            final BaseSolrService<SF> solrService) {

        this.solrService = solrService;
    }

    @Override
    public GlobalSearchResultDTO search(final Long userId, final String str) throws SolrServerException, NoSuchFieldException, IOException {

        final SearchGlobalDTO searchDTO = new SearchGlobalDTO();

        searchDTO.setQuery(str);
        searchDTO.setCollections(STUDIES_COLLECTION, USER_COLLECTION, DATA_SOURCE_COLLECTION);

        searchDTO.setResultFields(ID, TITLE, TYPE);

        final SolrQuery query = conversionService.convert(searchDTO, SolrQuery.class);
        final QueryResponse response = solrService.search(STUDIES_COLLECTION, query);

        final SearchResult<SolrDocument> searchResult = new SearchResult<>(query, response, response.getResults());

        return conversionService.convert(
                searchResult,
                GlobalSearchResultDTO.class
        );
    }

    public List<Object> search(final String str, final String domain) {

        return new ArrayList<>();
    }
}
