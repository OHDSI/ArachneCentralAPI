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
 * Created: April 20, 2017
 *
 */

package com.odysseusinc.arachne.portal.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "comment_topics_w_count")
public class CommentTopic implements Serializable{
    @Id
    @SequenceGenerator(name = "comment_topics_pk_sequence", sequenceName = "comment_topics_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comment_topics_pk_sequence")
    private Long id;
    @OneToMany(mappedBy = "topic", targetEntity = Comment.class, fetch = FetchType.LAZY)
    private List<Comment> comments = new LinkedList<>();
    @Column(insertable = false, updatable = false)
    private Long count;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public List<Comment> getComments() {

        return comments;
    }

    public void setComments(List<Comment> comments) {

        this.comments = comments;
    }

    public Long getCount() {

        return count;
    }

    public void setCount(Long count) {

        this.count = count;
    }
}
