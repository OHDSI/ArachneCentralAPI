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
 * Authors: Anastasiia Klochkova
 * Created: May 24, 2018
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.portal.api.v1.dto.UploadFileDTO;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class MultipartFileToUploadFileDTOConverter extends BaseConversionServiceAwareConverter<MultipartFile, UploadFileDTO> {
    private static final String LINK_TYPE = "links";
    private static final Logger LOGGER = LoggerFactory.getLogger(MultipartFileToUploadFileDTOConverter.class);

    @Override
    public UploadFileDTO convert(MultipartFile source) {

        UploadFileDTO uploadFileDTO = new UploadFileDTO();
        uploadFileDTO.setFile(source);
        uploadFileDTO.setLabel(source.getOriginalFilename());
        if (LINK_TYPE.equalsIgnoreCase(source.getName())) {
            try {
                uploadFileDTO.setLink(new String(source.getBytes()));
            } catch (IOException e) {
                LOGGER.error("Failed to read link", e);
            }
        }
        return uploadFileDTO;
    }
}
