/*
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
 * Authors: Anton Gackovka
 * Created: February 26, 2018
 */

package com.odysseusinc.arachne.portal.model.solr;

import com.odysseusinc.arachne.portal.api.v1.dto.ArachneConsts;
import com.odysseusinc.arachne.portal.service.impl.breadcrumb.EntityType;
import java.util.stream.Stream;

public enum SolrCollection {
    DATA_SOURCES(ArachneConsts.Domains.DATA_SOURCES, "Data sources", EntityType.DATA_SOURCE),
    USERS(       ArachneConsts.Domains.USERS,        "Users",        EntityType.USER),
    ANALYSES(    ArachneConsts.Domains.ANALYISES,    "Analyses",     EntityType.ANALYSIS),
    STUDIES(     ArachneConsts.Domains.STUDIES,      "Studies",      EntityType.STUDY),
    PAPERS(      ArachneConsts.Domains.PAPERS,       "Papers",       EntityType.PAPER)
    ;

    private final String name;
    private final String title;
    private final EntityType breadcrumbType;

    SolrCollection(final String value, final String title, final EntityType breadcrumbType) {

        this.name = value;
        this.breadcrumbType = breadcrumbType;
        this.title = title;
    }

    public String getName() {

        return this.name;
    }

    public EntityType getBreadcrumbType() {

        return breadcrumbType;
    }

    public String getTitle() {

        return this.title;
    }

    public static SolrCollection getByCollectionName(final String collectionName) {

        return Stream.of(SolrCollection.values())
                .filter(v -> v.getName().equals(collectionName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Solr collection " + collectionName + " doesn't exist"));
    }

    public static String[] names() {

        return Stream.of(SolrCollection.values())
                .map(SolrCollection::getName)
                .toArray(String[]::new);
    }
}
