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
 * Created: September 11, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters.study;

import com.odysseusinc.arachne.portal.api.v1.dto.StudyDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.converters.BaseConversionServiceAwareConverter;
import com.odysseusinc.arachne.portal.model.AbstractUserStudyListItem;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.util.ArachneConverterUtils;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseUserStudyItemToStudyDTOConverter<S extends StudyDTO> extends BaseConversionServiceAwareConverter<AbstractUserStudyListItem, S> {

    @Autowired
    private ArachneConverterUtils converterUtils;

    @Override
    public S convert(AbstractUserStudyListItem source) {

        S studyDto = createResultObject();

        StudyDTO baseObject = conversionService.convert(source.getStudy(), getDtoClass());
        baseObject.setFavourite(source.getFavourite());

        converterUtils.shallowCopy(studyDto, baseObject);
        final Study study = source.getStudy();
        studyDto.setPrivacy(study.getPrivacy());

        proceedAdditionalFields(studyDto, source);

        return studyDto;
    }

    protected abstract Class<S> getDtoClass();
}
