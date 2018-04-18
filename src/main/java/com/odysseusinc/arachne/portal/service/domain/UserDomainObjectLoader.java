package com.odysseusinc.arachne.portal.service.domain;

import com.odysseusinc.arachne.commons.utils.UserIdUtils;
import java.io.Serializable;

public class UserDomainObjectLoader extends GenericDomainObjectLoader {

    public UserDomainObjectLoader(Class domainClazz) {

        super(domainClazz);
    }

    public DomainObjectLoader withTargetId(Serializable targetId) {

        this.targetId = UserIdUtils.uuidToId(targetId.toString());
        return this;
    }
}
