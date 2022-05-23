package com.odysseusinc.arachne.portal.config.tenancy;

import com.odysseusinc.arachne.portal.model.HasTenant;
import com.odysseusinc.arachne.portal.model.security.HasTenants;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.context.SecurityContextHolder;

public class TenantContext {

    /**
     *
     * @deprecated To me removed. Still used in some Solr classes, but these are not intended for multi-tenant sessions.
     * Too afraid to rewrite them how
     */
    public static Long getCurrentTenant() {
        List<Long> tenants = getActiveTenants();
        if (tenants.size() > 1) {
            throw new AssertionError("User sessions with multiple tenants are currently not supported. Active tenants: " + tenants.size());
        } else {
            return tenants.stream().findFirst().orElse(null);
        }
    }

    public static List<Long> getActiveTenants() {
        return ifHasTenants().map(HasTenants::getActiveTenantIds).orElseGet(Collections::emptyList);
    }

    public static void verify(HasTenant subject) {
        Long tenantId = subject.getTenantId();
        ifHasTenants().ifPresent(ht -> {
            // Ideally, if current user doesn't implment HasTenant, we should fail here.
            // However, for the moment there are still some operations (Lucene indexing) that requires entities from all tenants.
            // These cases should be reviewed and addressed individually.
            List<Long> tenants = ht.getActiveTenantIds();
            if (!tenants.contains(tenantId)) {
                throw new SecurityException(
                        "Attempted to access [" + ((Object) subject).getClass() + "] in tenant [" + tenantId + "], but only tenants " + tenants + " are available"
                );
            }
        });
    }

    private static Optional<HasTenants> ifHasTenants() {
        return Optional.ofNullable(
                SecurityContextHolder.getContext().getAuthentication()
        ).map(authentication -> {
            // TODO Checking for tenants in 2 places isn't nice, but with DataNodeAuthenticationToken principal is
            //  an entity, so amending transient fields not initialized from DB there would be an abstraction failure
            if (authentication instanceof HasTenants) {
                return ((HasTenants) authentication);
            } else {
                Object principal = authentication.getPrincipal();
                return principal instanceof HasTenants ? ((HasTenants) principal) : null;
            }
        });
    }

    /**
     * @deprecated Does nothing. Tenant is now provided from user information stored in SecurityContext.
     * Used only in tests and should be removed there.
     */
    public static void setCurrentTenant(Long currentTenant) {
    }

}
