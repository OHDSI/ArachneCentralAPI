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
 * Created: May 19, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.portal.api.v1.dto.AchillesFileDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.CharacterizationDTO;
import com.odysseusinc.arachne.portal.model.achilles.Characterization;
import java.util.ArrayList;
import java.util.stream.Collectors;
import com.odysseusinc.arachne.portal.api.v1.dto.converters.BaseConversionServiceAwareConverter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;


@Component
public class CharacterizationToCharacterizationDTOConverter extends BaseConversionServiceAwareConverter<Characterization, CharacterizationDTO> {

    @Override
    public CharacterizationDTO convert(Characterization characterization) {

        CharacterizationDTO dto = new CharacterizationDTO();
        dto.setId(characterization.getId());
        dto.setDate(characterization.getDate());
        if (characterization.getFiles() != null) {
            dto.setFiles(characterization.getFiles()
                    .stream()
                    .map(f -> conversionService.convert(f, AchillesFileDTO.class))
                    .collect(Collectors.toList()));
        } else {
            dto.setFiles(new ArrayList<>());
        }
        return dto;
    }
}
