package com.odysseusinc.arachne.portal.config.tenancy;

import org.apache.commons.lang3.ObjectUtils;

public class TenantContext {

    private static ThreadLocal<Long> currentUserId = new ThreadLocal<>();
    private static ThreadLocal<Long> currentTenant = new ThreadLocal<>();

    public static Long getCurrentUserId() {

        return ObjectUtils.firstNonNull(currentUserId.get(), -1L);
    }

    public static void setCurrentUserId(Long currentUserId) {

        TenantContext.currentUserId.set(currentUserId);
    }

    public static Long getCurrentTenant() {

        return currentTenant.get();
    }

    public static void setCurrentTenant(Long currentTenant) {

        TenantContext.currentTenant.set(currentTenant);
    }
}
