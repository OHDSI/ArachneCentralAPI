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
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva
 * Created: February 15, 2018
 *
 */

package com.odysseusinc.arachne.portal.model;

import com.odysseusinc.arachne.commons.utils.UserIdUtils;
import com.odysseusinc.arachne.portal.api.v1.dto.converters.UserSolrExtractors;
import com.odysseusinc.arachne.portal.model.security.Tenant;
import com.odysseusinc.arachne.portal.model.solr.SolrCollection;
import com.odysseusinc.arachne.portal.model.solr.SolrFieldAnno;
import com.odysseusinc.arachne.portal.service.BaseSolrService;
import com.odysseusinc.arachne.portal.service.impl.breadcrumb.Breadcrumb;
import com.odysseusinc.arachne.portal.service.impl.breadcrumb.BreadcrumbType;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;

@MappedSuperclass
@SolrFieldAnno(name = BaseSolrService.TITLE, postfix = false, extractor = UserSolrExtractors.TitleExtractor.class)
public class BaseUser implements IUser, Serializable {

    @ManyToMany(targetEntity = Role.class, fetch = FetchType.LAZY)
    @JoinTable(name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    protected List<Role> roles;

    @Id
    @SequenceGenerator(name = "users_pk_sequence", sequenceName = "users_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_pk_sequence")
    protected Long id;

    @Column(nullable = false)
    protected String password;

    @Column(nullable = false)
    protected String email;

    @SolrFieldAnno(query = true)
    @Column(nullable = false, length = 100)
    protected String firstname;

    @SolrFieldAnno(query = true)
    @Column(nullable = false, length = 100)
    protected String middlename;

    @SolrFieldAnno(query = true)
    @Column(nullable = false, length = 100)
    protected String lastname;

    @Column(nullable = false, length = 100)
    protected String organization;

    @Column
    protected Boolean enabled;

    @Column(name = "email_confirmed", nullable = false)
    protected Boolean emailConfirmed;

    @Column
    protected Date created;

    @Column
    protected Date updated;

    @Column
    protected String registrationCode;

    @Column(name = "last_password_reset")
    protected Date lastPasswordReset;

    @ManyToMany(targetEntity = Skill.class, fetch = FetchType.LAZY)
    @JoinTable(name = "users_skills",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id", referencedColumnName = "id"))
    protected Set<Skill> skills;

    @SolrFieldAnno(query = true)
    @OneToMany(targetEntity = UserLink.class, mappedBy = "user")
    protected List<UserLink> links;

    @SolrFieldAnno(query = true)
    @OneToMany(targetEntity = UserPublication.class, mappedBy = "user")
    protected List<UserPublication> publications;

    @SolrFieldAnno(query = true, filter = true)
    @ManyToOne(targetEntity = ProfessionalType.class, optional = false)
    protected ProfessionalType professionalType;

    @SolrFieldAnno(query = true)
    @Column(nullable = true)
    protected String affiliation;

    @SolrFieldAnno(query = true)
    @Column(nullable = true)
    protected String personalSummary;

    @SolrFieldAnno(query = true)
    @Column(nullable = true, name = "contact_info")
    protected String contactEmail;

    @SolrFieldAnno(query = true)
    @Column(nullable = true)
    protected String phone;

    @SolrFieldAnno(query = true)
    @Column(nullable = true)
    protected String mobile;

    @SolrFieldAnno(query = true)
    @Column(nullable = true)
    protected String address1;

    @SolrFieldAnno(query = true)
    @Column(nullable = true)
    protected String address2;

    @SolrFieldAnno(query = true)
    @Column(nullable = true)
    protected String city;

    @SolrFieldAnno(query = true, filter = true)
    @ManyToOne(targetEntity = StateProvince.class)
    protected StateProvince stateProvince;

    @SolrFieldAnno(query = true)
    @Column(nullable = true)
    protected String zipCode;

    @SolrFieldAnno(query = true, filter = true)
    @ManyToOne(targetEntity = Country.class)
    protected Country country;

    @Column
    protected String origin;

    @Column
    protected String username;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    protected Set<DataNodeUser> dataNodeUsers = new HashSet<>();

    @ManyToMany(targetEntity = Tenant.class, fetch = FetchType.LAZY)
    @JoinTable(name = "tenant_dependent_users_view",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "tenant_id", referencedColumnName = "id"))
    @SolrFieldAnno(filter = true, postfix = false, sort = false, extractor = UserSolrExtractors.TenantsExtractor.class)
    protected Set<Tenant> tenants;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "active_tenant_id")
    protected Tenant activeTenant;

    @Override
    public SolrCollection getCollection() {

        return SolrCollection.USERS;
    }

    @Override
    public BreadcrumbType getCrumbType() {

        return BreadcrumbType.USER;
    }

    @Override
    public Long getCrumbId() {

        return getId();
    }

    @Override
    public String getCrumbTitle() {

        return getFirstname() + " " + getMiddlename() + " " + getLastname();
    }

    @Override
    public Breadcrumb getCrumbParent() {

        return null;
    }

    @Override
    public int hashCode() {

        return Objects.hashCode(this.getId());
    }

    @Override
    public boolean equals(final Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof BaseUser)) {
            return false;
        }
        final BaseUser s = (BaseUser) obj;
        return Objects.equals(getId(), s.getId());
    }

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public String getPassword() {

        return password;
    }

    public void setPassword(String password) {

        this.password = password;
    }

    public String getEmail() {

        return email;
    }

    public void setEmail(String email) {

        this.email = email;
    }

    public String getFirstname() {

        return firstname;
    }

    public void setFirstname(String firstname) {

        this.firstname = firstname;
    }

    public String getMiddlename() {

        return middlename;
    }

    public void setMiddlename(String middlename) {

        this.middlename = middlename;
    }

    public String getLastname() {

        return lastname;
    }

    public void setLastname(String lastname) {

        this.lastname = lastname;
    }

    @Override
    public String getOrganization() {

        return organization;
    }

    @Override
    public void setOrganization(String organization) {

        this.organization = organization;
    }

    public ProfessionalType getProfessionalType() {

        return professionalType;
    }

    public void setProfessionalType(ProfessionalType professionalType) {

        this.professionalType = professionalType;
    }

    public String getAffiliation() {

        return affiliation;
    }

    public void setAffiliation(String affiliation) {

        this.affiliation = affiliation;
    }

    public String getPersonalSummary() {

        return personalSummary;
    }

    public void setPersonalSummary(String personalSummary) {

        this.personalSummary = personalSummary;
    }


    public String getContactEmail() {

        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {

        this.contactEmail = contactEmail;
    }


    public Boolean getEnabled() {

        return enabled;
    }

    public void setEnabled(Boolean enabled) {

        this.enabled = enabled;
    }

    public Boolean getEmailConfirmed() {

        return emailConfirmed;
    }

    public void setEmailConfirmed(Boolean emailConfirmed) {

        this.emailConfirmed = emailConfirmed;
    }

    public List<Role> getRoles() {

        return roles;
    }

    public void setRoles(List<Role> roles) {

        this.roles = roles;
    }

    public Date getLastPasswordReset() {

        return lastPasswordReset;
    }

    public void setLastPasswordReset(Date lastPasswordReset) {

        this.lastPasswordReset = lastPasswordReset;
    }

    public Date getCreated() {

        return created;
    }

    public void setCreated(Date created) {

        this.created = created;
    }

    public Date getUpdated() {

        return updated;
    }

    public void setUpdated(Date updated) {

        this.updated = updated;
    }

    public String getRegistrationCode() {

        return registrationCode;
    }

    public void setRegistrationCode(String registrationCode) {

        this.registrationCode = registrationCode;
    }

    public Set<Skill> getSkills() {

        return skills;
    }

    public void setSkills(Set<Skill> skills) {

        this.skills = skills;
    }


    public String getUuid() {

        return UserIdUtils.idToUuid(getId());
    }

    public List<UserLink> getLinks() {

        return links;
    }

    public void setLinks(List<UserLink> links) {

        this.links = links;
    }

    public List<UserPublication> getPublications() {

        return publications;
    }

    public void setPublications(List<UserPublication> publications) {

        this.publications = publications;
    }

    public String getPhone() {

        return phone;
    }

    public void setPhone(String phone) {

        this.phone = phone;
    }

    public String getMobile() {

        return mobile;
    }

    public void setMobile(String mobile) {

        this.mobile = mobile;
    }

    public String getAddress1() {

        return address1;
    }

    public void setAddress1(String address1) {

        this.address1 = address1;
    }

    public String getAddress2() {

        return address2;
    }

    public void setAddress2(String address2) {

        this.address2 = address2;
    }

    public String getCity() {

        return city;
    }

    public void setCity(String city) {

        this.city = city;
    }

    public StateProvince getStateProvince() {

        return stateProvince;
    }

    public void setStateProvince(StateProvince stateProvince) {

        this.stateProvince = stateProvince;
    }

    public String getZipCode() {

        return zipCode;
    }

    public void setZipCode(String zipCode) {

        this.zipCode = zipCode;
    }

    public Country getCountry() {

        return country;
    }

    public void setCountry(Country country) {

        this.country = country;
    }

    public String getOrigin() {

        return origin;
    }

    public void setOrigin(String origin) {

        this.origin = origin;
    }

    public String getUsername() {

        return username;
    }

    public void setUsername(String username) {

        this.username = username;
    }

    public Set<DataNodeUser> getDataNodeUsers() {

        return dataNodeUsers;
    }

    public void setDataNodeUsers(Set<DataNodeUser> dataNodeUsers) {

        this.dataNodeUsers = dataNodeUsers;
    }

    public String getFullName() {

        return firstname + " " + (middlename != null ? middlename : "") + " " + lastname;
    }

    public Set<Tenant> getTenants() {

        return tenants;
    }

    public void setTenants(Set<Tenant> tenants) {

        this.tenants = tenants;
    }

    public Tenant getActiveTenant() {

        return activeTenant;
    }

    public void setActiveTenant(Tenant activeTenant) {

        this.activeTenant = activeTenant;
    }
}
