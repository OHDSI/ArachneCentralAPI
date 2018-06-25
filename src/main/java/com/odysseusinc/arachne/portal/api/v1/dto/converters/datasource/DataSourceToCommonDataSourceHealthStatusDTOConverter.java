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
 * Created: September 05, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters.datasource;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonDataSourceHealthStatusDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.converters.BaseConversionServiceAwareConverter;
import com.odysseusinc.arachne.portal.model.DataSource;
import org.springframework.stereotype.Component;

@Component
public class DataSourceToCommonDataSourceHealthStatusDTOConverter
        extends BaseConversionServiceAwareConverter<DataSource, CommonDataSourceHealthStatusDTO> {

    @Override
    public CommonDataSourceHealthStatusDTO convert(DataSource source) {

        final CommonDataSourceHealthStatusDTO dataSourceHealthStatusDTO = new CommonDataSourceHealthStatusDTO();
        dataSourceHealthStatusDTO.setCentralId(source.getId());
        dataSourceHealthStatusDTO.setDeleted(source.getDeleted() != null);
        dataSourceHealthStatusDTO.setStatus(source.getHealthStatus());
        dataSourceHealthStatusDTO.setReason(source.getHealthStatusDescription());
        return dataSourceHealthStatusDTO;
    }
}
