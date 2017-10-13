/**
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
 * Created: September 19, 2017
 *
 */

package com.odysseusinc.arachne.portal.repository;

import static com.odysseusinc.arachne.portal.service.RoleService.ROLE_ADMIN;

import com.odysseusinc.arachne.portal.model.User;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

@NoRepositoryBean
public interface BaseUserRepository<U extends User> extends JpaRepository<U, Long>,
        JpaSpecificationExecutor<U> {

    List<U> findByIdIn(List<Long> idList);

    U findByEmailAndEnabledTrue(String email);

    U findByEmailAndEnabledFalse(String email);

    U findByOriginAndUsername(String userOrigin, String username);

    U findByOriginAndUsernameAndEnabledTrue(String userOrigin, String username);

    U findByRegistrationCode(String activateCode);

    U findByEmail(String email);

    @Query(nativeQuery = true, value = "SELECT * FROM users "
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

    @Query(nativeQuery = true, value = "SELECT * FROM users"
            + " WHERE id NOT IN "
            + "      (SELECT user_id  FROM users_studies_extended WHERE study_id=:studyId "
            + "       AND lower(status) NOT IN ('declined', 'deleted'))\n"
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

    @Query(nativeQuery = true, value = "SELECT * FROM users u "
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


    U findByUuid(String uuid);

    @Query("SELECT u FROM User u")
    List<U> getAll();

    Page<U> findAllBy(Pageable pageable);

    List<U> findByRoles_name(String role, Sort sort);

    List<U> findAllByUsernameInAndEnabledTrue(List<String> userNames);

    @Query(nativeQuery = true, value = "SELECT DISTINCT u.* FROM users u "
            + "JOIN users_studies_extended ul ON ul.user_id = u.id "
            + "JOIN studies_data_sources d ON d.study_id = ul.study_id "
            + "WHERE "
            + "d.data_source_id = :datasourceId "
            + " AND UPPER(d.status) = 'APPROVED' "
            + " AND UPPER(ul.status) = 'APPROVED'")
    List<U> listApprovedByDatasource(@Param("datasourceId") Long datasourceId);
}
