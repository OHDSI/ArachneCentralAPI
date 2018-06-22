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
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva, Konstantin Yaroshovets
 * Created: February 26, 2018
 *
 */

package com.odysseusinc.arachne.portal.model.search;

import com.odysseusinc.arachne.portal.model.Analysis_;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.model.SubmissionGroup;
import com.odysseusinc.arachne.portal.model.SubmissionGroup_;
import com.odysseusinc.arachne.portal.model.SubmissionInsight;
import com.odysseusinc.arachne.portal.model.SubmissionStatus;
import com.odysseusinc.arachne.portal.model.SubmissionStatusHistoryElement;
import com.odysseusinc.arachne.portal.model.SubmissionStatusHistoryElement_;
import com.odysseusinc.arachne.portal.model.Submission_;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;

public class SubmissionGroupSpecification implements Specification<SubmissionGroup> {

    private SubmissionGroupSearch criteria;

    public SubmissionGroupSpecification(SubmissionGroupSearch criteria) {

        this.criteria = criteria;
    }

    @Override
    public Predicate toPredicate(Root<SubmissionGroup> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        List<Predicate> predicates = new ArrayList<>();
        final Path<Long> analysisIdPath = root.get(SubmissionGroup_.analysis).get(Analysis_.id);
        predicates.add(cb.equal(analysisIdPath, criteria.getAnalysisId()));

        final Boolean hasInsight = criteria.getHasInsight();
        final Boolean showHidden = criteria.getShowHidden();

        if (Objects.isNull(showHidden) || !showHidden) {
            final Path<Boolean> hiddenPath = root.join(SubmissionGroup_.submissions).get(Submission_.hidden);
            predicates.add(cb.isFalse(hiddenPath));
        }
        if (Objects.nonNull(hasInsight)) {
            final Path<SubmissionInsight> insightPath = root.join(SubmissionGroup_.submissions).join(Submission_.submissionInsight, JoinType.LEFT);
            predicates.add(hasInsight ? cb.isNotNull(insightPath) : cb.isNull(insightPath));
        }

        final Set<Long> dataSourceIds = criteria.getDataSourceIds();
        if (!CollectionUtils.isEmpty(dataSourceIds)) {
            final ListJoin<SubmissionGroup, Submission> submissionListJoin = root.join(SubmissionGroup_.submissions);
            final Path<Long> longPath = submissionListJoin.get(Submission_.dataSource).get("id");
            final CriteriaBuilder.In<Long> in = cb.in(longPath);
            dataSourceIds.forEach(in::value);
            predicates.add(in);
        }

        final Set<SubmissionStatus> submissionStatuses = criteria.getSubmissionStatuses();
        if (!CollectionUtils.isEmpty(submissionStatuses)) {
            final ListJoin<Submission, SubmissionStatusHistoryElement> statusHistoryElementListJoin
                    = root.join(SubmissionGroup_.submissions).join(Submission_.statusHistory);
            final Path<SubmissionStatus> statusPath = statusHistoryElementListJoin.get(SubmissionStatusHistoryElement_.status);
            final CriteriaBuilder.In<SubmissionStatus> in = cb.in(statusPath);
            submissionStatuses.forEach(in::value);
            predicates.add(
                    cb.and(
                            cb.isTrue(statusHistoryElementListJoin.get(SubmissionStatusHistoryElement_.isLast)),
                            in
                    )
            );
        }
        query.distinct(true);
        return cb.and(predicates.toArray(new Predicate[predicates.size()]));
    }
}
