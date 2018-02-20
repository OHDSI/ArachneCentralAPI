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
 * Created: February 15, 2018
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import static com.odysseusinc.arachne.portal.service.BaseSolrService.ANALYSES_COLLECTION;
import static com.odysseusinc.arachne.portal.service.BaseSolrService.DATA_SOURCE_COLLECTION;
import static com.odysseusinc.arachne.portal.service.BaseSolrService.STUDIES_COLLECTION;
import static com.odysseusinc.arachne.portal.service.BaseSolrService.USER_COLLECTION;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odysseusinc.arachne.portal.api.v1.dto.BreadcrumbDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.GlobalSearchDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.GlobalSearchResultDTO;
import com.odysseusinc.arachne.portal.service.BaseSolrService;
import com.odysseusinc.arachne.portal.service.impl.solr.SearchResult;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.solr.common.SolrDocument;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;

@Component
public class GlobalSearchResultToGlobalSearchResultDTOConverter
        extends SearchResultToFacetedSearchResultDTOConverter
        implements Converter<SearchResult<SolrDocument>, GlobalSearchResultDTO>, InitializingBean {

    @Autowired
    private GenericConversionService conversionService;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterPropertiesSet() throws Exception {

        conversionService.addConverter(this);
    }

    @Override
    public GlobalSearchResultDTO convert(final SearchResult<SolrDocument> source) {
        return new GlobalSearchResultDTO(
                buildContent(source),
                buildPageRequest(source),
                getTotal(source)
        );
    }

    @Override
    protected List<GlobalSearchDTO> buildContent(final SearchResult source) {

        return source.getSolrResponse().getResults().stream().map(v -> {
            GlobalSearchDTO dto = new GlobalSearchDTO();

            final String title = getValue(v, BaseSolrService.TITLE);
            dto.setTitle(title);
            dto.setId(getValue(v, BaseSolrService.ID));
            dto.setLabel(getLabel(getValue(v, BaseSolrService.TYPE)));

            List<BreadcrumbDTO> breadcrumbs = getBreadCrumbs(v);
            // last element of breadcrumbs should be with solr title
            breadcrumbs.get(breadcrumbs.size()-1).setTitle(title);
            dto.setBreadcrumbs(breadcrumbs);

            return dto;
        }).collect(Collectors.toList());
    }

    private List<BreadcrumbDTO> getBreadCrumbs(final SolrDocument document)  {

        final List<BreadcrumbDTO> result;
        try {
            result = objectMapper.readValue(getValue(document, BaseSolrService.BREADCRUMBS), new TypeReference<List<BreadcrumbDTO>>(){});
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        return result;
    }

    private String getValue(final SolrDocument document, final String field) {

        return String.valueOf(document.get(field));
    }

    private String getLabel(final String type) {

        switch(type) {
            case USER_COLLECTION:
                return "Users";
            case STUDIES_COLLECTION:
                return "Studies";
            case DATA_SOURCE_COLLECTION:
                return "Data Sources";
            case ANALYSES_COLLECTION:
                return "Analyses";
            default:
                throw new IllegalArgumentException("There is no Solr domain type for " + type);
        }
    }
}
