package com.odysseusinc.arachne.portal.service.domain;

import com.odysseusinc.arachne.portal.model.DataSource;
import javax.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.support.Repositories;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

@Service
public class DomainObjectLoaderFactory {

    private Repositories repositories;
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    public DomainObjectLoaderFactory(WebApplicationContext appContext, EntityManagerFactory entityManagerFactory) {

        this.repositories = new Repositories(appContext);
        this.entityManagerFactory = entityManagerFactory;
    }

    public DomainObjectLoader getDomainObjectLoader(Class domainClazz) {

        DomainObjectLoader domainObjectLoader;
        if (DataSource.class.isAssignableFrom(domainClazz))
            domainObjectLoader = new DsDomainObjectLoader(domainClazz);
        else
            domainObjectLoader = new GenericDomainObjectLoader(domainClazz);

        return domainObjectLoader.withServices(repositories, entityManagerFactory);
    }
}
