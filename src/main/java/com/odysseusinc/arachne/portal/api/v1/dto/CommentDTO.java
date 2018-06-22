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
 * Created: April 20, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.hibernate.validator.constraints.NotEmpty;

public class CommentDTO {

    private Long id;
    private Date date;
    @NotEmpty
    private String comment;
    private List<CommentDTO> comments = new LinkedList<>();
    private UserInfoDTO author;
    private Long parentId;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public Date getDate() {

        return date;
    }

    public void setDate(Date date) {

        this.date = date;
    }

    public String getComment() {

        return comment;
    }

    public void setComment(String comment) {

        this.comment = comment;
    }

    public List<CommentDTO> getComments() {

        return comments;
    }

    public void setComments(List<CommentDTO> comments) {

        this.comments = comments;
    }

    public UserInfoDTO getAuthor() {

        return author;
    }

    public void setAuthor(UserInfoDTO author) {

        this.author = author;
    }

    public Long getParentId() {

        return parentId;
    }

    public void setParentId(Long parentId) {

        this.parentId = parentId;
    }
}
