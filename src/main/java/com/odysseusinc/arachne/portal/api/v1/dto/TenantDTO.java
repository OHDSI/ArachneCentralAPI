package com.odysseusinc.arachne.portal.api.v1.dto;

public class TenantDTO {

    private Long id;
    private String name;
    private Boolean isActive;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public Boolean getActive() {

        return isActive;
    }

    public void setActive(Boolean active) {

        isActive = active;
    }
}
