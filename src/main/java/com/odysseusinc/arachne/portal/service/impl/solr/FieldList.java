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

package com.odysseusinc.arachne.portal.service.impl.solr;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by PGrafkin on 07.02.2017.
 */
public class FieldList<T extends SolrField> extends ArrayList<T> {

    public T getByName(String name) {
        return this
                .stream()
                .filter(field -> field.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public T getBySolrName(String name) {
        return this
                .stream()
                .filter(field -> field.getSolrName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public List<T> getSearchableFields() {
        return this
                .stream()
                .filter(SolrField::getSearchable)
                .collect(Collectors.toList());
    }

    public List<T> getFacetedFields() {
        return this
                .stream()
                .filter(SolrField::getFaceted)
                .collect(Collectors.toList());
    }
}
