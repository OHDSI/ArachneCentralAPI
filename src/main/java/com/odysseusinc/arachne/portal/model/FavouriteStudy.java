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
 * Created: May 10, 2017
 *
 */

package com.odysseusinc.arachne.portal.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@Table(name = "favourite_studies")
@IdClass(FavouriteStudy.class)
public class FavouriteStudy implements Serializable {

    public FavouriteStudy() {

    }

    public FavouriteStudy(Long userId, Long studyId) {

        this.userId = userId;
        this.studyId = studyId;
    }

    @Id
    @Column(name = "study_id")
    private Long studyId;

    @Id
    @Column(name = "user_id")
    private Long userId;

    public Long getStudyId() {

        return studyId;
    }

    public void setStudyId(Long studyId) {

        this.studyId = studyId;
    }

    public Long getUserId() {

        return userId;
    }

    public void setUserId(Long userId) {

        this.userId = userId;
    }
}
