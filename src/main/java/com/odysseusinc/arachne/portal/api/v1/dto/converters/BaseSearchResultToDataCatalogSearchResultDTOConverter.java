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
 * Created: September 08, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonBaseDataSourceDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.FacetOptionList;
import com.odysseusinc.arachne.portal.api.v1.dto.FacetedSearchResultDTO;
import com.odysseusinc.arachne.portal.model.DataSource;
import com.odysseusinc.arachne.portal.service.BaseDataSourceService;
import com.odysseusinc.arachne.portal.service.impl.solr.FieldList;
import com.odysseusinc.arachne.portal.service.impl.solr.SearchResult;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("unused")
public abstract class BaseSearchResultToDataCatalogSearchResultDTOConverter<DS_DTO extends CommonBaseDataSourceDTO, D extends FacetedSearchResultDTO<?>>
        extends SearchResultToFacetedSearchResultDTOConverter
        implements Converter<SearchResult<? extends DataSource>, D>, InitializingBean {

    @Autowired
    protected GenericConversionService conversionService;

    @Autowired
    protected BaseDataSourceService dataSourceService;

    @Override
    public void afterPropertiesSet() throws Exception {

        conversionService.addConverter(this);
    }

    protected FieldList getSolrFields() {

        return dataSourceService.getSolrFields();
    }

    protected List<DS_DTO> buildContent(SearchResult source) {

        return (List<DS_DTO>) source.getEntityList().stream()
                .map(dataSource -> conversionService.convert(dataSource, getDataSourceDTOClass()))
                .collect(Collectors.toList());
    }


    @Override
    public D convert(SearchResult<? extends DataSource> source) {

        return newDataCatalogSearchResultDTO(
                buildContent(source),
                buildFacets(source, getSolrFields()),
                buildPageRequest(source),
                getTotal(source)
        );
    }

    protected abstract D newDataCatalogSearchResultDTO(List<DS_DTO> dataSourceDTOS,
                                                       Map<String, FacetOptionList> stringFacetOptionListMap,
                                                       PageRequest pageRequest, long total);

    protected abstract Class<DS_DTO> getDataSourceDTOClass();

}
