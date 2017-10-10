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
 * Created: December 05, 2016
 *
 */

package com.odysseusinc.arachne.portal.repository;

import com.odysseusinc.arachne.portal.model.DataSourceStatus;
import com.odysseusinc.arachne.portal.model.StudyDataSourceLink;
import com.odysseusinc.arachne.portal.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface StudyDataSourceLinkRepository extends CrudRepository<StudyDataSourceLink, Long> {

    @Query("SELECT l FROM StudyDataSourceLink l "
            + " JOIN l.dataSource.dataNode.dataNodeUsers u "
            + " JOIN u.dataNodeRole r"
            + " WHERE :owner = u.user AND r = 'ADMIN' AND l.status=:status")
    List<StudyDataSourceLink> findByOwnerAndStatus(@Param("owner") User owner, @Param("status") DataSourceStatus status);

    @Query("SELECT l FROM StudyDataSourceLink l "
            + " JOIN l.dataSource.dataNode.dataNodeUsers u "
            + " JOIN u.dataNodeRole r"
            + " WHERE :owner = u.user AND r = 'ADMIN' AND l.id=:id")
    StudyDataSourceLink findByIdAndOwner(@Param("id") Long id, @Param("owner") User owner);

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

    @Query("select l from StudyDataSourceLink l "
            + " where l.dataSource.id = :dataSourceId "
            + " and l.status = 'APPROVED'")
    List<StudyDataSourceLink> findNotDeletedByDataSourceId(@Param("dataSourceId") Long dataSourceId);

}
