/*
 *  Copyright 2017 Observational Health Data Sciences and Informatics
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

import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.hibernate.Hibernate;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;

/**
 *
 */
public class EntityUtils {

    private static RetryTemplate retry = new RetryTemplate();

    private EntityUtils() {

    }

    public static <T> T unproxy(T object) {

        return (T)Hibernate.unproxy(object);
    }

    public static <T> T unproxyAndConvert(Object object, Class<T> clazz) {

        return (T)Hibernate.unproxy(object);
    }

    /**
     * Splits the given collection and runs the given function on them.
     *
     * @param f callback
     * @param list list of values
     * @param batchSize size of batch
     */
    public static <T> void split(final Consumer<List<T>> f, final List<T> list, final int batchSize) {

        Lists.partition(list, batchSize).forEach(f);
    }

    /**
     * Splits the given collection and runs the given function on them.
     *
     * @param f callback
     * @param list list of values
     * @param batchSize size of batch
     */
    public static <T> void splitWithRetry(final Consumer<List<T>> f, final List<T> list, final int batchSize) {

        Lists.partition(list, batchSize).forEach(v -> retry.execute((RetryCallback<Void, RuntimeException>) context -> {
            f.accept(v);
            return null;
        }));
    }

    public static void main(final String[] args) {

        final List<Integer> ints = IntStream.range(0, 1_000_000).boxed().collect(Collectors.toList());
        split(EntityUtils::check, ints, 200_000);
    }

    private static void check(final List<?> ints) {

        System.out.println(ints.size());
        throw new RuntimeException();
    }

}
