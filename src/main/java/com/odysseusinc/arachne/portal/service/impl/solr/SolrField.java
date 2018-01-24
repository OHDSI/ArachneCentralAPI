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
 * Created: February 09, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.impl.solr;

import java.util.List;
import java.util.Set;

public class SolrField {

    private String name;
    private Class dataType;
    private Boolean isSearchable;
    private Boolean isFaceted;

    public SolrField() {
        this.dataType = String.class;
    }

    public SolrField(String name) {
        this();
        this.name = name;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public Class getDataType() {

        return dataType;
    }

    public void setDataType(Class dataType) {

        this.dataType = dataType;
    }

    public Boolean getSearchable() {

        return isSearchable;
    }

    public void setSearchable(Boolean searchable) {

        isSearchable = searchable;
    }

    public Boolean getFaceted() {

        return isFaceted;
    }

    public void setFaceted(Boolean faceted) {

        isFaceted = faceted;
    }

    protected String getDynamicPostfix() {
        String postfix;
        // NOTE: sort on multiValued fields is not available
        if (dataType.equals(Integer.class)) {
            postfix = "_i";
        } else if (dataType.isAssignableFrom(List.class) || dataType.isAssignableFrom(Set.class)) {
            postfix = "_ts";
        } else {
            postfix = "_txt";
        }
        return postfix;
    }

    public String getSolrName() {
        return name + getDynamicPostfix();
    }
}
