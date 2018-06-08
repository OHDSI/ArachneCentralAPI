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
 * Authors: Anastasiia Klochkova
 * Created: June 5, 2018
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters.study;

import com.odysseusinc.arachne.portal.api.v1.dto.ParticipantDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.WorkspaceDTO;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.model.ParticipantRole;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.UserStudyExtended;
import com.odysseusinc.arachne.portal.service.BaseStudyService;
import com.odysseusinc.arachne.portal.service.analysis.AnalysisService;
import com.odysseusinc.arachne.portal.util.ArachneConverterUtils;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseStudyToWorkspaceDTOConverter<S extends Study, DTO extends WorkspaceDTO> extends BaseStudyToCommonStudyDTOConverter<S, DTO> {

    @Autowired
    public BaseStudyToWorkspaceDTOConverter(BaseStudyService studyService, AnalysisService analysisService, ArachneConverterUtils converterUtils) {

        super(studyService, analysisService, converterUtils);
    }

    @Override
    public DTO convert(S source) {

        final DTO workspaceDTO = super.convert(source);
        if (!source.getParticipants().isEmpty()) {
            Optional<UserStudyExtended> leadOptional = source.getParticipants()
                    .stream()
                    .filter(participant -> participant.getRole().equals(ParticipantRole.LEAD_INVESTIGATOR))
                    .findFirst();
            workspaceDTO.setLeadParticipant(conversionService.convert(leadOptional.orElseThrow(() -> {
                final String message = "Lead investigator for workspace id = " + workspaceDTO.getId() + " doesn't exist";
                return new NotExistException(message, UserStudyExtended.class);
            }), ParticipantDTO.class));
        }
        return workspaceDTO;
    }
}
