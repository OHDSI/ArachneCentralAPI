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
 * Created: October 03, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.impl;

import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.CommentTopic;
import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.model.DataNodeRole;
import com.odysseusinc.arachne.portal.model.DataSource;
import com.odysseusinc.arachne.portal.model.Organization;
import com.odysseusinc.arachne.portal.model.Paper;
import com.odysseusinc.arachne.portal.model.ParticipantRole;
import com.odysseusinc.arachne.portal.model.ParticipantStatus;
import com.odysseusinc.arachne.portal.model.ResultFile;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.model.SubmissionGroup;
import com.odysseusinc.arachne.portal.model.SubmissionInsight;
import com.odysseusinc.arachne.portal.model.SubmissionInsightSubmissionFile;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.model.UserStudyExtended;
import com.odysseusinc.arachne.portal.model.security.ArachneUser;
import com.odysseusinc.arachne.portal.repository.AnalysisRepository;
import com.odysseusinc.arachne.portal.repository.DataNodeRepository;
import com.odysseusinc.arachne.portal.repository.DataNodeUserRepository;
import com.odysseusinc.arachne.portal.repository.ResultFileRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionInsightSubmissionFileRepository;
import com.odysseusinc.arachne.portal.repository.UserRepository;
import com.odysseusinc.arachne.portal.repository.UserStudyExtendedRepository;
import com.odysseusinc.arachne.portal.repository.UserStudyGroupedRepository;
import com.odysseusinc.arachne.portal.repository.submission.SubmissionRepository;
import com.odysseusinc.arachne.portal.util.DataNodeUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

public abstract class BaseArachneSecureServiceImpl<P extends Paper, DS extends DataSource> implements com.odysseusinc.arachne.portal.service.BaseArachneSecureService<P, DS> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(com.odysseusinc.arachne.portal.service.BaseArachneSecureService.class);

    @Value("${portal.organizations.enableCreateByNonAdmin}")
    private Boolean canUserCreateOrganization;

    protected final UserStudyGroupedRepository userStudyGroupedRepository;
    protected final AnalysisRepository analysisRepository;
    protected final SubmissionRepository submissionRepository;
    protected final DataNodeRepository<DataNode> dataNodeRepository;
    protected final DataNodeUserRepository dataNodeUserRepository;
    protected final UserStudyExtendedRepository userStudyExtendedRepository;
    protected final SubmissionInsightSubmissionFileRepository submissionInsightSubmissionFileRepository;
    protected final ResultFileRepository resultFileRepository;
    protected final UserRepository userRepository;

    @Autowired
    public BaseArachneSecureServiceImpl(UserStudyGroupedRepository userStudyGroupedRepository,
                                        AnalysisRepository analysisRepository,
                                        SubmissionRepository submissionRepository,
                                        DataNodeRepository dataNodeRepository,
                                        DataNodeUserRepository dataNodeUserRepository,
                                        UserStudyExtendedRepository userStudyExtendedRepository,
                                        SubmissionInsightSubmissionFileRepository submissionInsightSubmissionFileRepository,
                                        ResultFileRepository resultFileRepository,
                                        UserRepository userRepository) {

        this.userStudyGroupedRepository = userStudyGroupedRepository;
        this.analysisRepository = analysisRepository;
        this.submissionRepository = submissionRepository;
        this.dataNodeRepository = dataNodeRepository;
        this.dataNodeUserRepository = dataNodeUserRepository;
        this.userStudyExtendedRepository = userStudyExtendedRepository;
        this.submissionInsightSubmissionFileRepository = submissionInsightSubmissionFileRepository;
        this.resultFileRepository = resultFileRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipantRole> getRolesByStudy(ArachneUser user, Study study) {

        return study != null ? getParticipantRoles(user.getId(), study) : new LinkedList<>();
    }

    @Override
    public List<ParticipantRole> getRolesByAnalysis(ArachneUser user, Analysis analysis) {

        List<ParticipantRole> result = new LinkedList<>();
        if (analysis != null) {
            if (analysis.getStudy() != null) {
                result = getRolesByStudy(user, analysis.getStudy());
            } else {
                Analysis byId = analysisRepository.findOne(analysis.getId());
                result = byId != null ? getRolesByStudy(user, byId.getStudy()) : result;
            }
        }
        return result;
    }

    @Override
    public List<ParticipantRole> getRolesBySubmission(ArachneUser user, Submission submission) {

        List<ParticipantRole> result = new LinkedList<>();
        if (submission != null) {
            Analysis analysis = submission.getSubmissionGroup().getAnalysis();
            if (analysis != null && analysis.getStudy() != null) {
                result = getRolesByStudy(user, analysis.getStudy());
                final DataNode dataNode = submission.getDataSource().getDataNode();
                if (!DataNodeUtils.isDataNodeOwner(dataNode, user.getId())) {
                    // check if we are not owner - delete owner role O_O
                    result.removeIf(ParticipantRole.DATA_SET_OWNER::equals);
                }
            } else if (analysis != null) {

                Submission byId = submissionRepository.findOne(analysis.getId());
                result = byId != null ? getRolesByStudy(user, analysis.getStudy()) : result;
            }
        }
        return result;
    }

    @Override
    public List<ParticipantRole> getRolesBySubmissionGroup(ArachneUser user, SubmissionGroup submissionGroup) {

        List<ParticipantRole> result = new LinkedList<>();
        Optional.ofNullable(submissionGroup).ifPresent(sg -> {
            Analysis analysis = submissionGroup.getAnalysis();
            if (analysis != null && analysis.getStudy() != null) {
                result.addAll(getRolesByStudy(user, analysis.getStudy()));
            }
        });
        return result;
    }

    @Override
    public List<ParticipantRole> getRolesByDataSource(ArachneUser user, DS dataSource) {

        List<ParticipantRole> participantRoles = getRolesByDataNode(user, dataSource.getDataNode());
        participantRoles.add(ParticipantRole.DATA_SET_USER);
        return participantRoles;
    }

    @Override
    public List<ParticipantRole> getRolesByDataNode(ArachneUser user, DataNode dataNode) {

        final User standardUser = new User();
        standardUser.setId(user.getId());
        List<ParticipantRole> participantRoles = new ArrayList<>();
        dataNodeUserRepository.findByDataNodeAndUser(dataNode, standardUser)
                .ifPresent(dataNodeUser -> {
                    final Set<DataNodeRole> dataNodeRoles = dataNodeUser.getDataNodeRole();
                    if (dataNodeRoles != null && !dataNodeRoles.isEmpty() && dataNodeRoles.contains(DataNodeRole.ADMIN)) {
                        participantRoles.add(ParticipantRole.DATA_SET_OWNER);
                    } else {
                        participantRoles.add(ParticipantRole.DATA_SET_USER);
                    }
                });
        return participantRoles;
    }

    @Override
    public boolean test(Long id) {

        System.out.println("TEST " + id);
        return true;
    }

    @Override
    public List<ParticipantRole> getRolesByPaper(ArachneUser user, P paper) {

        return new LinkedList<>(getRolesByStudy(user, paper.getStudy()));
    }

    @Override
    public List<ParticipantRole> getRolesByInsight(ArachneUser user, SubmissionInsight domainObject) {

        return getRolesBySubmission(user, domainObject.getSubmission());
    }

    @Override
    public List<ParticipantRole> getRolesByCommentTopic(ArachneUser user, CommentTopic topic) {

        SubmissionInsightSubmissionFile submissionInsightLink = submissionInsightSubmissionFileRepository.findByCommentTopic(topic);
        SubmissionInsight insight;

        if (submissionInsightLink == null) {
            ResultFile resultFile = resultFileRepository.findByCommentTopic(topic);
            insight = resultFile.getSubmission().getSubmissionInsight();
        } else {
            insight = submissionInsightLink.getSubmissionInsight();
        }

        return getRolesByInsight(user, insight);
    }

    @Override
    public List<ParticipantRole> getRolesByOrganization(ArachneUser user, Organization organization) {

        final User standardUser = userRepository.findByEmailAndEnabledTrue(user.getUsername());
        final boolean admin = standardUser.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
        ParticipantRole role = admin ? ParticipantRole.ORGANIZATION_ADMIN : canUserCreateOrganization ? ParticipantRole.ORGANIZATION_CREATOR : ParticipantRole.ORGANIZATION_READER;
        return Arrays.asList(role);
    }

    public List<ParticipantRole> getParticipantRoles(final Long userId, final Study study) {

        List<UserStudyExtended> userStudyList = userStudyExtendedRepository.findByUserIdAndStudyIdAndStatusIn(
                userId,
                study.getId(),
                Arrays.asList(ParticipantStatus.APPROVED, ParticipantStatus.PENDING)
        );
        return userStudyList.stream()
                .map(userStudy -> userStudy.getStatus().equals(ParticipantStatus.APPROVED)
                        ? ParticipantRole.valueOf(userStudy.getRole().name())
                        : ParticipantRole.STUDY_PENDING_CONTRIBUTOR)
                .collect(Collectors.toList());
    }
}
