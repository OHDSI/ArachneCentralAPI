package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.commons.api.v1.dto.AtlasShortDTO;
import com.odysseusinc.arachne.portal.model.IAtlas;
import org.springframework.stereotype.Component;

@Component
public class AtlasToAtlasShortDTOConverter extends BaseConversionServiceAwareConverter<IAtlas, AtlasShortDTO> {

    @Override
    public AtlasShortDTO convert(IAtlas source) {

        AtlasShortDTO result = new AtlasShortDTO();

        result.setCentralId(source.getId());
        result.setName(source.getName());
        result.setVersion(source.getVersion());

        return result;
    }
}
