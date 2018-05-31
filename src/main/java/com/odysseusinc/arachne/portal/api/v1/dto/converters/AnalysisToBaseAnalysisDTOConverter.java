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
 * Created: May 18, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.portal.api.v1.dto.BaseAnalysisDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.PermissionsDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.ShortBaseAnalysisDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.StudyShortDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UserInfoDTO;
import com.odysseusinc.arachne.portal.model.Analysis;
import org.springframework.stereotype.Component;

@Component
public class AnalysisToBaseAnalysisDTOConverter extends BaseConversionServiceAwareConverter<Analysis, BaseAnalysisDTO> {

    @Override
    public BaseAnalysisDTO convert(Analysis source) {

        ShortBaseAnalysisDTO baseDTO = conversionService.convert(source, ShortBaseAnalysisDTO.class);
        BaseAnalysisDTO analysisDTO = new BaseAnalysisDTO(baseDTO);
        analysisDTO.setAuthor(conversionService.convert(source.getAuthor(), UserInfoDTO.class));
        analysisDTO.setTitle(source.getTitle());
        analysisDTO.setDescription(source.getDescription());
        analysisDTO.setUpdated(source.getUpdated());
        analysisDTO.setStudy(conversionService.convert(source.getStudy(), StudyShortDTO.class));

        analysisDTO.setPermissions(conversionService.convert(source, PermissionsDTO.class));
        return analysisDTO;
    }
}
