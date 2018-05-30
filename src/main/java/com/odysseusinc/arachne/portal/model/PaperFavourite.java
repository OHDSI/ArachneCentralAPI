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
 * Created: August 11, 2017
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
@Table(name = "paper_favourites")
@IdClass(PaperFavourite.class)
public class PaperFavourite implements Serializable {

    @Id
    @Column(name = "paper_id")
    private Long paperId;

    @Id
    @Column(name = "user_id")
    private Long userId;

    public PaperFavourite() {

    }

    public PaperFavourite(Long userId, Long paperId) {

        this.userId = userId;
        this.paperId = paperId;
    }

    public Long getPaperId() {

        return paperId;
    }

    public void setPaperId(Long paperId) {

        this.paperId = paperId;
    }

    public Long getUserId() {

        return userId;
    }

    public void setUserId(Long userId) {

        this.userId = userId;
    }
}
