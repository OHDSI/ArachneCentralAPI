/*
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
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva, Konstantin Yaroshovets
 * Created: February 26, 2018
 *
 */

package com.odysseusinc.arachne.portal.model.search;

import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.model.SubmissionGroup_;
import com.odysseusinc.arachne.portal.model.SubmissionStatus;
import com.odysseusinc.arachne.portal.model.SubmissionStatusHistoryElement;
import com.odysseusinc.arachne.portal.model.SubmissionStatusHistoryElement_;
import com.odysseusinc.arachne.portal.model.Submission_;
import io.jsonwebtoken.lang.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

public class SubmissionSpecification implements Specification<Submission> {

    private final Set<Long> submissionGroupIds;
    private Set<SubmissionStatus> statuses;
    private Set<Long> dataSourceIds;

    private final List<Predicate> predicates = new ArrayList<>();

    public SubmissionSpecification(Set<Long> submissionGroupIds) {

        if (Collections.isEmpty(submissionGroupIds)) {
            throw new IllegalArgumentException("Constructor parameters must not be empty");
        }
        this.submissionGroupIds = submissionGroupIds;
    }

    public static SubmissionSpecification.SubmissionSearchBuilder builder(Set<Long> submissionGroupIds) {

        return new SubmissionSpecification.SubmissionSearchBuilder(submissionGroupIds);
    }

    public static class SubmissionSearchBuilder {

        private final Set<Long> submissionGroupIds;
        private Set<SubmissionStatus> statuses;
        private Set<Long> dataSourceIds;

        public SubmissionSearchBuilder(Set<Long> submissionGroupIds) {

            this.submissionGroupIds = submissionGroupIds;
        }

        public SubmissionSpecification.SubmissionSearchBuilder withStatuses(Set<SubmissionStatus> statuses) {

            this.statuses = statuses;
            return this;
        }

        public SubmissionSpecification.SubmissionSearchBuilder withDataSourceIds(Set<Long> dataSourceIds) {

            this.dataSourceIds = dataSourceIds;
            return this;
        }

        public SubmissionSpecification build() {

            final SubmissionSpecification submissionSearch = new SubmissionSpecification(submissionGroupIds);
            submissionSearch.statuses = statuses;
            submissionSearch.dataSourceIds = dataSourceIds;
            return submissionSearch;
        }
    }

    @Override
    public Predicate toPredicate(Root<Submission> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        final Path<Long> submissionGroupId = root.get(Submission_.submissionGroup).get(SubmissionGroup_.id);
        addInPredicate(cb, submissionGroupId, submissionGroupIds);

        if (!Collections.isEmpty(dataSourceIds)) {
            final Path<Long> dataSourceIdPath = root.get(Submission_.dataSource).get("id");
            addInPredicate(cb, dataSourceIdPath, dataSourceIds);
        }

        if (!Collections.isEmpty(statuses)) {
            final Subquery<SubmissionStatusHistoryElement> subquery = query.subquery(SubmissionStatusHistoryElement.class);
            final Root<SubmissionStatusHistoryElement> subqueryFrom = subquery.from(SubmissionStatusHistoryElement.class);

            final Path<SubmissionStatus> status = subqueryFrom.get(SubmissionStatusHistoryElement_.status);
            final CriteriaBuilder.In<Object> in = cb.in(status);
            statuses.forEach(in::value);

            final Subquery<SubmissionStatusHistoryElement> where = subquery.select(subqueryFrom).where(
                    cb.and(
                            cb.equal(subqueryFrom.get(SubmissionStatusHistoryElement_.submission).get(Submission_.id), root.get(Submission_.id)),
                            cb.isTrue(subqueryFrom.get(SubmissionStatusHistoryElement_.isLast)),
                            in
                    )
            );
            predicates.add(cb.exists(where));
        }
        return cb.and(predicates.toArray(new Predicate[predicates.size()]));
    }

    private <T extends Object> void addInPredicate(CriteriaBuilder cb, Path<T> path, Set<T> collection) {

        final CriteriaBuilder.In<T> in = cb.in(path);
        collection.forEach(in::value);
        predicates.add(in);
    }
}
