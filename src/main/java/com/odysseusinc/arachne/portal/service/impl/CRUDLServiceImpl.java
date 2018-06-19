/*
 *
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
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva, Mikhail Mironov
 * Created: November 22, 2016
 *
 */

package com.odysseusinc.arachne.portal.service.impl;

import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.NotUniqueException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.model.AnalysisFile;
import com.odysseusinc.arachne.portal.service.CRUDLService;
import org.springframework.data.repository.CrudRepository;


public abstract class CRUDLServiceImpl<T> implements CRUDLService<T> {

    public abstract Class<T> getType();

    public abstract CrudRepository<T, Long> getRepository();

    @Override
    public T getById(Long id) throws NotExistException {

        if (id == null) {
            throw new NotExistException("id is null", getType());
        }
        T entity = getRepository().findOne(id);
        if (entity == null) {
            throw new NotExistException(getType());
        }
        return entity;
    }

    @Override
    public void delete(Long id) throws NotExistException {

        if (id == null) {
            throw new NotExistException("id is null", getType());
        }
        getRepository().delete(id);
    }

    @Override
    public T create(T object) throws NotUniqueException, PermissionDeniedException, NotExistException {

        return getRepository().save(object);
    }

    @Override
    public T update(T object) throws NotUniqueException, NotExistException, ValidationException {

        return getRepository().save(object);
    }

    @Override
    public Iterable<T> list() {

        return getRepository().findAll();
    }
}
