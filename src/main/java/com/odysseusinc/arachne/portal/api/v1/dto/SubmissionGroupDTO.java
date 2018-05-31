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
 * Created: April 26, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
import java.util.Date;
import java.util.List;

public class SubmissionGroupDTO {
    private Long id;
    private Date created;
    private List<SubmissionDTO> submissions;
    private Integer queryFilesCount;
    private String checksum;
    private CommonAnalysisType analysisType;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public List<SubmissionDTO> getSubmissions() {

        return submissions;
    }

    public void setSubmissions(List<SubmissionDTO> submissions) {

        this.submissions = submissions;
    }

    public Date getCreated() {

        return created;
    }

    public void setCreated(Date created) {

        this.created = created;
    }

    public Integer getQueryFilesCount() {

        return queryFilesCount;
    }

    public void setQueryFilesCount(Integer queryFilesCount) {

        this.queryFilesCount = queryFilesCount;
    }

    public String getChecksum() {

        return checksum;
    }

    public void setChecksum(String checksum) {

        this.checksum = checksum;
    }

    public void setAnalysisType(CommonAnalysisType analysisType) {

        this.analysisType = analysisType;
    }

    public CommonAnalysisType getAnalysisType() {

        return analysisType;
    }
}
