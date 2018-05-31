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
 * Created: May 03, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.portal.api.v1.dto.AnalysisDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.Commentable;
import com.odysseusinc.arachne.portal.api.v1.dto.CommentableResultFileDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.CommentableSubmissionFileDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.DataSourceDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.PermissionsDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.ShortUserDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.StudyShortDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.SubmissionDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.SubmissionInsightDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.SubmissionStatusDTO;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.model.SubmissionInsight;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import com.odysseusinc.arachne.portal.api.v1.dto.converters.BaseConversionServiceAwareConverter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;

@Component
public class SubmissionInsightToSubmissionInsightDTOConverter extends BaseConversionServiceAwareConverter<SubmissionInsight, SubmissionInsightDTO> {

    @Override
    public SubmissionInsightDTO convert(SubmissionInsight source) {

        final Submission submission = source.getSubmission();
        final SubmissionInsightDTO dto = new SubmissionInsightDTO();

        SubmissionDTO shortSubmissionDTO = new SubmissionDTO();
        shortSubmissionDTO.setId(submission.getId());
        shortSubmissionDTO.setCreatedAt(submission.getCreated());
        shortSubmissionDTO.setStatus(conversionService.convert(submission.getStatus(), SubmissionStatusDTO.class));
        shortSubmissionDTO.setAuthor(conversionService.convert(submission.getAuthor(), ShortUserDTO.class));

        dto.setCreated(source.getCreated());
        dto.setName(source.getName());
        dto.setDescription(source.getDescription());
        dto.setSubmission(shortSubmissionDTO);
        final List<Commentable> submissionFileDTOS
                = source.getSubmissionInsightSubmissionFiles()
                .stream()
                .map(submissionFile -> conversionService.convert(submissionFile, CommentableSubmissionFileDTO.class))
                .collect(Collectors.toList());
        dto.setCodeFiles(submissionFileDTOS);
        final List<Commentable> resultFileDTOS = submission.getResultFiles()
                .stream()
                .map(resultFile -> conversionService.convert(resultFile, CommentableResultFileDTO.class))
                .collect(Collectors.toList());
        dto.setResultFiles(resultFileDTOS);
        dto.setDataSource(conversionService.convert(submission.getDataSource(), DataSourceDTO.class));
        final AnalysisDTO analysisDTO = analysisConverter(source.getSubmission().getAnalysis());
        dto.setAnalysis(analysisDTO);
        dto.setCommentsCount(source.getCommentsCount());
        dto.setPermissions(conversionService.convert(source, PermissionsDTO.class));
        return dto;
    }

    private AnalysisDTO analysisConverter(final Analysis analysis) {

        final AnalysisDTO analysisDTO = new AnalysisDTO();
        analysisDTO.setId(analysis.getId());
        analysisDTO.setTitle(analysis.getTitle());
        final StudyShortDTO studyDTO = new StudyShortDTO();
        final Study study = analysis.getStudy();
        studyDTO.setId(study.getId());
        studyDTO.setTitle(study.getTitle());
        analysisDTO.setStudy(studyDTO);
        return analysisDTO;
    }
}
