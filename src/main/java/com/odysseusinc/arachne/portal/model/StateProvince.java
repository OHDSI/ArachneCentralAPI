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
 * Created: February 15, 2017
 *
 */

package com.odysseusinc.arachne.portal.model;

import com.odysseusinc.arachne.portal.model.solr.SolrValue;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "states_provinces")
public class StateProvince implements SolrValue {

    @Id
    @SequenceGenerator(name = "states_provinces_seq", sequenceName = "states_provinces_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "states_provinces_seq")
    private Long id;

    @Column
    private String name;

    @Column
    private String isoCode;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getIsoCode() {

        return isoCode;
    }

    public void setIsoCode(String isoCode) {

        this.isoCode = isoCode;
    }

    @Override
    public Object getSolrValue() {

        return id;
    }

    @Override
    public Object getSolrQueryValue() {

        return name;
    }
}
