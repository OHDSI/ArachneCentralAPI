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

import com.odysseusinc.arachne.commons.api.v1.dto.CommonBaseDataSourceDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.converters.BaseConversionServiceAwareConverter;
import com.odysseusinc.arachne.portal.model.IDataSource;

public abstract class BaseDataSourceToCommonDataSourceDTOConverter<DS extends IDataSource, DTO extends CommonBaseDataSourceDTO>
        extends BaseConversionServiceAwareConverter<DS, DTO> {

    @Override
    public DTO convert(DS dataSource) {

        if (dataSource == null) {
            return null;
        }

        DTO commonDataSourceDTO = createResultObject();
        commonDataSourceDTO.setId(dataSource.getId());
        commonDataSourceDTO.setUuid(dataSource.getUuid());
        commonDataSourceDTO.setName(dataSource.getName());
        commonDataSourceDTO.setModelType(dataSource.getModelType());
        commonDataSourceDTO.setCdmVersion(dataSource.getCdmVersion());

        proceedAdditionalFields(commonDataSourceDTO, dataSource);
        commonDataSourceDTO.setPublished(dataSource.getPublished());
        commonDataSourceDTO.setDbmsType(dataSource.getDbmsType());
        return commonDataSourceDTO;

    }
}
