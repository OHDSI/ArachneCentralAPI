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
 * Created: April 26, 2017
 *
 */

package com.odysseusinc.arachne.portal.model;

import com.google.common.base.Objects;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
import com.odysseusinc.arachne.portal.service.impl.breadcrumb.Breadcrumb;
import com.odysseusinc.arachne.portal.service.impl.breadcrumb.BreadcrumbType;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(name = "submission_groups")
public class SubmissionGroup implements Breadcrumb {

    @Id
    @SequenceGenerator(name = "submission_groups_pk_sequence", sequenceName = "submission_groups_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "submission_groups_pk_sequence")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Analysis analysis;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = User.class)
    private IUser author;

    @OneToMany(mappedBy = "submissionGroup", targetEntity = Submission.class, fetch = FetchType.LAZY)
    private List<Submission> submissions;

    @LazyCollection(LazyCollectionOption.EXTRA)
    @OneToMany(mappedBy = "submissionGroup", targetEntity = SubmissionFile.class)
    private List<SubmissionFile> files;

    @Column(updatable = false)
    private Date created;

    @Column
    private Date updated;

    @Column
    private String checksum;

    @Column
    @Enumerated(EnumType.STRING)
    private CommonAnalysisType analysisType;

    public BreadcrumbType getCrumbType() {

        return BreadcrumbType.SUBMISSION_GROUP;
    }

    public Breadcrumb getCrumbParent() {

        return this.getAnalysis();
    }

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public Analysis getAnalysis() {

        return analysis;
    }

    public void setAnalysis(Analysis analysis) {

        this.analysis = analysis;
    }

    public IUser getAuthor() {

        return author;
    }

    public void setAuthor(IUser author) {

        this.author = author;
    }

    public List<Submission> getSubmissions() {

        return submissions;
    }

    public void setSubmissions(List<Submission> submissions) {

        this.submissions = submissions;
    }

    public List<SubmissionFile> getFiles() {

        return files;
    }

    public void setFiles(List<SubmissionFile> files) {

        this.files = files;
    }

    public Date getCreated() {

        return created;
    }

    public void setCreated(Date created) {

        this.created = created;
    }

    public Date getUpdated() {

        return updated;
    }

    public void setUpdated(Date updated) {

        this.updated = updated;
    }

    public String getChecksum() {

        return checksum;
    }

    public void setChecksum(String checksum) {

        this.checksum = checksum;
    }

    public CommonAnalysisType getAnalysisType() {

        return analysisType;
    }

    public void setAnalysisType(CommonAnalysisType analysisType) {

        this.analysisType = analysisType;
    }

    @Override
    public String toString() {

        return Objects.toStringHelper(this)
                .add("id", id)
                .add("analysis", analysis != null ? analysis.getId() : null)
                .add("author", author != null ? author.getId() : null)
                .add("analysisType", analysisType)
                .toString();
    }
}
