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
 * Created: November 15, 2016
 *
 */

package com.odysseusinc.arachne.portal.model;

import com.google.common.base.Objects;
import com.odysseusinc.arachne.portal.model.statemachine.IsState;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * Created by AKrutov on 15.11.2016.
 */
@Entity
@Table(name = "study_statuses")
public class StudyStatus implements IsState {

    @Id
    @SequenceGenerator(name = "study_status_pk_sequence", sequenceName = "study_statuses_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "study_status_pk_sequence")
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    @Override
    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }


    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof StudyStatus)) {
            return false;
        }
        StudyStatus that = (StudyStatus) o;
        return Objects.equal(id, that.getId());
    }

    @Override
    public int hashCode() {

        return Objects.hashCode(id);
    }

    @Override
    public String toString() {

        return "StudyStatus{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
