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
 * Created: October 03, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.portal.api.v1.dto.AnalysisDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.AnalysisFileDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.BaseAnalysisDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.PermissionsDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.StudyShortDTO;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.AnalysisFile;
import com.odysseusinc.arachne.portal.util.ArachneConverterUtils;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseAnalysisToAnalysisDTOConverter<A extends Analysis, AD extends AnalysisDTO>
        extends BaseConversionServiceAwareConverter<A, AD> {

    @Autowired
    private ArachneConverterUtils converterUtils;

    @Override
    public AD convert(A source) {

        BaseAnalysisDTO baseAnalysisDTO = conversionService.convert(source, BaseAnalysisDTO.class);
        AD analysisDTO = createResultObject();
        converterUtils.shallowCopy(analysisDTO, baseAnalysisDTO);
        analysisDTO.setStudy(conversionService.convert(source.getStudy(), StudyShortDTO.class));
        if (source.getFiles() != null) {
            for (AnalysisFile analysisFile : source.getFiles()) {
                analysisDTO.getFiles().add(conversionService.convert(analysisFile, AnalysisFileDTO.class));
            }
        }

        analysisDTO.setPermissions(conversionService.convert(source, PermissionsDTO.class));
        analysisDTO.setLocked(source.getLocked());
        return analysisDTO;
    }
}
