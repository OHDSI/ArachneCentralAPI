package com.odysseusinc.arachne.portal.service.domain;

import java.io.Serializable;
import javax.persistence.EntityManagerFactory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.support.Repositories;

public abstract class DomainObjectLoader {

    protected Repositories repositories;
    protected EntityManagerFactory entityManagerFactory;

    protected Class domainClazz;
    protected Serializable targetId;

    public DomainObjectLoader(Class domainClazz) {

        this.domainClazz = domainClazz;
    }

    public DomainObjectLoader withServices(
            Repositories repositories,
            EntityManagerFactory entityManagerFactory
    ) {

        this.repositories = repositories;
        this.entityManagerFactory = entityManagerFactory;
        return this;
    }

    public DomainObjectLoader withTargetId(Serializable targetId) {

        this.targetId = targetId;
        return this;
    }

    public DomainObjectLoader withTargetId(Object domainObject) {

        this.targetId = getTargetId(domainObject);
        return this;
    }

    protected CrudRepository getRepository() {

        return (CrudRepository) repositories.getRepositoryFor(domainClazz);
    }

    protected Serializable getTargetId(Object domainObject) {

        return null;
    }

    public Object loadDomainObject() {

        return null;
    }
}
