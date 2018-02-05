package com.odysseusinc.arachne.portal.model.security;

import com.odysseusinc.arachne.portal.model.DataSource;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.User;
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

@Entity
@Table(name = "security_groups")
public class SecurityGroup {

    @Id
    @SequenceGenerator(name = "security_groups_pk_sequence", sequenceName = "security_groups_id_seq", allocationSize = 1)
    private Long id;

    @Column
    private String name;

    @ManyToMany(targetEntity = User.class, fetch = FetchType.LAZY)
    @JoinTable(name = "security_groups_users",
            joinColumns = @JoinColumn(name = "security_group_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
    private Set<User> users;

    @ManyToMany(targetEntity = DataSource.class, fetch = FetchType.LAZY)
    @JoinTable(name = "security_groups_data_sources",
            joinColumns = @JoinColumn(name = "security_group_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "data_source_id", referencedColumnName = "id"))
    private Set<DataSource> dataSources;

    @OneToMany(targetEntity = Study.class, mappedBy = "securityGroup")
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
}
