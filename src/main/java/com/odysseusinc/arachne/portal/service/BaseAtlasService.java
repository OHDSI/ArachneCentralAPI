package com.odysseusinc.arachne.portal.service;

import com.odysseusinc.arachne.portal.model.IAtlas;
import java.util.List;

public interface BaseAtlasService<T extends IAtlas> {

    List<T> findAll();

    T register(T atlas);

    T update(Long id, T atlas);

    T updateUnsafeInAnyTenant(T atlas);

    void delete(Long id);

    T findByIdInAnyTenant(Long id);
}
