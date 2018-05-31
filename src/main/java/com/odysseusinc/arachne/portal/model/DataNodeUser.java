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
 * Created: May 23, 2017
 *
 */

package com.odysseusinc.arachne.portal.model;

import java.util.Objects;
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
@Table(name = "datanodes_users")
public class DataNodeUser {

    @Id
    @SequenceGenerator(name = "datanodes_users_pk_sequence", sequenceName = "datanodes_users_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "datanodes_users_pk_sequence")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = RawUser.class)
    @JoinColumn(name = "user_id")
    private IUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "datanode_id")
    private DataNode dataNode;

    public DataNodeUser() {

    }

    public DataNodeUser(IUser user, DataNode dataNode) {

        this.user = user;
        this.dataNode = dataNode;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataNodeUser that = (DataNodeUser) o;
        return Objects.equals(user, that.user) &&
                Objects.equals(dataNode, that.dataNode);
    }

    @Override
    public int hashCode() {

        return Objects.hash(user, dataNode);
    }

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public IUser getUser() {

        return user;
    }

    public void setUser(IUser user) {

        this.user = user;
    }

    public DataNode getDataNode() {

        return dataNode;
    }

    public void setDataNode(DataNode dataNode) {

        this.dataNode = dataNode;
    }
}
