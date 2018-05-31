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
 * Created: January 25, 2017
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
import javax.persistence.PrePersist;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "studies_users")
@SQLDelete(sql = "UPDATE studies_users SET deleted_at = current_timestamp, status = 'DELETED', comment = null WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class UserStudy implements Invitationable {

    @Id
    @SequenceGenerator(name = "studies_users_pk_sequence", sequenceName = "studies_users_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "studies_users_pk_sequence")
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
    private Date created;

    @Column
    private String token;

    @ManyToOne(optional = false, targetEntity = User.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private IUser createdBy;

    @Column(name = "deleted_at")
    private Date deletedAt;

    @Column
    private String comment;

    @PrePersist
    public void prePersist() {

        if (created == null) {
            this.setCreated(new Date());
        }
    }

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

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public ParticipantStatus getStatus() {

        return status;
    }

    public void setStatus(ParticipantStatus status) {

        this.status = status;
    }

    public Date getCreated() {

        return created;
    }

    @Override
    public String getInvitationType() {

        return InvitationType.COLLABORATOR;
    }

    @Override
    public IUser getAuthor() {

        return getCreatedBy();
    }

    @Override
    public String getActionType() {

        return "invited you to collaborate on";
    }

    @Override
    public Study getEntity() {

        return study;
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

    public String getComment() {

        return comment;
    }

    public void setComment(String comment) {

        this.comment = comment;
    }
}
