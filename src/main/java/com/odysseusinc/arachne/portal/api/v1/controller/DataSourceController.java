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
 * Created: January 13, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.controller;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonDataSourceDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.DataCatalogSearchResultDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.DataSourceDTO;
import com.odysseusinc.arachne.portal.model.DataSource;
import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.service.DataSourceService;
import com.odysseusinc.arachne.portal.service.StudyDataSourceService;
import com.odysseusinc.arachne.portal.util.ArachneConverterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SuppressWarnings("unused")
public class DataSourceController
        extends BaseDataSourceController<IDataSource, CommonDataSourceDTO, DataSourceDTO, DataCatalogSearchResultDTO> {

    Logger log = LoggerFactory.getLogger(DataSourceController.class);

    @Autowired
    public DataSourceController(final GenericConversionService conversionService,
                                final DataSourceService dataSourceService,
                                final ArachneConverterUtils converterUtils,
                                final StudyDataSourceService studyDataSourceService
    ) {

        super(conversionService,
                dataSourceService,
                converterUtils,
                studyDataSourceService);
    }

    @Override
    protected Class<DataSourceDTO> getDataSourceDTOClass() {

        return DataSourceDTO.class;
    }

    @Override
    protected Class<DataCatalogSearchResultDTO> getSearchResultClass() {

        return DataCatalogSearchResultDTO.class;
    }

    @Override
    protected Class<CommonDataSourceDTO> getDTOClass() {

        return CommonDataSourceDTO.class;
    }


    @Override
    protected DataSource convertDTOToDataSource(CommonDataSourceDTO dto) {

        return this.conversionService.convert(dto, DataSource.class);
    }

    @Override
    protected CommonDataSourceDTO convertDataSourceToDTO(IDataSource dataSource) {

        return this.conversionService.convert(dataSource, CommonDataSourceDTO.class);
    }

}
