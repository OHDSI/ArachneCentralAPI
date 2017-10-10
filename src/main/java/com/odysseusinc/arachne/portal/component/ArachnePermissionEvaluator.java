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
 * Created: February 11, 2017
 *
 */

package com.odysseusinc.arachne.portal.component;

import static com.odysseusinc.arachne.portal.security.ArachnePermission.DELETE_ANALYSIS_FILES;
import static com.odysseusinc.arachne.portal.security.ArachnePermission.DELETE_DATASOURCE;

import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.AnalysisFile;
import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.model.DataSource;
import com.odysseusinc.arachne.portal.model.Paper;
import com.odysseusinc.arachne.portal.model.ParticipantRole;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.model.SubmissionGroup;
import com.odysseusinc.arachne.portal.model.UserStudyGrouped;
import com.odysseusinc.arachne.portal.model.security.ArachneUser;
import com.odysseusinc.arachne.portal.repository.AnalysisRepository;
import com.odysseusinc.arachne.portal.repository.BaseDataSourceRepository;
import com.odysseusinc.arachne.portal.repository.PaperRepository;
import com.odysseusinc.arachne.portal.repository.StudyRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionGroupRepository;
import com.odysseusinc.arachne.portal.repository.submission.SubmissionRepository;
import com.odysseusinc.arachne.portal.security.ArachnePermission;
import com.odysseusinc.arachne.portal.security.HasArachnePermissions;
import com.odysseusinc.arachne.portal.service.BaseArachneSecureService;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component("ArachnePermissionEvaluator")
public class ArachnePermissionEvaluator<T extends Paper, D extends DataSource> implements PermissionEvaluator {

    protected final BaseArachneSecureService<T, D> secureService;
    protected final StudyRepository studyRepository;
    protected final SubmissionRepository submissionRepository;
    protected final AnalysisRepository analysisRepository;
    protected final BaseDataSourceRepository dataSourceRepository;
    protected final SubmissionGroupRepository submissionGroupRepository;
    protected final PaperRepository<T> paperRepository;

    @Autowired
    public ArachnePermissionEvaluator(
            BaseArachneSecureService<T, D> secureService,
            StudyRepository studyRepository,
            SubmissionRepository submissionRepository,
            AnalysisRepository analysisRepository,
            BaseDataSourceRepository dataSourceRepository,
            SubmissionGroupRepository submissionGroupRepository,
            PaperRepository<T> paperRepository) {

        this.secureService = secureService;
        this.studyRepository = studyRepository;
        this.submissionRepository = submissionRepository;
        this.analysisRepository = analysisRepository;
        this.dataSourceRepository = dataSourceRepository;
        this.submissionGroupRepository = submissionGroupRepository;
        this.paperRepository = paperRepository;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object domainObject, Object permissions) {

        if (authentication.getPrincipal() instanceof ArachneUser) {
            ArachneUser user = (ArachneUser) authentication.getPrincipal();
            List<ArachnePermission> arachnePermissions = new LinkedList<>();
            if (permissions instanceof ArachnePermission) {
                arachnePermissions.add((ArachnePermission) permissions);
            } else if (permissions instanceof List) {
                for (Object permission : (List) permissions) {
                    if (permission instanceof ArachnePermission) {
                        arachnePermissions.add((ArachnePermission) permission);
                    }
                }
            }
            if (!arachnePermissions.isEmpty()) {
                Set<ArachnePermission> allPermission = getAllPermissions(domainObject, user);
                return allPermission != null && allPermission.containsAll(arachnePermissions);
            }
        }
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId,
                                 String targetType, Object permission) {

        switch (targetType) {
            case "Study": {
                Study study = studyRepository.findOne((Long) targetId);
                return hasPermission(authentication, study, permission);
            }
            case "Analysis": {
                Analysis analysis = analysisRepository.findOne((Long) targetId);
                return hasPermission(authentication, analysis, permission);
            }
            case "Submission": {
                Submission submission = submissionRepository.findOne((Long) targetId);
                return hasPermission(authentication, submission, permission);
            }
            case "DataSource": {
                DataSource dataSource;
                if (targetId instanceof String) {
                    dataSource = dataSourceRepository.findByUuid((String) targetId);
                } else if (targetId instanceof Long) {
                    dataSource = dataSourceRepository.findOne((Long) targetId);
                } else {
                    return false;
                }
                return hasPermission(authentication, dataSource, permission);
            }
            case "SubmissionGroup": {
                SubmissionGroup submissionGroup = submissionGroupRepository.findOne((Long) targetId);
                return hasPermission(authentication, submissionGroup, permission);
            }
            case "Paper": {
                final Paper paper = paperRepository.findOne((Long) targetId);
                return hasPermission(authentication, paper, permission);
            }
        }
        return false;
    }

    private Set<ArachnePermission> getAllPermissions(Object domainObject, ArachneUser user) {

        List<ParticipantRole> roles;
        Set<ArachnePermission> allPermission = new HashSet<>();
        if (prehandlePermissions(domainObject, user, allPermission)) {
            // work with permissions is done
        } else if (domainObject instanceof Study) {
            roles = secureService.getRolesByStudy(user, (Study) domainObject);
            allPermission = getArachnePermissions(roles);
        } else if (domainObject instanceof Analysis) {
            roles = secureService.getRolesByAnalysis(user, (Analysis) domainObject);
            allPermission = getArachnePermissions(roles);
            for (Iterator<ArachnePermission> iterator = allPermission.iterator(); iterator.hasNext(); ) {
                ArachnePermission arachnePermission = iterator.next();
                Analysis analysis = (Analysis) domainObject;
                if (ArachnePermission.DELETE_ANALYSIS.equals(arachnePermission)
                        && (
                        analysis.getAuthor() == null
                                || !user.getId().equals(analysis.getAuthor().getId())
                                || (analysis.getFiles() != null && !analysis.getFiles().isEmpty())
                                || (analysis.getSubmissions() != null && !analysis.getSubmissions().isEmpty())
                )
                        ) {
                    iterator.remove();
                }
            }
        } else if (domainObject instanceof Submission) {
            roles = secureService.getRolesBySubmission(user, (Submission) domainObject);
            allPermission = getArachnePermissions(roles);
        } else if (domainObject instanceof AnalysisFile) {
            AnalysisFile analysisFile = (AnalysisFile) domainObject;
            Analysis analysis = analysisFile.getAnalysis();
            roles = secureService.getRolesByAnalysis(user, analysis);
            allPermission = getArachnePermissions(roles);
            if (analysisFile.getAuthor() != null && analysisFile.getAuthor().getId() != null
                    && analysisFile.getAuthor().getId().equals(user.getId())) {
                allPermission.add(DELETE_ANALYSIS_FILES);
            }
        } else if (domainObject instanceof DataSource) {
            roles = secureService.getRolesByDataSource(user, (D) domainObject);
            allPermission = getArachnePermissions(roles);
            if (user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
                allPermission.add(DELETE_DATASOURCE);
            }
        } else if (domainObject instanceof DataNode) {
            roles = secureService.getRolesByDataNode(user, (DataNode) domainObject);
            allPermission = getArachnePermissions(roles);
        } else if (domainObject instanceof SubmissionGroup) {
            roles = secureService.getRolesBySubmissionGroup(user, (SubmissionGroup) domainObject);
            allPermission = getArachnePermissions(roles);
        } else if (domainObject instanceof  Paper) {
            roles = secureService.getRolesByPaper(user, (T) domainObject);
            allPermission.addAll(getArachnePermissions(roles));

        }
        posthandlePermissions(allPermission, domainObject);
        return allPermission;
    }

    protected void posthandlePermissions(Set<ArachnePermission> allPermission, Object domainObject) {

        if (domainObject instanceof Analysis) {
            if (allPermission.isEmpty()) {
                allPermission.add(ArachnePermission.ACCESS_STUDY);
            }
        }
    }

    protected boolean prehandlePermissions(Object domainObject, ArachneUser user, Set<ArachnePermission> allPermission) {

        return false;
    }

    protected Set<ArachnePermission> getArachnePermissions(List<ParticipantRole> roles) {

        Set<ArachnePermission> allPermission = new HashSet<>();
        for (ParticipantRole role : roles) {
            allPermission.addAll(Arrays.asList(role.getPermissions()));
        }
        return allPermission;
    }


    public boolean addPermissions(ArachneUser user, HasArachnePermissions hasPermissionsObj) {

        Set<ArachnePermission> allPermissions = getAllPermissions(hasPermissionsObj, user);
        hasPermissionsObj.setPermissions(allPermissions);
        if (hasPermissionsObj instanceof Study) {
            Study study = (Study) hasPermissionsObj;
            for (Analysis analysis : study.getAnalyses()) {
                addPermissions(user, analysis);
            }
        } else if (hasPermissionsObj instanceof Analysis) {
            final Analysis analysis = (Analysis) hasPermissionsObj;
            final List<SubmissionGroup> submissionGroups = analysis.getSubmissionGroups();
            if (!CollectionUtils.isEmpty(submissionGroups)) {
                submissionGroups.forEach(submissionGroup -> submissionGroup.getSubmissions().forEach(submission -> {
                    final Set<ArachnePermission> submissionPermissions = getAllPermissions(submission, user);
                    submission.setPermissions(submissionPermissions);
                }));
            }
        }
        return true;
    }

    public boolean addPermissions(ArachneUser user, Page<UserStudyGrouped> userStudyLinks) {

        for (UserStudyGrouped userStudyLink : userStudyLinks) {
            Study study = userStudyLink.getStudy();
            Set<ArachnePermission> allPermissions = getAllPermissions(study, user);
            study.setPermissions(allPermissions);
        }

        return true;
    }

    public boolean processPermissions(ArachneUser user, HasArachnePermissions hasPermissionsObj) {

        addPermissions(user, hasPermissionsObj);
        return true;
    }

}
