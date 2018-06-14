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
 * Created: November 30, 2016
 *
 */

package com.odysseusinc.arachne.portal.model.search;

import com.odysseusinc.arachne.portal.model.AbstractUserStudyListItem;
import com.odysseusinc.arachne.portal.model.AbstractUserStudyListItem_;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.StudyKind;
import com.odysseusinc.arachne.portal.model.StudyStatus;
import com.odysseusinc.arachne.portal.model.StudyStatus_;
import com.odysseusinc.arachne.portal.model.StudyType;
import com.odysseusinc.arachne.portal.model.StudyType_;
import com.odysseusinc.arachne.portal.model.Study_;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.model.User_;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

public class StudySpecification<T extends AbstractUserStudyListItem> implements Specification<T> {
    private final StudySearch criteria;

    public StudySpecification(final StudySearch criteria) {

        this.criteria = criteria;
    }

    @Override
    public Predicate toPredicate(final Root<T> root, final CriteriaQuery<?> query,
                                 final CriteriaBuilder cb) {

        final Path<User> user = root.get(AbstractUserStudyListItem_.user);
        if (Long.class != query.getResultType()) {
            // presence of this code while *count query is executing leads to error: query specified join fetching, but the owner of...
            // because we don't want to fetch data, we just want to fetch count of entities
            root.fetch(AbstractUserStudyListItem_.study);
        }
        final Path<Study> study = root.get(AbstractUserStudyListItem_.study);
        final Path<String> title = study.get(Study_.title);
        final Path<StudyStatus> studyStatus = study.get(Study_.status);
        final Path<Long> studyStatusId = studyStatus.get(StudyStatus_.id);
        final Path<StudyType> studyType = study.get(Study_.type);
        final Path<String> studyTypeName = studyType.get(StudyType_.name);
        final Path<Long> studyTypeId = studyType.get(StudyType_.id);
        final Path<Boolean> favourite = root.get(AbstractUserStudyListItem_.favourite);
        final Path<StudyKind> studyKind = study.get(Study_.kind);

        final List<Predicate> predicates = new LinkedList<>();
        if (criteria.getUserId() != null) {
            predicates.add(cb.equal(user.get(User_.id), criteria.getUserId()));
        }
        final Boolean criteriaFavourite = criteria.getFavourite();
        if (criteriaFavourite != null) {
            predicates.add(cb.equal(favourite, criteriaFavourite));
        }
        final Long[] statusId = criteria.getStatus();
        if (statusId != null && statusId.length > 0) {
            predicates.add(studyStatusId.in(statusId));
        }
        final Long[] typeId = criteria.getType();
        if (typeId != null && typeId.length > 0) {
            predicates.add(studyTypeId.in(typeId));
        }
        final Boolean my = criteria.getMy();
        if (my) {
            predicates.add(cb.isNotNull(root.get(AbstractUserStudyListItem_.role)));
        }
        if (criteria.getQuery() != null) {
            predicates.add(cb.or(
                    isQuerySimiliarTo(cb, title),
                    isQuerySimiliarTo(cb, studyTypeName)
                    )
            );
        }
        final StudyKind kind = criteria.getKind();
        if (kind != null) {
            predicates.add(cb.and(cb.equal(studyKind, kind)));
        }

        final Boolean privacy = criteria.getPrivacy();
        if (privacy != null) {
            predicates.add(cb.equal(study.get(Study_.privacy), privacy));
        }

        getAdditionalPredicate(root, query, cb).map(predicates::add);

        return cb.and(predicates.toArray(new Predicate[predicates.size()]));
    }

    private Predicate isQuerySimiliarTo(final CriteriaBuilder cb, final Path<String> title) {

        return cb.like(cb.lower(title), "%" + criteria.getQuery().toLowerCase() + "%");
    }

    protected Optional<Predicate> getAdditionalPredicate(final Root<T> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {

        return Optional.empty();
    }
}
