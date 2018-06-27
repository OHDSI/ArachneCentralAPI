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

import java.util.Date;
import java.util.List;

public class InvitationDTO {
    private Long id;
    private ShortUserDTO user;
    private String actionType;
    private String type;
    private List<ActionDTO> actionList;
    private Object entity;
    private Date date;
    private String comment;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public ShortUserDTO getUser() {

        return user;
    }

    public void setUser(ShortUserDTO user) {

        this.user = user;
    }

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public String getActionType() {

        return actionType;
    }

    public void setActionType(String actionType) {

        this.actionType = actionType;
    }

    public List<ActionDTO> getActionList() {

        return actionList;
    }

    public void setActionList(List<ActionDTO> actionList) {

        this.actionList = actionList;
    }

    public Date getDate() {

        return date;
    }

    public void setDate(Date date) {

        this.date = date;
    }

    public Object getEntity() {

        return entity;
    }

    public void setEntity(Object entity) {

        this.entity = entity;
    }

    public String getComment() {

        return comment;
    }

    public void setComment(String comment) {

        this.comment = comment;
    }
}
