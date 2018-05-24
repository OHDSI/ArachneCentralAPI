package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.portal.api.v1.dto.TenantBaseDTO;
import com.odysseusinc.arachne.portal.model.security.Tenant;
import org.springframework.stereotype.Component;

/**
 * @author SMaletsky
 */
@Component
public class TenantBaseDTOToTenantConverter extends BaseConversionServiceAwareConverter<TenantBaseDTO, Tenant> {

    @Override
    public Tenant convert(TenantBaseDTO source) {

        Tenant tenant = new Tenant();
        tenant.setId(source.getId());
        tenant.setName(source.getName());
        tenant.setDefault(source.getDefault());
        return tenant;
    }
}
