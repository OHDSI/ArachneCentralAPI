package com.odysseusinc.arachne.portal.service.submission;

import com.odysseusinc.arachne.portal.exception.AlreadyExistException;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.model.CommentTopic;
import com.odysseusinc.arachne.portal.model.SubmissionInsight;
import com.odysseusinc.arachne.portal.model.SubmissionInsightSubmissionFile;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface SubmissionInsightService {

    SubmissionInsight getSubmissionInsight(Long submissionId) throws NotExistException;

    Set<CommentTopic> getInsightComments(SubmissionInsight insight, Integer size, Sort sort);

    SubmissionInsight createSubmissionInsight(Long submissionId, SubmissionInsight insight)
            throws AlreadyExistException, NotExistException;

    void deleteSubmissionInsightSubmissionFileLinks(List<SubmissionInsightSubmissionFile> links);

    SubmissionInsight updateSubmissionInsight(Long submissionId, SubmissionInsight insight);

    Page<SubmissionInsight> getInsightsByStudyId(Long studyId, Pageable pageable);

    void deleteSubmissionInsight(Long submissionId) throws NotExistException;

    void tryDeleteSubmissionInsight(Long submissionInsightId);
}
