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
 * Created: May 17, 2017
 *
 */

package com.odysseusinc.arachne.portal.model;

import com.odysseusinc.arachne.portal.api.v1.dto.InvitationType;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.hibernate.validator.constraints.NotEmpty;

@Entity
@Table(name = "analysis_unlock_requests")
public class AnalysisUnlockRequest implements Invitationable, Serializable {

    @Id
    @SequenceGenerator(name = "analysis_unlock_requests_pk_sequence",
            sequenceName = "analysis_unlock_requests_id_seq", allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "analysis_unlock_requests_pk_sequence")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = User.class)
    private IUser user;
    @ManyToOne(fetch = FetchType.LAZY)
    private Analysis analysis;
    @Column
    private Date created;
    @NotEmpty
    @Column
    private String description;
    @Column
    @Enumerated(EnumType.STRING)
    private AnalysisUnlockRequestStatus status;
    @Column
    private String token;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public IUser getUser() {

        return user;
    }

    public void setUser(IUser user) {

        this.user = user;
    }

    public Analysis getAnalysis() {

        return analysis;
    }

    public void setAnalysis(Analysis analysis) {

        this.analysis = analysis;
    }

    public Date getCreated() {

        return created;
    }

    public void setCreated(Date created) {

        this.created = created;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public AnalysisUnlockRequestStatus getStatus() {

        return status;
    }

    public void setStatus(AnalysisUnlockRequestStatus status) {

        this.status = status;
    }

    public String getToken() {

        return token;
    }

    public void setToken(String token) {

        this.token = token;
    }

    @Override
    public String getInvitationType() {

        return InvitationType.UNLOCK_ANALYSIS;
    }

    @Override
    public IUser getAuthor() {

        return getUser();
    }

    @Override
    public String getActionType() {

        return "requested to unlock files for";
    }

    @Override
    public Analysis getEntity() {

        return analysis;
    }
}
