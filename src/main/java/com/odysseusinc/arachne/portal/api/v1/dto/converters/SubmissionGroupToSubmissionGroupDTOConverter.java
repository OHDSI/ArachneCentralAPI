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
 * Created: April 26, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.portal.api.v1.dto.SubmissionDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.SubmissionGroupDTO;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.model.SubmissionGroup;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;


@Component
public class SubmissionGroupToSubmissionGroupDTOConverter extends BaseConversionServiceAwareConverter<SubmissionGroup, SubmissionGroupDTO> {

    @Override
    public SubmissionGroupDTO convert(SubmissionGroup submissionGroup) {

        SubmissionGroupDTO dto = new SubmissionGroupDTO();
        dto.setId(submissionGroup.getId());
        dto.setCreated(submissionGroup.getCreated());
        if (conversionService.canConvert(Submission.class, SubmissionDTO.class)) {

            List<SubmissionDTO> submissions = submissionGroup.getSubmissions().stream()
                    .map(s -> conversionService.convert(s, SubmissionDTO.class)).collect(Collectors.toList());
            dto.setSubmissions(submissions);
        }
        dto.setQueryFilesCount(submissionGroup.getFiles().size());
        dto.setChecksum(submissionGroup.getChecksum());
        dto.setAnalysisType(submissionGroup.getAnalysisType());
        return dto;
    }

    private void incrementSummary(Map<String, Integer> summary, String contentType) {

        Integer count = summary.getOrDefault(contentType, 0);
        summary.put(contentType, ++count);
    }
}
