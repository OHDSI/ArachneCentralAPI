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
 * Created: April 26, 2017
 *
 */

package com.odysseusinc.arachne.portal.repository;

import com.odysseusinc.arachne.portal.model.SubmissionGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubmissionGroupRepository extends JpaRepository<SubmissionGroup, Long>, JpaSpecificationExecutor<SubmissionGroup> {

    @Query(
            value = "SELECT sg FROM SubmissionGroup sg " +
                    "INNER JOIN FETCH sg.submissions s " +
                    "LEFT JOIN FETCH s.submissionInsight " +
                    "WHERE sg.analysis.id = :analysisId",
            countQuery = "SELECT COUNT(sg) FROM SubmissionGroup sg WHERE sg.analysis.id = :analysisId"
    )
    Page<SubmissionGroup> findAllByAnalysisId(@Param("analysisId") Long analysisId, Pageable pageable);
}
