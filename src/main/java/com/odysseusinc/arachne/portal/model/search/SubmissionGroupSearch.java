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
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva, Konstantin Yaroshovets
 * Created: February 26, 2018
 *
 */

package com.odysseusinc.arachne.portal.model.search;

import com.odysseusinc.arachne.portal.model.SubmissionStatus;
import java.util.Set;

public class SubmissionGroupSearch {
    private Long analysisId;
    private Set<Long> dataSourceIds;
    private Boolean hasInsight;
    private Set<SubmissionStatus> submissionStatuses;
    private Integer page = 1;
    private Integer pageSize = 10;
    private Boolean showHidden;

    public Long getAnalysisId() {

        return analysisId;
    }

    public void setAnalysisId(Long analysisId) {

        this.analysisId = analysisId;
    }

    public Set<Long> getDataSourceIds() {

        return dataSourceIds;
    }

    public void setDataSourceIds(Set<Long> dataSourceIds) {

        this.dataSourceIds = dataSourceIds;
    }

    public Boolean getHasInsight() {

        return hasInsight;
    }

    public void setHasInsight(Boolean hasInsight) {

        this.hasInsight = hasInsight;
    }

    public Set<SubmissionStatus> getSubmissionStatuses() {

        return submissionStatuses;
    }

    public void setSubmissionStatuses(Set<SubmissionStatus> submissionStatuses) {

        this.submissionStatuses = submissionStatuses;
    }

    public Integer getPage() {

        return page;
    }

    public void setPage(Integer page) {

        this.page = page;
    }

    public Integer getPageSize() {

        return pageSize;
    }

    public void setPageSize(Integer pagesize) {

        this.pageSize = pagesize;
    }

    public Boolean getShowHidden() {

        return showHidden;
    }

    public void setShowHidden(Boolean showHidden) {

        this.showHidden = showHidden;
    }
}
