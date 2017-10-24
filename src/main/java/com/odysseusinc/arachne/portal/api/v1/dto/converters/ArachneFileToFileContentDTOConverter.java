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
 * Created: May 10, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.commons.utils.CommonFileUtils;
import com.odysseusinc.arachne.portal.api.v1.dto.FileContentDTO;
import com.odysseusinc.arachne.portal.model.AnalysisFile;
import com.odysseusinc.arachne.portal.model.ArachneFile;
import com.odysseusinc.arachne.portal.model.ResultFile;
import com.odysseusinc.arachne.portal.model.SubmissionFile;
import com.odysseusinc.arachne.portal.util.AnalysisHelper;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import com.odysseusinc.arachne.portal.api.v1.dto.converters.BaseConversionServiceAwareConverter;
import org.springframework.stereotype.Component;

@Component
public class ArachneFileToFileContentDTOConverter extends BaseConversionServiceAwareConverter<ArachneFile, FileContentDTO> {


    @Autowired
    private AnalysisHelper analysisHelper;


    @Override
    public FileContentDTO convert(ArachneFile source) {

        FileContentDTO fileContentDTO = new FileContentDTO();
        fileContentDTO.setUuid(source.getUuid());
        fileContentDTO.setName(source.getRealName());
        fileContentDTO.setCreated(source.getCreated());
        fileContentDTO.setLabel(source.getLabel());
        if (source instanceof AnalysisFile && ((AnalysisFile) source).getAnalysis() != null) {
            fileContentDTO.setAnalysisId(((AnalysisFile) source).getAnalysis().getId());
        }
        if (source instanceof SubmissionFile) {
            String contentType = CommonFileUtils.getContentType(
                    source.getRealName(),
                    Objects.toString(analysisHelper.getSubmissionFile((SubmissionFile) source))
            );

            fileContentDTO.setDocType(contentType);
        } else if (source instanceof ResultFile) {
            String contentType = CommonFileUtils.getContentType(
                    source.getRealName(),
                    Objects.toString(analysisHelper.getResultFile((ResultFile) source))
            );
            fileContentDTO.setDocType(contentType);
        }
        return fileContentDTO;
    }
}
