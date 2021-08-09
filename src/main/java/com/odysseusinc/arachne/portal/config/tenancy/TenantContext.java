package com.odysseusinc.arachne.portal.config.tenancy;

import com.odysseusinc.arachne.portal.model.security.ArachneUser;
import java.util.Optional;
import org.springframework.security.core.context.SecurityContextHolder;

public class TenantContext {

    public static Long getCurrentTenant() {
        return Optional.ofNullable(
                SecurityContextHolder.getContext().getAuthentication()
        ).map(authentication -> {
            Object principal = authentication.getPrincipal();
            return  (principal instanceof ArachneUser) ? ((ArachneUser) principal).getActiveTenantId() : null;
        }).orElse(null);
    }

    /**
     * @deprecated Does nothing. Tenant is now provided from user information stored in SecurityContext.
     * Used only in tests and should be removed there.
     */
    public static void setCurrentTenant(Long currentTenant) {
    }
}
