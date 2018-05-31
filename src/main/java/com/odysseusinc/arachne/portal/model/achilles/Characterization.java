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
 * Created: May 19, 2017
 *
 */

package com.odysseusinc.arachne.portal.model.achilles;

import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.model.RawDataSource;
import java.sql.Timestamp;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "characterizations")
public class Characterization {
    @Id
    @SequenceGenerator(name = "characterization_pk_sequence", sequenceName = "characterizations_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "characterization_pk_sequence")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = RawDataSource.class)
    @JoinColumn(name = "datasource_id")
    private IDataSource dataSource;
    @Column
    private Timestamp date;
    @OneToMany(targetEntity = AchillesFile.class, cascade = CascadeType.REMOVE)
    @JoinTable(
            name = "achilles_regular_files",
            joinColumns = @JoinColumn(name = "characterization_id", insertable = false, updatable = false),
            inverseJoinColumns = @JoinColumn(name = "achilles_file_id", insertable = false, updatable = false)
    )
    private Set<AchillesFile> files;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public IDataSource getDataSource() {

        return dataSource;
    }

    public void setDataSource(IDataSource dataSource) {

        this.dataSource = dataSource;
    }

    public Timestamp getDate() {

        return date;
    }

    public void setDate(Timestamp date) {

        this.date = date;
    }

    public Set<AchillesFile> getFiles() {

        return files;
    }

    public void setFiles(Set<AchillesFile> files) {

        this.files = files;
    }

}
