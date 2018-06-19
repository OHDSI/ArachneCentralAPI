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

import com.odysseusinc.arachne.portal.api.v1.dto.PaperDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.PaperFileDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.PermissionsDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.StudyMediumDTO;
import com.odysseusinc.arachne.portal.model.Paper;
import com.odysseusinc.arachne.portal.model.Study;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import com.odysseusinc.arachne.portal.api.v1.dto.converters.BaseConversionServiceAwareConverter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;

public abstract class BasePaperToPaperDTOConverter<P extends Paper, PD extends PaperDTO>
        extends BaseConversionServiceAwareConverter<P, PD> {

    @Override
    public PD convert(P paper) {

        final PD paperDTO = createResultObject();

        paperDTO.setId(paper.getId());
        final Study study = paper.getStudy();
        final StudyMediumDTO studyDTO = conversionService.convert(study, StudyMediumDTO.class);
        paperDTO.setStudy(studyDTO);

        paperDTO.setPublishState(paper.getPublishState());
        paperDTO.setPublishedDate(paper.getPublishedDate());

        final List<PaperFileDTO> protocols = paper.getProtocols().stream()
                .map(protocolFile -> conversionService.convert(protocolFile, PaperFileDTO.class))
                .collect(Collectors.toList());
        paperDTO.setProtocols(protocols);

        final List<PaperFileDTO> paperFileDTOs = paper.getPapers().stream()
                .map(paperFile -> conversionService.convert(paperFile, PaperFileDTO.class))
                .collect(Collectors.toList());
        paperDTO.setPapers(paperFileDTOs);

        paperDTO.setPermissions(conversionService.convert(paper, PermissionsDTO.class));

        proceedAdditionalFields(paperDTO, paper);

        return paperDTO;
    }
}
