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
import com.odysseusinc.arachne.commons.api.v1.dto.CommonHealthStatus;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.model.DataSource;
import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.service.BaseDataNodeService;
import com.odysseusinc.arachne.portal.service.BaseDataSourceService;
import com.odysseusinc.arachne.portal.service.BaseUserService;
import com.odysseusinc.arachne.portal.service.DataNodeService;
import com.odysseusinc.arachne.portal.service.DataSourceService;
import com.odysseusinc.arachne.portal.service.OrganizationService;
import com.odysseusinc.arachne.portal.service.StudyDataSourceService;
import com.odysseusinc.arachne.portal.service.analysis.AnalysisService;
import com.odysseusinc.arachne.portal.service.analysis.BaseAnalysisService;
import com.odysseusinc.arachne.portal.util.ArachneConverterUtils;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataNodeController extends BaseDataNodeController<IDataSource, CommonDataSourceDTO, DataNode> {
    public DataNodeController(AnalysisService analysisService,
                              DataNodeService baseDataNodeService,
                              DataSourceService dataSourceService,
                              GenericConversionService genericConversionService,
                              BaseUserService userService,
                              StudyDataSourceService studyDataSourceService,
                              ArachneConverterUtils converterUtils,
                              OrganizationService organizationService) {

        super(analysisService,
                baseDataNodeService,
                dataSourceService,
                genericConversionService,
                userService,
                studyDataSourceService,
                converterUtils,
                organizationService);
    }

    @Override
    protected Class<DataNode> getDataNodeDNClass() {

        return DataNode.class;
    }

    @Override
    protected DataNode buildEmptyDN() {

        return super.buildEmptyDataNode();
    }

    @Override
    protected DataSource convertCommonDataSourceDtoToDataSource(CommonDataSourceDTO commonDataSourceDTO) {

        DataSource ds = conversionService.convert(commonDataSourceDTO, DataSource.class);
        ds.setModelType(null);
        ds.setHealthStatus(CommonHealthStatus.GREEN);
        return ds;
    }
}
