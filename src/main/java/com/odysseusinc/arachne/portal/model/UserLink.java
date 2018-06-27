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
 * Created: January 13, 2017
 *
 */

package com.odysseusinc.arachne.portal.model;

import com.odysseusinc.arachne.portal.model.solr.SolrValue;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "user_links")
public class UserLink implements SolrValue {
    @Id
    @SequenceGenerator(name = "user_links_pk_sequence", sequenceName = "user_links_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_links_pk_sequence")
    private Long id;

    @ManyToOne(optional = false, targetEntity = User.class, fetch = FetchType.LAZY)
    private IUser user;

    @Column(length = 1024)
    private String description;

    @Column
    private String title;

    @Column
    private String url;

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

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public String getTitle() {

        return title;
    }

    public void setTitle(String title) {

        this.title = title;
    }

    public String getUrl() {

        return url;
    }

    public void setUrl(String url) {

        this.url = url;
    }

    @Transient
    @Override
    public Object getSolrValue() {
        return id;
    }

    @Transient
    @Override
    public Object getSolrQueryValue() {

        return description + ' ' + title + ' ' + url;
    }
}
