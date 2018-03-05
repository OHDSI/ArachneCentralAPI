package com.odysseusinc.arachne.portal.service.impl;

import com.odysseusinc.arachne.portal.model.security.Tenant;
import com.odysseusinc.arachne.portal.repository.BaseTenantRepository;
import com.odysseusinc.arachne.portal.service.TenantService;
import java.util.Set;

public abstract class BaseTenantServiceImpl implements TenantService {

    protected BaseTenantRepository<Tenant> tenantRepository;

    public BaseTenantServiceImpl(BaseTenantRepository<Tenant> tenantRepository) {

        this.tenantRepository = tenantRepository;
    }

    public Set<Tenant> getDefault() {

        return tenantRepository.findAllByIsDefaultTrue();
    }
}
