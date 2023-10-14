package com.odysseusinc.arachne.portal.config.tenancy;

import java.util.List;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver {

    private static Long DEFAULT_TENANT_ID = -1L;

    @Override
    public String resolveCurrentTenantIdentifier() {
        List<Long> tenants = TenantContext.getActiveTenants();
        // When org.hibernate.MultiTenancyStrategy.SCHEMA is active, Hibernate always calls this method via
        // CurrentTenantIdentifierResolver interface. However for system-to-system integration we effectively
        // ignore Hibernate tenant support and manage tenants via HasTenantEventListener.
        // Since a normal user can only have a single active tenant, we fall back to default tenant if multiple
        // active tenants are detected, assumung S2S.
        // When all tenant management is switched away from views to events, this class is to be deleted completely
        return String.valueOf(tenants.stream().reduce((a, b) -> DEFAULT_TENANT_ID).orElse(DEFAULT_TENANT_ID));
    }

    @Override
    public boolean validateExistingCurrentSessions() {

        return true;
    }
}
