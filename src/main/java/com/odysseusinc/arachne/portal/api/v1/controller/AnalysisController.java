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
 * Created: January 13, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.controller;


import com.odysseusinc.arachne.portal.api.v1.dto.AnalysisCreateDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.AnalysisDTO;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.service.BaseDataNodeService;
import com.odysseusinc.arachne.portal.service.DataReferenceService;
import com.odysseusinc.arachne.portal.service.ImportService;
import com.odysseusinc.arachne.portal.service.ToPdfConverter;
import com.odysseusinc.arachne.portal.service.analysis.AnalysisFilesSavingService;
import com.odysseusinc.arachne.portal.service.analysis.BaseAnalysisService;
import com.odysseusinc.arachne.portal.service.analysis.heracles.HeraclesAnalysisService;
import com.odysseusinc.arachne.portal.service.submission.BaseSubmissionService;
import com.odysseusinc.arachne.portal.service.submission.SubmissionInsightService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@SuppressWarnings("unused")
@RestController
public class AnalysisController extends BaseAnalysisController<Analysis, AnalysisDTO, DataNode, AnalysisCreateDTO> {

    public static final String RUN_PLP_ANALYSIS_FILE_NAME = "run_plp_analysis.r";
    public static final String RUN_IR_ANALYSIS_FILE_NAME = "run_ir_analysis.r";


    public AnalysisController(
            BaseDataNodeService baseDataNodeService,
            BaseAnalysisService analysisService,
            DataReferenceService dataReferenceService,
            GenericConversionService conversionService,
            SimpMessagingTemplate wsTemplate,
            JmsTemplate jmsTemplate,
            ImportService importService,
            BaseSubmissionService submissionService,
            ToPdfConverter toPdfConverter,
            SubmissionInsightService submissionInsightService,
            HeraclesAnalysisService heraclesAnalysisService,
            AnalysisFilesSavingService analysisFilesSavingService) {

        super(analysisService,
                submissionService,
                dataReferenceService,
                jmsTemplate,
                conversionService,
                baseDataNodeService,
                importService,
                wsTemplate,
                toPdfConverter,
                submissionInsightService,
                heraclesAnalysisService,
                analysisFilesSavingService);
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

        files.add(new MockMultipartFile(RUN_PLP_ANALYSIS_FILE_NAME, RUN_PLP_ANALYSIS_FILE_NAME, null,
                readResource("r/" + RUN_PLP_ANALYSIS_FILE_NAME)));
    }


    private String getPath(Resource resource) throws IOException {
        if (resource instanceof ClassPathResource) {
            return ((ClassPathResource)resource).getPath();
        } else if (resource instanceof FileSystemResource) {
            return ((FileSystemResource)resource).getPath();
        } else {
            return resource.getFile().getPath();
        }
    }

}


