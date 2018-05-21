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
 * Created: October 05, 2017
 *
 */

package com.odysseusinc.arachne.portal.repository;

import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.User;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;

public class UserSpecifications {

    private static final String USERS_DATA_JOIN_TENANTS_USERS_IN_QUERY = "SELECT id FROM users_data u JOIN tenants_users tu ON tu.user_id = u.id " + "WHERE tu.tenant_id IN (:tenants) " + "GROUP BY u.id HAVING count(u.id) > :count";

    public static <U extends IUser> Specification<U> userEnabled() {

        return (root, query, cb) -> cb.equal(root.get("enabled"), true);
    }

    public static <U extends IUser> Specification<U> emailConfirmed() {

        return (root, query, cb) -> cb.equal(root.get("emailConfirmed"), true);
    }

    public static <U extends IUser> Specification<U> hasEmail() {

        return (root, query, cb) -> cb.isNotNull(root.get("email"));
    }

    public static <U extends IUser> Specification<U> withFieldLike(String field, String namePattern) {

        return (root, query, cb) -> cb.like(root.get(field), namePattern);
    }

    public static <U extends IUser> Specification<U> usersIn(final Long[] tenantIds, EntityManager em) {

        return ((root, criteriaQuery, cb) -> {
            List<Long> tenantList = java.util.Arrays.asList(tenantIds);
            Query query = em.createNativeQuery(USERS_DATA_JOIN_TENANTS_USERS_IN_QUERY);
            query.setParameter("tenants", tenantList);
            query.setParameter("count", tenantList.size() - 1);
            List<String> userIds = query.getResultList();

            return root.in(userIds);
        });
    }

    public static <U extends IUser> Specification<U> withNameLike(String namePattern) {

        Specifications<U> spec = Specifications.where(withFieldLike("firstname", namePattern));
        return spec.or(withFieldLike("middlename", namePattern))
                .or(withFieldLike("lastname", namePattern));
    }

    public static <U extends IUser> Specification<U> withEmailLike(String emailPattern) {

        return withFieldLike("email", emailPattern);
    }

    public static <U extends IUser> Specification<U> withNameOrEmailLike(String pattern) {

        Specifications<U> spec = Specifications.where(withNameLike(pattern));
        return spec.or(withEmailLike(pattern));
    }

}
