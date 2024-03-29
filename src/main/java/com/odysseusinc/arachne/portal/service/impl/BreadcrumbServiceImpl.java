/*
 *
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
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva, Mikhail Mironov
 * Created: June 13, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.impl;

import com.odysseusinc.arachne.commons.config.ArachneConfiguration;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.Paper;
import com.odysseusinc.arachne.portal.model.RawDataSource;
import com.odysseusinc.arachne.portal.modules.ModuleHelper;
import com.odysseusinc.arachne.portal.repository.AnalysisRepository;
import com.odysseusinc.arachne.portal.repository.BaseRawDataSourceRepository;
import com.odysseusinc.arachne.portal.repository.BaseRawUserRepository;
import com.odysseusinc.arachne.portal.repository.PaperRepository;
import com.odysseusinc.arachne.portal.repository.StudyRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionGroupRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionInsightRepository;
import com.odysseusinc.arachne.portal.repository.submission.SubmissionRepository;
import com.odysseusinc.arachne.portal.service.BreadcrumbService;
import com.odysseusinc.arachne.portal.service.impl.breadcrumb.Breadcrumb;
import com.odysseusinc.arachne.portal.service.impl.breadcrumb.EntityType;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BreadcrumbServiceImpl implements BreadcrumbService {

    //TODO repositories are used here to avoid cycles in deps, should be fixed later ofc
    private final StudyRepository studyRepository;
    private final AnalysisRepository analysisRepository;
    private final SubmissionRepository submissionRepository;
    private final SubmissionGroupRepository submissionGroupRepository;
    private final SubmissionInsightRepository submissionInsightRepository;
    private final BaseRawUserRepository<IUser> userRepository;
    private final BaseRawDataSourceRepository<RawDataSource> dataSourceRepository;
    private final PaperRepository<Paper> paperRepository;
    private final ArachneConfiguration arachneConfiguration;

    @Autowired
    public BreadcrumbServiceImpl(final StudyRepository studyService,
                                 final AnalysisRepository analysisRepository,
                                 final SubmissionRepository submissionService,
                                 final SubmissionInsightRepository submissionInsightRepository,
                                 final SubmissionGroupRepository submissionGroupRepository,
                                 final BaseRawUserRepository userRepository,
                                 final BaseRawDataSourceRepository dataSourceRepository,
                                 final PaperRepository<Paper> paperRepository,
                                 final ArachneConfiguration arachneConfiguration) {

        this.studyRepository = studyService;
        this.analysisRepository = analysisRepository;
        this.submissionRepository = submissionService;
        this.submissionInsightRepository = submissionInsightRepository;
        this.submissionGroupRepository = submissionGroupRepository;
        this.userRepository = userRepository;
        this.dataSourceRepository = dataSourceRepository;
        this.paperRepository = paperRepository;
        this.arachneConfiguration = arachneConfiguration;
    }

    // Entry point which ensures that we verify permissions
    private Breadcrumb getBreadcrumbByTypeAndId(EntityType type, Long id) throws NotExistException {

        switch (type) {
            case STUDY:
                return studyRepository.getOne(id);
            case ANALYSIS:
                return analysisRepository.getOne(id);
            case SUBMISSION_GROUP:
                return submissionGroupRepository.getOne(id);
            case SUBMISSION:
                return submissionRepository.getOne(id);
            case INSIGHT:
                if (arachneConfiguration.isModuleDisabled(ModuleHelper.INSIGHT)) {
                    return null;
                }
                return submissionInsightRepository.findOneBySubmissionId(id);
            case USER:
                return userRepository.getOne(id);
            case DATA_SOURCE:
                return dataSourceRepository.getOne(id);
            case PAPER:
                if (arachneConfiguration.isModuleDisabled(ModuleHelper.INSIGHT)) {
                    return null;
                }
                return paperRepository.getOne(id);
        }
        return null;
    }

    @Override
    public List<Breadcrumb> getBreadcrumbs(final EntityType type, final Long id) throws NotExistException {

        final Breadcrumb breadcrumb = getBreadcrumbByTypeAndId(type, id);
        return getBreadcrumbs(breadcrumb);
    }

    @Override
    public List<Breadcrumb> getBreadcrumbs(Breadcrumb breadcrumb) throws NotExistException {

        final List<Breadcrumb> breadcrumbList = new ArrayList<>();
        breadcrumbList.add(breadcrumb);

        while (breadcrumb.getCrumbParent() != null) {
            breadcrumb = breadcrumb.getCrumbParent();
            breadcrumbList.add(0, breadcrumb);
        }

        return breadcrumbList;
    }

}
