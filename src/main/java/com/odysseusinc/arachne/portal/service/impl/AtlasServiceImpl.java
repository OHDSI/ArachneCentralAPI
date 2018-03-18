package com.odysseusinc.arachne.portal.service.impl;

import com.odysseusinc.arachne.portal.model.IAtlas;
import com.odysseusinc.arachne.portal.repository.BaseAtlasRepository;
import com.odysseusinc.arachne.portal.repository.BaseRawAtlasRepository;
import com.odysseusinc.arachne.portal.service.AtlasService;
import com.odysseusinc.arachne.portal.service.TenantService;
import org.springframework.stereotype.Service;

@Service
public class AtlasServiceImpl extends BaseAtlasServiceImpl<IAtlas> implements AtlasService {

    public AtlasServiceImpl(BaseAtlasRepository atlasRepository, BaseRawAtlasRepository baseRawAtlasRepository, TenantService tenantService) {

        super(atlasRepository, baseRawAtlasRepository, tenantService);
    }
}
