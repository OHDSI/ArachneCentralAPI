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
 * Created: September 18, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.submission.impl;

import static com.odysseusinc.arachne.portal.model.SubmissionStatus.FAILED;

import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.model.SubmissionStatus;
import com.odysseusinc.arachne.portal.repository.submission.BaseSubmissionRepository;
import com.odysseusinc.arachne.portal.service.analysis.BaseAnalysisService;
import com.odysseusinc.arachne.portal.service.submission.BaseSubmissionInvalidateService;
import com.odysseusinc.arachne.portal.service.submission.BaseSubmissionService;
import com.odysseusinc.arachne.portal.service.submission.SubmissionInvalidateService;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

public abstract class BaseSubmissionInvalidateServiceImpl<T extends Submission, A extends Analysis>
        implements BaseSubmissionInvalidateService<T> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(SubmissionInvalidateService.class);
    protected static final Collection<String> STATUSES = new ArrayList<String>() {
        {
            this.add(SubmissionStatus.STARTING.name());
            this.add(SubmissionStatus.IN_PROGRESS.name());
        }
    };
    protected final BaseSubmissionRepository<T> submissionRepository;
    protected final BaseAnalysisService<A> analysisService;
    protected final BaseSubmissionService<T, A> submissionService;
    @Value("${submission.timeout.days}")
    private int timeoutDays = 3;

    public BaseSubmissionInvalidateServiceImpl(BaseSubmissionRepository<T> submissionRepository,
                                               BaseAnalysisService<A> analysisService,
                                               BaseSubmissionService<T, A> submissionService) {

        this.submissionRepository = submissionRepository;
        this.analysisService = analysisService;
        this.submissionService = submissionService;
    }

    @Override
    @Scheduled(cron = "${submission.invalidate.cron}")
    public void invalidateSubmission() {

        LOGGER.info("Invalidating submissions");

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -getTimeoutDays());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date expirationDate = calendar.getTime();

        List<T> submissions = submissionRepository.findByCreatedBeforeAndStatusIn(expirationDate,
                getStatuses());
        submissions.forEach(getSubmissionInvalidationRule());
        LOGGER.info("Invalidation finished, failed {} submissions", submissions.size());
    }

    protected Collection<String> getStatuses() {

        return STATUSES;
    }

    protected int getTimeoutDays() {

        return timeoutDays;
    }

    protected Consumer<T> getSubmissionInvalidationRule() {

        return s -> {
            submissionService.moveSubmissionToNewStatus(s, FAILED, null, null);
        };
    }
}
