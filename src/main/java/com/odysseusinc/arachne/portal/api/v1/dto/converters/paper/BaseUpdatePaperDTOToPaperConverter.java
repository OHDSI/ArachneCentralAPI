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
 * Created: September 14, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters.paper;

import com.odysseusinc.arachne.portal.api.v1.dto.UpdatePaperDTO;
import com.odysseusinc.arachne.portal.model.Paper;
import org.springframework.beans.factory.annotation.Autowired;
import com.odysseusinc.arachne.portal.api.v1.dto.converters.BaseConversionServiceAwareConverter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;

public class BaseUpdatePaperDTOToPaperConverter<UpDTO extends UpdatePaperDTO, P extends Paper> extends BaseConversionServiceAwareConverter<UpDTO, P> {

    @Override
    public P convert(UpDTO source) {

        final P paper = createResultObject();
        paper.setPublishState(source.getPublishState());

        proceedAdditionalFields(paper, source);

        return paper;
    }
}
