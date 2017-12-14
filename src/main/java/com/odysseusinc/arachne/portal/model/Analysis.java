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
 * Created: November 22, 2016
 *
 */

package com.odysseusinc.arachne.portal.model;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
import com.odysseusinc.arachne.portal.security.ArachnePermission;
import com.odysseusinc.arachne.portal.security.HasArachnePermissions;
import com.odysseusinc.arachne.portal.service.impl.breadcrumb.Breadcrumb;
import com.odysseusinc.arachne.portal.service.impl.breadcrumb.BreadcrumbType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedSubgraph;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.DiscriminatorFormula;

@NamedEntityGraphs(value = {
        @NamedEntityGraph(
                name = "Analysis.default",
                attributeNodes = {
                        @NamedAttributeNode(value = "submissions"),
                        @NamedAttributeNode(value = "submissions", subgraph = "submissionsGraph")
                },
                subgraphs = {
                        @NamedSubgraph(
                                name = "submissionsGraph",
                                attributeNodes = {
                                        @NamedAttributeNode(value = "author"),
                                        @NamedAttributeNode(value = "submissionGroup"),
                                        @NamedAttributeNode(value = "submissionInsight"),
                                        @NamedAttributeNode(value = "dataSource")
                                }
                        )
                }
        )
})
@Entity
@Table(name = "analyses")
@DiscriminatorFormula("'ANALYSIS_ENTITY'")
public class Analysis implements HasArachnePermissions, Breadcrumb {

    public Analysis() {

    }

    public Analysis(String title, String description, CommonAnalysisType type) {

        this.title = title;
        this.description = description;
        this.type = type;
    }

    @Id
    @SequenceGenerator(name = "analyses_pk_sequence", sequenceName = "analyses_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "analyses_pk_sequence")
    private Long id;

    @Column
    private String title;

    @Column(length = 1000)
    private String description;

    @Column
    private Date created;

    @Column
    private Date updated;

    @ManyToOne
    private User author;

    @ManyToOne
    private Study study;

    @Column(name = "ord")
    private Integer ord;

    @Column(name = "is_locked")
    private Boolean locked = false;

    @OneToMany(mappedBy = "analysis", targetEntity = AnalysisFile.class)
    @OrderBy("label asc")
    private List<AnalysisFile> files = new ArrayList<>();

    @OneToMany(mappedBy = "analysis", targetEntity = Submission.class)
    @OrderBy("id asc")
    private List<Submission> submissions;

    @OneToMany(mappedBy = "analysis", targetEntity = SubmissionGroup.class)
    @OrderBy("created desc")
    private List<SubmissionGroup> submissionGroups = new ArrayList<>();

    @Transient
    private Set<ArachnePermission> permissions;

    @Column
    @Enumerated(EnumType.STRING)
    private CommonAnalysisType type;

    public BreadcrumbType getCrumbType() {

        return BreadcrumbType.ANALYSIS;
    }

    public String getCrumbTitle() {

        return this.getTitle();
    }

    public Breadcrumb getCrumbParent() {

        return this.getStudy();
    }

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public String getTitle() {

        return title;
    }

    public void setTitle(String title) {

        this.title = title;
    }

    public User getAuthor() {

        return author;
    }

    public void setAuthor(User author) {

        this.author = author;
    }

    public Study getStudy() {

        return study;
    }

    public void setStudy(Study study) {

        this.study = study;
    }

    public Integer getOrd() {

        return ord;
    }

    public void setOrd(Integer ord) {

        this.ord = ord;
    }

    public Boolean getLocked() {

        return locked;
    }

    public void setLocked(Boolean locked) {

        this.locked = locked;
    }

    public List<AnalysisFile> getFiles() {

        return files;
    }

    public void setFiles(List<AnalysisFile> files) {

        this.files = files;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
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

    public List<Submission> getSubmissions() {

        return submissions;
    }

    public void setSubmissions(List<Submission> submissions) {

        this.submissions = submissions;
    }

    @Override
    public Set<ArachnePermission> getPermissions() {

        return permissions;
    }

    @Override
    public void setPermissions(Set<ArachnePermission> permissions) {

        this.permissions = permissions;
    }

    public List<SubmissionGroup> getSubmissionGroups() {

        return submissionGroups;
    }

    public void setSubmissionGroups(List<SubmissionGroup> submissionGroups) {

        this.submissionGroups = submissionGroups;
    }

    public CommonAnalysisType getType() {

        return type;
    }

    public void setType(CommonAnalysisType type) {

        this.type = type;
    }
}
