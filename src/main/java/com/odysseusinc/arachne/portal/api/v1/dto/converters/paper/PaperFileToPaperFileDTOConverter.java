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

import com.odysseusinc.arachne.portal.api.v1.dto.PaperFileDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UserInfoDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.converters.BaseConversionServiceAwareConverter;
import com.odysseusinc.arachne.portal.model.AbstractPaperFile;
import org.springframework.stereotype.Component;

@Component
public class PaperFileToPaperFileDTOConverter
        extends BaseConversionServiceAwareConverter<AbstractPaperFile, PaperFileDTO> {

    @Override
    public PaperFileDTO convert(AbstractPaperFile paperFile) {

        final PaperFileDTO paperFileDTO = new PaperFileDTO();

        paperFileDTO.setUuid(paperFile.getUuid());
        paperFileDTO.setName(paperFile.getRealName());
        paperFileDTO.setLabel(paperFile.getLabel());
        paperFileDTO.setCreated(paperFile.getCreated());
        paperFileDTO.setUpdated(paperFile.getUpdated());
        paperFileDTO.setDocType(paperFile.getContentType());
        final UserInfoDTO authorDTO = conversionService.convert(paperFile.getAuthor(), UserInfoDTO.class);
        paperFileDTO.setAuthor(authorDTO);
        paperFileDTO.setLink(paperFile.getLink());
        paperFileDTO.setAntivirusStatus(paperFile.getAntivirusStatus());
        paperFileDTO.setAntivirusDescription(paperFile.getAntivirusDescription());

        paperFileDTO.setInsightId(paperFile.getPaper().getId());

        return paperFileDTO;
    }
}
