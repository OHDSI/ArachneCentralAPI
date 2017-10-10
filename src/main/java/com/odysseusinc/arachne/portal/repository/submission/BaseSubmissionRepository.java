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
 * Created: September 18, 2017
 *
 */

package com.odysseusinc.arachne.portal.repository.submission;

import com.odysseusinc.arachne.portal.model.Submission;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

@NoRepositoryBean
public interface BaseSubmissionRepository<T extends Submission> extends JpaRepository<T, Long> {
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

    @Query(nativeQuery = true, value = "SELECT s.* FROM "
            + "(SELECT h.submission_id, max(h.date) AS status_date FROM submission_status_history h "
            + "GROUP BY h.submission_id) gh "
            + "INNER JOIN submission_status_history sh "
            + "ON sh.submission_id = gh.submission_id AND sh.date = gh.status_date "
            + "JOIN submissions s ON s.id = sh.submission_id "
            + "WHERE sh.date < :before AND sh.status IN (:status)")
    List<T> findByCreatedBeforeAndStatusIn(@Param("before") Date before,
                                                    @Param("status") Collection<String> statuses);

    @Query(nativeQuery = true, value = "SELECT * "
            + "FROM submissions s "
            + "  JOIN submission_status_history sh ON s.id = sh.submission_id AND sh.is_last = TRUE "
            + "  JOIN data_sources ds ON ds.id = s.data_source_id "
            + "  JOIN datanodes dn ON dn.id = ds.data_node_id "
            + "WHERE UPPER(sh.status) = 'STARTING' "
            + "  AND dn.sid = :dataNodeUuid ")
    List<T> findUnprocessedForDataNode(@Param("dataNodeUuid") String dataNodeUuid);

    @Query(nativeQuery = true, value = "SELECT sub.*\n"
            + "FROM submissions sub\n"
            + "  INNER JOIN data_sources ds ON ds.id = sub.data_source_id\n"
            + "  INNER JOIN datanodes dn ON dn.id = ds.data_node_id\n"
            + "  INNER JOIN datanodes_users dnu ON dnu.datanode_id = dn.id\n"
            + "  INNER JOIN datanodes_users_roles dur ON dur.datanode_user_id = dnu.id AND dur.datanode_role = 'ADMIN'"
            + "  INNER JOIN submission_status_history ssh ON ssh.submission_id = sub.id "
            + "   AND ssh.is_last = TRUE "
            + "   AND ssh.status in ('FAILED', 'PENDING', 'EXECUTED')\n"
            + "WHERE dnu.user_id = :ownerId")
    List<T> findWaitingForApprovalSubmissionsByOwnerId(@Param("ownerId") Long ownerId);

    Optional<T> findByIdAndToken(Long id, String token);

    T findById(Long id);
}
