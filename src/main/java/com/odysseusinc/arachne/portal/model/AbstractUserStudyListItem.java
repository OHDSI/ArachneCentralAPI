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
 * Created: September 07, 2017
 *
 */

package com.odysseusinc.arachne.portal.model;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class AbstractUserStudyListItem {
    @Id
    private Long id;


    @ManyToOne(optional = false, targetEntity = RawUser.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private IUser user;


    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;

    @Column(name = "roles")
    private String role;

    @Column
    private Boolean favourite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "first_lead_id")
    private User firstLead;

    public IUser getUser() {

        return user;
    }

    public void setUser(final IUser user) {

        this.user = user;
    }

    public Study getStudy() {

        return study;
    }

    public void setStudy(Study study) {

        this.study = study;
    }

    public String getRole() {

        return role;
    }

    public void setRole(String role) {

        this.role = role;
    }

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public Boolean getFavourite() {

        return favourite;
    }

    public void setFavourite(Boolean favourite) {

        this.favourite = favourite;
    }

    public User getFirstLead() {

        return firstLead;
    }

    public void setFirstLead(User firstLead) {

        this.firstLead = firstLead;
    }
}
