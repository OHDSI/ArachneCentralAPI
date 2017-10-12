/*
 * Copyright 2017 Observational Health Data Sciences and Informatics
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Company: Odysseus Data Services, Inc.
 * Product Owner/Architecture: Gregory Klebanov
 * Authors: Pavel Grafkin
 * Created: October 12, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.domain;

import com.odysseusinc.arachne.portal.model.DataSource;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.repository.CrudRepository;

public class DsDomainObjectLoader extends DomainObjectLoader {

    public DsDomainObjectLoader(Class domainClazz) {

        super(domainClazz);
    }

    @Override
    protected Serializable getTargetId(Object domainObject) {

        return ObjectUtils.firstNonNull(((DataSource) domainObject).getUuid(), ((DataSource) domainObject).getId());
    }

    private Object loadById() {

        return getRepository().findOne(targetId);
    }

    private Object loadByUuid() {

        CrudRepository repo = getRepository();
        try {
            return repo
                    .getClass()
                    .getMethod("findByUuid", String.class)
                    .invoke(repo, targetId);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Object loadDomainObject() {

        if (targetId == null) {
            return null;
        } else if (StringUtils.isNumeric(targetId.toString())) {
            return loadById();
        } else {
            return loadByUuid();
        }
    }
}
