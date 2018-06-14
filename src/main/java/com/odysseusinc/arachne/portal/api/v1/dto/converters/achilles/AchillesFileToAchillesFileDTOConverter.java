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
 * Created: May 23, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters.achilles;

import com.odysseusinc.arachne.portal.api.v1.dto.AchillesFileDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.converters.BaseConversionServiceAwareConverter;
import com.odysseusinc.arachne.portal.model.achilles.AchillesFile;
import org.springframework.stereotype.Component;


@Component
public class AchillesFileToAchillesFileDTOConverter extends BaseConversionServiceAwareConverter<AchillesFile, AchillesFileDTO> {

    @Override
    public AchillesFileDTO convert(AchillesFile achillesFile) {

        AchillesFileDTO dto = new AchillesFileDTO();
        dto.setFilePath(achillesFile.getFilePath());
        return dto;
    }
}
