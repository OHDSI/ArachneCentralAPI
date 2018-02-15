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
 * Created: February 15, 2018
 */

package com.odysseusinc.arachne.portal.api.v1.dto;

import com.odysseusinc.arachne.portal.service.BaseSolrService;

public class GlobalSearchDomainDTO {

    public GlobalSearchDomainDTO(final String type) {

        this.value = type;

        switch(type) {
            case BaseSolrService.USER_COLLECTION:
                this.module = ArachneConsts.Modules.EXPERT_FINDER;
                this.label = "Users";
                break;
            case BaseSolrService.STUDIES_COLLECTION:
                this.module = ArachneConsts.Modules.STUDY_NOTEBOOK;
                this.label = "Studies";
                break;
            case BaseSolrService.DATA_SOURCE_COLLECTION:
                this.module = ArachneConsts.Modules.DATA_CATALOG;
                this.label = "Data Sources";
                break;
            default:
                throw new IllegalArgumentException("There is not Solr domain type for " + type);

        }
    }

    private String label;
    private String value;
    private String module;

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

    public String getModule() {

        return module;
    }

    public void setModule(final String module) {

        this.module = module;
    }
}