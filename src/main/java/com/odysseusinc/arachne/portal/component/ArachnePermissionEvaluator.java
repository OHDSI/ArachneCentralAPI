/*
 * Copyright 2017 Observational Health Data Sciences and Informatics
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
 * Created: October 10, 2017
 */

package com.odysseusinc.arachne.portal.component;

import static com.odysseusinc.arachne.portal.component.PermissionDsl.authorIs;
import static com.odysseusinc.arachne.portal.component.PermissionDsl.domainObject;
import static com.odysseusinc.arachne.portal.component.PermissionDsl.hasRole;
import static com.odysseusinc.arachne.portal.component.PermissionDsl.instanceOf;
import static com.odysseusinc.arachne.portal.security.ArachnePermission.ACCESS_STUDY;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.CrudRepository;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


@Component("ArachnePermissionEvaluator")
public class ArachnePermissionEvaluator<T extends Paper, D extends DataSource> implements PermissionEvaluator {

    protected final BaseArachneSecureService<T, D> secureService;
    protected final StudyRepository studyRepository;
    protected final SubmissionRepository submissionRepository;
    protected final AnalysisRepository analysisRepository;
    protected final BaseDataSourceRepository dataSourceRepository;
    protected final SubmissionGroupRepository submissionGroupRepository;
    protected final PaperRepository<T> paperRepository;
    private Map<String, CrudRepository> repositoryMap = new HashMap<>();

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
        initRepositoriesMap();
    }

    private void initRepositoriesMap() {

        repositoryMap.put("Study", studyRepository);
        repositoryMap.put("Analysis", analysisRepository);
        repositoryMap.put("Submission", submissionRepository);
        repositoryMap.put("SubmissionGroup", submissionGroupRepository);
        repositoryMap.put("DataSource", dataSourceRepository);
        repositoryMap.put("Paper", paperRepository);
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
                return Objects.nonNull(allPermission) && allPermission.containsAll(arachnePermissions);
            }
        }
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId,
                                 String targetType, Object permission) {

        Object domainObject = null;
        if (repositoryMap.containsKey(targetType)) {
            CrudRepository repository = repositoryMap.get(targetType);
            if (Objects.equals(targetType, "DataSource")
                    && targetId instanceof String) {
                domainObject = dataSourceRepository.findByUuid(targetId.toString());
            } else {
                domainObject = repository.findOne(targetId);
            }
        }
        return Objects.nonNull(domainObject)
                && hasPermission(authentication, domainObject, permission);
    }

    protected PermissionDsl studyRules(Object domainObject, ArachneUser user) {

        return domainObject(domainObject).when(instanceOf(Study.class))
                .then(study -> Collections.singleton(ACCESS_STUDY)).and()
                .then(study -> getArachnePermissions(secureService.getRolesByStudy(user, study))).apply();
    }

    protected PermissionDsl analysisRules(Object domainObject, ArachneUser user) {

        return domainObject(domainObject).when(instanceOf(Analysis.class))
                .then(analysis -> Collections.singleton(ACCESS_STUDY)).and()
                .then(analysis -> getArachnePermissions(secureService.getRolesByAnalysis(user, analysis)))
                .filter((analysis, permission) -> !(ArachnePermission.DELETE_ANALYSIS.equals(permission)
                        && (
                        Objects.isNull(analysis.getAuthor())
                                || !Objects.equals(user.getId(), analysis.getAuthor().getId())
                                || (Objects.nonNull(analysis.getFiles()) && !analysis.getFiles().isEmpty())
                                || (Objects.nonNull(analysis.getSubmissions()) && !analysis.getSubmissions().isEmpty())
                ))).apply();
    }

    protected PermissionDsl submissionRules(Object domainObject, ArachneUser user) {

        return domainObject(domainObject).when(instanceOf(Submission.class))
                .then(submission -> getArachnePermissions(secureService.getRolesBySubmission(user, submission))).apply();
    }

    protected PermissionDsl analysisFileRules(Object domainObject, ArachneUser user) {

        return domainObject(domainObject).when(instanceOf(AnalysisFile.class).and(authorIs(user)))
                .then(file -> Collections.singleton(DELETE_ANALYSIS_FILES)).apply()
                .when(instanceOf(AnalysisFile.class).and(authorIs(user)))
                .then(file -> Collections.singleton(DELETE_ANALYSIS_FILES)).apply();
    }

    protected PermissionDsl dataSourceRules(Object domainObject, ArachneUser user) {

        return domainObject(domainObject).when(instanceOf(DataSource.class))
                .then(dataSource -> getArachnePermissions(secureService.getRolesByDataSource(user, (D) dataSource))).apply()
                .when(instanceOf(DataSource.class).and(hasRole(user, "ROLE_ADMIN")))
                .then(dataSource -> Collections.singleton(DELETE_DATASOURCE)).apply();

    }

    protected PermissionDsl dataNodeRules(Object domainObject, ArachneUser user) {

        return domainObject(domainObject).when(instanceOf(DataNode.class))
                .then(dataNode -> getArachnePermissions(secureService.getRolesByDataNode(user, dataNode))).apply();
    }

    protected PermissionDsl submissionGroupRules(Object domainObject, ArachneUser user) {

        return domainObject(domainObject).when(instanceOf(SubmissionGroup.class))
                .then(submissionGroup -> getArachnePermissions(secureService.getRolesBySubmissionGroup(user, submissionGroup)))
                .apply();
    }

    protected PermissionDsl paperRules(Object domainObject, ArachneUser user) {

        return domainObject(domainObject).when(instanceOf(Paper.class))
                .then(paper -> getArachnePermissions(secureService.getRolesByPaper(user, (T) paper))).apply();
    }

    protected PermissionDsl additionalRules(Object domainObject, ArachneUser user) {

        return domainObject(domainObject);
    }

    protected Set<ArachnePermission> getAllPermissions(Object domainObject, ArachneUser user) {

        return domainObject(domainObject)
                .with(studyRules(domainObject, user))
                .with(analysisRules(domainObject, user))
                .with(submissionRules(domainObject, user))
                .with(analysisFileRules(domainObject, user))
                .with(dataSourceRules(domainObject, user))
                .with(dataNodeRules(domainObject, user))
                .with(submissionGroupRules(domainObject, user))
                .with(paperRules(domainObject, user))
                .with(additionalRules(domainObject, user))
                .getPermissions();
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
