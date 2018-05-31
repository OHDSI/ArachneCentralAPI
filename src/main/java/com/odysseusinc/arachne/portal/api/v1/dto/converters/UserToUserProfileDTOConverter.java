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
 * Created: January 13, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.portal.api.v1.dto.UserLinkDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UserProfileDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UserProfileGeneralDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UserPublicationDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.dictionary.SkillDTO;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.Skill;
import com.odysseusinc.arachne.portal.model.UserLink;
import com.odysseusinc.arachne.portal.model.UserPublication;
import java.util.HashSet;
import java.util.LinkedList;
import org.springframework.stereotype.Component;


@Component
public class UserToUserProfileDTOConverter extends BaseConversionServiceAwareConverter<IUser, UserProfileDTO> {


    @Override
    public UserProfileDTO convert(IUser user) {

        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getUuid());
        dto.setEnabled(user.getEnabled());
        dto.setCreated(user.getCreated());
        dto.setUpdated(user.getUpdated());
        HashSet<SkillDTO> skills = new HashSet<>();
        for (Skill skill : user.getSkills()) {
            skills.add(conversionService.convert(skill, SkillDTO.class));
        }
        dto.setSkills(skills);

        LinkedList<UserPublicationDTO> publications = new LinkedList<>();
        for (UserPublication userPublication : user.getPublications()) {
            publications.add(conversionService.convert(userPublication, UserPublicationDTO.class));
        }
        dto.setPublications(publications);

        LinkedList<UserLinkDTO> links = new LinkedList<>();
        for (UserLink userLink : user.getLinks()) {
            links.add(conversionService.convert(userLink, UserLinkDTO.class));
        }
        dto.setLinks(links);
        dto.setGeneral(conversionService.convert(user, UserProfileGeneralDTO.class));
        return dto;
    }


}
