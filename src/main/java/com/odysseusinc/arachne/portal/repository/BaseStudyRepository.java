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
 * Created: September 12, 2017
 *
 */

package com.odysseusinc.arachne.portal.repository;

import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.statemachine.ObjectRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

@NoRepositoryBean
public interface BaseStudyRepository<T extends Study> extends JpaRepository<T, Long>, ObjectRepository<T>, JpaSpecificationExecutor<T> {

    List<T> findByTitle(String title);

    @Query(nativeQuery = true,
            value = "SELECT * " +
                    "FROM studies JOIN studies_users ON studies.id = studies_users.study_id " +
                    "AND studies_users.role = 'LEAD_INVESTIGATOR' " +
                    "WHERE "
                    + " studies.id NOT IN (SELECT study_id FROM users_studies_extended "
                    + "                    WHERE user_id=:participantId "
                    + "                    AND lower(status) IN ('pending', 'approved')) "
                    + " AND studies_users.user_id=:ownerId AND lower(title) SIMILAR TO :suggestRequest"
                    + " AND lower(studies_users.status) = 'approved'")
    Iterable<T> suggestByParticipantId(@Param("suggestRequest") String suggestRequest,
                                       @Param("ownerId") Long id,
                                       @Param("participantId") Long participantId);

    @Query(nativeQuery = true,
            value = "SELECT studies.* " +
                    "FROM studies JOIN studies_users ON studies.id = studies_users.study_id "
                    + " AND studies_users.role = 'LEAD_INVESTIGATOR' "
                    + " WHERE studies.id NOT IN "
                    + " (SELECT study_id FROM studies_data_sources WHERE data_source_id=:datasourceId"
                    + " AND lower(studies_data_sources.status) IN ('pending', 'approved')) "
                    + " AND studies_users.user_id=:ownerId AND lower(title) SIMILAR TO :suggestRequest"
                    + " AND lower(studies_users.status) = 'approved'")
    Iterable<T> suggestByDatasourceId(@Param("suggestRequest") String suggestRequest,
                                      @Param("ownerId") Long id,
                                      @Param("datasourceId") Long datasourceId);

    List<T> findByIdIn(List<Long> ids);

    @Query(nativeQuery = true, value = "SELECT * FROM studies_data where id = :studyId")
    T findByIdInAnyTenant(@Param("studyId") Long id);

    @Query(nativeQuery = true, value = "SELECT s.* FROM studies_data s JOIN papers p on s.id = p.study_id")
    List<T> findWithPapersInAnyTenant();

    @Query(nativeQuery = true, value = "SELECT s.* FROM studies_data s WHERE EXISTS(SELECT 1 FROM analyses a WHERE a.study_id = s.id)")
    List<T> findWithAnalysesInAnyTenant();

    @Query(nativeQuery = true, value = "SELECT * FROM studies_data WHERE id IN :studyIds")
    List<T> findByIdsInAnyTenant(@Param("studyIds") Collection<Long> ids);

    @Query("SELECT s, u FROM UserStudy u JOIN u.study s WHERE s.kind = com.odysseusinc.arachne.portal.model.StudyKind.WORKSPACE AND u.user.id = :userId")
    T findWorkspaceForUser(@Param("userId") Long userId);
}