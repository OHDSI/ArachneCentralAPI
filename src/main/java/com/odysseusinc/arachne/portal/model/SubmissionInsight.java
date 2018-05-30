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
 * Created: May 03, 2017
 *
 */

package com.odysseusinc.arachne.portal.model;

import com.odysseusinc.arachne.portal.security.ArachnePermission;
import com.odysseusinc.arachne.portal.security.HasArachnePermissions;
import com.odysseusinc.arachne.portal.service.impl.breadcrumb.Breadcrumb;
import com.odysseusinc.arachne.portal.service.impl.breadcrumb.BreadcrumbType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "submission_insights")
public class SubmissionInsight implements Serializable, Breadcrumb, HasArachnePermissions {
    @Id
    @SequenceGenerator(name = "submission_insights_pk_sequence", sequenceName = "submission_insights_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "submission_insights_pk_sequence")
    private Long id;
    @Column
    private Date created;
    @Column
    private String name;
    @Column
    private String description;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id")
    private Submission submission;
    @OneToMany(mappedBy = "submissionInsight", fetch = FetchType.LAZY)
    private List<SubmissionInsightSubmissionFile> submissionInsightSubmissionFiles = new ArrayList<>();
    @Transient
    private Long commentsCount;
    @Transient
    private Set<ArachnePermission> permissions;

    public BreadcrumbType getCrumbType() {

        return BreadcrumbType.INSIGHT;
    }

    public Long getCrumbId() {

        return this.getSubmission().getId();
    }

    public String getCrumbTitle() {

        return this.getName();
    }

    public Breadcrumb getCrumbParent() {

        return this.getSubmission();
    }

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public Date getCreated() {

        return created;
    }

    public void setCreated(Date created) {

        this.created = created;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public Submission getSubmission() {

        return submission;
    }

    public void setSubmission(Submission submission) {

        this.submission = submission;
    }

    public Long getCommentsCount() {

        return commentsCount;
    }

    public void setCommentsCount(Long commentCount) {

        this.commentsCount = commentCount;
    }

    public List<SubmissionInsightSubmissionFile> getSubmissionInsightSubmissionFiles() {

        return submissionInsightSubmissionFiles;
    }

    public void setSubmissionInsightSubmissionFiles(List<SubmissionInsightSubmissionFile> submissionInsightSubmissionFiles) {

        this.submissionInsightSubmissionFiles = submissionInsightSubmissionFiles;
    }

    @Override
    public Set<ArachnePermission> getPermissions() {
        return permissions;
    }

    @Override
    public void setPermissions(Set<ArachnePermission> permissions) {
        this.permissions = permissions;
    }
}
