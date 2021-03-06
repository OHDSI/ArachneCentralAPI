/*
 *
 * Copyright 2018 Odysseus Data Services, inc.
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
 * Created: January 25, 2017
 *
 */

package com.odysseusinc.arachne.portal.service;

import com.odysseusinc.arachne.portal.service.impl.solr.SolrField;
import org.apache.solr.client.solrj.response.FacetField;

public interface SolrService extends BaseSolrService<SolrField> {

    static String fieldNameToFacet(SolrField solrField) {

        return solrField.getSolrName() + (solrField.getPostfixNeeded() ? "_facet" : "");
    }

    static String facetToFieldName(FacetField facetField) {

        return facetField.getName().replaceAll("_facet$", "");
    }

}
