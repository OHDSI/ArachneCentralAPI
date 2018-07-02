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
 * Created: September 20, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.impl;

import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.NotUniqueException;
import com.odysseusinc.arachne.portal.model.Role;
import com.odysseusinc.arachne.portal.repository.RoleRepository;
import com.odysseusinc.arachne.portal.service.BaseRoleService;
import com.odysseusinc.arachne.portal.service.RoleService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

public abstract class BaseRoleServiceImpl<R extends Role> extends CRUDLServiceImpl<R> implements BaseRoleService<R> {

    @Autowired
    private RoleRepository<R> roleRepository;

    @Override
    public R create(R role) throws NotUniqueException {

        List<R> roles = roleRepository.findByName(role.getName());
        if (!roles.isEmpty()) {
            throw new NotUniqueException("name", "Not unique");
        }
        return roleRepository.save(role);
    }

    @Override
    public R update(R role) throws NotUniqueException, NotExistException {

        if (!roleRepository.exists(role.getId())) {
            throw new NotExistException("update: role with id=" + role.getId() + " not exist", getType());
        }
        List<R> roles = roleRepository.findByName(role.getName());
        if (!roles.isEmpty()) {
            throw new NotUniqueException("name", "Not unique");
        }
        return roleRepository.save(role);
    }

    @Override
    public CrudRepository<R, Long> getRepository() {

        return roleRepository;
    }

    @Override
    public List<R> findByName(String name) {
        return roleRepository.findByName(name);
    }

}
