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
 * Created: September 18, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters.submission;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.odysseusinc.arachne.portal.api.v1.dto.BaseSubmissionDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.DataSourceDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.PermissionsDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.SubmissionStatusDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.SubmissionStatusHistoryElementDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.converters.BaseConversionServiceAwareConverter;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.model.SubmissionStatus;
import com.odysseusinc.arachne.portal.model.SubmissionStatusHistoryElement;
import com.odysseusinc.arachne.portal.model.security.ArachneUser;
import com.odysseusinc.arachne.portal.util.DataNodeUtils;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class BaseSubmissionToBaseSubmissionDTOConverter<T extends Submission, DTO extends BaseSubmissionDTO>
        extends BaseConversionServiceAwareConverter<T, DTO> {
    private static final String NOT_EXISTS_SUBMISSION_STATUS
            = "There is no '%s' status in submission with id '%s'";

    @Override
    public void afterPropertiesSet() throws Exception {

        conversionService.addConverter(this);
    }

    @Override
    public DTO convert(T source) {

        DTO dto = createResultObject();
        dto.setConversionSource(source);
        dto.setId(source.getId());
        dto.setStatus(getStatusDTO(source));
        dto.setCreatedAt(source.getCreated());

        final Status status = statusConverter(source.getStatus());
        final Boolean resultConfirmed = status.isResultConfirmed();

        IDataSource dataSource = source.getDataSource();
        dto.setDataSource(conversionService.convert(dataSource, DataSourceDTO.class));
        Long loggedUserId = ((ArachneUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        final boolean isOwner = DataNodeUtils.isDataNodeOwner(dataSource.getDataNode(), loggedUserId);
        if (isOwner || (resultConfirmed != null && resultConfirmed)) {
            dto.setResultFilesCount(source.getResultFiles().size());
        }
        dto.setPermissions(conversionService.convert(source, PermissionsDTO.class));
        proceedAdditionalFields(dto, source);
        final JsonElement resultInfo = source.getResultInfo();
        Gson gson = new Gson();
        Object result = null;
        if (Objects.nonNull(resultInfo)) {
            if (resultInfo.isJsonObject()) {
                result = gson.fromJson(resultInfo, Map.class);
            } else if (resultInfo.isJsonArray()) {
                Type type = new TypeToken<List<HashMap<String, Object>>>() {
                }.getType();
                result = gson.fromJson(resultInfo, type);
            }
        }
        dto.setResultInfo(result);
        dto.setHidden(source.getHidden());
        return dto;
    }

    protected SubmissionStatusDTO getStatusDTO(Submission source) {

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


    protected Status statusConverter(SubmissionStatus status) {

        Boolean isExecConfirmed = null;
        Boolean isResultConfirmed = null;
        switch (status) {
            case NOT_APPROVED: {
                isExecConfirmed = false;
                break;
            }
            case STARTING:
            case QUEUE_PROCESSING:
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

    protected class Status {

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
