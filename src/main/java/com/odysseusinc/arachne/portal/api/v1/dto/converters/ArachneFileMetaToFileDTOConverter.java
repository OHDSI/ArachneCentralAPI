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
 * Created: January 13, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.commons.utils.UserIdUtils;
import com.odysseusinc.arachne.portal.api.v1.dto.FileDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UserInfoDTO;
import com.odysseusinc.arachne.storage.model.ArachneFileMeta;
import org.springframework.stereotype.Component;

@Component
public class ArachneFileMetaToFileDTOConverter extends BaseConversionServiceAwareConverter<ArachneFileMeta, FileDTO> {

    @Override
    public FileDTO convert(ArachneFileMeta source) {

        FileDTO fileDTO = new FileDTO();

        fileDTO.setUuid(source.getUuid());
        fileDTO.setPath(source.getPath());
        fileDTO.setName(source.getName());
        fileDTO.setCreated(source.getCreated());
        fileDTO.setUpdated(source.getUpdated());
        fileDTO.setDocType(source.getContentType());
        fileDTO.setAuthor(new UserInfoDTO(UserIdUtils.idToUuid(source.getCreatedBy())));
        return fileDTO;
    }
}
