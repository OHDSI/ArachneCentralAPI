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
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva, Mikhail Mironov
 * Created: November 30, 2016
 *
 */

package com.odysseusinc.arachne.portal.model.search;

import com.odysseusinc.arachne.portal.model.AbstractUserStudyListItem;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.StudyStatus;
import com.odysseusinc.arachne.portal.model.StudyType;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

public class StudySpecification<T extends AbstractUserStudyListItem> implements Specification<T> {
    private StudySearch criteria;

    public StudySpecification(StudySearch criteria) {

        this.criteria = criteria;
    }

    @Override
    public Predicate toPredicate(final Root<T> root, final CriteriaQuery<?> query,
                                 final CriteriaBuilder cb) {

        final Path<Study> user = root.get("user");
        Fetch<Object, Object> study1 = root.fetch("study");
        final Path<Study> study = root.get("study");
        final Path<String> title = study.get("title");
        final Path<StudyStatus> studyStatus = study.get("status");
        final Path<Long> studyStatusId = studyStatus.get("id");
        final Path<StudyType> studyType = study.get("type");
        final Path<Long> studyTypeId = studyType.get("id");
        final Path<Boolean> favourite = root.get("favourite");

        List<Predicate> predicates = new LinkedList<>();
        if (criteria.getUserId() != null) {
            predicates.add(cb.equal(user.get("id"), criteria.getUserId()));
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
            predicates.add(cb.isNotNull(root.get("role")));
        }
        if (criteria.getQuery() != null) {
            predicates.add(cb.like(cb.lower(title), "%" + criteria.getQuery().toLowerCase() + "%"));
        }

        final Boolean privacy = criteria.getPrivacy();
        if (privacy != null) {
            predicates.add(cb.equal(study.get("privacy"), privacy));
        }

        getAdditionalPredicate(root, query, cb).map(predicates::add);

        return cb.and(predicates.toArray(new Predicate[predicates.size()]));
    }

    protected Optional<Predicate> getAdditionalPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        return Optional.empty();
    }
}
