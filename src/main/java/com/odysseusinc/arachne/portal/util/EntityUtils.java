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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.hibernate.Hibernate;

public class EntityUtils {

    private EntityUtils() {

    }

    public static <T> T unproxy(T object) {

        return (T)Hibernate.unproxy(object);
    }

    public static <T> T unproxyAndConvert(Object object, Class<T> clazz) {

        return (T)Hibernate.unproxy(object);
    }

    public static <T, R> List<R> batchCall(Function<List<T>, List<R>> f, List<T> list, int batchSize) {

        final List<R> result = new ArrayList<>(list.size());

        while(true) {

            final List<T> sublist = new ArrayList<>();
            result.addAll(f.apply(sublist));

            break;
        }

        return result;
    }
}
