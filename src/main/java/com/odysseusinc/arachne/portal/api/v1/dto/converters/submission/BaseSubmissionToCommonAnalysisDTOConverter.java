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
 * Created: September 18, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters.submission;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisFileDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonArachneUserDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonStudyDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.converters.BaseConversionServiceAwareConverter;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.model.SubmissionFile;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseSubmissionToCommonAnalysisDTOConverter<T extends Submission>
        extends BaseConversionServiceAwareConverter<T, CommonAnalysisDTO> {
    Logger log = LoggerFactory.getLogger(SubmissionToCommonAnalysisDTOConverter.class);

    @Override
    public CommonAnalysisDTO convert(T source) {

        final CommonAnalysisDTO dto = createResultObject();
        dto.setId(source.getId());
        final Analysis analysis = source.getSubmissionGroup().getAnalysis();
        if (analysis != null) {
            dto.setName(analysis.getTitle());
            dto.setType(analysis.getType());
            Study study = analysis.getStudy();
            if (study != null && conversionService.canConvert(study.getClass(), CommonStudyDTO.class)) {
                dto.setStudy(conversionService.convert(study, CommonStudyDTO.class));
            }
        }
        IUser author = source.getSubmissionGroup().getAuthor();
        if (author != null && conversionService.canConvert(author.getClass(), CommonArachneUserDTO.class)) {
            CommonArachneUserDTO userDTO = conversionService.convert(author, CommonArachneUserDTO.class);
            dto.setOwner(userDTO);
        }
        dto.setUpdateSubmissionStatusPassword(source.getUpdatePassword());
        dto.setCentralDataSourceId(source.getDataSource().getId());
        for (SubmissionFile submissionFile : source.getSubmissionGroup().getFiles()) {
            if (submissionFile.getExecutable() && StringUtils.isEmpty(dto.getExecutableFileName())) {
                dto.setExecutableFileName(submissionFile.getRealName());
                dto.setInnerExecutableFilename(submissionFile.getEntryPoint());
            }
        }
        dto.setAnalysisFiles(source.getSubmissionGroup()
                .getFiles()
                .stream()
                .map(file -> conversionService.convert(file, CommonAnalysisFileDTO.class))
                .collect(Collectors.toList()));

        proceedAdditionalFields(dto, source);
        return dto;
    }


    @Override
    protected CommonAnalysisDTO createResultObject() {

        return new CommonAnalysisDTO();
    }
}
