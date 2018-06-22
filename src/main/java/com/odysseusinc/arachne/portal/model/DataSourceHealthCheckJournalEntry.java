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
 * Created: July 26, 2017
 *
 */

package com.odysseusinc.arachne.portal.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "data_source_health_check_journal")
public class DataSourceHealthCheckJournalEntry {

    @Id
    @SequenceGenerator(name = "data_source_health_check_journal_pk_sequence",
            sequenceName = "data_source_health_check_journal_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "data_source_health_check_journal_pk_sequence")
    private Long id;

    @NotNull
    @Column
    private Date created;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = RawDataSource.class)
    private IDataSource dataSource;

    @NotNull
    private Long delay;

    public DataSourceHealthCheckJournalEntry() {

    }

    public DataSourceHealthCheckJournalEntry(Date created, IDataSource dataSource, Long delay) {

        this.created = created;
        this.dataSource = dataSource;
        this.delay = delay;
    }

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public Date getCreated() {

        return created;
    }

    public void setCreated(Date created) {

        this.created = created;
    }

    public IDataSource getDataSource() {

        return dataSource;
    }

    public void setDataSource(IDataSource dataSource) {

        this.dataSource = dataSource;
    }

    public Long getDelay() {

        return delay;
    }

    public void setDelay(Long delay) {

        this.delay = delay;
    }
}
