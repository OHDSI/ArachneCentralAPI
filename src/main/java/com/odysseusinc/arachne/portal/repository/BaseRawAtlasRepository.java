package com.odysseusinc.arachne.portal.repository;

import com.odysseusinc.arachne.portal.model.IAtlas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseRawAtlasRepository<A extends IAtlas> extends JpaRepository<A, Long> {
}
