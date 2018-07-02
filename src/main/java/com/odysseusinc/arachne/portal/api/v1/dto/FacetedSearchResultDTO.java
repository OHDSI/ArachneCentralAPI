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

import com.odysseusinc.arachne.portal.api.v1.controller.CustomPageImpl;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Pageable;

/**
 * Created by PGrafkin on 31.01.2017.
 */
public class FacetedSearchResultDTO<T> extends CustomPageImpl<T> {

    private Map<String, FacetOptionList> facets;

    public FacetedSearchResultDTO(List<T> content, Map<String, FacetOptionList> facets, Pageable pageable, long total) {

        super(content, pageable, total);
        this.facets = facets;
    }

    public Map<String, FacetOptionList> getFacets() {

        return facets;
    }

    public void setFacets(Map<String, FacetOptionList> facets) {

        this.facets = facets;
    }
}
