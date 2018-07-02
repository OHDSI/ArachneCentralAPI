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
 * Created: September 11, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters.study;

import com.odysseusinc.arachne.portal.api.v1.dto.ParticipantDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.PermissionsDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.StudyDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.dictionary.StudyStatusDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.dictionary.StudyTypeDTO;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.UserStudyExtended;
import com.odysseusinc.arachne.portal.model.security.Tenant;
import com.odysseusinc.arachne.portal.service.BaseStudyService;
import com.odysseusinc.arachne.portal.service.analysis.AnalysisService;
import com.odysseusinc.arachne.portal.util.ArachneConverterUtils;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseStudyToStudyDTOConverter<S extends Study, DTO extends StudyDTO> extends BaseStudyToCommonStudyDTOConverter<S, DTO> {

    @Autowired
    public BaseStudyToStudyDTOConverter(BaseStudyService studyService, AnalysisService analysisService, ArachneConverterUtils converterUtils) {

        super(studyService, analysisService, converterUtils);
    }

    @Override
    public DTO convert(final S source) {

        final DTO studyDTO = super.convert(source);
        final Tenant studyTenant = source.getTenant();
        for (final UserStudyExtended studyUserLink : source.getParticipants()) {
            final ParticipantDTO participantDTO = conversionService.convert(studyUserLink, ParticipantDTO.class);
            if (!studyUserLink.getUser().getTenants().contains(studyTenant)) {
                participantDTO.setCanBeRecreated(Boolean.FALSE);
            }
            studyDTO.getParticipants().add(participantDTO);
        }
        studyDTO.setStatus(conversionService.convert(source.getStatus(), StudyStatusDTO.class));
        studyDTO.setType(conversionService.convert(source.getType(), StudyTypeDTO.class));
        studyDTO.setEndDate(source.getEndDate());
        studyDTO.setStartDate(source.getStartDate());
        studyDTO.setDescription(source.getDescription());
        studyDTO.setCreated(source.getCreated());
        studyDTO.setUpdated(source.getUpdated());
        studyDTO.setPaperId(source.getPaper() == null ? null : source.getPaper().getId());
        studyDTO.setPrivacy(source.getPrivacy());
        proceedAdditionalFields(studyDTO, source);
        return studyDTO;
    }
}
