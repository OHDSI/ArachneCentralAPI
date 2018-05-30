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
 * Created: February 09, 2017
 *
 */

package com.odysseusinc.arachne.portal.model.solr;

import com.odysseusinc.arachne.portal.api.v1.dto.converters.SolrFieldExtractor;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.enterprise.inject.spi.Producer;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by PGrafkin on 08.02.2017.
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Repeatable(SolrFieldAnnos.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface SolrFieldAnno {

    String name() default StringUtils.EMPTY;
    Class<?> clazz() default String.class;

    boolean query() default false;
    boolean filter() default false;
    boolean postfix() default true;
    boolean sort() default true;

    Class<? extends SolrFieldExtractor<?>>[] extractor() default {};
}
