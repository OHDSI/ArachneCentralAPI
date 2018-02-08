package com.odysseusinc.arachne.portal.model.security;

import com.odysseusinc.arachne.portal.model.DataSource;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.model.solr.SolrValue;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "tenants")
public class Tenant implements SolrValue {

    @Id
    @SequenceGenerator(name = "tenants_pk_sequence", sequenceName = "tenants_id_seq", allocationSize = 1)
    private Long id;

    @Column
    private String name;

    @ManyToMany(targetEntity = User.class, fetch = FetchType.LAZY)
    @JoinTable(name = "tenants_users",
            joinColumns = @JoinColumn(name = "tenant_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
    private Set<User> users;

    @ManyToMany(targetEntity = DataSource.class, fetch = FetchType.LAZY)
    @JoinTable(name = "tenants_data_sources",
            joinColumns = @JoinColumn(name = "tenant_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "data_source_id", referencedColumnName = "id"))
    private Set<DataSource> dataSources;

    @OneToMany(targetEntity = Study.class, mappedBy = "tenant")
    private Set<Study> studies;

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

    public Set<User> getUsers() {

        return users;
    }

    public void setUsers(Set<User> users) {

        this.users = users;
    }

    public Set<DataSource> getDataSources() {

        return dataSources;
    }

    public void setDataSources(Set<DataSource> dataSources) {

        this.dataSources = dataSources;
    }

    public Set<Study> getStudies() {

        return studies;
    }

    public void setStudies(Set<Study> studies) {

        this.studies = studies;
    }

    @Transient
    @Override
    public Object getSolrValue() {

        return id;
    }

    @Transient
    @Override
    public Object getSolrQueryValue() {

        return id;
    }

    @Override
    public boolean equals(final Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof Tenant)) {
            return false;
        }
        final Tenant s = (Tenant) obj;
        return java.util.Objects.equals(id, s.id);
    }
}
