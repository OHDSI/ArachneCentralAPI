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
 * Created: September 18, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters.submission;

import com.odysseusinc.arachne.portal.api.v1.dto.DataSourceDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.PermissionsDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.SubmissionDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.SubmissionInsightDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.SubmissionStatusDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.SubmissionStatusHistoryElementDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.converters.BaseConversionServiceAwareConverter;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.model.DataSource;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.model.SubmissionInsight;
import com.odysseusinc.arachne.portal.model.SubmissionStatus;
import com.odysseusinc.arachne.portal.model.SubmissionStatusHistoryElement;
import com.odysseusinc.arachne.portal.model.security.ArachneUser;
import com.odysseusinc.arachne.portal.util.DataNodeUtils;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class BaseSubmissionToSubmissionDTOConverter<T extends Submission, DTO extends SubmissionDTO>
        extends BaseConversionServiceAwareConverter<T, DTO> {
    private static final String NOT_EXISTS_SUBMISSION_STATUS
            = "There is no '%s' status in submission with id '%s'";

    @Override
    public DTO convert(T source) {

        DTO dto = createResultObject();
        dto.setConversionSource(source);
        dto.setId(source.getId());
        dto.setStatus(getStatusDTO(source));
        dto.setCreatedAt(source.getCreated());
        dto.setInsight(insightDTO(source.getSubmissionInsight()));
        final Status status = statusConverter(source.getStatus());
        final Boolean execConfirmed = status.isExecConfirmed();
        dto.setIsExecConfirmed(execConfirmed);
        final Boolean resultConfirmed = status.isResultConfirmed();
        dto.setIsResultConfirmed(resultConfirmed);
        dto.setAction(source.getStatus().toString());
        DataSource dataSource = source.getDataSource();
        dto.setDataSource(conversionService.convert(dataSource, DataSourceDTO.class));
        Long loggedUserId = ((ArachneUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        final boolean isOwner = DataNodeUtils.isDataNodeOwner(dataSource.getDataNode(), loggedUserId);
        dto.setIsOwner(isOwner);
        if (isOwner || (resultConfirmed != null && resultConfirmed)) {
            dto.setResultFilesCount(source.getResultFiles().size());
        }
        dto.setPermissions(conversionService.convert(source, PermissionsDTO.class));
        proceedAdditionalFields(dto, source);
        return dto;
    }

    private SubmissionStatusDTO getStatusDTO(Submission source) {

        SubmissionStatus status = source.getStatus();
        if (status.isDeclined()) {
            SubmissionStatusHistoryElement element = source.getStatusHistory().stream()
                    .filter(e -> e.getStatus() == status).findFirst()
                    .orElseThrow(() ->
                            new NotExistException(String.format(NOT_EXISTS_SUBMISSION_STATUS, status, source.getId()),
                                    SubmissionStatus.class));
            return conversionService.convert(element, SubmissionStatusHistoryElementDTO.class);
        }
        return new SubmissionStatusDTO(source.getStatus());
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

    private Status statusConverter(SubmissionStatus status) {

        Boolean isExecConfirmed = null;
        Boolean isResultConfirmed = null;
        switch (status) {
            case NOT_APPROVED: {
                isExecConfirmed = false;
                break;
            }
            case STARTING:
            case IN_PROGRESS:
            case EXECUTED:
            case FAILED: {
                isExecConfirmed = true;
                break;
            }
            case EXECUTED_REJECTED:
            case FAILED_REJECTED: {
                isExecConfirmed = true;
                isResultConfirmed = false;
                break;
            }
            case EXECUTED_PUBLISHED:
            case FAILED_PUBLISHED: {
                isExecConfirmed = true;
                isResultConfirmed = true;
                break;
            }
            default: {

            }
        }
        return new Status(isExecConfirmed, isResultConfirmed);
    }

    private class Status {

        private Boolean isExecConfirmed = null;
        private Boolean isResultConfirmed = null;

        Status(Boolean isExecConfirmed, Boolean isResultConfirmed) {

            this.isExecConfirmed = isExecConfirmed;
            this.isResultConfirmed = isResultConfirmed;
        }

        Boolean isExecConfirmed() {

            return isExecConfirmed;
        }

        Boolean isResultConfirmed() {

            return isResultConfirmed;
        }
    }
}
