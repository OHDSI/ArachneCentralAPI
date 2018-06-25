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
 * Created: May 19, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.odysseusinc.arachne.portal.model.DataSource;
import com.odysseusinc.arachne.portal.repository.AchillesReportRepository;
import com.odysseusinc.arachne.portal.repository.BaseDataSourceRepository;
import com.odysseusinc.arachne.portal.repository.DataNodeRepository;
import com.odysseusinc.arachne.portal.repository.RawDataSourceRepository;
import com.odysseusinc.arachne.portal.service.AchillesService;
import com.odysseusinc.arachne.portal.util.ArachneConverterUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/api/v1/achilles")
public class AchillesController extends BaseAchillesController<DataSource> {

    @Autowired
    public AchillesController(AchillesService achillesService,
                              BaseDataSourceRepository dataSourceRepository,
                              RawDataSourceRepository rawDataSourceRepository,
                              GenericConversionService conversionService,
                              ObjectMapper objectMapper,
                              DataNodeRepository dataNodeRepository,
                              AchillesReportRepository achillesReportRepository,
                              ArachneConverterUtils converterUtils) {

        super(dataSourceRepository,
                rawDataSourceRepository,
                dataNodeRepository,
                converterUtils,
                achillesService,
                objectMapper,
                achillesReportRepository,
                conversionService);

    }


}
