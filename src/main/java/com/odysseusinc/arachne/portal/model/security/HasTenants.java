package com.odysseusinc.arachne.portal.model.security;

import java.util.List;

public interface HasTenants {
    List<Long> getActiveTenantIds();
}
