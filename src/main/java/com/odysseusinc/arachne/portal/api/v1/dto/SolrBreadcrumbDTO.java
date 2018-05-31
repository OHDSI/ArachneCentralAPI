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
 * Authors: Anton Gackovka
 * Created: February 20, 2018
 */

package com.odysseusinc.arachne.portal.api.v1.dto;

import com.odysseusinc.arachne.portal.service.impl.breadcrumb.BreadcrumbType;

public class SolrBreadcrumbDTO {

    private BreadcrumbType entityType;
    private Long id;
    private String title;

    public SolrBreadcrumbDTO(BreadcrumbType entityType, Long id, String title) {

        this.entityType = entityType;
        this.id = id;
        this.title = title;
    }

    public BreadcrumbType getEntityType() {

        return entityType;
    }

    public void setEntityType(BreadcrumbType entityType) {

        this.entityType = entityType;
    }

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public String getTitle() {

        return title;
    }

    public void setTitle(String title) {

        this.title = title;
    }
}
