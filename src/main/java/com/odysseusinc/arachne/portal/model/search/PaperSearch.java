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
 * Created: August 01, 2017
 *
 */

package com.odysseusinc.arachne.portal.model.search;

import com.odysseusinc.arachne.portal.model.PublishState;
import com.odysseusinc.arachne.portal.model.User;
import javax.validation.constraints.NotNull;

public class PaperSearch {

    private Integer page = 1;
    private Integer pageSize = Integer.MAX_VALUE;
    private String sortBy = "study.endDate";

    private String query;
    private PublishState publishState;
    private Boolean favourite;
    private boolean sortAsc = true;

    public Integer getPage() {

        return page;
    }

    public void setPage(Integer page) {

        this.page = page;
    }

    public String getQuery() {

        return query;
    }

    public void setQuery(String query) {

        this.query = query;
    }

    public void setPublishState(PublishState publishState) {

        this.publishState = publishState;
    }

    public void setFavourite(Boolean favourite) {

        this.favourite = favourite;
    }

    public boolean isSortAsc() {

        return sortAsc;
    }

    public void setSortAsc(boolean sortAsc) {

        this.sortAsc = sortAsc;
    }

    public PublishState getPublishState() {

        return publishState;
    }

    public Boolean getFavourite() {

        return favourite;
    }

    public String getSortBy() {

        return sortBy;
    }

    public void setSortBy(String sortBy) {

        this.sortBy = sortBy;
    }

    public boolean getSortAsc() {

        return sortAsc;
    }

    public void setSortAsc(Boolean sortAsc) {

        this.sortAsc = sortAsc;
    }

    public Integer getPageSize() {

        return pageSize;
    }

    public void setPageSize(Integer pageSize) {

        this.pageSize = pageSize;
    }
}
