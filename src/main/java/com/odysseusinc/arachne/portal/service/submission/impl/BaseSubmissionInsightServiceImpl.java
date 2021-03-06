package com.odysseusinc.arachne.portal.service.submission.impl;

import static com.odysseusinc.arachne.portal.model.SubmissionStatus.EXECUTED_PUBLISHED;
import static com.odysseusinc.arachne.portal.model.SubmissionStatus.FAILED_PUBLISHED;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphUtils;
import com.odysseusinc.arachne.portal.exception.AlreadyExistException;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.model.CommentTopic;
import com.odysseusinc.arachne.portal.model.ResultFile;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.model.SubmissionInsight;
import com.odysseusinc.arachne.portal.model.SubmissionInsightSubmissionFile;
import com.odysseusinc.arachne.portal.model.SubmissionStatus;
import com.odysseusinc.arachne.portal.repository.SubmissionInsightRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionInsightSubmissionFileRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionResultFileRepository;
import com.odysseusinc.arachne.portal.service.CommentService;
import com.odysseusinc.arachne.portal.service.submission.SubmissionInsightService;
import com.odysseusinc.arachne.portal.service.submission.SubmissionService;
import com.odysseusinc.arachne.portal.util.EntityUtils;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

public abstract class BaseSubmissionInsightServiceImpl implements SubmissionInsightService {

    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseSubmissionInsightServiceImpl.class);

    private static final String CREATING_INSIGHT_LOG = "Creating Insight for Submission with id='{}'";
    private static final String UPDATING_INSIGHT_LOG = "Updating Insight for Submission with id='{}'";
    private static final String INSIGHT_NOT_EXIST_EXCEPTION = "Insight for Submission with id='%s' does not exist";
    private static final String INSIGHT_ALREADY_EXISTS_EXCEPTION = "Insight for Submission with id='%s' already exists";
    private static final String SUBMISSION_NOT_EXIST_EXCEPTION = "Submission with id='%s' does not exist";
    public static final String DELETING_INSIGHT_LOG = "Deleting Insight for Submission with id='{}'";

    protected final SubmissionInsightRepository submissionInsightRepository;
    protected final SubmissionInsightSubmissionFileRepository submissionInsightSubmissionFileRepository;
    protected final CommentService commentService;
    protected final SubmissionResultFileRepository submissionResultFileRepository;
    protected final SubmissionService submissionService;

    @Autowired
    public BaseSubmissionInsightServiceImpl(
            SubmissionInsightRepository submissionInsightRepository,
            SubmissionInsightSubmissionFileRepository submissionInsightSubmissionFileRepository,
            CommentService commentService,
            SubmissionResultFileRepository submissionResultFileRepository,
            SubmissionService submissionService) {

        this.submissionInsightRepository = submissionInsightRepository;
        this.submissionInsightSubmissionFileRepository = submissionInsightSubmissionFileRepository;
        this.commentService = commentService;
        this.submissionResultFileRepository = submissionResultFileRepository;
        this.submissionService = submissionService;
    }

    @Override
    @PostAuthorize("@ArachnePermissionEvaluator.addPermissions(principal, returnObject )")
    public SubmissionInsight getSubmissionInsight(Long submissionId) throws NotExistException {

        final SubmissionInsight insight = submissionInsightRepository.findOneBySubmissionId(submissionId);
        throwNotExistExceptionIfNull(insight, submissionId);
        return insight;
    }

    @Override
    public Set<CommentTopic> getInsightComments(SubmissionInsight insight, Integer size, Sort sort) {

        final Set<CommentTopic> topics = extractCommentTopics(insight);
        return commentService.list(topics, size, sort);
    }

    @Override
    @PreAuthorize("hasPermission(#submissionId,  'Submission', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).EDIT_ANALYSIS)")
    public SubmissionInsight createSubmissionInsight(Long submissionId, SubmissionInsight insight)
            throws AlreadyExistException, NotExistException {

        LOGGER.info(CREATING_INSIGHT_LOG, submissionId);
        final SubmissionInsight submissionInsight = submissionInsightRepository.findOneBySubmissionId(submissionId);
        if (submissionInsight != null) {
            final String message = String.format(INSIGHT_ALREADY_EXISTS_EXCEPTION, submissionId);
            throw new AlreadyExistException(message);
        }
        final List<SubmissionStatus> allowedStatuses = Arrays.asList(EXECUTED_PUBLISHED, FAILED_PUBLISHED);
        final Submission submission = submissionService.getSubmissionByIdAndStatus(submissionId, allowedStatuses);
        throwNotExistExceptionIfNull(submission, submissionId);
        insight.setId(null);
        insight.setCreated(new Date());
        insight.setSubmission(submission);
        final SubmissionInsight savedInsight = submissionInsightRepository.save(insight);
        final List<SubmissionInsightSubmissionFile> submissionInsightSubmissionFiles
                = submission.getSubmissionGroup().getFiles()
                .stream()
                .map(sf -> new SubmissionInsightSubmissionFile(savedInsight, sf, new CommentTopic()))
                .collect(Collectors.toList());
        submissionInsightSubmissionFileRepository.saveAll(submissionInsightSubmissionFiles);
        final List<ResultFile> resultFiles = submission.getResultFiles();
        resultFiles.forEach(resultFile -> resultFile.setCommentTopic(new CommentTopic()));
        submissionResultFileRepository.saveAll(resultFiles);
        return savedInsight;
    }

    @Override
    public void deleteSubmissionInsightSubmissionFileLinks(List<SubmissionInsightSubmissionFile> links) {

        submissionInsightSubmissionFileRepository.deleteAll(links);
    }

    @Override
    @PreAuthorize("hasPermission(#submissionId, 'Submission', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).EDIT_INSIGHT)")
    public SubmissionInsight updateSubmissionInsight(Long submissionId, SubmissionInsight insight)
            throws NotExistException {

        LOGGER.info(UPDATING_INSIGHT_LOG, submissionId);
        final SubmissionInsight exist = submissionInsightRepository.findOneBySubmissionId(submissionId);
        throwNotExistExceptionIfNull(exist, submissionId);
        if (insight.getName() != null) {
            exist.setName(insight.getName());
        }
        if (insight.getDescription() != null) {
            exist.setDescription(insight.getDescription());
        }
        return submissionInsightRepository.save(exist);
    }

    @Override
    public Page<SubmissionInsight> getInsightsByStudyId(Long studyId, Pageable pageable) {

        final Page<SubmissionInsight> page = submissionInsightRepository.findAllWithCommentsOrDescIsNotEmpty(studyId, pageable);
        final List<SubmissionInsight> insights = page.getContent();
        if (!insights.isEmpty()) {
            final List<Long> ids = insights.stream().map(SubmissionInsight::getId).collect(Collectors.toList());
            final Map<Long, Long> counts = submissionInsightRepository.countCommentsByTopicIds(ids)
                    .stream()
                    .collect(Collectors.toMap(o -> ((BigInteger) o[0]).longValue(), o -> ((BigInteger) o[1]).longValue()));
            insights.forEach(insight -> insight.setCommentsCount(counts.getOrDefault(insight.getId(), 0L)));
        }
        return page;
    }

    @Override
    @PreAuthorize("hasPermission(#submissionId,  'Submission', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).EDIT_ANALYSIS)")
    public void deleteSubmissionInsight(Long submissionId) throws NotExistException {

        LOGGER.info(DELETING_INSIGHT_LOG, submissionId);
        final Submission submission = submissionService.getSubmissionById(
                submissionId,
                EntityUtils.fromAttributePaths("submissionInsight", "resultFiles")
        );
        throwNotExistExceptionIfNull(submission, submissionId);
        throwNotExistExceptionIfNull(submission.getSubmissionInsight(), submissionId);
        final List<ResultFile> resultFiles = submission.getResultFiles();
        resultFiles.forEach(resultFile -> resultFile.setCommentTopic(null));
        submissionResultFileRepository.saveAll(resultFiles);
        submissionInsightRepository.deleteBySubmissionId(submissionId);
    }

    @Override
    public void tryDeleteSubmissionInsight(Long submissionInsightId) {

        submissionInsightRepository.deleteById(submissionInsightId);
    }

    private void throwNotExistExceptionIfNull(Submission submission, Long submissionId) throws NotExistException {

        if (submission == null) {
            String message = String.format(SUBMISSION_NOT_EXIST_EXCEPTION, submissionId);
            throw new NotExistException(message, Submission.class);
        }
    }

    private void throwNotExistExceptionIfNull(SubmissionInsight submissionInsight, Long submissionId) throws NotExistException {

        if (submissionInsight == null) {
            final String message = String.format(INSIGHT_NOT_EXIST_EXCEPTION, submissionId);
            throw new NotExistException(message, SubmissionInsight.class);
        }
    }

    private Set<CommentTopic> extractCommentTopics(SubmissionInsight insight) {

        final Stream<CommentTopic> submissionFilesTopics = insight.getSubmissionInsightSubmissionFiles()
                .stream()
                .map(SubmissionInsightSubmissionFile::getCommentTopic);
        final Submission submission = insight.getSubmission();
        final Stream<CommentTopic> resultFileTopics = submission.getResultFiles()
                .stream()
                .map(ResultFile::getCommentTopic);
        return Stream.concat(submissionFilesTopics, resultFileTopics).map(commentTopic -> {
            commentTopic.setComments(new LinkedList<>());
            return commentTopic;
        }).collect(Collectors.toSet());
    }
}
