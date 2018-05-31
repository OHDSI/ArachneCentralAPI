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
 * Created: September 08, 2017
 *
 */

package com.odysseusinc.arachne.portal.repository;

import com.odysseusinc.arachne.portal.model.ParticipantRole;
import com.odysseusinc.arachne.portal.model.ParticipantStatus;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.model.UserStudyExtended;
import java.util.List;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface UserStudyExtendedRepository extends CrudRepository<UserStudyExtended, Long>,
        JpaSpecificationExecutor<UserStudyExtended> {

    List<UserStudyExtended> findByUserIdAndStudyIdAndStatusIn(Long userId, Long studyId, List<ParticipantStatus> pending);

    @Query(nativeQuery = true, value = "SELECT * FROM  users_studies_extended WHERE study_id = :studyId "
            + "ORDER BY"
            + "  CASE WHEN (status != 'DELETED')"
            + "    THEN 0"
            + "  ELSE 1"
            + "  END,"

            + "  CASE WHEN (role = 'LEAD_INVESTIGATOR')"
            + "    THEN 0"
            + "  WHEN (role = 'DATA_SET_OWNER')"
            + "    THEN 1"
            + "  ELSE 2"
            + "  END,"

            + "  CASE WHEN (status = 'APPROVED')"
            + "    THEN 0"
            + "  WHEN (status = 'PENDING')"
            + "    THEN 1"
            + "  WHEN (status = 'DECLINED')"
            + "    THEN 2"
            + "  ELSE 3"
            + "  END"
    )
    List<UserStudyExtended> findByStudy(@Param("studyId") Long studyId);

    @Query(nativeQuery = true,
            value = "SELECT * FROM  users_studies_extended WHERE study_id = :studyId AND status not in ('DELETED', 'DECLINED')")
    List<UserStudyExtended> findByStudyIdAndStatusNotDeletedOrDeclined(@Param("studyId") Long studyId);

    List<UserStudyExtended> findByStudyAndRoleAndStatus(Study study, ParticipantRole role, ParticipantStatus status);
}
