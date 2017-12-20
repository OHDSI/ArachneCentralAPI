/*
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
 * Created: June 13, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.impl;

import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.service.BreadcrumbService;
import com.odysseusinc.arachne.portal.service.StudyService;
import com.odysseusinc.arachne.portal.service.SubmissionInsightService;
import com.odysseusinc.arachne.portal.service.analysis.BaseAnalysisService;
import com.odysseusinc.arachne.portal.service.impl.breadcrumb.Breadcrumb;
import com.odysseusinc.arachne.portal.service.impl.breadcrumb.BreadcrumbType;
import com.odysseusinc.arachne.portal.service.submission.SubmissionService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BreadcrumbServiceImpl implements BreadcrumbService {

    private StudyService studyService;
    private BaseAnalysisService<Analysis> analysisService;
    private final SubmissionService submissionService;
    private final SubmissionInsightService submissionInsightService;

    @Autowired
    public BreadcrumbServiceImpl(StudyService studyService,
                                 BaseAnalysisService<Analysis> analysisService,
                                 SubmissionService submissionService,
                                 SubmissionInsightService submissionInsightService) {

        this.studyService = studyService;
        this.analysisService = analysisService;
        this.submissionService = submissionService;
        this.submissionInsightService = submissionInsightService;
    }

    // Entry point which ensures that we verify permissions
    private Breadcrumb getBreadcrumbByTypeAndId(BreadcrumbType type, Long id) throws NotExistException {

        switch (type) {
            case STUDY:
                return studyService.getById(id);
            case ANALYSIS:
                return analysisService.getById(id);
            case SUBMISSION_GROUP:
                return submissionService.getSubmissionGroupById(id);
            case SUBMISSION:
                return submissionService.getSubmissionById(id);
            case INSIGHT:
                return submissionInsightService.getSubmissionInsight(id);
        }
        return null;
    }

    public List<Breadcrumb> getBreadcrumbs(BreadcrumbType type, Long id) throws NotExistException {

        List<Breadcrumb> breadcrumbList = new ArrayList<>();
        Breadcrumb breadcrumb = getBreadcrumbByTypeAndId(type, id);
        breadcrumbList.add(breadcrumb);

        while (breadcrumb.getCrumbParent() != null) {
            breadcrumb = breadcrumb.getCrumbParent();
            breadcrumbList.add(0, breadcrumb);
        }

        return breadcrumbList;
    }

}
