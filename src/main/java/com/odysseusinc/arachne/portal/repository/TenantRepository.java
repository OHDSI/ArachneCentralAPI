/*
 *
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
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva, Mikhail Mironov
 * Created: October 19, 2016
 *
 */

package com.odysseusinc.arachne.portal.repository;

import com.odysseusinc.arachne.portal.model.security.Tenant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TenantRepository<T extends Tenant> extends JpaRepository<T, Long> {

    Optional<Tenant> findFirstByDataSourcesIdAndUsersId(@Param("dataSourceId") Long dataSourceId, @Param("userId") Long userId);

    @Query(
            "SELECT t1 " +
                    "FROM Tenant t1 INNER JOIN t1.users u1, Tenant t2 INNER JOIN t2.users u2 " +
                    "WHERE u1.id = :firstUserId AND u2.id = :secondUserId AND t1.id = t2.id"
    )
    List<Tenant> findCommonForUsers(@Param("firstUserId") Long firstUserId, @Param("secondUserId") Long secondUserId);
}
