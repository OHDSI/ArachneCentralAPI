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
 * Created: November 18, 2016
 *
 */

package com.odysseusinc.arachne.portal.model;

import com.google.common.base.Objects;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonHealthStatus;
import com.odysseusinc.arachne.portal.security.ArachnePermission;
import com.odysseusinc.arachne.portal.security.HasArachnePermissions;
import javax.persistence.JoinColumn;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "datanodes")
public class DataNode implements HasArachnePermissions {

    @Id
    @SequenceGenerator(name = "datanodes_pk_sequence", sequenceName = "datanodes_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "datanodes_pk_sequence")
    private Long id;

    @Size(max = 50)
    @Column(length = 50, name = "sid")
    private String sid;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    private DataNodeStatus status;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "dataNode", targetEntity = DataSource.class)
    private Set<DataSource> dataSources;

    @Column(name = "health_status")
    @Enumerated(value = EnumType.STRING)
    private CommonHealthStatus healthStatus = CommonHealthStatus.NOT_COLLECTED;

    @Column(name = "health_status_description")
    private String healthStatusDescription;

    @Column(name = "token")
    private String token;

    @Column(name = "created")
    private Date created;

    @NotNull
    @Column(name = "is_virtual")
    private Boolean virtual;

    @OneToMany(mappedBy = "dataNode", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<DataNodeUser> dataNodeUsers = new HashSet<>();

    @OneToMany(mappedBy = "dataNode", fetch = FetchType.LAZY)
    private Set<Atlas> atlasList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Transient
    private Set<ArachnePermission> permissions;

    @Column
    private Boolean published;

    public String getAtlasVersion() {

        Set<Atlas> atlasList = this.getAtlasList();

        if (atlasList == null || atlasList.size() == 0) {
            return null;
        }

        return atlasList.stream()
                .filter(a -> a.getVersion() != null)
                .map(Atlas::getVersion)
                .sorted()
                .findAny()
                .orElse(null);
    }

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    @Deprecated
    public String getSid() {

        return sid;
    }

    @Deprecated
    public void setSid(String sid) {

        this.sid = sid;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public Set<DataSource> getDataSources() {

        return dataSources;
    }

    public void setDataSources(Set<DataSource> dataSources) {

        this.dataSources = dataSources;
    }

    public DataNodeStatus getStatus() {

        return status;
    }

    public void setStatus(DataNodeStatus status) {

        this.status = status;
    }

    public CommonHealthStatus getHealthStatus() {

        return healthStatus;
    }

    public void setHealthStatus(CommonHealthStatus healthStatus) {

        this.healthStatus = healthStatus;
    }

    public String getHealthStatusDescription() {

        return healthStatusDescription;
    }

    public void setHealthStatusDescription(String healthStatusDescription) {

        this.healthStatusDescription = healthStatusDescription;
    }

    public String getToken() {

        return token;
    }

    public void setToken(String token) {

        this.token = token;
    }

    public Date getCreated() {

        return created;
    }

    public void setCreated(Date created) {

        this.created = created;
    }

    public Boolean getVirtual() {

        return virtual;
    }

    public void setVirtual(Boolean virtual) {

        this.virtual = virtual;
    }

    public Set<DataNodeUser> getDataNodeUsers() {

        return dataNodeUsers;
    }

    public void setDataNodeUsers(Set<DataNodeUser> dataNodeUsers) {

        this.dataNodeUsers.clear();
        if (dataNodeUsers != null) {
            this.dataNodeUsers.addAll(dataNodeUsers);
        }
    }

    public Set<Atlas> getAtlasList() {

        return atlasList;
    }

    public void setAtlasList(Set<Atlas> atlasList) {

        this.atlasList = atlasList;
    }

    public Boolean getPublished() {
        return published;
    }

    public void setPublished(Boolean published) {
        this.published = published;
    }

    public Organization getOrganization() {

        return organization;
    }

    public void setOrganization(Organization organization) {

        this.organization = organization;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DataNode dataNode = (DataNode) o;
        return Objects.equal(sid, dataNode.sid)
                && Objects.equal(token, dataNode.token);
    }

    @Override
    public int hashCode() {

        return Objects.hashCode(sid, token);
    }

    @Override
    public Set<ArachnePermission> getPermissions() {

        return permissions;
    }

    @Override
    public void setPermissions(Set<ArachnePermission> permissions) {

        this.permissions = permissions;
    }
}
