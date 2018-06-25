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
 * Created: February 11, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.impl;

import com.odysseusinc.arachne.portal.model.DataSource;
import com.odysseusinc.arachne.portal.model.Paper;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.repository.AnalysisRepository;
import com.odysseusinc.arachne.portal.repository.DataNodeRepository;
import com.odysseusinc.arachne.portal.repository.DataNodeUserRepository;
import com.odysseusinc.arachne.portal.repository.ResultFileRepository;
import com.odysseusinc.arachne.portal.repository.StudyRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionInsightSubmissionFileRepository;
import com.odysseusinc.arachne.portal.repository.TenantRepository;
import com.odysseusinc.arachne.portal.repository.UserRepository;
import com.odysseusinc.arachne.portal.repository.UserStudyExtendedRepository;
import com.odysseusinc.arachne.portal.repository.UserStudyGroupedRepository;
import com.odysseusinc.arachne.portal.repository.submission.SubmissionRepository;
import com.odysseusinc.arachne.portal.service.ArachneSecureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component()
@Transactional(rollbackFor = Exception.class)
public class ArachneSecureServiceImpl extends BaseArachneSecureServiceImpl<Paper, DataSource>
        implements ArachneSecureService {

    @Autowired
    public ArachneSecureServiceImpl(UserStudyGroupedRepository userStudyGroupedRepository,
                                    AnalysisRepository analysisRepository,
                                    SubmissionRepository submissionRepository,
                                    DataNodeRepository dataNodeRepository,
                                    DataNodeUserRepository dataNodeUserRepository,
                                    UserStudyExtendedRepository userStudyExtendedRepository,
                                    SubmissionInsightSubmissionFileRepository submissionInsightSubmissionFileRepository,
                                    ResultFileRepository resultFileRepository,
                                    TenantRepository tenantRepository,
                                    StudyRepository studyRepository,
                                    UserRepository userRepository
    ) {

        super(userStudyGroupedRepository,
                analysisRepository,
                submissionRepository,
                dataNodeRepository,
                dataNodeUserRepository,
                userStudyExtendedRepository,
                submissionInsightSubmissionFileRepository,
                resultFileRepository,
                tenantRepository,
                studyRepository,
                userRepository);
    }

    @Override
    public Study getStudyByIdInAnyTenant(final Long studyId) {

        return studyRepository.findByIdInAnyTenant(studyId);
    }
}
