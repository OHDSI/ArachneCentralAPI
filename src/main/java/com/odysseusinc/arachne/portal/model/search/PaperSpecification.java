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
 * Created: August 01, 2017
 *
 */

package com.odysseusinc.arachne.portal.model.search;

import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.Paper;
import com.odysseusinc.arachne.portal.model.Paper_;
import com.odysseusinc.arachne.portal.model.ParticipantStatus;
import com.odysseusinc.arachne.portal.model.PublishState;
import com.odysseusinc.arachne.portal.model.RawUser;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.model.UserStudyExtended;
import com.odysseusinc.arachne.portal.model.Study_;
import com.odysseusinc.arachne.portal.model.UserStudyExtended_;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

public class PaperSpecification<T extends Paper> implements Specification<T> {

    private static final String FAVOURITE = "favourite";

    protected final IUser user;
    private final PaperSearch criteria;

    public PaperSpecification(@NotNull PaperSearch criteria, IUser user) {

        this.user = user;
        this.criteria = criteria;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        final Path<Long> id = root.get(Paper_.id);
        final Path<Study> study = root.get(Paper_.study);
        final Path<Long> studyId = study.get(Study_.id);
        final Path<String> studyTitle = study.get(Study_.title);

        final Path<PublishState> publishState = root.get(Paper_.publishState);
        Expression<List> followers = root.get(Paper_.followers);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(getAdditionalPredicates(root, query, cb, studyId, id));

        if (criteria.getPublishState() != null) {
            predicates.add(cb.equal(publishState, criteria.getPublishState()));
        }

        if (criteria.getFavourite() != null) {
            predicates.add(
                    criteria.getFavourite()
                            ? cb.isMember(user, followers)
                            : cb.isNotMember(user, followers)
            );
        }

        if (criteria.getQuery() != null) {
            predicates.add(cb.like(cb.lower(studyTitle), getLowerPostfixPrefixLikeForm(criteria.getQuery().toLowerCase())));
        }

        final String sortBy = criteria.getSortBy();
        if (sortBy != null) {
            boolean sortAsc = criteria.getSortAsc();

            Expression<String> path;

            switch (sortBy) {
                case FAVOURITE:
                    path = cb.size(followers).as(String.class);
                    sortAsc = !sortAsc;
                    break;
                default:
                    Path relative = root;
                    for (String field : sortBy.split("\\.")) {
                        relative = relative.get(field);
                    }
                    path = relative;
            }
            Order order;
            if (sortAsc) {
                order = cb.asc(path);
            } else {
                order = cb.desc(path);
            }
            query.orderBy(order);
        }

        return cb.and(predicates.toArray(new Predicate[predicates.size()]));
    }


    protected Predicate getAdditionalPredicates(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb, Path<Long> studyId, Path<Long> id) {

        final Path<PublishState> publishState = root.get(Paper_.publishState);

        return cb.or(
                cb.equal(publishState, PublishState.PUBLISHED),
                cb.exists(getUserStudyLinkSubquery(query, cb, studyId))
        );
    }

    protected Subquery<UserStudyExtended> getUserStudyLinkSubquery(CriteriaQuery<?> query, CriteriaBuilder cb, Path<Long> studyId) {

        final Subquery<UserStudyExtended> userStudyExtendedLinkSubquery = query.subquery(UserStudyExtended.class);
        final Root<UserStudyExtended> userStudyLinkRoot = userStudyExtendedLinkSubquery.from(UserStudyExtended.class);
        userStudyExtendedLinkSubquery.select(userStudyLinkRoot);

        final Path<RawUser> linkUser = userStudyLinkRoot.get(UserStudyExtended_.user);
        final Path<Study> linkStudy = userStudyLinkRoot.get(UserStudyExtended_.study);
        final Path<Long> linkStudyId = linkStudy.get(Study_.id);
        final Path<ParticipantStatus> linkStatus = userStudyLinkRoot.get(UserStudyExtended_.status);

        Predicate userStudyPredicate = cb.and(cb.equal(linkUser, user), cb.equal(linkStudyId, studyId));
        userStudyPredicate = cb.and(userStudyPredicate, cb.notEqual(linkStatus, ParticipantStatus.DELETED));
        userStudyExtendedLinkSubquery.where(userStudyPredicate);
        return userStudyExtendedLinkSubquery;
    }

    private String lowerForm(String string) {

        return StringUtils.lowerCase(string);
    }

    private String postfixLikeForm(String string) {

        return string + "%";
    }

    private String prefixLikeForm(String string) {

        return "%" + string;
    }

    private String getLowerPostfixPrefixLikeForm(String string) {

        return lowerForm(postfixLikeForm(prefixLikeForm(string)));
    }
}
