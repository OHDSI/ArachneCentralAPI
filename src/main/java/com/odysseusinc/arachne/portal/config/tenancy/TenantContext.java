package com.odysseusinc.arachne.portal.config.tenancy;

import com.odysseusinc.arachne.portal.model.security.HasTenant;
import java.util.Optional;
import org.springframework.security.core.context.SecurityContextHolder;

public class TenantContext {

    public static Long getCurrentTenant() {
        return Optional.ofNullable(
                SecurityContextHolder.getContext().getAuthentication()
        ).map(authentication -> {
            // TODO Checking for tenants in 2 places isn't nice, but with DataNodeAuthenticationToken principal is
            //  an entity, so amending transient fields not initialized from DB there would be an abstraction failure
            if (authentication instanceof HasTenant) {
                return ((HasTenant) authentication).getActiveTenantId();
            } else {
                Object principal = authentication.getPrincipal();
                return (principal instanceof HasTenant) ? ((HasTenant) principal).getActiveTenantId() : null;
            }
        }).orElse(null);
    }

    /**
     * @deprecated Does nothing. Tenant is now provided from user information stored in SecurityContext.
     * Used only in tests and should be removed there.
     */
    public static void setCurrentTenant(Long currentTenant) {
    }
}
