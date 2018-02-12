package com.odysseusinc.arachne.portal.service;

import com.odysseusinc.arachne.portal.model.security.Tenant;
import java.util.List;

public interface TenantService {

    List<Tenant> getDefault();
}
