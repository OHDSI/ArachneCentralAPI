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
 * Created: February 15, 2018
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odysseusinc.arachne.portal.api.v1.dto.BreadcrumbDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.GlobalSearchDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.GlobalSearchResultDTO;
import com.odysseusinc.arachne.portal.model.solr.SolrCollection;
import com.odysseusinc.arachne.portal.service.BaseSolrService;
import com.odysseusinc.arachne.portal.service.impl.solr.SearchResult;
import com.odysseusinc.arachne.portal.service.impl.solr.SolrField;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrDocument;
import org.jetbrains.annotations.NotNull;
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

        final Map<String, Map<String, List<String>>> hlMap = source.getSolrResponse().getHighlighting();

        return source.getSolrResponse().getResults().stream().map(v -> {
            GlobalSearchDTO dto = new GlobalSearchDTO();

            final String title = getValue(v, BaseSolrService.TITLE);
            dto.setId(getValue(v, BaseSolrService.ID));
            dto.setLabel(getLabel(getValue(v, BaseSolrService.TYPE)));

            List<BreadcrumbDTO> breadcrumbs = getBreadCrumbs(v);
            // last element of breadcrumbs should be with solr title
            breadcrumbs.get(breadcrumbs.size()-1).setTitle(title);
            dto.setBreadcrumbs(breadcrumbs);

            String id = Optional.ofNullable(getValue(v, BaseSolrService.SYSTEM_ID)).orElseThrow(() -> new IllegalArgumentException("Solr document must contain field ID"));
            
            hlMap.get(id)
                    .forEach((key, value) -> dto.addHighlight(
                            getFieldNameWithoutPostfix(key),
                            getFieldValue(value)
                    ));

            return dto;
        }).collect(Collectors.toList());
    }

    private String getFieldValue(final List<String> value) {
            
        if (value == null) {
            return null;
        }
        
        
        return value.stream().findFirst().orElseThrow(() -> new IllegalArgumentException("Found value cannot be null"));
    }

    private String getFieldNameWithoutPostfix(final String key) {

        return StringUtils.replace(StringUtils.remove(StringUtils.remove(key, SolrField.TXT_POSTFIX), SolrField.TS_POSTFIX), "_", " ");
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

        return SolrCollection.getByCollectionName(type).getTitle();
    }
}
