/**
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
import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.model.DataNodeRole;
import com.odysseusinc.arachne.portal.model.DataSource;
import com.odysseusinc.arachne.portal.model.Paper;
import com.odysseusinc.arachne.portal.model.ParticipantRole;
import com.odysseusinc.arachne.portal.model.ParticipantStatus;
import com.odysseusinc.arachne.portal.model.PublishState;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.model.SubmissionGroup;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.model.UserStudyGrouped;
import com.odysseusinc.arachne.portal.model.security.ArachneUser;
import com.odysseusinc.arachne.portal.repository.AnalysisRepository;
import com.odysseusinc.arachne.portal.repository.DataNodeRepository;
import com.odysseusinc.arachne.portal.repository.DataNodeUserRepository;
import com.odysseusinc.arachne.portal.repository.UserStudyGroupedRepository;
import com.odysseusinc.arachne.portal.repository.submission.SubmissionRepository;
import com.odysseusinc.arachne.portal.security.ArachnePermission;
import com.odysseusinc.arachne.portal.util.DataNodeUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public abstract class BaseArachneSecureServiceImpl<P extends Paper, DS extends DataSource> implements com.odysseusinc.arachne.portal.service.BaseArachneSecureService<P, DS> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(com.odysseusinc.arachne.portal.service.BaseArachneSecureService.class);

    protected final UserStudyGroupedRepository userStudyGroupedRepository;
    protected final AnalysisRepository analysisRepository;
    protected final SubmissionRepository submissionRepository;
    protected final DataNodeRepository dataNodeRepository;
    protected final DataNodeUserRepository dataNodeUserRepository;

    @Autowired
    public BaseArachneSecureServiceImpl(UserStudyGroupedRepository userStudyGroupedRepository,
                                        AnalysisRepository analysisRepository,
                                        SubmissionRepository submissionRepository,
                                        DataNodeRepository dataNodeRepository,
                                        DataNodeUserRepository dataNodeUserRepository) {

        this.userStudyGroupedRepository = userStudyGroupedRepository;
        this.analysisRepository = analysisRepository;
        this.submissionRepository = submissionRepository;
        this.dataNodeRepository = dataNodeRepository;
        this.dataNodeUserRepository = dataNodeUserRepository;
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
                    for (Iterator<ParticipantRole> iterator = result.iterator(); iterator.hasNext(); ) {
                        ParticipantRole participantRole = iterator.next();
                        if (ParticipantRole.DATA_SET_OWNER.equals(participantRole)) {
                            iterator.remove();
                        }
                    }
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
        DataNode dataNodeFromDb = dataNodeRepository.findBySid(dataNode.getSid());
        dataNodeUserRepository.findByDataNodeAndUser(dataNodeFromDb, standardUser)
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

    protected boolean checkIfAccessPaperPermissionPresent(List<ParticipantRole> participantRoles) {

        return participantRoles.stream()
                .flatMap(r -> Arrays.stream(r.getPermissions()))
                .anyMatch(p -> p == ArachnePermission.ACCESS_PAPER);
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

    public List<ParticipantRole> getParticipantRoles(final Long userId, final Study study) {

        final List<ParticipantRole> result = new LinkedList<>();
        final List<UserStudyGrouped> byUserAndStudy = userStudyGroupedRepository.findByUserIdAndStudyId(userId, study.getId());
        for (UserStudyGrouped userStudyLink : byUserAndStudy) {
            Arrays.stream(
                    userStudyLink.getRole() != null
                            ? userStudyLink.getRole().split(",")
                            : new String[]{})
                    .forEach(link ->
                            result.add(ParticipantRole.valueOf(link)));
        }
        return result;
    }
}
