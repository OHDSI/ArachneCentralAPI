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
 * Created: January 18, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;

public class ApproveDTO {

    @NotNull
    private Long id;

    @NotNull
    private Boolean isApproved;

    private Boolean isSuccess;

    private String comment;

    @JsonCreator
    public ApproveDTO(@JsonProperty("id") Long idIn,
                      @JsonProperty("isApproved") Boolean isApprovedIn,
                      @JsonProperty("isSuccess") Boolean isSuccessIn,
                      @JsonProperty("comment") String comment) {

        id = idIn;
        isApproved = isApprovedIn;
        isSuccess = isSuccessIn;
        this.comment = comment;
    }

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public Boolean getIsApproved() {

        return isApproved;
    }

    public void setIsApproved(Boolean approved) {

        isApproved = approved;
    }

    public Boolean getIsSuccess() {

        return isSuccess;
    }

    public void setIsSuccess(Boolean success) {

        isSuccess = success;
    }

    public String getComment() {

        return comment;
    }

    public void setComment(String comment) {

        this.comment = comment;
    }
}
