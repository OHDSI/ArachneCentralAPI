package com.odysseusinc.arachne.portal.service.impl;

import com.odysseusinc.arachne.portal.model.security.Tenant;
import com.odysseusinc.arachne.portal.repository.TenantRepository;
import com.odysseusinc.arachne.portal.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TenantServiceImpl extends BaseTenantServiceImpl implements TenantService {

    @Autowired
    public TenantServiceImpl(TenantRepository<Tenant> tenantRepository) {

        super(tenantRepository);
    }
}
