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

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.odysseusinc.arachne.portal.model.IUser;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

@NoRepositoryBean
public interface BaseRawUserRepository<U extends IUser> extends JpaRepository<U, Long> {

    U findByIdAndEnabledTrue(Long id);

    U findByOriginAndUsername(String userOrigin, String username);

    U findByRegistrationCode(String activateCode);

    U findByEmail(String email, EntityGraph entityGraph);

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
}
