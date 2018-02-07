package com.odysseusinc.arachne.portal.config.tenancy;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver {

    private static Long DEFAULT_TENANT_ID = -1L;

    @Override
    public String resolveCurrentTenantIdentifier() {

        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId != null) {
            return tenantId.toString();
        }
        return DEFAULT_TENANT_ID.toString();
    }

    @Override
    public boolean validateExistingCurrentSessions() {

        return true;
    }
}
