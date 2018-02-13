package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.portal.api.v1.dto.TenantDTO;
import com.odysseusinc.arachne.portal.model.security.Tenant;
import org.springframework.stereotype.Component;

@Component
public class TenantToTenantDTO extends BaseConversionServiceAwareConverter<Tenant, TenantDTO> {


    @Override
    public TenantDTO convert(Tenant source) {

        TenantDTO tenantDTO = new TenantDTO();
        tenantDTO.setId(source.getId());
        tenantDTO.setName(source.getName());
        return tenantDTO;
    }
}
