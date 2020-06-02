package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonCountryDTO;
import com.odysseusinc.arachne.portal.model.Country;
import org.springframework.stereotype.Component;

@Component
public class CommonCountryDTOToCountryConverter extends BaseConversionServiceAwareConverter<CommonCountryDTO, Country> {

    @Override
    public Country convert(CommonCountryDTO dto) {

        Country country = new Country();
        country.setName(dto.getName());
        country.setIsoCode(dto.getIsoCode());
        return country;
    }
}
