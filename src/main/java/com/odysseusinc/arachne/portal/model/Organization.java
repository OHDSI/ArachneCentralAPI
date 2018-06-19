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
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva, Konstantin Yaroshovets
 * Created: February 6, 2018
 *
 */

package com.odysseusinc.arachne.portal.model;

import com.google.common.base.MoreObjects;
import com.odysseusinc.arachne.portal.security.ArachnePermission;
import com.odysseusinc.arachne.portal.security.HasArachnePermissions;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "organizations")
public class Organization implements Serializable, HasArachnePermissions {

    @Id
    @SequenceGenerator(name = "organizations_pk_sequence", sequenceName = "organizations_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "organizations_pk_sequence")
    private Long id;

    private String name;

    @OneToMany(mappedBy = "organization")
    private List<DataNode> dataNodes;

    @Transient
    protected Set<ArachnePermission> permissions;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public List<DataNode> getDataNodes() {

        return dataNodes;
    }

    public void setDataNodes(List<DataNode> dataNodes) {

        this.dataNodes = dataNodes;
    }

    @Override
    public Set<ArachnePermission> getPermissions() {

        return permissions;
    }

    @Override
    public void setPermissions(Set<ArachnePermission> permissions) {

        this.permissions = permissions;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Organization that = (Organization) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getId());
    }

    @Override
    public String toString() {

        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("dataNodes", dataNodes)
                .toString();
    }
}
