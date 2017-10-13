/**
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
 * Created: November 07, 2016
 *
 */

package com.odysseusinc.arachne.portal.model;

import com.odysseusinc.arachne.portal.model.statemachine.HasState;
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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.DiscriminatorFormula;

@Entity
@Table(name = "studies")
@DiscriminatorFormula("'Entity'")
public class Study implements HasArachnePermissions, Breadcrumb, HasState<StudyStatus> {
    public Study() {

    }

    public Study(Study study) {

        this.title = study.title;
        this.description = study.description;
        this.type = study.type;
        this.created = study.created;
        this.updated = study.updated;
        this.status = study.status;
        this.participants = study.participants;
        this.permissions = study.permissions;
        this.analyses = study.analyses;
        this.files = study.files;
        this.dataSources = study.dataSources;
        this.startDate = study.startDate;
        this.endDate = study.endDate;
    }

    @Id
    @SequenceGenerator(name = "studies_pk_sequence", sequenceName = "studies_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "studies_pk_sequence")
    private Long id;

    @Column(length = 1024)
    private String title;

    @Column(length = 10000)
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    private StudyType type;

    @Column
    private Date created;

    @Column
    private Date updated;

    @ManyToOne(fetch = FetchType.EAGER)
    private StudyStatus status;

    @OneToMany(mappedBy = "study")
    private List<UserStudyExtended> participants;

    @Transient
    private Set<ArachnePermission> permissions;

    @OneToMany(mappedBy = "study", targetEntity = Analysis.class)
    @OrderBy("ord ASC")
    private List<Analysis> analyses = new ArrayList<>();

    @OneToMany(mappedBy = "study", targetEntity = StudyFile.class)
    private List<StudyFile> files;

    @OneToMany(targetEntity = StudyDataSourceLink.class, mappedBy = "study")
    @OrderBy("deleted_at DESC, created ASC")
    private List<StudyDataSourceLink> dataSources;

    @OneToOne(mappedBy = "study")
    private Paper paper;

    @Column
    private Date startDate;

    @Column
    private Date endDate;

    public BreadcrumbType getCrumbType() {

        return BreadcrumbType.STUDY;
    }

    public String getCrumbTitle() {

        return this.getTitle();
    }

    public Breadcrumb getCrumbParent() {

        return null;
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

    public List<UserStudyExtended> getParticipants() {

        return participants;
    }

    public void setParticipants(List<UserStudyExtended> participants) {

        this.participants = participants;
    }

    public Date getStartDate() {

        return startDate;
    }

    public void setStartDate(Date startDate) {

        this.startDate = startDate;
    }

    public Date getEndDate() {

        return endDate;
    }

    public void setEndDate(Date endDate) {

        this.endDate = endDate;
    }

    public List<StudyDataSourceLink> getDataSources() {

        return dataSources;
    }

    public void setDataSources(List<StudyDataSourceLink> dataSources) {

        this.dataSources = dataSources;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public StudyType getType() {

        return type;
    }

    public void setType(StudyType type) {

        this.type = type;
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

    public StudyStatus getStatus() {

        return status;
    }

    public void setStatus(StudyStatus status) {

        this.status = status;
    }

    public List<Analysis> getAnalyses() {

        return analyses;
    }

    public void setAnalyses(List<Analysis> analyses) {

        this.analyses = analyses;
    }

    public List<StudyFile> getFiles() {

        return files;
    }

    public void setFiles(List<StudyFile> files) {

        this.files = files;
    }

    @Override
    public Set<ArachnePermission> getPermissions() {

        return permissions;
    }

    @Override
    public void setPermissions(Set<ArachnePermission> permissions) {

        this.permissions = permissions;
    }

    @Override
    public StudyStatus getState() {
        return getStatus();
    }

    @Override
    public void setState(StudyStatus state) {
        setStatus(state);
    }

    public Paper getPaper() {

        return paper;
    }

    public void setPaper(Paper paper) {

        this.paper = paper;
    }
}
