/*
 *
 * Copyright 2018 Odysseus Data Services, inc.
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
 * Created: August 21, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.portal.api.v1.dto.ShortUserDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.SubmissionStatusDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.SubmissionStatusHistoryElementDTO;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.SubmissionStatus;
import com.odysseusinc.arachne.portal.model.SubmissionStatusHistoryElement;
import com.odysseusinc.arachne.portal.model.User;
import java.util.Date;
import com.odysseusinc.arachne.portal.api.v1.dto.converters.BaseConversionServiceAwareConverter;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("unused")
public class SubmissionStatusHistoryToSubmissionStatusHistoryDTOConverter
        extends BaseConversionServiceAwareConverter<SubmissionStatusHistoryElement, SubmissionStatusHistoryElementDTO> {


    @Override
    public SubmissionStatusHistoryElementDTO convert(SubmissionStatusHistoryElement source) {

        Date date = source.getDate();
        SubmissionStatus status = source.getStatus();
        SubmissionStatusDTO submissionStatusDTO = null;
        if (status != null) {
            submissionStatusDTO = new SubmissionStatusDTO(status);
        }
        IUser author = source.getAuthor();
        ShortUserDTO shortUser = null;
        if (author != null) {
            shortUser = conversionService.convert(author, ShortUserDTO.class);
        }
        return new SubmissionStatusHistoryElementDTO(date, submissionStatusDTO, shortUser, source.getComment());
    }
}
