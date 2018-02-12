package com.odysseusinc.arachne.portal.service.impl;

import com.odysseusinc.arachne.portal.model.security.Tenant;
import com.odysseusinc.arachne.portal.repository.TenantRepository;
import com.odysseusinc.arachne.portal.service.TenantService;
import java.util.List;

public abstract class BaseTenantServiceImpl implements TenantService {

    private TenantRepository<Tenant> tenantRepository;

    public BaseTenantServiceImpl(TenantRepository<Tenant> tenantRepository) {

        this.tenantRepository = tenantRepository;
    }

    public List<Tenant> getDefault() {

        return tenantRepository.findAllByIsDefaultTrue();
    }
}
