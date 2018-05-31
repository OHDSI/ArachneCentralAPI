/*
 * Copyright 2018 Observational Health Data Sciences and Informatics
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

import com.odysseusinc.arachne.portal.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.support.Repositories;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import javax.persistence.EntityManagerFactory;

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

        if (User.class.isAssignableFrom(domainClazz)) {
            domainObjectLoader = new UserDomainObjectLoader(domainClazz);
        } else {
            domainObjectLoader = new GenericDomainObjectLoader(domainClazz);
        }

        return domainObjectLoader.withServices(repositories, entityManagerFactory);
    }
}
