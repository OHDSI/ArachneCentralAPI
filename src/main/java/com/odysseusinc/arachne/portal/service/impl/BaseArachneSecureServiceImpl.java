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

import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.CommentTopic;
import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.model.DataSourceStatus;
import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.Organization;
import com.odysseusinc.arachne.portal.model.Paper;
import com.odysseusinc.arachne.portal.model.ParticipantRole;
import com.odysseusinc.arachne.portal.model.ParticipantStatus;
import com.odysseusinc.arachne.portal.model.RawUser;
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
import com.odysseusinc.arachne.portal.repository.BaseTenantRepository;
import com.odysseusinc.arachne.portal.repository.DataNodeRepository;
import com.odysseusinc.arachne.portal.repository.DataNodeUserRepository;
import com.odysseusinc.arachne.portal.repository.ResultFileRepository;
import com.odysseusinc.arachne.portal.repository.StudyRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionInsightSubmissionFileRepository;
import com.odysseusinc.arachne.portal.repository.UserRepository;
import com.odysseusinc.arachne.portal.repository.UserStudyExtendedRepository;
import com.odysseusinc.arachne.portal.repository.UserStudyGroupedRepository;
import com.odysseusinc.arachne.portal.repository.submission.SubmissionRepository;
import com.odysseusinc.arachne.portal.security.ArachnePermission;
import com.odysseusinc.arachne.portal.service.BaseRoleService;
import com.odysseusinc.arachne.portal.util.DataNodeUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

public abstract class BaseArachneSecureServiceImpl<P extends Paper, DS extends IDataSource> implements com.odysseusinc.arachne.portal.service.BaseArachneSecureService<P, DS> {

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
    protected final BaseTenantRepository tenantRepository;
    protected final StudyRepository studyRepository;
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
                                        BaseTenantRepository tenantRepository,
                                        StudyRepository studyRepository,
                                        UserRepository userRepository) {

        this.userStudyGroupedRepository = userStudyGroupedRepository;
        this.analysisRepository = analysisRepository;
        this.submissionRepository = submissionRepository;
        this.dataNodeRepository = dataNodeRepository;
        this.dataNodeUserRepository = dataNodeUserRepository;
        this.userStudyExtendedRepository = userStudyExtendedRepository;
        this.submissionInsightSubmissionFileRepository = submissionInsightSubmissionFileRepository;
        this.resultFileRepository = resultFileRepository;
        this.tenantRepository = tenantRepository;
        this.studyRepository = studyRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipantRole> getRolesByStudy(ArachneUser user, Study study) {

        List<ParticipantRole> participantRoles = new LinkedList<>();

        if (study != null) {

            boolean requiredFieldsAreSet = ObjectUtils.allNotNull(study.getId(), study.getTenant(), study.getPrivacy());

            if (!requiredFieldsAreSet) {
                study = studyRepository.findOne(study.getId());
            }

            if (Objects.equals(study.getTenant().getId(), user.getActiveTenantId())) {

                if (!study.getPrivacy()) {
                    participantRoles.add(ParticipantRole.STUDY_READER);
                }
                participantRoles.addAll(getParticipantRoles(user.getId(), study));
            }
        }

        return participantRoles;
    }

    @Override
    public List<ParticipantRole> getRolesByAnalysis(ArachneUser user, Analysis analysis) {

        List<ParticipantRole> result = new LinkedList<>();
        // sometimes this method can be called from places where it is impossible to retrieve study
        ensureAnalysisStudyCanBeRetrieved(analysis);
        if (analysis != null) {
            if (analysis.getStudy() != null) {
                result = getRolesByStudy(user, analysis.getStudy());
            } else {
                Analysis byId = analysisRepository.findOne(analysis.getId());
                result = byId != null ? getRolesByStudy(user, byId.getStudy()) : result;
            }
            if (analysis.getAuthor().getId().equals(user.getId())) {
                result.add(ParticipantRole.ANALYSIS_OWNER);
            }
        }
        return result;
    }

    protected void ensureAnalysisStudyCanBeRetrieved(final Analysis analysis) {

        final Long studyId = analysis.getStudy().getId();
        final Study foundStudy = getStudyByIdInAnyTenant(studyId);
        analysis.setStudy(foundStudy);
    }
    
    public abstract Study getStudyByIdInAnyTenant(Long studyId);

    @Override
    public List<ParticipantRole> getRolesBySubmission(ArachneUser user, Submission submission) {

        List<ParticipantRole> result = new LinkedList<>();
        if (submission != null) {
            Analysis analysis = submission.getSubmissionGroup().getAnalysis();
            result = getRolesByAnalysis(user, analysis);

            final DataNode dataNode = submission.getDataSource().getDataNode();
            if (!DataNodeUtils.isDataNodeOwner(dataNode, user.getId())) {
                // There can be many DATA_SET_OWNER-s in a single study, owning different data sources
                // But in case of Submission, we are interested, whether current user is owner of the submission's DS
                result.removeIf(ParticipantRole.DATA_SET_OWNER::equals);
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

        if (tenantRepository.findFirstByDataSourcesIdAndUsersId(dataSource.getId(), user.getId()).isPresent()) {
            participantRoles.add(ParticipantRole.DATA_SET_USER);
        }
        return participantRoles;
    }

    @Override
    public List<ParticipantRole> getRolesByDataNode(ArachneUser user, DataNode dataNode) {

        List<ParticipantRole> participantRoles = new ArrayList<>();
        if (checkDataNodeAdmin(user, dataNode)) {
            participantRoles.add(ParticipantRole.DATA_SET_OWNER);
            participantRoles.add(ParticipantRole.DATANODE_ADMIN);
        }
        if (canImportFromDatanode(user, dataNode)) {
            participantRoles.add(ParticipantRole.DATA_NODE_IMPORTER);
        }
        return participantRoles;
    }

    @Override
    public boolean canImportFromDatanode(ArachneUser user, DataNode dataNode) {

        return true;
    }

    @Override
    public boolean wasDataSourceApproved(Analysis analysis, Long dataSourceId) {

        return analysis
                .getStudy()
                .getDataSources()
                .stream()
                .filter(link -> Objects.equals(link.getStatus(), DataSourceStatus.APPROVED))
                .anyMatch(link -> Objects.equals(link.getDataSource().getId(), dataSourceId));
    }

    public boolean checkDataNodeAdmin(ArachneUser user, DataNode dataNode) {

        final RawUser standardUser = new RawUser();
        standardUser.setId(user.getId());

        return dataNodeUserRepository.findByDataNodeAndUserId(dataNode, standardUser.getId()).isPresent();
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
        final boolean admin = standardUser.getRoles().stream().anyMatch(r -> r.getName().equals(BaseRoleService.ROLE_ADMIN));
        ParticipantRole role = admin ? ParticipantRole.ORGANIZATION_ADMIN : canUserCreateOrganization ? ParticipantRole.ORGANIZATION_CREATOR : ParticipantRole.ORGANIZATION_READER;
        return Arrays.asList(role);
    }

    @Override
    public Set<ArachnePermission> getPermissionsForUser(ArachneUser user, IUser targetUser) {

        Set<ArachnePermission> permissions = new HashSet<>();
        if (tenantRepository.findCommonForUsers(user.getId(), targetUser.getId()).size() > 0) {
            permissions.add(ArachnePermission.ACCESS_USER);
        }
        return permissions;
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
