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
 * Created: May 03, 2017
 *
 */

package com.odysseusinc.arachne.portal.repository;

import com.odysseusinc.arachne.portal.model.SubmissionInsight;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface SubmissionInsightRepository extends JpaRepository<SubmissionInsight, Long> {

    SubmissionInsight findOneBySubmissionId(Long submissionId);

    @Modifying
    @Transactional
    @Query("DELETE FROM SubmissionInsight si WHERE si.submission.id = :submissionId")
    void deleteBySubmissionId(@Param("submissionId") Long submissionId);

    @Query(" SELECT si FROM SubmissionInsight si "
            + " WHERE si.submission.analysis.study.id = :studyId "
            + "  AND ((si.description IS NOT NULL AND si.description <> '') OR "
            + "    EXISTS (SELECT f FROM SubmissionInsightSubmissionFile f "
            + "      WHERE f.submissionInsight = si AND f.commentTopic.comments IS NOT EMPTY "
            + "    ) OR "
            + "    EXISTS (SELECT rf FROM ResultFile rf "
            + "      WHERE rf.submission = si.submission AND rf.commentTopic.comments IS NOT EMPTY "
            + "    )"
            + "  ) ")
    Page<SubmissionInsight> findAllWithCommentsOrDescIsNotEmpty(@Param("studyId") Long studyId, Pageable pageable);

    @Query(value = " SELECT id, count(comments) "
            + " FROM ( "
            + "   SELECT DISTINCT "
            + "     submission_insights.id AS id, "
            + "     comments.id AS comments "
            + "   FROM submission_insights "
            + "     JOIN submissions ON submission_insights.submission_id = submissions.id "
            + "     JOIN submission_groups ON submissions.submission_group_id = submission_groups.id "
            + "     JOIN result_files ON submissions.id = result_files.submission_id "
            + "     JOIN comments ON result_files.comment_topic_id = comments.topic_id "
            + "   UNION ALL "
            + "   SELECT DISTINCT "
            + "     submission_insights.id AS id, "
            + "     comments.id AS comments "
            + "   FROM submission_insights "
            + "     JOIN submission_insight_submission_files ON submission_insights.id = submission_insight_submission_files.submission_insight_id "
            + "     JOIN comments ON submission_insight_submission_files.comment_topic_id = comments.topic_id "
            + " ) AS unioned "
            + " WHERE id IN :ids "
            + " GROUP BY id ", nativeQuery = true)
    List<Object[]> countCommentsByTopicIds(@Param("ids") List<Long> ids);
}
