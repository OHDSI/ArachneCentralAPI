package com.odysseusinc.arachne.portal.service;

import com.odysseusinc.arachne.portal.model.security.Tenant;
import java.util.Set;

public interface BaseTenantService<T extends Tenant> {

    Set<T> getDefault();
}
