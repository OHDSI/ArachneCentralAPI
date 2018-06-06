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

import com.odysseusinc.arachne.portal.api.v1.dto.CreateStudyDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.StudyDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.StudyListDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.WorkspaceDTO;
import com.odysseusinc.arachne.portal.model.AbstractUserStudyListItem;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.DataSource;
import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.StudyViewItem;
import com.odysseusinc.arachne.portal.model.search.StudySearch;
import com.odysseusinc.arachne.portal.model.statemachine.study.StudyStateMachine;
import com.odysseusinc.arachne.portal.service.StudyFileService;
import com.odysseusinc.arachne.portal.service.StudyService;
import com.odysseusinc.arachne.portal.service.StudyTypeService;
import com.odysseusinc.arachne.portal.service.submission.SubmissionInsightService;
import com.odysseusinc.arachne.portal.service.analysis.BaseAnalysisService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

@SuppressWarnings("unused")
@RestController
public class StudyController extends BaseStudyController<Study, IDataSource, Analysis, StudyDTO, WorkspaceDTO, StudySearch, StudyViewItem, StudyListDTO> {
    public StudyController(StudyService studyService,
                           BaseAnalysisService<Analysis> analysisService,
                           GenericConversionService conversionService,
                           SimpMessagingTemplate wsTemplate,
                           StudyFileService fileService,
                           StudyStateMachine studyStateMachine,
                           SubmissionInsightService submissionInsightService,
                           StudyTypeService studyTypeService) {

        super(studyService,
                analysisService,
                conversionService,
                wsTemplate,
                fileService,
                studyStateMachine,
                submissionInsightService,
                studyTypeService);
    }

    @Override
    public Study convert(CreateStudyDTO studyDTO) {

        return conversionService.convert(studyDTO, Study.class);
    }

    @Override
    public Study convert(StudyDTO studyDto) {

        return conversionService.convert(studyDto, Study.class);
    }

    @Override
    protected StudyDTO convert(StudyViewItem myStudy) {

        return conversionService.convert(myStudy, StudyDTO.class);
    }

    @Override
    protected StudyDTO convertStudyToStudyDTO(Study study) {

        return conversionService.convert(study, StudyDTO.class);
    }

    @Override
    protected WorkspaceDTO convertStudyToWorkspaceDTO(Study study) {

        return conversionService.convert(study, WorkspaceDTO.class);
    }

    @Override
    protected StudyListDTO convertListItem(AbstractUserStudyListItem userStudyItem) {

        return conversionService.convert(userStudyItem, StudyListDTO.class);
    }
}
