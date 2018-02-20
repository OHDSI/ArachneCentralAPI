/*
 *
 * Copyright 2017 Observational Health Data Sciences and Informatics
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
 * Created: November 07, 2016
 *
 */

package com.odysseusinc.arachne.portal.model;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonCDMVersionDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonHealthStatus;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonModelType;
import com.odysseusinc.arachne.portal.model.security.Tenant;
import com.odysseusinc.arachne.portal.model.solr.SolrFieldAnno;
import com.odysseusinc.arachne.portal.security.ArachnePermission;
import com.odysseusinc.arachne.portal.security.HasArachnePermissions;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import org.hibernate.validator.constraints.NotBlank;

@MappedSuperclass
public abstract class BaseDataSource implements IDataSource, Serializable, HasArachnePermissions {
    @Id
    @SequenceGenerator(name = "data_sources_pk_sequence", sequenceName = "data_sources_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "data_sources_pk_sequence")
    protected Long id;
    @Pattern(
            message = "Must be valid UUID.",
            regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$"
    )
    @Column(name = "uuid", nullable = false, unique = true)
    protected String uuid;
    @SolrFieldAnno(query = true)
    @NotBlank
    @Column(name = "name", nullable = false, unique = true)
    protected String name;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    protected DataNode dataNode;
    @Transient
    protected Set<ArachnePermission> permissions;
    @SolrFieldAnno(query = true, filter = true)
    @NotNull
    @Column
    @Enumerated(EnumType.STRING)
    protected CommonModelType modelType;
    @Column
    protected Date created = new Date();
    @Column
    @Enumerated(value = EnumType.STRING)
    protected CommonHealthStatus healthStatus = CommonHealthStatus.NOT_COLLECTED;
    @Column
    protected String healthStatusDescription;
    @Column
    protected Date deleted;
    @SolrFieldAnno(query = true, filter = true)
    @Column
    @Enumerated(EnumType.STRING)
    protected CommonCDMVersionDTO cdmVersion;
    @SolrFieldAnno(query = true, filter = true)
    @NotBlank
    @Column
    protected String organization;

    @ManyToMany(targetEntity = Tenant.class, fetch = FetchType.LAZY)
    @JoinTable(name = "tenants_data_sources",
            joinColumns = @JoinColumn(name = "data_source_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "tenant_id", referencedColumnName = "id"))
    @SolrFieldAnno(filter = true)
    protected Set<Tenant> tenants = new HashSet<>();

    @Override
    public boolean equals(final Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof BaseDataSource)) {
            return false;
        }
        final BaseDataSource s = (BaseDataSource) obj;
        return java.util.Objects.equals(getId(), s.getId());
    }

    @Override
    public int hashCode() {

        return java.util.Objects.hashCode(this.getId());
    }

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public String getUuid() {

        return uuid;
    }

    public void setUuid(String uuid) {

        this.uuid = uuid;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public DataNode getDataNode() {

        return dataNode;
    }

    public void setDataNode(DataNode dataNode) {

        this.dataNode = dataNode;
    }

    public Set<ArachnePermission> getPermissions() {

        return permissions;
    }

    public void setPermissions(Set<ArachnePermission> permissions) {

        this.permissions = permissions;
    }

    public CommonModelType getModelType() {

        return modelType;
    }

    public void setModelType(CommonModelType modelType) {

        this.modelType = modelType;
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

    public Date getCreated() {

        return created;
    }

    public void setCreated(Date created) {

        this.created = created;
    }

    public Date getDeleted() {

        return deleted;
    }

    public void setDeleted(Date deletedAt) {

        this.deleted = deletedAt;
    }

    public CommonCDMVersionDTO getCdmVersion() {

        return cdmVersion;
    }

    public void setCdmVersion(CommonCDMVersionDTO cdmVersion) {

        this.cdmVersion = cdmVersion;
    }

    public String getOrganization() {

        return organization;
    }

    public void setOrganization(String organization) {

        this.organization = organization;
    }

    public Set<Tenant> getTenants() {

        return tenants;
    }

    public void setTenants(Set<Tenant> tenants) {

        this.tenants = tenants;
    }
}