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
 * Created: February 15, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto;

import java.util.Date;

public class SubmissionStatusHistoryElementDTO extends SubmissionStatusDTO {

    private Date date;
    private ShortUserDTO author;
    private String comment;

    public SubmissionStatusHistoryElementDTO() {

    }

    public SubmissionStatusHistoryElementDTO(Date date, SubmissionStatusDTO status, ShortUserDTO author,
                                             String comment) {

        super(status);
        this.date = date;
        this.author = author;
        this.comment = comment;
    }

    public Date getDate() {

        return date;
    }

    public void setDate(Date date) {

        this.date = date;
    }

    public ShortUserDTO getAuthor() {

        return author;
    }

    public void setAuthor(ShortUserDTO author) {

        this.author = author;
    }

    public String getComment() {

        return comment;
    }

    public void setComment(String comment) {

        this.comment = comment;
    }
}
