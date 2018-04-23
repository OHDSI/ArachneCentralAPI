package com.odysseusinc.arachne.portal.service.impl;

import com.odysseusinc.arachne.portal.model.IAtlas;
import com.odysseusinc.arachne.portal.repository.BaseAtlasRepository;
import com.odysseusinc.arachne.portal.repository.BaseRawAtlasRepository;
import com.odysseusinc.arachne.portal.service.BaseAtlasService;
import com.odysseusinc.arachne.portal.service.TenantService;
import java.util.List;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

public class BaseAtlasServiceImpl<T extends IAtlas> implements BaseAtlasService<T> {

    private final BaseAtlasRepository<T> baseAtlasRepository;
    private final BaseRawAtlasRepository<T> baseRawAtlasRepository;
    private final TenantService tenantService;

    public BaseAtlasServiceImpl(BaseAtlasRepository<T> baseAtlasRepository, BaseRawAtlasRepository<T> baseRawAtlasRepository, TenantService tenantService) {

        this.baseAtlasRepository = baseAtlasRepository;
        this.baseRawAtlasRepository = baseRawAtlasRepository;
        this.tenantService = tenantService;
    }

    @Override
    public List<T> findAll() {

        return baseAtlasRepository.findAll();
    }

    @Override
    public T register(T atlas) {

        atlas.setTenants(tenantService.getDefault());
        return baseAtlasRepository.save(atlas);
    }

    /**
     * Searches for Atlas independently of current user's tenant and independently of the Atlas'es relations with any Tenant.
     *
     * @param id ID of Atlas to search for.
     * @return Atlas instance.
     */
    @Override
    @PostAuthorize("returnObject.dataNode == authentication.principal || hasRole('ROLE_ADMIN')")
    public T findByIdInAnyTenant(Long id) {

        return baseRawAtlasRepository.findOne(id);
    }

    @Override
    @PostAuthorize("returnObject == null || returnObject.dataNode == authentication.principal || hasRole('ROLE_ADMIN')")
    public T findByNameAndNodeInAnyTenant(String name, Long dataNodeId) {

        return baseRawAtlasRepository.findByNameAndDataNodeId(name, dataNodeId);
    }

    @Override
    @PreAuthorize("@rawAtlasRepository.findOne(#id)?.dataNode == authentication.principal")
    public T update(Long id, T atlas) {

        T existing = findByIdInAnyTenant(id);

        if (atlas.getName() != null) {
            existing.setName(atlas.getName());
        }

        if (atlas.getVersion() != null) {
            existing.setVersion(atlas.getVersion());
        }

        return baseRawAtlasRepository.save(existing);
    }

    @Override
    @Secured({"ROLE_ADMIN"})
    public T updateUnsafeInAnyTenant(T atlas) {

        return baseRawAtlasRepository.save(atlas);
    }

    @Override
    @PreAuthorize("@rawAtlasRepository.findOne(#id)?.dataNode == authentication.principal")
    public void delete(Long id) {

        baseRawAtlasRepository.delete(id);
    }

}
