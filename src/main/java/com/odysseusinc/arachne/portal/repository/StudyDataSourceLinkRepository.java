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
 * Created: December 05, 2016
 *
 */

package com.odysseusinc.arachne.portal.repository;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import com.odysseusinc.arachne.portal.model.DataSourceStatus;
import com.odysseusinc.arachne.portal.model.StudyDataSourceLink;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudyDataSourceLinkRepository extends EntityGraphJpaRepository<StudyDataSourceLink, Long> {

    @Query("SELECT l FROM StudyDataSourceLink l "
            + " JOIN l.dataSource.dataNode.dataNodeUsers u "
            + " JOIN FETCH l.study "
            + " WHERE :ownerId = u.user.id AND l.status=:status")
    List<StudyDataSourceLink> findByOwnerIdAndStatus(@Param("ownerId") Long ownerId,
                                                     @Param("status") DataSourceStatus status);

    @Query("SELECT l FROM StudyDataSourceLink l "
            + " JOIN l.study s"
            + " JOIN l.dataSource.dataNode.dataNodeUsers u "
            + " WHERE :ownerId = u.user.id AND s.id = :studyId AND l.status=:status")
    List<StudyDataSourceLink> findByOwnerIdAndStudyIdAndStatus(@Param("ownerId") Long ownerId,
                                                               @Param("studyId") Long studyId,
                                                               @Param("status") DataSourceStatus status);

    @Query("SELECT l FROM StudyDataSourceLink l "
            + " JOIN l.dataSource.dataNode.dataNodeUsers u "
            + " WHERE :ownerId = u.user.id AND l.id=:id")
    StudyDataSourceLink findByIdAndOwnerId(@Param("id") Long id, @Param("ownerId") Long ownerId);

    @Query(nativeQuery = true, value = "SELECT * FROM studies_data_sources l WHERE "
            + "l.study_id=:studyId AND l.data_source_id=:dataSourceId")
    StudyDataSourceLink findByDataSourceIdAndStudyId(@Param("dataSourceId") Long dataSourceId,
                                                     @Param("studyId") Long studyId);

    List<StudyDataSourceLink> findByStudyId(Long studyId);

    StudyDataSourceLink findById(Long id);

    @Query(value = "SELECT * FROM studies_data_sources WHERE id = :id",
            nativeQuery = true)
    StudyDataSourceLink findByIdIncludingDeleted(@Param("id") Long id);

    StudyDataSourceLink findByIdAndStatusAndToken(Long id, DataSourceStatus status, String token);

    StudyDataSourceLink findByStudyIdAndDataSourceId(Long study, Long dataSourceId);

    @Query("select l from StudyDataSourceLink l "
            + " where l.study.id = :studyId "
            + "and l.status = 'APPROVED' "
            + "and l.dataSource.deleted is null ")
    List<StudyDataSourceLink> findApprovedNotDeletedByStudyId(@Param("studyId") Long studyId);

    @Query("select l.study.id from StudyDataSourceLink l "
            + " where l.dataSource.id = :dataSourceId "
            + " and l.status = 'APPROVED'")
    List<Long> findStudyIdsOfNotDeletedLinksByDataSourceId(@Param("dataSourceId") Long dataSourceId);

    List<StudyDataSourceLink> findByStudyId(Long id, EntityGraph graph);

    @Modifying
    @Query(nativeQuery = true, value = "UPDATE studies_data_sources "
            + " SET status = 'DELETED' "
            + " WHERE data_source_id = :dataSourceId AND study_id in (SELECT id from studies_data WHERE tenant_id = :tenantId)")
    void setLinksBetweenStudiesAndDsDeleted(@Param("tenantId") Long tenantId, @Param("dataSourceId") Long dataSourceId);
}
