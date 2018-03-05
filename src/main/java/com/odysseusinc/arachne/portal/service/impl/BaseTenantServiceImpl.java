package com.odysseusinc.arachne.portal.service.impl;

import com.odysseusinc.arachne.portal.model.security.Tenant;
import com.odysseusinc.arachne.portal.repository.BaseTenantRepository;
import com.odysseusinc.arachne.portal.service.BaseTenantService;
import java.util.Set;

public abstract class BaseTenantServiceImpl<T extends Tenant> implements BaseTenantService<T> {

    protected BaseTenantRepository<T> tenantRepository;

    public BaseTenantServiceImpl(BaseTenantRepository<T> tenantRepository) {

        this.tenantRepository = tenantRepository;
    }

    public Set<T> getDefault() {

        return tenantRepository.findAllByIsDefaultTrue();
    }
}
