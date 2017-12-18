/*
 *
 * Copyright 2017 Observational Health Data Sciences and Informatics
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

import com.odysseusinc.arachne.portal.api.v1.dto.ResultFileDTO;
import com.odysseusinc.arachne.portal.model.AbstractResultFile;
import org.springframework.stereotype.Component;

@Component
public class ResultFileToResultFileDTOConverter extends BaseConversionServiceAwareConverter<AbstractResultFile, ResultFileDTO> {

    @Override
    public ResultFileDTO convert(AbstractResultFile source) {

        ResultFileDTO resultFileDTO = new ResultFileDTO();

        resultFileDTO.setLabel(source.getLabel());
        resultFileDTO.setUuid(source.getUuid());
        resultFileDTO.setName(source.getRealName());
        resultFileDTO.setCreated(source.getCreated());
        resultFileDTO.setSubmissionId(source.getSubmission().getId());
        resultFileDTO.setManuallyUploaded(source.isManuallyUploaded());
        resultFileDTO.setDocType(source.getContentType());

        return resultFileDTO;
    }
}
