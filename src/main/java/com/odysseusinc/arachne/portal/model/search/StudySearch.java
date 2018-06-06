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
 * Created: November 30, 2016
 *
 */

package com.odysseusinc.arachne.portal.model.search;

import com.odysseusinc.arachne.portal.model.StudyKind;

public class StudySearch {
    private String query;
    private Long userId;
    private Long[] type;
    private Long[] status;
    private Boolean favourite;
    private String sortBy;
    private Boolean sortAsc;
    private Integer page;
    private Integer pagesize;
    private Boolean my = Boolean.FALSE;
    private Boolean privacy;
    private StudyKind kind;

    public String getSortBy() {

        return sortBy;
    }

    public void setSortBy(String sortBy) {

        this.sortBy = sortBy;
    }

    public Boolean getSortAsc() {

        return sortAsc;
    }

    public void setSortAsc(Boolean sortAsc) {

        this.sortAsc = sortAsc;
    }

    public Integer getPage() {

        return page;
    }

    public void setPage(Integer page) {

        this.page = page;
    }

    public Integer getPagesize() {

        return pagesize;
    }

    public void setPagesize(Integer pagesize) {

        this.pagesize = pagesize;
    }

    public String getQuery() {

        return query;
    }

    public void setQuery(String query) {

        this.query = query;
    }

    public Long[] getType() {

        return type;
    }

    public void setType(Long[] type) {

        this.type = type;
    }

    public Long[] getStatus() {

        return status;
    }

    public void setStatus(Long[] status) {

        this.status = status;
    }

    public Boolean getFavourite() {

        return favourite;
    }

    public void setFavourite(Boolean favourite) {

        this.favourite = favourite;
    }

    public Long getUserId() {

        return userId;
    }

    public void setUserId(Long userId) {

        this.userId = userId;
    }

    public Boolean getMy() {

        return my;
    }

    public void setMy(Boolean my) {

        this.my = my;
    }

    public Boolean getPrivacy() {

        return privacy;
    }

    public void setPrivacy(Boolean privacy) {

        this.privacy = privacy;
    }

    public StudyKind getKind() {

        return kind;
    }

    public void setKind(StudyKind kind) {

        this.kind = kind;
    }
}
