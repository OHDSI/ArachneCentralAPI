/*
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
 * Authors: Anton Gackovka
 * Created: February 12, 2018
 */

package com.odysseusinc.arachne.portal.service.impl;

import static com.odysseusinc.arachne.portal.service.BaseSolrService.*;

import com.odysseusinc.arachne.portal.api.v1.dto.GlobalSearchResultDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.SearchGlobalDTO;
import com.odysseusinc.arachne.portal.config.tenancy.TenantContext;
import com.odysseusinc.arachne.portal.model.solr.SolrCollection;
import com.odysseusinc.arachne.portal.model.solr.SolrEntityCreatedEvent;
import com.odysseusinc.arachne.portal.service.BaseGlobalSearchService;
import com.odysseusinc.arachne.portal.service.BaseSolrService;
import com.odysseusinc.arachne.portal.service.SolrService;
import com.odysseusinc.arachne.portal.service.impl.solr.SearchResult;
import com.odysseusinc.arachne.portal.service.impl.solr.SolrField;
import java.io.IOException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
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

        searchDTO.setResultFields(ID, SYSTEM_ID, TITLE, TYPE, BREADCRUMBS);

        final String[] collections = calculateCollections(searchDTO);
        searchDTO.setCollections(collections);
        final String tenantId = TenantContext.getCurrentTenant().toString();
        final SolrQuery query = conversionService.convert(searchDTO, SolrQuery.class);
        
        query.setQuery(buildSearchString(searchDTO.getQuery(), userId, tenantId));

        setupHighlight(searchDTO.getQuery(), query);
        
        final QueryResponse response = solrService.search(collections[0], query);

        final SearchResult<SolrDocument> searchResult = new SearchResult<>(query, response, response.getResults());

        return conversionService.convert(
                searchResult,
                GlobalSearchResultDTO.class
        );
    }

    private void setupHighlight(final String query, final SolrQuery solrQuery) {

        solrQuery.setHighlight(Boolean.TRUE);

        solrQuery.set("hl.q", "*" + query + "*");

        solrQuery.setHighlightFragsize(40);
        
        solrQuery.setHighlightSimplePre("[b]");
        solrQuery.setHighlightSimplePost("[/b]");
        
        solrQuery.addHighlightField("*" + SolrField.TS_POSTFIX);
        solrQuery.addHighlightField("*" + SolrField.TXT_POSTFIX);
    }

    private String[] calculateCollections(final SearchGlobalDTO searchDTO) {

        if (ArrayUtils.isEmpty(searchDTO.getCollections())) {
            return SolrCollection.names();
        } else {
            return searchDTO.getCollections();
        }
    }

    private String buildSearchString(final String query, final Long userId, final String tenantId) {


        final String q = 
                "(entity_type:studies AND (is_public:true OR (is_public:false AND participants:userId)) AND tenants:%tenantId AND query:*%query*) OR " +
                "(entity_type:papers AND (readers_ts:userId OR _query_:\"{!join from=entity_id fromIndex=studies to=study_id_txt}(participants:userId AND tenants:%tenantId)\") AND query:*%query*) OR " +
                "(entity_type:data-sources AND tenants:%tenantId AND query:*%query*) OR " +
                "(entity_type:analyses AND _query_:\"{!join from=entity_id fromIndex=studies to=study_id_txt}(participants:userId AND tenants:%tenantId)\" AND query:*%query*) OR " +
                "(entity_type:users AND tenants:%tenantId AND query:*%query*)";
        
        final String modifiedString = StringUtils.replaceEach(
                q, 
                new String[]{
                        "userId",
                        "%tenantId",
                        "%query"
                }, 
                new String[]{
                        String.valueOf(userId),
                        tenantId,
                        query
                }
        );
        return modifiedString;
    }

    @EventListener
    public void update(final SolrEntityCreatedEvent event) {

        solrService.indexBySolr(event.getSolrEntity());
    }
}
