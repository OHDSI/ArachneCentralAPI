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
 * Created: September 27, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
import com.odysseusinc.arachne.portal.api.v1.dto.AnalysisCreateDTO;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.Study;

public abstract class BaseAnalysisCreateDTOToAnalysisConverter<A extends Analysis, S extends Study, DTO extends AnalysisCreateDTO> extends BaseConversionServiceAwareConverter<DTO, A> {


    @Override
    public A convert(DTO dto) {

        A analysis = newAnalysis();

        analysis.setTitle(dto.getTitle());
        analysis.setDescription(dto.getDescription());

        final CommonAnalysisType analysisType = CommonAnalysisType.valueOf(dto.getTypeId());
        analysis.setType(analysisType);

        S study = newStudy();
        study.setId(dto.getStudyId());
        analysis.setStudy(study);

        return analysis;
    }

    protected abstract A newAnalysis();
    protected abstract S newStudy();
}
