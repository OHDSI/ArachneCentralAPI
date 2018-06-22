/*
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
 * Created: October 10, 2017
 *
 */

package com.odysseusinc.arachne.portal.component;

import com.odysseusinc.arachne.portal.security.ArachnePermission;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class PermissionDsl {

    private Object domainObject;
    private Set<ArachnePermission> permissions = new HashSet<>();

    private PermissionDsl(Object domainObject) {

        this.domainObject = domainObject;
    }

    public static PermissionDsl domainObject(Object domainObject) {

        return new PermissionDsl(domainObject);
    }

    public <T> PermissionExecutor<T> when(Predicate<T> predicate) {

        return new PermissionExecutor(this, predicate, getDomainObject());
    }

    public <T> PermissionExecutor<T> when(T value, Predicate<T> predicate) {

        return new PermissionExecutor(this, predicate, value);
    }

    public Set<ArachnePermission> getPermissions() {

        return permissions;
    }

    public Object getDomainObject() {

        return domainObject;
    }

    void addPermissions(Collection<ArachnePermission> permissions) {

        if (Objects.nonNull(permissions)) {
            this.permissions.addAll(permissions);
        }
    }

    void replacePermissions(Set<ArachnePermission> result) {

        if (Objects.nonNull(result)) {
            this.permissions = new HashSet<>(result);
        }
    }

    PermissionDsl with(PermissionDsl another) {

        addPermissions(another.getPermissions());
        return this;
    }

    public static class PermissionExecutor<T> {

        private PermissionDsl dsl;
        private Predicate<T> predicate;
        private T value;
        private Set<ArachnePermission> result = new HashSet<>();

        public PermissionExecutor(PermissionDsl dsl, Predicate<T> predicate, T value) {

            this.dsl = dsl;
            this.predicate = predicate;
            this.value = value;
        }

        public PermissionExecutor<T> then(Function<T, Set> expr) {

            if (predicate.test(value)) {
                T domainObject = (T) dsl.getDomainObject();
                result = expr.apply(domainObject);
            }
            return this;
        }

        public PermissionExecutor<T> thenIfEmpty(Function<T, Set> expr) {

            if (predicate.test(value) && result.isEmpty()) {
                result.addAll(expr.apply((T) dsl.getDomainObject()));
            }
            return this;
        }

        public PermissionExecutor<T> filter(BiFunction<T, ArachnePermission, Boolean> cond) {

            T domainObject = (T) dsl.getDomainObject();
            if (predicate.test(value)) {
                result = result.stream()
                        .filter(p -> cond.apply(domainObject, p))
                        .collect(Collectors.toSet());
            }
            return this;
        }

        public PermissionExecutor<T> and() {

            dsl.addPermissions(this.result);
            return this;
        }

        public PermissionDsl apply() {

            dsl.addPermissions(this.result);
            return dsl;
        }

        public PermissionDsl replace() {

            dsl.replacePermissions(this.result);
            return dsl;
        }
    }
}
