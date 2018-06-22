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
 * Created: September 18, 2017
 *
 */

package com.odysseusinc.arachne.portal.repository.submission;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import com.odysseusinc.arachne.portal.model.Submission;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

@NoRepositoryBean
public interface BaseSubmissionRepository<T extends Submission> extends EntityGraphJpaRepository <T, Long>, JpaSpecificationExecutor<T> {
    @Query(nativeQuery = true, value = "SELECT sub.* FROM submissions sub "
            + "JOIN (SELECT submission_id, status FROM submission_status_history "
            + "WHERE submission_id = :id ORDER BY date DESC LIMIT 1) h ON h.submission_id = sub.id "
            + "WHERE sub.id = :id AND sub.update_password = :password AND h.status IN (:status)")
    T findByIdAndUpdatePasswordAndStatusIn(@Param("id") Long id,
                                                    @Param("password") String updatePassword,
                                                    @Param("status") List<String> status);

    @Query(nativeQuery = true, value = "SELECT sub.* FROM submissions sub "
            + "JOIN (SELECT submission_id, status FROM submission_status_history "
            + "WHERE submission_id = :id ORDER BY date DESC LIMIT 1) h ON h.submission_id = sub.id "
            + "WHERE sub.id = :id AND h.status IN :status")
    T findByIdAndStatusIn(@Param("id") Long id, @Param("status") Collection<String> statuses);

    T findByIdAndUpdatePassword(Long id, String updatePassword);

    @Query(nativeQuery = true, value = "SELECT * "
            + "FROM submissions s "
            + "  JOIN submission_status_history sh ON s.id = sh.submission_id AND sh.is_last = TRUE "
            + "  JOIN data_sources_data ds ON ds.id = s.data_source_id "
            + "  JOIN datanodes dn ON dn.id = ds.data_node_id "
            + "WHERE UPPER(sh.status) = 'STARTING' "
            + "  AND dn.id = :dataNodeId ")
    List<T> findUnprocessedForDataNode(@Param("dataNodeId") Long dataNodeId);

    @Query(nativeQuery = true, value = "SELECT sub.*\n"
            + "FROM submissions sub\n"
            + "  INNER JOIN data_sources ds ON ds.id = sub.data_source_id\n"
            + "  INNER JOIN datanodes dn ON dn.id = ds.data_node_id\n"
            + "  INNER JOIN datanodes_users dnu ON dnu.datanode_id = dn.id\n"
            + "  INNER JOIN submission_status_history ssh ON ssh.submission_id = sub.id "
            + "   AND ssh.is_last = TRUE "
            + "   AND ssh.status in ('FAILED', 'PENDING', 'EXECUTED')\n"
            + "WHERE dnu.user_id = :ownerId")
    List<T> findWaitingForApprovalSubmissionsByOwnerId(@Param("ownerId") Long ownerId);

    Optional<T> findByIdAndToken(Long id, String token);

    T findById(Long id);

    T findById(Long id, EntityGraph entityGraph);

    @Query(nativeQuery = true, value =
            "SELECT n FROM ( " +
            "   SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS n " +
            "   FROM submissions WHERE analysis_id = :analysisId) inr " +
            "WHERE inr.id = :submissionId"
    )
    Integer findSubmissionPositionInAnalysis(@Param("analysisId") Long analysisId, @Param("submissionId") Long submissionid);

    List<T> findByIdIn(List<Long> ids);
}
