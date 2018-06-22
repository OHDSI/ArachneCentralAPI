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
 * Created: September 18, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters.submission;

import com.odysseusinc.arachne.portal.api.v1.dto.SubmissionDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.SubmissionInsightDTO;
import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.model.SubmissionInsight;
import com.odysseusinc.arachne.portal.model.security.ArachneUser;
import com.odysseusinc.arachne.portal.util.DataNodeUtils;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class BaseSubmissionToSubmissionDTOConverter<T extends Submission, DTO extends SubmissionDTO>
        extends BaseSubmissionToBaseSubmissionDTOConverter<T, DTO> {

    @Override
    public DTO convert(T source) {

        DTO dto = super.convert(source);
        dto.setInsight(insightDTO(source.getSubmissionInsight()));
        final Status status = statusConverter(source.getStatus());
        final Boolean execConfirmed = status.isExecConfirmed();
        dto.setIsExecConfirmed(execConfirmed);
        final Boolean resultConfirmed = status.isResultConfirmed();
        dto.setIsResultConfirmed(resultConfirmed);
        dto.setAction(source.getStatus().toString());
        IDataSource dataSource = source.getDataSource();
        Long loggedUserId = ((ArachneUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        final boolean isOwner = DataNodeUtils.isDataNodeOwner(dataSource.getDataNode(), loggedUserId);
        dto.setIsOwner(isOwner);
        return dto;
    }

    private SubmissionInsightDTO insightDTO(SubmissionInsight insight) {

        SubmissionInsightDTO insightDTO = null;
        if (insight != null) {
            insightDTO = new SubmissionInsightDTO();
            insightDTO.setName(insight.getName());
            insightDTO.setCreated(insight.getCreated());
        }
        return insightDTO;
    }

}
