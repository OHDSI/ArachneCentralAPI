/**
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

package com.odysseusinc.arachne.portal.api.v1.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.odysseusinc.arachne.portal.service.impl.submission.SubmissionAction;
import java.util.Date;
import java.util.List;

public class SubmissionDTO extends DTO {

    private Long id;
    private Integer order;
    private DataSourceDTO dataSource;
    private SubmissionStatusDTO status;
    private String action;
    private Integer resultFilesCount;
    private Boolean isOwner;
    private Boolean isExecConfirmed;
    private Boolean isResultConfirmed;
    private PermissionsDTO permissions;
    private Date createdAt;
    private SubmissionInsightDTO insight;
    private ShortUserDTO author;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<SubmissionAction> availableActionList;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public Integer getOrder() {

        return order;
    }

    public void setOrder(Integer order) {

        this.order = order;
    }

    public SubmissionStatusDTO getStatus() {

        return status;
    }

    public void setStatus(SubmissionStatusDTO status) {

        this.status = status;
    }

    public DataSourceDTO getDataSource() {

        return dataSource;
    }

    public void setDataSource(DataSourceDTO dataSource) {

        this.dataSource = dataSource;
    }

    public String getAction() {

        return action;
    }

    public void setAction(String action) {

        this.action = action;
    }

    public Integer getResultFilesCount() {

        return resultFilesCount;
    }

    public void setResultFilesCount(Integer resultFilesCount) {

        this.resultFilesCount = resultFilesCount;
    }

    public Boolean getIsOwner() {

        return isOwner;
    }

    public void setIsOwner(Boolean isOwner) {

        this.isOwner = isOwner;
    }

    public Boolean getIsExecConfirmed() {

        return isExecConfirmed;
    }

    public void setIsExecConfirmed(Boolean isExecConfirmed) {

        this.isExecConfirmed = isExecConfirmed;
    }

    public Boolean getIsResultConfirmed() {

        return isResultConfirmed;
    }

    public void setIsResultConfirmed(Boolean isResultConfirmed) {

        this.isResultConfirmed = isResultConfirmed;
    }

    public PermissionsDTO getPermissions() {

        return permissions;
    }

    public void setPermissions(PermissionsDTO permissions) {

        this.permissions = permissions;
    }

    public Date getCreatedAt() {

        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {

        this.createdAt = createdAt;
    }

    public SubmissionInsightDTO getInsight() {

        return insight;
    }

    public void setInsight(SubmissionInsightDTO insight) {

        this.insight = insight;
    }

    public ShortUserDTO getAuthor() {

        return author;
    }

    public void setAuthor(ShortUserDTO author) {
        
        this.author = author;
    }

    public List<SubmissionAction> getAvailableActionList() {

        return availableActionList;
    }

    public void setAvailableActionList(List<SubmissionAction> availableActionList) {

        this.availableActionList = availableActionList;
    }
}
