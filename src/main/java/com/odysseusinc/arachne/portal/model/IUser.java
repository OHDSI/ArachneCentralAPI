package com.odysseusinc.arachne.portal.model;


import com.odysseusinc.arachne.portal.model.security.Tenant;
import com.odysseusinc.arachne.portal.model.solr.SolrEntity;
import com.odysseusinc.arachne.portal.service.impl.breadcrumb.Breadcrumb;
import java.util.Date;
import java.util.List;
import java.util.Set;

public interface IUser extends Breadcrumb, SolrEntity {

    int hashCode();

    boolean equals(final Object obj);

    Long getId();

    void setId(Long id);

    String getPassword();

    void setPassword(String password);

    String getEmail();

    void setEmail(String email);

    String getFirstname();

    void setFirstname(String firstname);

    String getMiddlename();

    void setMiddlename(String middlename);

    String getLastname();

    void setLastname(String lastname);

    String getOrganization();

    void setOrganization(String organization);

    ProfessionalType getProfessionalType();

    void setProfessionalType(ProfessionalType professionalType);

    String getAffiliation();

    void setAffiliation(String affiliation);

    String getPersonalSummary();

    void setPersonalSummary(String personalSummary);


    String getContactEmail();

    void setContactEmail(String contactEmail);


    Boolean getEnabled();

    void setEnabled(Boolean enabled);

    Boolean getEmailConfirmed();

    void setEmailConfirmed(Boolean emailConfirmed);

    List<Role> getRoles();

    void setRoles(List<Role> roles);

    Date getLastPasswordReset();

    void setLastPasswordReset(Date lastPasswordReset);

    Date getCreated();

    void setCreated(Date created);

    Date getUpdated();

    void setUpdated(Date updated);

    String getRegistrationCode();

    void setRegistrationCode(String registrationCode);

    Set<Skill> getSkills();

    void setSkills(Set<Skill> skills);


    String getUuid();

    List<UserLink> getLinks();

    void setLinks(List<UserLink> links);

    List<UserPublication> getPublications();

    void setPublications(List<UserPublication> publications);

    String getPhone();

    void setPhone(String phone);

    String getMobile();

    void setMobile(String mobile);

    String getAddress1();

    void setAddress1(String address1);

    String getAddress2();

    void setAddress2(String address2);

    String getCity();

    void setCity(String city);

    StateProvince getStateProvince();

    void setStateProvince(StateProvince stateProvince);

    String getZipCode();

    void setZipCode(String zipCode);

    Country getCountry();

    void setCountry(Country country);

    String getOrigin();

    void setOrigin(String origin);

    String getUsername();

    void setUsername(String username);

    Set<DataNodeUser> getDataNodeUsers();

    void setDataNodeUsers(Set<DataNodeUser> dataNodeUsers);

    String getFullName();

    Set<Tenant> getTenants();

    void setTenants(Set<Tenant> tenants);

    Tenant getActiveTenant();

    void setActiveTenant(Tenant activeTenant);
}
