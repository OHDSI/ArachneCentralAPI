package com.odysseusinc.arachne.portal.service.submission.impl;

import com.odysseusinc.arachne.portal.repository.SubmissionInsightRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionInsightSubmissionFileRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionResultFileRepository;
import com.odysseusinc.arachne.portal.service.CommentService;
import com.odysseusinc.arachne.portal.service.submission.SubmissionService;
import org.springframework.stereotype.Service;

@Service
public class SubmissionInsightServiceImpl extends BaseSubmissionInsightServiceImpl {
    public SubmissionInsightServiceImpl(SubmissionInsightRepository submissionInsightRepository, SubmissionInsightSubmissionFileRepository submissionInsightSubmissionFileRepository, CommentService commentService, SubmissionResultFileRepository submissionResultFileRepository, SubmissionService submissionService) {

        super(submissionInsightRepository, submissionInsightSubmissionFileRepository, commentService, submissionResultFileRepository, submissionService);
    }
}
