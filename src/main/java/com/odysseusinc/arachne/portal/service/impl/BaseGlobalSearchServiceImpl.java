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
import com.odysseusinc.arachne.portal.api.v1.dto.SearchGlobalDTO;
import com.odysseusinc.arachne.portal.model.solr.SolrCollection;
import com.odysseusinc.arachne.portal.model.solr.SolrEntityCreatedEvent;
import com.odysseusinc.arachne.portal.service.BaseGlobalSearchService;
import com.odysseusinc.arachne.portal.service.BaseSolrService;
import com.odysseusinc.arachne.portal.service.impl.solr.SearchResult;
import com.odysseusinc.arachne.portal.service.impl.solr.SolrField;
import java.io.IOException;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
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
    //TODO refactor, move out converting code
    public GlobalSearchResultDTO search(final Long userId, final SearchGlobalDTO searchDTO) throws SolrServerException, NoSuchFieldException, IOException {

        searchDTO.setCollections(SolrCollection.names());
        searchDTO.setResultFields(ID, TITLE, TYPE, BREADCRUMBS);

        final SolrQuery query = conversionService.convert(searchDTO, SolrQuery.class);
        query.setQuery(buildSearchString(searchDTO.getQuery(), userId));
        
        // We use STUDIES here as "default" collection also because something should be mentioned in url
        final QueryResponse response = solrService.search(SolrCollection.STUDIES.getName(), query, Boolean.TRUE);

        final SearchResult<SolrDocument> searchResult = new SearchResult<>(query, response, response.getResults());

        return conversionService.convert(
                searchResult,
                GlobalSearchResultDTO.class
        );
    }

    private String buildSearchString(final String query, final Long userId) {

        //TODO here can be used Lucene.queryBuilder, will be done later
        return String.format("(is_public:true OR (is_public:false AND participants:%d)) AND query:*%s*", userId, query);
    }

    @EventListener
    public void update(final SolrEntityCreatedEvent event) {

        solrService.indexBySolr(event.getSolrEntity());
    }
}
