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
 * Created: October 03, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.impl;

import com.odysseusinc.arachne.portal.model.AbstractUserStudyListItem;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.Comment;
import com.odysseusinc.arachne.portal.model.CommentTopic;
import com.odysseusinc.arachne.portal.model.DataSource;
import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.model.Paper;
import com.odysseusinc.arachne.portal.model.ResultFile;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.model.SubmissionGroup;
import com.odysseusinc.arachne.portal.model.SubmissionInsight;
import com.odysseusinc.arachne.portal.model.SubmissionInsightSubmissionFile;
import com.odysseusinc.arachne.portal.model.search.PaperSearch;
import com.odysseusinc.arachne.portal.model.search.StudySearch;
import com.odysseusinc.arachne.portal.service.BaseAdminService;
import com.odysseusinc.arachne.portal.service.BasePaperService;
import com.odysseusinc.arachne.portal.service.BaseStudyService;
import com.odysseusinc.arachne.portal.service.CommentService;
import com.odysseusinc.arachne.portal.service.submission.SubmissionInsightService;
import com.odysseusinc.arachne.portal.service.analysis.BaseAnalysisService;
import com.odysseusinc.arachne.portal.service.submission.BaseSubmissionService;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.CollectionUtils;

public abstract class BaseAdminServiceImpl<
        S extends Study,
        DS extends IDataSource,
        SS extends StudySearch,
        SU extends AbstractUserStudyListItem,
        A extends Analysis,
        P extends Paper,
        PS extends PaperSearch,
        SB extends Submission> implements BaseAdminService<S, DS, SS, SU, A, P, PS, SB> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseAdminServiceImpl.class);

    private final BaseStudyService<S, DS, SS, SU> studyService;
    private final BaseAnalysisService<A> analysisService;
    private final BasePaperService<P, PS, S, DS, SS, SU> paperService;
    private final BaseSubmissionService<SB, A> submissionService;
    private final CommentService commentService;
    private final SubmissionInsightService submissionInsightService;

    @Autowired
    public BaseAdminServiceImpl(BaseStudyService<S, DS, SS, SU> studyService,
                                BaseAnalysisService<A> analysisService,
                                BasePaperService<P, PS, S, DS, SS, SU> paperService,
                                BaseSubmissionService<SB, A> submissionService,
                                CommentService commentService,
                                SubmissionInsightService submissionInsightService) {

        this.studyService = studyService;
        this.analysisService = analysisService;
        this.paperService = paperService;
        this.submissionService = submissionService;
        this.commentService = commentService;
        this.submissionInsightService = submissionInsightService;
    }

    /**
     * Deletes all information related to the given studies: studies folders, analyses files,
     * submission files, submission result files, paper files, ... .
     */
    @Override
    @Secured("ROLE_ADMIN")
    public boolean cascadeDeleteStudiesByIds(List<Long> studyIds) {

        if (CollectionUtils.isEmpty(studyIds)) {
            return Boolean.FALSE;
        }

        LOGGER.info("Attempt to deleteComment studies: {}", studyIds);

        List<S> studies = studyService.getByIds(studyIds);

        List<A> analyses = getAnalysesFromStudies(studies);

        deleteSubmissions(analyses);

        deleteSubmissionGroups(analyses);

        deleteAnalyses(analyses);

        deletePapers(studies);

        beforeStudiesDelete(studies);

        deleteStudies(studies);

        LOGGER.info("Studies with these ids : {} were deleted", studyIds);

        return Boolean.TRUE;
    }

    protected List<A> getAnalysesFromStudies(List<S> studies) {

        return studies.stream()
                    .filter(Objects::nonNull)
                    .flatMap(s -> s.getAnalyses().stream())
                    .map(v -> (A) v)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
    }

    protected void beforeStudiesDelete(List<S> studies) {
    }

    private void deleteSubmissionGroups(List<A> analyses) {

        List<SubmissionGroup> groups = analyses.stream()
                .filter(Objects::nonNull)
                .flatMap(a -> a.getSubmissionGroups().stream())
                .filter(Objects::nonNull)
                .peek(submissionGroup -> submissionGroup.getFiles().forEach(analysisService::deleteSubmissionFile))
                .collect(Collectors.toList());

        submissionService.deleteSubmissionGroups(groups);
    }

    private void deleteStudies(List<S> studies) {

        List<S> filteredStudies = studies.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        studyService.fullDelete(filteredStudies);
    }

    private void deletePapers(List<S> studies) {

        List<P> papers = studies.stream()
                .filter(Objects::nonNull)
                .map(s -> (P) s.getPaper())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        paperService.fullDelete(papers);
    }

    private void deleteAnalyses(List<A> analyses) {

        List<A> filteredAnalyses = analyses.stream().filter(Objects::nonNull).collect(Collectors.toList());
        analysisService.fullDelete(filteredAnalyses);
    }

    private void deleteSubmissions(List<A> analyses) {

        List<SB> submissions = analyses.stream()
                .filter(Objects::nonNull)
                .flatMap(a -> a.getSubmissions().stream())
                .map(submission -> (SB) submission)
                .filter(Objects::nonNull)
                .peek(submission -> {
                    deleteSubmissionStatusHistory(submission);
                    deleteSubmissionInsight(submission);
                    deleteSubmissionResultFiles(submission);
                })
                .collect(Collectors.toList());
        submissionService.deleteSubmissions(submissions);
    }

    private void deleteSubmissionStatusHistory(SB submission) {

        submissionService.deleteSubmissionStatusHistory(submission.getStatusHistory());
    }

    private void deleteSubmissionResultFiles(SB submission) {

        for (ResultFile resultFile : submission.getResultFiles()) {
            submissionService.deleteSubmissionResultFile(resultFile);
        }
    }

    private void deleteSubmissionInsight(SB submission) {

        SubmissionInsight insight = submission.getSubmissionInsight();

        if (insight == null) {
            return;
        }

        List<SubmissionInsightSubmissionFile> submissionInsightSubmissionFiles = insight.getSubmissionInsightSubmissionFiles();
        for (SubmissionInsightSubmissionFile link : submissionInsightSubmissionFiles) {

            CommentTopic topic = link.getCommentTopic();

            List<Comment> comments = topic.getComments();
            commentService.deleteComments(comments);
            commentService.deleteTopic(topic);
        }

        submissionInsightService.deleteSubmissionInsightSubmissionFileLinks(submissionInsightSubmissionFiles);

        submissionInsightService.tryDeleteSubmissionInsight(insight.getId());
    }
}
