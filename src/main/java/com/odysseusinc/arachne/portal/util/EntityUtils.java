/*
 *  Copyright 2018 Odysseus Data Services, inc.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Company: Odysseus Data Services, Inc.
 *  Product Owner/Architecture: Gregory Klebanov
 *  Authors: Anton Gackovka
 *  Created: December 16, 2017
 *
 */

package com.odysseusinc.arachne.portal.util;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphUtils;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.Consumer;
import org.hibernate.Hibernate;

public class EntityUtils {

    private EntityUtils() {

    }

    public static <T> T unproxy(T object) {

        return (T) Hibernate.unproxy(object);
    }

    public static <T> T unproxyAndConvert(Object object, Class<T> clazz) {

        return (T) Hibernate.unproxy(object);
    }

    public static EntityGraph fromAttributePaths(final String... strings) {

        return EntityGraphUtils.fromAttributePaths(strings);
    }

    /**
     * Splits the given collection and runs the given function on them.
     *
     * @param f callback
     * @param list list of values
     * @param batchSize size of batch
     */
    public static <T> void splitAndApplyBatchFunction(final Consumer<List<T>> f, final List<T> list, final int batchSize) {

        Lists.partition(list, batchSize).forEach(f);
    }
}
