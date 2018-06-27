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

import com.google.gson.JsonObject;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "achilles_files")
public class AchillesFile {
    @Id
    @SequenceGenerator(name = "achilles_file_pk_sequence", sequenceName = "achilles_files_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "achilles_file_pk_sequence")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private Characterization characterization;
    @Column(nullable = false)
    private String filePath;
    @Column(name = "data", nullable = false)
    @Type(type = "com.odysseusinc.arachne.portal.repository.hibernate.JsonbType")
    private JsonObject data;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public Characterization getCharacterization() {

        return characterization;
    }

    public void setCharacterization(Characterization characterization) {

        this.characterization = characterization;
    }

    public String getFilePath() {

        return filePath;
    }

    public void setFilePath(String filePath) {

        this.filePath = filePath;
    }

    public JsonObject getData() {

        return data;
    }

    public void setData(JsonObject data) {

        this.data = data;
    }

}
