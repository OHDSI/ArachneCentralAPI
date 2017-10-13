/**
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
 * Created: May 03, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto;

import java.util.Date;
import java.util.List;

public class SubmissionInsightDTO {

    private Date created;
    private String name;
    private String description;
    private List<Commentable> codeFiles;
    private List<Commentable> resultFiles;
    private DataSourceDTO dataSource;
    private AnalysisDTO analysis;
    private SubmissionDTO submission;
    private List<Commentable> recentCommentEntities;
    private Long commentsCount;

    public Date getCreated() {

        return created;
    }

    public void setCreated(Date created) {

        this.created = created;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public List<Commentable> getCodeFiles() {

        return codeFiles;
    }

    public void setCodeFiles(List<Commentable> codeFiles) {

        this.codeFiles = codeFiles;
    }

    public List<Commentable> getResultFiles() {

        return resultFiles;
    }

    public void setResultFiles(List<Commentable> resultFiles) {

        this.resultFiles = resultFiles;
    }

    public DataSourceDTO getDataSource() {

        return dataSource;
    }

    public void setDataSource(DataSourceDTO dataSource) {

        this.dataSource = dataSource;
    }

    public AnalysisDTO getAnalysis() {

        return analysis;
    }

    public void setAnalysis(AnalysisDTO analysis) {

        this.analysis = analysis;
    }

    public SubmissionDTO getSubmission() {

        return submission;
    }

    public void setSubmission(SubmissionDTO submission) {

        this.submission = submission;
    }

    public List<Commentable> getRecentCommentEntities() {

        return recentCommentEntities;
    }

    public void setRecentCommentEntities(List<Commentable> recentCommentEntities) {

        this.recentCommentEntities = recentCommentEntities;
    }

    public Long getCommentsCount() {

        return commentsCount;
    }

    public void setCommentsCount(Long commentsCount) {

        this.commentsCount = commentsCount;
    }
}
