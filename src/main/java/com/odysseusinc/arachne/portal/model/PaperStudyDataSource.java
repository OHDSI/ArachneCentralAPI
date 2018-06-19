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
 * Created: July 14, 2017
 *
 */

package com.odysseusinc.arachne.portal.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "paper_study_data_sources")
public class PaperStudyDataSource {

    @Id
    @SequenceGenerator(name = "paper_study_data_sources_pk_sequence",
            sequenceName = "paper_study_data_sources_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "paper_study_data_sources_pk_sequence")
    private Long id;

    @ManyToOne(optional = false, targetEntity = Paper.class, fetch = FetchType.LAZY)
    private Paper paper;
    @OneToOne(fetch = FetchType.LAZY, targetEntity = DataSource.class)
    @JoinColumn(name = "data_source_id")
    private IDataSource dataSource;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public Paper getPaper() {

        return paper;
    }

    public void setPaper(Paper paper) {

        this.paper = paper;
    }

    public IDataSource getDataSource() {

        return dataSource;
    }

    public void setDataSource(IDataSource dataSource) {

        this.dataSource = dataSource;
    }
}
