package com.odysseusinc.arachne.portal.api.v1.dto;

public class TenantPersonalDTO extends TenantBaseDTO {

    private Boolean isActive;

    public Boolean getActive() {

        return isActive;
    }

    public void setActive(Boolean active) {

        isActive = active;
    }
}
