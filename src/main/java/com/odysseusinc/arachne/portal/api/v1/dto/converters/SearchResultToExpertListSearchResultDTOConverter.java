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

import com.odysseusinc.arachne.portal.api.v1.dto.ExpertListSearchResultDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UserProfileDTO;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.service.BaseUserService;
import com.odysseusinc.arachne.portal.service.impl.solr.FieldList;
import com.odysseusinc.arachne.portal.service.impl.solr.SearchResult;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;

/**
 * Created by PGrafkin on 31.01.2017.
 */
@Component
@SuppressWarnings("unused")
public class SearchResultToExpertListSearchResultDTOConverter
        extends SearchResultToFacetedSearchResultDTOConverter
        implements Converter<SearchResult<User>, ExpertListSearchResultDTO>, InitializingBean {

    @Autowired
    private GenericConversionService conversionService;

    @Autowired
    private BaseUserService userService;

    @Override
    public void afterPropertiesSet() throws Exception {

        conversionService.addConverter(this);
    }

    protected FieldList getSolrFields() {
        return userService.getSolrFields();
    }

    protected List<UserProfileDTO> buildContent(SearchResult source) {
        List<UserProfileDTO> userProfileDTOList = new ArrayList<>();
        for (Object entity: source.getEntityList()) {
            User user = (User) entity;
            userProfileDTOList.add(conversionService.convert(user, UserProfileDTO.class));
        }
        return userProfileDTOList;
    }

    @Override
    public ExpertListSearchResultDTO convert(SearchResult<User> source) {
        return new ExpertListSearchResultDTO(
                buildContent(source),
                buildFacets(source, getSolrFields()),
                buildPageRequest(source),
                getTotal(source)
        );
    }

}
