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
 * Created: November 26, 2016
 *
 */

package com.odysseusinc.arachne.portal.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "analyses_files")
public class AnalysisFile extends ArachneFile {
    @Id
    @SequenceGenerator(name = "analysis_file_pk_sequence", sequenceName = "analyses_files_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "analysis_file_pk_sequence")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    protected Analysis analysis;

    @Column(nullable = false)
    private Boolean executable;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = User.class)
    private IUser author;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = User.class)
    @JoinColumn(name = "updated_by")
    private IUser updatedBy;

    @Column(name = "version")
    private Integer version;

    @Column(name = "entry_point")
    private String entryPoint;

    @ManyToOne(targetEntity = DataReference.class, fetch = FetchType.LAZY)
    private DataReference dataReference;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public Analysis getAnalysis() {

        return analysis;
    }

    public void setAnalysis(Analysis analysis) {

        this.analysis = analysis;
    }

    public Boolean getExecutable() {

        return executable;
    }

    public void setExecutable(Boolean executable) {

        this.executable = executable;
    }

    public IUser getAuthor() {

        return author;
    }

    public void setAuthor(IUser author) {

        this.author = author;
    }

    public IUser getUpdatedBy() {

        return updatedBy;
    }

    public void setUpdatedBy(IUser updatedBy) {

        this.updatedBy = updatedBy;
    }

    public Integer getVersion() {

        return version;
    }

    public void incrementVersion() {

        version++;
    }

    public void setVersion(Integer version) {

        this.version = version;
    }

    public DataReference getDataReference() {

        return dataReference;
    }

    public void setDataReference(DataReference dataReference) {

        this.dataReference = dataReference;
    }

    public String getEntryPoint() {

        return entryPoint;
    }

    public void setEntryPoint(String entryPoint) {

        this.entryPoint = entryPoint;
    }
}
