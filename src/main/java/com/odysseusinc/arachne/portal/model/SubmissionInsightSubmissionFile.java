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
 * Created: July 24, 2017
 *
 */

package com.odysseusinc.arachne.portal.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "submission_insight_submission_files")
public class SubmissionInsightSubmissionFile {

    @Id
    @SequenceGenerator(name = "submission_insight_submission_files_pk_sequence", sequenceName = "submission_insight_submission_files_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "submission_insight_submission_files_pk_sequence")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private SubmissionInsight submissionInsight;

    @OneToOne(fetch = FetchType.LAZY)
    private SubmissionFile submissionFile;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private CommentTopic commentTopic;

    public SubmissionInsightSubmissionFile() {

    }

    public SubmissionInsightSubmissionFile(SubmissionInsight submissionInsight,
                                           SubmissionFile submissionFile,
                                           CommentTopic commentTopic) {

        this.submissionInsight = submissionInsight;
        this.submissionFile = submissionFile;
        this.commentTopic = commentTopic;
    }

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public SubmissionInsight getSubmissionInsight() {

        return submissionInsight;
    }

    public void setSubmissionInsight(SubmissionInsight submissionInsight) {

        this.submissionInsight = submissionInsight;
    }

    public SubmissionFile getSubmissionFile() {

        return submissionFile;
    }

    public void setSubmissionFile(SubmissionFile submissionFile) {

        this.submissionFile = submissionFile;
    }

    public CommentTopic getCommentTopic() {

        return commentTopic;
    }

    public void setCommentTopic(CommentTopic commentTopic) {

        this.commentTopic = commentTopic;
    }
}
