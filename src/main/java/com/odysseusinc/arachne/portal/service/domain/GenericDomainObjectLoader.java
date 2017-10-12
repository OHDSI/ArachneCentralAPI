package com.odysseusinc.arachne.portal.service.domain;

import java.io.Serializable;

public class GenericDomainObjectLoader extends DomainObjectLoader {

    public GenericDomainObjectLoader(Class domainClazz) {

        super(domainClazz);
    }

    @Override
    protected Serializable getTargetId(Object domainObject) {

        return (Long) entityManagerFactory.getPersistenceUnitUtil().getIdentifier(domainObject);
    }

    @Override
    public Object loadDomainObject() {

        return getRepository().findOne(targetId);
    }
}
