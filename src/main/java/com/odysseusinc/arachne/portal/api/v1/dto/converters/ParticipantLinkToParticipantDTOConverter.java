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
 * Created: July 14, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import static com.odysseusinc.arachne.portal.model.ParticipantStatus.DECLINED;
import static org.apache.solr.common.StringUtils.isEmpty;

import com.odysseusinc.arachne.commons.api.v1.dto.OptionDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.DataOwnerParticipantDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.ParticipantDTO;
import com.odysseusinc.arachne.portal.model.DataSource;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.ParticipantLink;
import com.odysseusinc.arachne.portal.model.ParticipantRole;
import com.odysseusinc.arachne.portal.model.UserStudyExtended;
import org.springframework.stereotype.Component;

@Component
public class ParticipantLinkToParticipantDTOConverter
        extends BaseConversionServiceAwareConverter<ParticipantLink, ParticipantDTO> {

    @Override
    public ParticipantDTO convert(ParticipantLink participantLink) {

        ParticipantDTO participantDTO = new ParticipantDTO();

        ParticipantRole role = participantLink.getRole();
        final IUser user = participantLink.getUser();
        participantDTO.setId(user.getUuid());
        participantDTO.setFullName(user.getFullName());
        participantDTO.setRole(new OptionDTO(role.name(), role.toString()));
        participantDTO.setStatus(participantLink.getStatus().toString());

        if (participantLink instanceof UserStudyExtended) {
            if (DECLINED == participantLink.getStatus() && !isEmpty(((UserStudyExtended) participantLink).getComment())) {
                participantDTO.setComment(((UserStudyExtended) participantLink).getComment());
            }

            if (role.equals(ParticipantRole.DATA_SET_OWNER)) {
                DataSource ownedDataSource = ((UserStudyExtended) participantLink).getDataSource();
                participantDTO = new DataOwnerParticipantDTO(participantDTO, ownedDataSource.getId());
            }
        }

        return participantDTO;
    }
}
