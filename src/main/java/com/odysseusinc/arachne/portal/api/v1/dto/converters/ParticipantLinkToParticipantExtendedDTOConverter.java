/*
 *
 * Copyright 2017 Observational Health Data Sciences and Informatics
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
 * Created: July 21, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.portal.api.v1.dto.OptionDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.ParticipantExtendedDTO;
import com.odysseusinc.arachne.portal.model.ParticipantLink;
import com.odysseusinc.arachne.portal.model.ParticipantRole;
import com.odysseusinc.arachne.portal.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import com.odysseusinc.arachne.portal.api.v1.dto.converters.BaseConversionServiceAwareConverter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;

@Component
public class ParticipantLinkToParticipantExtendedDTOConverter
        extends BaseConversionServiceAwareConverter<ParticipantLink, ParticipantExtendedDTO> {

    @Override
    public ParticipantExtendedDTO convert(ParticipantLink participantLink) {

        final ParticipantExtendedDTO participantDTO = new ParticipantExtendedDTO();

        ParticipantRole role = participantLink.getRole();
        final User user = participantLink.getUser();
        participantDTO.setId(user.getId());
        participantDTO.setFullName(user.getFullName());
        participantDTO.setRole(new OptionDTO(role.name(), role.toString()));
        participantDTO.setStatus(participantLink.getStatus().toString());
        participantDTO.setAffilation(user.getAffiliation());
        participantDTO.setProfessionalType(user.getProfessionalType());
        return participantDTO;
    }
}
