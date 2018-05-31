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
 * Created: December 05, 2016
 *
 */

package com.odysseusinc.arachne.portal.model;

import com.odysseusinc.arachne.portal.api.v1.dto.InvitationType;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "studies_data_sources")
@SQLDelete(sql = "UPDATE studies_data_sources SET deleted_at = current_timestamp, status = 'DELETED' WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class StudyDataSourceLink implements Invitationable {

    @Id
    @SequenceGenerator(name = "studies_data_sources_pk_sequence",
            sequenceName = "studies_data_sources_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "studies_data_sources_pk_sequence")
    private Long id;

    @ManyToOne(optional = false, targetEntity = Study.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;

    @ManyToOne(optional = false, targetEntity = RawDataSource.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "data_source_id")
    private IDataSource dataSource;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private DataSourceStatus status;

    @Column
    private Date created;

    @Column
    private String token;

    @ManyToOne(optional = false, targetEntity = User.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private IUser createdBy;

    @Column(name = "deleted_at")
    private Date deletedAt;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public Study getStudy() {

        return study;
    }

    public void setStudy(Study study) {

        this.study = study;
    }

    public IDataSource getDataSource() {

        return dataSource;
    }

    public void setDataSource(IDataSource dataSource) {

        this.dataSource = dataSource;
    }

    public DataSourceStatus getStatus() {

        return status;
    }

    public void setStatus(DataSourceStatus status) {

        this.status = status;
    }

    public Date getCreated() {

        return created;
    }

    public void setCreated(Date created) {

        this.created = created;
    }

    public String getToken() {

        return token;
    }

    public void setToken(String token) {

        this.token = token;
    }

    public IUser getCreatedBy() {

        return createdBy;
    }

    public void setCreatedBy(IUser createdBy) {

        this.createdBy = createdBy;
    }

    public Date getDeletedAt() {

        return deletedAt;
    }

    public void setDeletedAt(Date deletedAt) {

        this.deletedAt = deletedAt;
    }

    @Override
    public String getInvitationType() {

        return InvitationType.DATA_OWNER;
    }

    @Override
    public IUser getAuthor() {

        return getCreatedBy();
    }

    @Override
    public String getActionType() {

        return String.format("requested access to data source \"%s\" for", getDataSource().getName());
    }

    @Override
    public Study getEntity() {

        return study;
    }
}
