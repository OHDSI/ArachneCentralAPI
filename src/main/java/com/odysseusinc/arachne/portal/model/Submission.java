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
 * Created: December 06, 2016
 *
 */

package com.odysseusinc.arachne.portal.model;

import com.google.common.base.Objects;
import com.google.gson.JsonObject;
import com.odysseusinc.arachne.portal.api.v1.dto.InvitationType;
import com.odysseusinc.arachne.portal.security.ArachnePermission;
import com.odysseusinc.arachne.portal.security.HasArachnePermissions;
import com.odysseusinc.arachne.portal.service.impl.breadcrumb.Breadcrumb;
import com.odysseusinc.arachne.portal.service.impl.breadcrumb.BreadcrumbType;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "submissions")
public class Submission implements HasArachnePermissions, Breadcrumb, Invitationable {

    @Id
    @SequenceGenerator(name = "submissions_pk_sequence", sequenceName = "submissions_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "submissions_pk_sequence")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private SubmissionGroup submissionGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    private Analysis analysis;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = User.class)
    private IUser author;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = RawDataSource.class)
    private IDataSource dataSource;

    @LazyCollection(LazyCollectionOption.EXTRA)
    @OneToMany(mappedBy = "submission", targetEntity = ResultFile.class)
    private List<ResultFile> resultFiles = new LinkedList<>();

    @Column
    private Date created;

    @Column
    private Date updated;

    @Column
    private String stdout;

    @Column(name = "stdout_date")
    private Date stdoutDate;

    @Column(name = "update_password")
    private String updatePassword;

    @Transient
    private SubmissionStatus status;

    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "submission", targetEntity = SubmissionStatusHistoryElement.class)
    private List<SubmissionStatusHistoryElement> statusHistory;

    @OneToOne(cascade = {CascadeType.ALL}, orphanRemoval = true, mappedBy = "submission", fetch = FetchType.LAZY)
    private SubmissionInsight submissionInsight;

    @Column
    private String token;

    @Column
    @Type(type = "com.odysseusinc.arachne.portal.repository.hibernate.JsonbType")
    private JsonObject resultInfo;

    @Column
    private Boolean hidden = false;

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Submission s = (Submission) obj;
        return java.util.Objects.equals(id, s.id);
    }

    @Transient
    private Set<ArachnePermission> permissions;

    public BreadcrumbType getCrumbType() {

        return BreadcrumbType.SUBMISSION;
    }

    public Breadcrumb getCrumbParent() {

        return this.getSubmissionGroup();
    }

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public IDataSource getDataSource() {

        return dataSource;
    }

    public void setDataSource(IDataSource dataSource) {

        this.dataSource = dataSource;
    }

    public List<ResultFile> getResultFiles() {

        return resultFiles;
    }

    public void setResultFiles(List<ResultFile> resultFiles) {

        this.resultFiles = resultFiles;
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

    public String getStdout() {

        return stdout;
    }

    public void setStdout(String stdout) {

        this.stdout = stdout;
    }

    public Date getStdoutDate() {

        return stdoutDate;
    }

    public void setStdoutDate(Date stdoutDate) {

        this.stdoutDate = stdoutDate;
    }

    public String getUpdatePassword() {

        return updatePassword;
    }

    public void setUpdatePassword(String updatePassword) {

        this.updatePassword = updatePassword;
    }

    public SubmissionStatus getStatus() {

        return status;
    }

    public List<SubmissionStatusHistoryElement> getStatusHistory() {

        return statusHistory;
    }

    public void setStatusHistory(List<SubmissionStatusHistoryElement> statusHistory) {

        this.statusHistory = statusHistory;
    }

    public SubmissionInsight getSubmissionInsight() {

        return submissionInsight;
    }

    public void setSubmissionInsight(SubmissionInsight submissionInsight) {

        this.submissionInsight = submissionInsight;
    }

    @Override
    public Set<ArachnePermission> getPermissions() {

        return permissions;
    }

    @Override
    public void setPermissions(Set<ArachnePermission> permissions) {

        this.permissions = permissions;
    }

    public SubmissionGroup getSubmissionGroup() {

        return submissionGroup;
    }

    public void setSubmissionGroup(SubmissionGroup submissionGroup) {

        this.submissionGroup = submissionGroup;
    }

    public Boolean getHidden() {

        return hidden;
    }

    public void setHidden(Boolean hidden) {

        this.hidden = hidden;
    }

    //TODO should be moved to service
    @PostLoad
    @PostPersist
    @PostUpdate
    public void postLoad() {

        if (statusHistory != null) {
            Optional<SubmissionStatusHistoryElement> historyElement = statusHistory.stream()
                    .max(Comparator.comparing(SubmissionStatusHistoryElement::getDate));
            status = historyElement.map(SubmissionStatusHistoryElement::getStatus)
                    .orElse(SubmissionStatus.STARTING);
        } else {
            status = SubmissionStatus.STARTING;
        }
    }

    @Override
    public String toString() {

        return Objects.toStringHelper(this)
                .add("id", id)
                .add("author", author != null ? author.getId() : null)
                .add("submissionGroup", submissionGroup != null ? submissionGroup.getId() : null)
                .add("status", status)
                .toString();
    }

    public String getToken() {

        return token;
    }

    public void setToken(String token) {

        this.token = token;
    }

    public JsonObject getResultInfo() {

        return resultInfo;
    }

    public void setResultInfo(JsonObject resultInfo) {

        this.resultInfo = resultInfo;
    }

    @Override
    public String getInvitationType() {

        return status.isFinished()
                ? InvitationType.APPROVE_PUBLISH_SUBMISSION : InvitationType.APPROVE_EXECUTE_SUBMISSION;
    }

    @Override
    public String getActionType() {

        return status.isFinished() ? "asks you to publish a submission on" : "asks you to execute a submission on";
    }

    @Override
    public Analysis getEntity() {

        return analysis;
    }
}
