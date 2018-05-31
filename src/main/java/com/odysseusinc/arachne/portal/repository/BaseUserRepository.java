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
 * Created: September 19, 2017
 *
 */

package com.odysseusinc.arachne.portal.repository;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import com.odysseusinc.arachne.portal.model.IUser;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

@NoRepositoryBean
public interface BaseUserRepository<U extends IUser> extends EntityGraphJpaRepository<U, Long>,
        JpaSpecificationExecutor<U> {

    List<U> findByIdIn(List<Long> idList);

    U findByEmailAndEnabledTrue(String email);

    U findByEmailAndEnabledFalse(String email);

    U findByOriginAndUsername(String userOrigin, String username);

    U findByOriginAndUsernameAndEnabledTrue(String userOrigin, String username);

    U findByEmail(String email, EntityGraph entityGraph);

    @Query(nativeQuery = true, value = "SELECT * FROM users"
            + " WHERE id NOT IN "
            + "      (SELECT user_id  FROM users_studies_extended WHERE study_id=:studyId "
            + "       AND lower(status) NOT IN ('declined', 'deleted') and role NOT IN ('DATA_SET_OWNER'))\n"
            + " AND (lower(firstname) SIMILAR TO :suggestRequest OR\n"
            + "      lower(lastname) SIMILAR TO :suggestRequest OR\n"
            + "      lower(middlename) SIMILAR TO :suggestRequest)"
            + " AND enabled = TRUE"
            + " LIMIT :limit")
    List<U> suggestToStudy(@Param("suggestRequest") String suggestRequest,
                           @Param("studyId") Long studyId,
                           @Param("limit") Integer limit);

    @Query(nativeQuery = true, value = "SELECT * FROM users"
            + " WHERE id NOT IN "
            + "      (SELECT user_id FROM paper_users WHERE paper_id = :paperId "
            + "       AND lower(status) NOT IN ('declined', 'deleted')) "
            + " AND id NOT IN "
            + "      (SELECT user_id FROM users_studies_extended "
            + "       WHERE study_id = (SELECT study_id AS paper_study_id FROM papers WHERE id = :paperId)"
            + "       AND lower(status) NOT IN ('declined', 'deleted')) "
            + " AND (lower(firstname) SIMILAR TO :suggestRequest OR "
            + "      lower(lastname) SIMILAR TO :suggestRequest OR "
            + "      lower(middlename) SIMILAR TO :suggestRequest) "
            + " AND enabled = TRUE "
            + " LIMIT :limit ")
    List<U> suggestToPaper(@Param("suggestRequest") String suggestRequest,
                           @Param("paperId") Long paperId,
                           @Param("limit") Integer limit);

    U findById(Long id);

    List<U> findAllByEnabledIsTrue(EntityGraph graph);

    @Query(nativeQuery = true, value = "SELECT * FROM users_data u WHERE enabled = TRUE")
    List<U> findAllEnabledFromAllTenants();

    Page<U> findAllBy(Pageable pageable);

    List<U> findAllByUsernameInAndEnabledTrue(List<String> userNames);

    @Query(nativeQuery = true, value = "SELECT DISTINCT u.* FROM users u "
            + "JOIN users_studies_extended ul ON ul.user_id = u.id "
            + "JOIN studies_data_sources d ON d.study_id = ul.study_id "
            + "WHERE "
            + "d.data_source_id = :datasourceId "
            + " AND UPPER(d.status) = 'APPROVED' "
            + " AND UPPER(ul.status) = 'APPROVED'")
    List<U> listApprovedByDatasource(@Param("datasourceId") Long datasourceId);

    @Modifying
    @Query(nativeQuery = true, value ="UPDATE studies_users su " +
            "SET status = 'DELETED' " +
            "WHERE su.study_id IN (SELECT id FROM studies_data WHERE tenant_id = :tenantId) AND " +
            "su.user_id = :userId")
    void setLinksBetweenStudiesAndUsersDeleted(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

    @Modifying
    @Query(nativeQuery = true, value = "UPDATE paper_users pu SET status = 'DELETED' \n" +
            "WHERE pu.paper_id IN (\n" +
            "  SELECT p.id FROM studies_data sd JOIN papers p on p.study_id = sd.id WHERE tenant_id = :tenantId \n" +
            ") AND pu.user_id = :userId")
    void setLinksBetweenPapersAndUsersDeleted(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

    @Modifying
    @Query(nativeQuery = true, value = "UPDATE paper_users pu SET status = 'APPROVED' \n" +
            "WHERE pu.paper_id IN (\n" +
            "  SELECT p.id FROM studies_data sd JOIN papers p on p.study_id = sd.id WHERE tenant_id = :tenantId \n" +
            ") AND pu.user_id = :userId AND pu.status = 'DELETED'")
    void revertBackUserToPapers(@Param("tenantId") Long tenantId, @Param("userId") Long userId);
}
