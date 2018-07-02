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

import com.odysseusinc.arachne.portal.api.v1.dto.UserProfileDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.dictionary.SkillDTO;
import com.odysseusinc.arachne.portal.model.Skill;
import com.odysseusinc.arachne.portal.model.User;
import java.util.HashSet;
import com.odysseusinc.arachne.portal.api.v1.dto.converters.BaseConversionServiceAwareConverter;
import org.springframework.stereotype.Component;


@Component
public class UserProfileDTOToUserConverter extends BaseConversionServiceAwareConverter<UserProfileDTO, User> {


    @Override
    public User convert(UserProfileDTO dto) {

        User user = new User();
        HashSet<Skill> skills = new HashSet<>();
        if (dto.getSkills() != null) {
            for (SkillDTO skillDTO : dto.getSkills()) {
                skills.add(conversionService.convert(skillDTO, Skill.class));
            }
        }
        user.setSkills(skills);
        return user;
    }
}
