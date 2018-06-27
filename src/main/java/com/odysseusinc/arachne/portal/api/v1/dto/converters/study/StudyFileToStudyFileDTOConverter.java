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
 * Created: September 08, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters.study;

import com.odysseusinc.arachne.portal.api.v1.dto.StudyFileDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UserInfoDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.converters.BaseConversionServiceAwareConverter;
import com.odysseusinc.arachne.portal.model.StudyFile;
import org.springframework.stereotype.Component;

@Component
public class StudyFileToStudyFileDTOConverter extends BaseConversionServiceAwareConverter<StudyFile, StudyFileDTO> {

    @Override
    public StudyFileDTO convert(StudyFile source) {

        StudyFileDTO studyFileDTO = new StudyFileDTO();
        studyFileDTO.setLabel(source.getLabel());
        studyFileDTO.setUuid(source.getUuid());
        studyFileDTO.setName(source.getRealName());
        studyFileDTO.setCreated(source.getCreated());
        studyFileDTO.setLink(source.getLink());
        studyFileDTO.setStudyId(source.getStudy().getId());
        studyFileDTO.setAuthor(conversionService.convert(source.getAuthor(), UserInfoDTO.class));
        studyFileDTO.setDocType(source.getContentType());
        studyFileDTO.setAntivirusStatus(source.getAntivirusStatus());
        studyFileDTO.setAntivirusDescription(source.getAntivirusDescription());
        return studyFileDTO;
    }
}
