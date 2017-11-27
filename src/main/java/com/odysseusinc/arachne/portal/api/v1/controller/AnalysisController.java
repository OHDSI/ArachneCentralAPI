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

import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
import com.odysseusinc.arachne.commons.utils.CommonFileUtils;
import com.odysseusinc.arachne.portal.api.v1.dto.AnalysisCreateDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.AnalysisDTO;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.service.BaseDataNodeService;
import com.odysseusinc.arachne.portal.service.BaseDataSourceService;
import com.odysseusinc.arachne.portal.service.DataReferenceService;
import com.odysseusinc.arachne.portal.service.ImportService;
import com.odysseusinc.arachne.portal.service.analysis.BaseAnalysisService;
import com.odysseusinc.arachne.portal.service.submission.BaseSubmissionService;
import java.io.IOException;
import java.util.List;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@SuppressWarnings("unused")
@RestController
public class AnalysisController extends BaseAnalysisController<Analysis, AnalysisDTO, DataNode, AnalysisCreateDTO> {

    public static final String RUN_CC_REPORTS_FILE_NAME = "run_cc_reports.r";

    static {
        ANALISYS_MIMETYPE_MAP.put(CommonAnalysisType.COHORT, CommonFileUtils.TYPE_COHORT_SQL);
        ANALISYS_MIMETYPE_MAP.put(CommonAnalysisType.ESTIMATION, CommonFileUtils.TYPE_ESTIMATION);
    }

    public AnalysisController(
            BaseDataSourceService dataSourceService,
            BaseDataNodeService baseDataNodeService,
            BaseAnalysisService analysisService,
            DataReferenceService dataReferenceService,
            GenericConversionService conversionService,
            SimpMessagingTemplate wsTemplate,
            JmsTemplate jmsTemplate,
            ImportService importService,
            BaseSubmissionService submissionService) {

        super(analysisService,
                submissionService,
                dataReferenceService,
                jmsTemplate,
                conversionService,
                baseDataNodeService,
                dataSourceService,
                importService,
                wsTemplate);
    }

    @Override
    protected Class<Analysis> getAnalysisClass() {

        return Analysis.class;
    }

    @Override
    protected Class<AnalysisDTO> getAnalysisDTOClass() {

        return AnalysisDTO.class;
    }

    @Override
    protected void attachPredictionFiles(List<MultipartFile> files) throws IOException {

        files.add(new MockMultipartFile("run_plp_analysis.r", "run_plp_analysis.r", null,
                readResource("r/run_plp_analysis.R")));
    }

    @Override
    protected void attachCohortCharacterizationFiles(List<MultipartFile> files) throws IOException {

        files.add(new MockMultipartFile(RUN_CC_REPORTS_FILE_NAME, RUN_CC_REPORTS_FILE_NAME, null,
                readResource("r/run_cc_reports.R")));
        // TODO ADD SQLs
    }

}


