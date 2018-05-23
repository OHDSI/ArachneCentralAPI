package com.odysseusinc.arachne.portal.api.v1.dto;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonUserRegistrationDTO;
import java.util.List;

/**
 * @author SMaletsky
 */
public class BulkUsersRegistrationDTO {
    private boolean emailConfirmationRequired;
    private List<TenantBaseDTO> tenantDtos;
    private List<CommonUserRegistrationDTO> userDtos;

    public boolean getEmailConfirmationRequired() {

        return emailConfirmationRequired;
    }

    public void setEmailConfirmationRequired(boolean emailConfirmationRequired) {

        this.emailConfirmationRequired = emailConfirmationRequired;
    }

    public List<TenantBaseDTO> getTenantDtos() {

        return tenantDtos;
    }

    public void setTenantDtos(List<TenantBaseDTO> tenantDtos) {

        this.tenantDtos = tenantDtos;
    }

    public List<CommonUserRegistrationDTO> getUserDtos() {

        return userDtos;
    }

    public void setUserDtos(List<CommonUserRegistrationDTO> userDtos) {

        this.userDtos = userDtos;
    }
}
