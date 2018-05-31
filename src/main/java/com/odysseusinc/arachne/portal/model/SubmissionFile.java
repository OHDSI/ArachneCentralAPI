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
 * Created: December 06, 2016
 *
 */

package com.odysseusinc.arachne.portal.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "submission_files")
public class SubmissionFile extends ArachneFile {
    @Id
    @SequenceGenerator(name = "submission_files_pk_sequence", sequenceName = "submission_files_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "submission_files_pk_sequence")
    private Long id;
    @Column(nullable = false)
    private Boolean executable;
    @ManyToOne(fetch = FetchType.LAZY)
    private SubmissionGroup submissionGroup;
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = User.class)
    private IUser author;
    @Column(name = "version")
    private Integer version;
    @Column(name = "checksum")
    private String checksum;
    @Column(name = "entry_point")
    private String entryPoint;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public SubmissionGroup getSubmissionGroup() {

        return submissionGroup;
    }

    public void setSubmissionGroup(SubmissionGroup submissionGroup) {

        this.submissionGroup = submissionGroup;
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

    public Integer getVersion() {

        return version;
    }

    public void setVersion(Integer version) {

        this.version = version;
    }

    public String getChecksum() {

        return checksum;
    }

    public void setChecksum(String checksum) {

        this.checksum = checksum;
    }

    public String getEntryPoint() {

        return entryPoint;
    }

    public void setEntryPoint(String entryPoint) {

        this.entryPoint = entryPoint;
    }
}
