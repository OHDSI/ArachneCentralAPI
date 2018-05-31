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
 * Created: July 27, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.portal.api.v1.dto.ActionDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.InvitationDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.InvitationEntityDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.ShortUserDTO;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.Invitationable;
import com.odysseusinc.arachne.portal.model.Paper;
import com.odysseusinc.arachne.portal.model.Study;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class InvitationableToInvitationDTOConverter
        extends BaseConversionServiceAwareConverter<Invitationable, InvitationDTO> {

    @Override
    public InvitationDTO convert(Invitationable source) {

        final InvitationDTO invitationDTO = new InvitationDTO();

        final List<ActionDTO> actionList = Arrays.asList(
                new ActionDTO("Accept", "accept", "success"),
                new ActionDTO("Decline", "decline", "cancel"));
        invitationDTO.setActionList(actionList);

        invitationDTO.setType(source.getInvitationType());
        invitationDTO.setId(source.getId());
        invitationDTO.setActionType(source.getActionType());
        invitationDTO.setDate(source.getCreated());
        invitationDTO.setUser(conversionService.convert(source.getAuthor(), ShortUserDTO.class));

        final Object entity = source.getEntity();
        String title = "";
        Long id = null;
        if (entity instanceof Study) {
            final Study study = (Study) entity;
            title = study.getTitle();
            id = study.getId();
        } else if (entity instanceof Paper) {
            final Paper paper = (Paper) entity;
            title = paper.getStudy().getDescription();
            id = paper.getId();
        } else if (entity instanceof Analysis) {
            final Analysis analysis = (Analysis) entity;
            title = analysis.getTitle();
            id = analysis.getId();
        }
        final InvitationEntityDTO studyShortDTO = getInvitationEntityDTO(title, id);
        invitationDTO.setEntity(studyShortDTO);
        return invitationDTO;
    }

    private InvitationEntityDTO getInvitationEntityDTO(String title, Long id) {

        final InvitationEntityDTO studyShortDTO = new InvitationEntityDTO();
        studyShortDTO.setTitle(title);
        studyShortDTO.setId(id);
        return studyShortDTO;
    }
}
