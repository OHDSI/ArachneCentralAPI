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
 * Created: September 08, 2017
 *
 */

package com.odysseusinc.arachne.portal.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "users_studies_extended")
public class UserStudyExtended implements ParticipantLink {

    @Id
    private Long id;

    @ManyToOne(optional = false, targetEntity = RawUser.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private IUser user;

    @ManyToOne(optional = false, targetEntity = Study.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private ParticipantRole role;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ParticipantStatus status;

    @Column
    private String comment;

    @ManyToOne(targetEntity = DataSource.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "owned_data_source_id")
    private DataSource dataSource;

    public IUser getUser() {

        return user;
    }

    public void setUser(IUser user) {

        this.user = user;
    }

    public Study getStudy() {

        return study;
    }

    public void setStudy(Study study) {

        this.study = study;
    }

    public ParticipantRole getRole() {

        return role;
    }

    public void setRole(ParticipantRole role) {

        this.role = role;
    }

    public ParticipantStatus getStatus() {

        return status;
    }

    public void setStatus(ParticipantStatus status) {

        this.status = status;
    }

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public String getComment() {

        return comment;
    }

    public void setComment(String comment) {

        this.comment = comment;
    }

    public DataSource getDataSource() {

        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {

        this.dataSource = dataSource;
    }
}
