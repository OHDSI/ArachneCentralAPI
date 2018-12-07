package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonStateProvinceDTO;
import com.odysseusinc.arachne.portal.model.StateProvince;
import org.springframework.stereotype.Component;

@Component
public class CommonStateProvinceDTOToStateProvinceConverter extends BaseConversionServiceAwareConverter<CommonStateProvinceDTO, StateProvince> {

	@Override
	public StateProvince convert(CommonStateProvinceDTO dto) {

		StateProvince stateProvince = new StateProvince();
		stateProvince.setName(dto.getName());
		stateProvince.setIsoCode(dto.getIsoCode());
		return stateProvince;
	}
}
