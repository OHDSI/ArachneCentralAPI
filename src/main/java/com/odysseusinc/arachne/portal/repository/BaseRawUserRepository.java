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
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva
 * Created: February 15, 2018
 *
 */

package com.odysseusinc.arachne.portal.repository;

import static com.odysseusinc.arachne.portal.service.BaseRoleService.ROLE_ADMIN;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaSpecificationExecutor;
import com.odysseusinc.arachne.portal.model.IUser;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

@NoRepositoryBean
public interface BaseRawUserRepository<U extends IUser> extends EntityGraphJpaRepository<U, Long>,
        EntityGraphJpaSpecificationExecutor<U> {

    U findByIdAndEnabledTrue(Long id);

    U findByOriginAndUsername(String userOrigin, String username);

    U findByRegistrationCode(String activateCode);

    U findByEmail(String email, EntityGraph entityGraph);

    U findByEmailIgnoreCase(String email, EntityGraph entityGraph);

    U findByEmail(String email);

    U findByEmailAndEnabledTrue(String email);

    U findByOriginAndUsernameAndEnabledTrue(String userOrigin, String username);

    @Query(nativeQuery = true, value = "SELECT * FROM users_data "
            + " WHERE "
            + "     (lower(firstname) SIMILAR TO :suggestRequest OR\n"
            + "     lower(lastname) SIMILAR TO :suggestRequest OR\n"
            + "     lower(middlename) SIMILAR TO :suggestRequest)"
            + " AND email NOT IN (:emails)"
            + " AND enabled = TRUE"
            + " LIMIT :limit")
    List<U> suggest(@Param("suggestRequest") String suggestRequest,
                    @Param("emails") List<String> emails,
                    @Param("limit") Integer limit);

    @Query(nativeQuery = true, value = "SELECT * FROM users_data u "
            + " WHERE (lower(u.firstname) SIMILAR TO :suggestRequest OR\n"
            + "        lower(u.lastname) SIMILAR TO :suggestRequest OR\n"
            + "        lower(u.middlename) SIMILAR TO :suggestRequest) "
            + " AND u.id NOT IN\n"
            + "          (SELECT user_id FROM users_roles ur\n"
            + "           LEFT JOIN roles r ON ur.role_id=r.id\n"
            + "           WHERE  r.name='" + ROLE_ADMIN + "')\n"
            + " AND enabled = TRUE"
            + " LIMIT :limit")
    List<U> suggestNotAdmin(@Param("suggestRequest") String suggestRequest, @Param("limit") Integer limit);

    List<U> findByRoles_name(String role, Sort sort);

    List<U> findByIdInAndEnabledTrue(Set<Long> userIds);
    
    List<U> findByIdIn(Collection<Long> userIds);

    List<U> findByEmailIn(List<String> emails);
}
