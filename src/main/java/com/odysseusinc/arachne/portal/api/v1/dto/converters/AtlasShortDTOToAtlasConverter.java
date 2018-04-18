package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.commons.api.v1.dto.AtlasShortDTO;
import com.odysseusinc.arachne.portal.model.Atlas;
import com.odysseusinc.arachne.portal.model.BaseAtlas;
import com.odysseusinc.arachne.portal.model.IAtlas;
import org.springframework.stereotype.Component;

@Component
public class AtlasShortDTOToAtlasConverter extends BaseConversionServiceAwareConverter<AtlasShortDTO, IAtlas> {

    @Override
    public BaseAtlas convert(AtlasShortDTO source) {

        Atlas result = new Atlas();

        result.setId(source.getCentralId());
        result.setName(source.getName());
        result.setVersion(source.getVersion());

        return result;
    }
}
