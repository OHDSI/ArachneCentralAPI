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
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva, Konstantin Yaroshovets
 * Created: February 6, 2018
 *
 */

package com.odysseusinc.arachne.portal.service.impl;

import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.model.Organization;
import com.odysseusinc.arachne.portal.repository.OrganizationRepository;
import com.odysseusinc.arachne.portal.service.OrganizationService;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrganizationServiceImpl implements OrganizationService {

    private final static Logger logger = LoggerFactory.getLogger(OrganizationService.class);

    private final OrganizationRepository organizationRepository;

    @Autowired
    public OrganizationServiceImpl(OrganizationRepository organizationRepository) {

        this.organizationRepository = organizationRepository;
    }

    @Override
    @Transactional
    @PreAuthorize("hasPermission(#organization, T(com.odysseusinc.arachne.portal.security.ArachnePermission).CREATE_ORGANIZATION)")
    public Organization create(Organization organization) throws ValidationException {

        final String name = organization.getName();
        if (StringUtils.isEmpty(name)) {
            throw new ValidationException("Organization must have name");
        }
        organization.setId(null);
        final Organization saved = organizationRepository.save(organization);
        logger.info("{} created", saved);
        return saved;
    }

    @Override
    @PostAuthorize("@ArachnePermissionEvaluator.addPermissions(principal, returnObject )")
    public Organization get(Long id) {

        return organizationRepository.getById(id).orElseThrow(() -> {
            final String message = String.format("Organization with id='%s' does not exist", id);
            return new NotExistException(message, Organization.class);
        });
    }

    @Override
    @Transactional
    @PostAuthorize("@ArachnePermissionEvaluator.addPermissions(principal, returnObject )")
    public Organization getOrCreate(Organization organization) throws ValidationException {

        try {
            return get(organization.getId());
        } catch (NotExistException ignored) {}
            return create(organization);
    }

    @Override
    @Transactional
    @PreAuthorize("hasPermission(#organization, 'Organization', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).UPDATE_ORGANIZATION)")
    @PostAuthorize("@ArachnePermissionEvaluator.addPermissions(principal, returnObject )")
    public Organization update(Organization organization) {

        final Organization exist = get(organization.getId());
        final String name = organization.getName();
        if (Objects.nonNull(name)) {
            exist.setName(name);
        }
        final Organization saved = organizationRepository.save(exist);
        logger.info("{} updated", saved);
        return saved;
    }

    @Override
    @PreAuthorize("hasPermission(#id, 'Organization', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).DELETE_ORGANIZATION)")
    public void delete(Long id) {

        final Organization exist = get(id);
        organizationRepository.delete(id);
        logger.info("{} deleted", exist);
    }

    @Override
    public List<Organization> suggest(final String query, final Integer limit) {

        final String suggestRequest = Arrays.stream(query.trim().split(" "))
                .map(String::toLowerCase)
                .collect(Collectors.joining("|", "%(", ")%"));
        return organizationRepository.suggest(suggestRequest, limit);
    }
}
