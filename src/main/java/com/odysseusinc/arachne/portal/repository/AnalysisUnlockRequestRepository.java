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
 * Created: May 17, 2017
 *
 */

package com.odysseusinc.arachne.portal.repository;

import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.AnalysisUnlockRequest;
import com.odysseusinc.arachne.portal.model.AnalysisUnlockRequestStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnalysisUnlockRequestRepository extends JpaRepository<AnalysisUnlockRequest, Long> {

    AnalysisUnlockRequest findByAnalysisAndStatus(Analysis analysis, AnalysisUnlockRequestStatus status);

    @Query(value = " SELECT aur.* "
            + " FROM analysis_unlock_requests AS aur "
            + "  JOIN analyses AS a ON aur.analysis_id = a.id "
            + "  JOIN studies AS s ON a.study_id = s.id "
            + "  JOIN studies_users AS su ON s.id = su.study_id "
            + " WHERE aur.status='PENDING' "
            + " AND su.status='APPROVED' "
            + " AND su.role='LEAD_INVESTIGATOR' "
            + " AND su.user_id=:leadId ",
            nativeQuery = true)
    List<AnalysisUnlockRequest> findAllByLeadId(@Param("leadId") Long leadId);

    @Query(value = " SELECT aur.* "
            + " FROM analysis_unlock_requests AS aur "
            + "  JOIN analyses AS a ON aur.analysis_id = a.id "
            + "  JOIN studies AS s ON a.study_id = s.id "
            + "  JOIN studies_users AS su ON s.id = su.study_id "
            + " WHERE aur.status='PENDING' "
            + " AND su.status='APPROVED' "
            + " AND su.role='LEAD_INVESTIGATOR' "
            + " AND su.user_id=:leadId "
            + " AND aur.id=:id",
            nativeQuery = true)
    Optional<AnalysisUnlockRequest> findOneByIdAndLeadId(@Param("id") Long id, @Param("leadId") Long leadId);

    Optional<AnalysisUnlockRequest> findByIdAndTokenAndStatus(Long id, String token, AnalysisUnlockRequestStatus status);
}
