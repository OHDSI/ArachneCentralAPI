/*
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
 * Authors: Anton Gackovka
 * Created: February 20, 2018
 */

package com.odysseusinc.arachne.portal.api.v1.dto;

import static com.odysseusinc.arachne.portal.service.BaseSolrService.ANALYSES_COLLECTION;
import static com.odysseusinc.arachne.portal.service.BaseSolrService.DATA_SOURCE_COLLECTION;
import static com.odysseusinc.arachne.portal.service.BaseSolrService.STUDIES_COLLECTION;
import static com.odysseusinc.arachne.portal.service.BaseSolrService.USER_COLLECTION;

public class GlobalSearchDomainDTO {

    public GlobalSearchDomainDTO(final String type) {

        this.value = type;

        switch(type) {
            case USER_COLLECTION:
                this.label = "Users";
                break;
            case STUDIES_COLLECTION:
                this.label = "Studies";
                break;
            case DATA_SOURCE_COLLECTION:
                this.label = "Data Sources";
                break;
            case ANALYSES_COLLECTION:
                this.label = "Analyses";
                break;
            default:
                throw new IllegalArgumentException("There is no Solr domain type for " + type);

        }
    }

    private String label;
    private String value;

    public String getLabel() {

        return label;
    }

    public void setLabel(final String label) {

        this.label = label;
    }

    public String getValue() {

        return value;
    }

    public void setValue(final String value) {

        this.value = value;
    }
}