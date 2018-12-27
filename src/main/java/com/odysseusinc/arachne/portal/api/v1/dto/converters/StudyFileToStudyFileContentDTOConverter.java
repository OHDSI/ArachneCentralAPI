/*
 *  Copyright 2018 Odysseus Data Services, inc.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Company: Odysseus Data Services, Inc.
 *  Product Owner/Architecture: Gregory Klebanov
 *  Authors: Anton Gackovka
 *  Created: October 18, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.portal.api.v1.dto.StudyFileContentDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UserInfoDTO;
import com.odysseusinc.arachne.portal.model.StudyFile;
import org.springframework.stereotype.Component;

@Component
public class StudyFileToStudyFileContentDTOConverter extends BaseConversionServiceAwareConverter<StudyFile, StudyFileContentDTO> {


    @Override
    public StudyFileContentDTO convert(StudyFile source) {

        StudyFileContentDTO fileContentDTO = new StudyFileContentDTO();
        fileContentDTO.setUuid(source.getUuid());
        fileContentDTO.setName(source.getRealName());
        fileContentDTO.setCreated(source.getCreated());
        fileContentDTO.setLabel(source.getLabel());
        fileContentDTO.setStudyId(source.getStudy().getId());
        fileContentDTO.setStudyLabel(source.getStudy().getTitle());
        fileContentDTO.setAuthor(conversionService.convert(source.getAuthor(), UserInfoDTO.class));

        fileContentDTO.setDocType(source.getContentType());
        fileContentDTO.setStudyId(source.getStudy().getId());

        return fileContentDTO;
    }
}
