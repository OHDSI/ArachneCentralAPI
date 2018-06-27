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
 * Created: September 14, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.controller.submission;

import com.odysseusinc.arachne.portal.api.v1.dto.SubmissionDTO;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.service.ToPdfConverter;
import com.odysseusinc.arachne.portal.service.analysis.BaseAnalysisService;
import com.odysseusinc.arachne.portal.service.submission.SubmissionInsightService;
import com.odysseusinc.arachne.portal.service.submission.SubmissionService;
import com.odysseusinc.arachne.portal.util.ContentStorageHelper;
import com.odysseusinc.arachne.storage.service.ContentStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SubmissionController extends BaseSubmissionController<Submission, Analysis, SubmissionDTO> {

    @Autowired
    public SubmissionController(BaseAnalysisService<Analysis> analysisService,
                                SubmissionService submissionService,
                                ToPdfConverter toPdfConverter,
                                SubmissionInsightService submissionInsightService,
                                ContentStorageService contentStorageService,
                                ContentStorageHelper contentStorageHelper) {

        super(analysisService, submissionService, toPdfConverter, submissionInsightService, contentStorageService, contentStorageHelper);
    }

    @Override
    protected Class<SubmissionDTO> getSubmissionDTOClass() {

        return SubmissionDTO.class;
    }

    @Override
    protected Class<Submission> getSubmissionClass() {

        return Submission.class;
    }
}
