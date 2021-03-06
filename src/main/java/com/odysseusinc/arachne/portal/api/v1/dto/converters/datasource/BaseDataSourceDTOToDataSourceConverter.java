/*
 *
 * Copyright 2018 Odysseus Data Services, inc.
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

public abstract class BaseDataSourceDTOToDataSourceConverter<DTO extends CommonBaseDataSourceDTO, DS extends IDataSource> extends BaseConversionServiceAwareConverter<DTO, DS> {

    @Override
    public final DS convert(DTO source) {

        if (source == null) {
            return null;
        }

        DS dataSource = createResultObject();
        dataSource.setId(source.getId());
        dataSource.setName(source.getName());
        dataSource.setModelType(source.getModelType());
        dataSource.setCdmVersion(source.getCdmVersion());
        dataSource.setPublished(source.getPublished());
        dataSource.setDbmsType(source.getDbmsType());
        dataSource.setAccessType(source.getAccessType());

        proceedAdditionalFields(dataSource, source);

        return dataSource;
    }
}
