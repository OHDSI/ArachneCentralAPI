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
 * Created: December 06, 2016
 *
 */

package com.odysseusinc.arachne.portal.model;

import java.util.Date;
import java.util.Optional;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;

@MappedSuperclass
@Access(AccessType.FIELD)
public class AbstractResultFile extends ArachneFile {
    @Id
    @SequenceGenerator(name = "result_files_pk_sequence", sequenceName = "result_files_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "result_files_pk_sequence")
    private Long id;
    @ManyToOne
    private Submission submission;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private CommentTopic commentTopic;
    @Column(name = "manual_upload")
    private Boolean manuallyUploaded;

    public AbstractResultFile() {

    }

    public AbstractResultFile(String uuid, String label, String realName, String contentType, Date created, Date updated, Submission submission, Boolean manuallyUploaded) {

        super(uuid, label, realName, contentType, created, updated);
        this.submission = submission;
        this.manuallyUploaded = manuallyUploaded;
    }

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public Submission getSubmission() {

        return submission;
    }

    public void setSubmission(Submission submission) {

        this.submission = submission;
    }

    public CommentTopic getCommentTopic() {

        return commentTopic;
    }

    public void setCommentTopic(CommentTopic commentTopic) {

        this.commentTopic = commentTopic;
    }

    public void setManuallyUploaded(Boolean manuallyUploaded) {

        this.manuallyUploaded = manuallyUploaded;
    }

    public boolean isManuallyUploaded() {

        return Optional.ofNullable(this.manuallyUploaded).orElse(false);
    }
}
