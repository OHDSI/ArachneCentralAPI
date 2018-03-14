package com.odysseusinc.arachne.portal.model;

import com.odysseusinc.arachne.portal.model.security.Tenant;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
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
import javax.persistence.Table;

@MappedSuperclass
public class BaseAtlas implements IAtlas {

    @Id
    @SequenceGenerator(name = "atlases_pk_sequence", sequenceName = "atlases_data_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "atlases_pk_sequence")
    private Long id;

    @Column
    private String name;

    @ManyToOne
    private DataNode dataNode;

    @Column
    private String version;

    @ManyToMany(targetEntity = Tenant.class, fetch = FetchType.LAZY)
    @JoinTable(name = "tenants_atlases",
            joinColumns = @JoinColumn(name = "atlas_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "tenant_id", referencedColumnName = "id"))
    protected Set<Tenant> tenants = new HashSet<>();

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseAtlas atlas = (BaseAtlas) o;

        return getId() != null ? getId().equals(atlas.getId()) : atlas.getId() == null;
    }

    @Override
    public int hashCode() {

        return getId() != null ? getId().hashCode() : 0;
    }

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

    public DataNode getDataNode() {

        return dataNode;
    }

    public void setDataNode(DataNode dataNode) {

        this.dataNode = dataNode;
    }

    public String getVersion() {

        return version;
    }

    public void setVersion(String version) {

        this.version = version;
    }

    public Set<Tenant> getTenants() {

        return tenants;
    }

    public void setTenants(Set<Tenant> tenants) {

        this.tenants = tenants;
    }
}
