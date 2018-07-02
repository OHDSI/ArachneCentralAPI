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
 * Created: July 19, 2017
 *
 */

package com.odysseusinc.arachne.portal.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "studies_data_sources_comment")
public class StudyDataSourceComment {

    public StudyDataSourceComment() {

    }

    public StudyDataSourceComment(Long studyDataSourceId, Long userId, String comment) {

        this.studyDataSourceId = studyDataSourceId;
        this.userId = userId;
        this.comment = comment;
    }

    @Id
    @SequenceGenerator(name = "studies_data_sources_comment_id_seq",
            sequenceName = "studies_data_sources_comment_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "studies_data_sources_comment_id_seq")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "studies_data_sources_id")
    private Long studyDataSourceId;

    @Column
    private String comment;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public Long getUserId() {

        return userId;
    }

    public void setUserId(Long userId) {

        this.userId = userId;
    }

    public Long getStudyDataSourceId() {

        return studyDataSourceId;
    }

    public void setStudyDataSourceId(Long studyDataSourceId) {

        this.studyDataSourceId = studyDataSourceId;
    }

    public String getComment() {

        return comment;
    }

    public void setComment(String comment) {

        this.comment = comment;
    }
}
