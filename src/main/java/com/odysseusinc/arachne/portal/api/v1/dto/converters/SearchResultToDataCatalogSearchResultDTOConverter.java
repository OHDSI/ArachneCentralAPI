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

import com.odysseusinc.arachne.portal.api.v1.dto.DataCatalogSearchResultDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.DataSourceDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.FacetOptionList;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class SearchResultToDataCatalogSearchResultDTOConverter extends BaseSearchResultToDataCatalogSearchResultDTOConverter<DataSourceDTO, DataCatalogSearchResultDTO> {
    @Override
    protected DataCatalogSearchResultDTO newDataCatalogSearchResultDTO(List<DataSourceDTO> dataSourceDTOS,
                                                                       Map<String, FacetOptionList> stringFacetOptionListMap,
                                                                       PageRequest pageRequest, long total) {

        return new DataCatalogSearchResultDTO(dataSourceDTOS, stringFacetOptionListMap, pageRequest, total);
    }

    @Override
    protected Class<DataSourceDTO> getDataSourceDTOClass() {

        return DataSourceDTO.class;
    }
}
