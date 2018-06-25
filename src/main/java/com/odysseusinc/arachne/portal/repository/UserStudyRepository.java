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
 * Created: January 25, 2017
 *
 */

package com.odysseusinc.arachne.portal.repository;

import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.ParticipantRole;
import com.odysseusinc.arachne.portal.model.ParticipantStatus;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.model.UserStudy;
import java.util.List;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface UserStudyRepository extends CrudRepository<UserStudy, Long>, JpaSpecificationExecutor<UserStudy> {

    List<UserStudy> findByUserAndStudy(User user, Study study);

    @Query(nativeQuery = true, value = "SELECT * FROM studies_users WHERE study_id = :studyId"
            + " AND user_id= :userId")
    UserStudy findOneByStudyIdAndUserId(@Param("studyId") Long studyId, @Param("userId") Long userId);

    UserStudy findOneByStudyAndUserId(Study study, Long userId);

    List<UserStudy> findByStudyAndRole(Study study, ParticipantRole role);

    @Query(value = "select us from UserStudy us join fetch us.study where us.status = :status and us.user.id = :userId")
    List<UserStudy> findByUserAndStatus(@Param("userId") Long userId, @Param("status") ParticipantStatus status);

    List<UserStudy> findByUserIdAndStudyIdAndStatus(Long userId, Long studyId, ParticipantStatus pending);

    UserStudy findByIdAndUserId(Long id, Long userId);

    UserStudy findByIdAndStatusAndToken(Long id, ParticipantStatus pending, String token);

    @Modifying
    @Transactional
    @Query(value = " DELETE FROM studies_users "
            + " WHERE studies_users.study_id = :studyId AND studies_users.user_id = :userId AND NOT "
            + " (SELECT count(*) > 0 AS tracked "
            + "  FROM studies "
            + "    FULL OUTER JOIN analyses ON studies.id = analyses.study_id "
            + "    FULL OUTER JOIN analyses_files ON analyses.id = analyses_files.analysis_id "
            + "    FULL OUTER JOIN submission_groups ON analyses.id = submission_groups.analysis_id "
            + "    FULL OUTER JOIN submissions ON submission_groups.id = submissions.submission_group_id "
            + "    FULL OUTER JOIN submission_files ON submission_groups.id = submission_files.submission_group_id "
            + "    FULL OUTER JOIN submission_insights ON submissions.id = submission_insights.submission_id "
            + "    FULL OUTER JOIN submission_insight_submission_files "
            + "      ON submission_insight_submission_files.submission_insight_id = submission_insights.id "
            + "    FULL OUTER JOIN result_files ON submissions.id = result_files.submission_id "
            + "    FULL OUTER JOIN comment_topics "
            + "      ON submission_insight_submission_files.comment_topic_id = comment_topics.id "
            + "         OR result_files.comment_topic_id = comment_topics.id "
            + "    FULL OUTER JOIN comments ON comment_topics.id = COMMENTS.topic_id "
            + "  WHERE studies.id = :studyId "
            + "        AND (analyses.author_id = :userId "
            + "             OR analyses_files.author_id = :userId "
            + "             OR submission_groups.author_id = :userId "
            + "             OR submissions.author_id = :userId "
            + "             OR submission_files.author_id = :userId "
            + "             OR comments.author_id = :userId "
            + "             OR user_id = :userId));"
            , nativeQuery = true)
    int hardRemoveIfNotTracked(@Param("studyId") Long studyId, @Param("userId") Long id);
}