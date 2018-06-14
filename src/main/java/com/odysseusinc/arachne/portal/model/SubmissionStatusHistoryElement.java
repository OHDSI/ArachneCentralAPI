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
 * Created: February 10, 2017
 *
 */

package com.odysseusinc.arachne.portal.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Table(name = "submission_status_history")
@Entity
public class SubmissionStatusHistoryElement {

    @Id
    @SequenceGenerator(
            name = "submission_status_history_pk_sequence",
            sequenceName = "submission_status_history_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "submission_status_history_pk_sequence")
    private Long id;

    @Column
    private Date date;

    @Column
    private String comment;

    @Column
    @Enumerated(EnumType.STRING)
    private SubmissionStatus status;

    @Column
    private boolean isLast;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = User.class)
    private IUser author;

    @ManyToOne(fetch = FetchType.LAZY)
    private Submission submission;

    public SubmissionStatusHistoryElement() {

    }

    public SubmissionStatusHistoryElement(Date date, SubmissionStatus status, IUser author, Submission submission,
                                          String comment) {

        this.date = date;
        this.status = status;
        this.author = author;
        this.submission = submission;
        if (status.isDeclined() && author != null) {
            this.comment = comment;
        }
    }

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

    public SubmissionStatus getStatus() {

        return status;
    }

    public void setStatus(SubmissionStatus status) {

        this.status = status;
    }

    public IUser getAuthor() {

        return author;
    }

    public void setAuthor(IUser author) {

        this.author = author;
    }

    public Submission getSubmission() {

        return submission;
    }

    public void setSubmission(Submission submission) {

        this.submission = submission;
    }

    public String getComment() {

        return comment;
    }

    public void setComment(String comment) {

        this.comment = comment;
    }

    public boolean isLast() {

        return isLast;
    }

    public void setLast(boolean last) {

        isLast = last;
    }
}
