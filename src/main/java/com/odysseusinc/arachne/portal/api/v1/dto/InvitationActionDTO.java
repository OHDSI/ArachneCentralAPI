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
 * Created: January 25, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto;

import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public class InvitationActionDTO {

    @NotNull
    protected Long id;

    @NotNull
    protected String type;

    @NotNull
    protected Boolean accepted;

    @Length(max = 255, message = "The field must be less than 255 characters")
    protected String comment;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public Boolean getAccepted() {

        return accepted;
    }

    public void setAccepted(Boolean accepted) {

        this.accepted = accepted;
    }

    public String getComment() {

        return comment;
    }

    public void setComment(String comment) {

        this.comment = comment;
    }
}
