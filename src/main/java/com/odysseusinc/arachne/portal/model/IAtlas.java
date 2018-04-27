package com.odysseusinc.arachne.portal.model;

import com.odysseusinc.arachne.portal.model.security.Tenant;
import java.util.Set;

public interface IAtlas {

    Long getId();

    void setId(Long id);

    String getName();

    void setName(String name);

    DataNode getDataNode();

    void setDataNode(DataNode dataNode);

    String getVersion();

    void setVersion(String version);

    Set<Tenant> getTenants();

    void setTenants(Set<Tenant> tenants);
}
