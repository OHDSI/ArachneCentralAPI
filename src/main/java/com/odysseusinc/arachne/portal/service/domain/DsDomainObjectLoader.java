package com.odysseusinc.arachne.portal.service.domain;

import com.odysseusinc.arachne.portal.model.DataSource;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import org.springframework.data.repository.CrudRepository;

public class DsDomainObjectLoader extends DomainObjectLoader {

    public DsDomainObjectLoader(Class domainClazz) {

        super(domainClazz);
    }

    @Override
    protected Serializable getTargetId(Object domainObject) {

        return ((DataSource) domainObject).getUuid();
    }

    @Override
    public Object loadDomainObject() {

        try {

            CrudRepository repo = getRepository();
            return repo
                    .getClass()
                    .getMethod("findByUuid", String.class)
                    .invoke(repo, targetId);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
    }
}
