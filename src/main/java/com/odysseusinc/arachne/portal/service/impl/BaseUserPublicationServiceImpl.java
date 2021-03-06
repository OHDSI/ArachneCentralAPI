/*
 *
 * Copyright 2018 Odysseus Data Services, inc.
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
 * Created: September 20, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.impl;

import com.odysseusinc.arachne.portal.model.UserPublication;
import com.odysseusinc.arachne.portal.repository.UserPublicationRepository;
import com.odysseusinc.arachne.portal.service.BaseUserPublicationService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

public abstract class BaseUserPublicationServiceImpl<UP extends UserPublication> extends CRUDLServiceImpl<UP> implements BaseUserPublicationService<UP> {

    @Autowired
    private UserPublicationRepository<UP> userPublicationRepository;

    @Override
    public CrudRepository<UP, Long> getRepository() {

        return userPublicationRepository;
    }

    @Override
    public List<UP> findByUserId(Long userId) {

        return userPublicationRepository.findByUserId(userId);
    }

    @Override
    public List<UP> findAll() {

        return userPublicationRepository.findAll();
    }
}
