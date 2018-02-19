/*
 *
 * Copyright 2017 Observational Health Data Sciences and Informatics
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

import com.odysseusinc.arachne.commons.api.v1.dto.CommonDataNodeRegisterDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonDataSourceDTO;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.BaseDataSource;
import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.model.DataSource;
import com.odysseusinc.arachne.portal.model.RawDataSource;
import com.odysseusinc.arachne.portal.service.BaseDataNodeService;
import com.odysseusinc.arachne.portal.service.BaseDataSourceService;
import com.odysseusinc.arachne.portal.service.BaseUserService;
import com.odysseusinc.arachne.portal.service.StudyDataSourceService;
import com.odysseusinc.arachne.portal.service.analysis.BaseAnalysisService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataNodeController extends BaseDataNodeController<BaseDataSource, RawDataSource, DataSource, CommonDataSourceDTO, DataNode> {
    public DataNodeController(BaseAnalysisService<Analysis> analysisService,
                              BaseDataNodeService<DataNode> baseDataNodeService,
                              BaseDataSourceService<BaseDataSource, RawDataSource, DataSource> dataSourceService,
                              GenericConversionService genericConversionService,
                              BaseUserService userService,
                              StudyDataSourceService studyDataSourceService) {

        super(analysisService,
                baseDataNodeService,
                dataSourceService,
                genericConversionService,
                userService,
                studyDataSourceService);
    }

    @Override
    protected DataNode convertRegisterDtoToDataNode(CommonDataNodeRegisterDTO commonDataNodeRegisterDTO) {

        return conversionService.convert(commonDataNodeRegisterDTO, DataNode.class);
    }

    @Override
    protected DataSource convertCommonDataSourceDtoToDataSource(CommonDataSourceDTO commonDataSourceDTO) {

        return conversionService.convert(commonDataSourceDTO, DataSource.class);
    }
}
