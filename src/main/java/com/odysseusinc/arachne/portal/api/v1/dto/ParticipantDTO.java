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

package com.odysseusinc.arachne.portal.api.v1.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.odysseusinc.arachne.commons.api.v1.dto.OptionDTO;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParticipantDTO {
    protected String id;
    protected String fullName;
    protected OptionDTO role;
    protected String status;
    private String comment;
    private boolean canBeRecreated = Boolean.TRUE;

    public ParticipantDTO() {

    }

    public ParticipantDTO(ParticipantDTO other) {

        this.id = other.id;
        this.fullName = other.fullName;
        this.role = other.role;
        this.status = other.status;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getFullName() {

        return fullName;
    }

    public void setFullName(String fullName) {

        this.fullName = fullName;
    }

    public OptionDTO getRole() {

        return role;
    }

    public void setRole(OptionDTO role) {

        this.role = role;
    }

    public String getStatus() {

        return status;
    }

    public void setStatus(String status) {

        this.status = status;
    }

    public String getComment() {

        return comment;
    }

    public void setComment(String comment) {

        this.comment = comment;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ParticipantDTO that = (ParticipantDTO) obj;
        return new EqualsBuilder()
                .append(id, that.id)
                .append(fullName, that.fullName)
                .append(role, that.role)
                .append(status, that.status)
                .append(comment, that.comment)
                .isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(fullName)
                .append(role)
                .append(status)
                .append(comment)
                .toHashCode();
    }

    public boolean isCanBeRecreated() {

        return canBeRecreated;
    }

    public void setCanBeRecreated(final boolean canBeRecreated) {

        this.canBeRecreated = canBeRecreated;
    }
}
