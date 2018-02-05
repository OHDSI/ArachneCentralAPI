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

import com.odysseusinc.arachne.portal.model.security.SecurityGroup;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SecurityGroupRepository<S extends SecurityGroup> extends JpaRepository<S, Long> {

    @Query(
            "SELECT sg " +
                    "FROM SecurityGroup sg " +
                    "INNER JOIN sg.dataSources ds " +
                    "INNER JOIN sg.users u " +
                    "WHERE ds.id = :dataSourceId " +
                    "AND u.id = :userId"
    )
    Optional<SecurityGroup> findAnyByDataSourceIdAndUserId(@Param("dataSourceId") Long dataSourceId, @Param("userId") Long userId);

    @Query(
            "SELECT sg1 " +
                    "FROM SecurityGroup sg1 INNER JOIN sg1.users u1, SecurityGroup sg2 INNER JOIN sg2.users u2 " +
                    "WHERE u1.id = :firstUserId AND u2.id = :secondUserId AND sg1.id = sg2.id"
    )
    Optional<SecurityGroup> findCommonForUsers(@Param("firstUserId") Long firstUserId, @Param("secondUserId") Long secondUserId);
}
