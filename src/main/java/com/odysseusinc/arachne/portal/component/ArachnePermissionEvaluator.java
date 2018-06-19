/*
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
 * Created: February 11, 2017
 *
 */

package com.odysseusinc.arachne.portal.component;

import static com.odysseusinc.arachne.portal.component.PermissionDsl.domainObject;
import static com.odysseusinc.arachne.portal.component.PermissionDslPredicates.*;
import static com.odysseusinc.arachne.portal.component.PermissionDslPredicates.AnalysisFilePredicates.analysisFileAuthorIs;
import static com.odysseusinc.arachne.portal.component.PermissionDslPredicates.AnalysisPredicates.analysisAuthorIs;
import static com.odysseusinc.arachne.portal.component.PermissionDslPredicates.hasRole;
import static com.odysseusinc.arachne.portal.component.PermissionDslPredicates.instanceOf;
import static com.odysseusinc.arachne.portal.security.ArachnePermission.DELETE_ANALYSIS_FILES;
import static com.odysseusinc.arachne.portal.security.ArachnePermission.DELETE_DATASOURCE;

import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.AnalysisFile;
import com.odysseusinc.arachne.portal.model.CommentTopic;
import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.model.DataSource;
import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.Organization;
import com.odysseusinc.arachne.portal.model.Paper;
import com.odysseusinc.arachne.portal.model.ParticipantRole;
import com.odysseusinc.arachne.portal.model.PublishState;
import com.odysseusinc.arachne.portal.model.RawDataSource;
import com.odysseusinc.arachne.portal.model.RawUser;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.model.SubmissionGroup;
import com.odysseusinc.arachne.portal.model.SubmissionInsight;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.model.UserStudyGrouped;
import com.odysseusinc.arachne.portal.model.security.ArachneUser;
import com.odysseusinc.arachne.portal.security.ArachnePermission;
import com.odysseusinc.arachne.portal.security.HasArachnePermissions;
import com.odysseusinc.arachne.portal.service.BaseArachneSecureService;
import com.odysseusinc.arachne.portal.service.domain.DomainObjectLoaderFactory;
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
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;


@Component("ArachnePermissionEvaluator")
public class ArachnePermissionEvaluator<T extends Paper, D extends IDataSource> implements PermissionEvaluator {

    protected final BaseArachneSecureService<T, D> secureService;
    protected final DomainObjectLoaderFactory domainObjectLoaderFactory;
    protected Map<String, Class> domainClassMap = new HashMap<>();

    @Autowired
    public ArachnePermissionEvaluator(
            BaseArachneSecureService<T, D> secureService,
            DomainObjectLoaderFactory domainObjectLoaderFactory) {

        this.secureService = secureService;
        this.domainObjectLoaderFactory = domainObjectLoaderFactory;

        initDomainClassMap();
    }

    protected void initDomainClassMap() {

        domainClassMap.put(Study.class.getSimpleName(), Study.class);
        domainClassMap.put(Analysis.class.getSimpleName(), Analysis.class);
        domainClassMap.put(AnalysisFile.class.getSimpleName(), AnalysisFile.class);
        domainClassMap.put(Submission.class.getSimpleName(), Submission.class);
        domainClassMap.put(SubmissionGroup.class.getSimpleName(), SubmissionGroup.class);
        domainClassMap.put(DataSource.class.getSimpleName(), DataSource.class);
        domainClassMap.put(Paper.class.getSimpleName(), Paper.class);
        domainClassMap.put(CommentTopic.class.getSimpleName(), CommentTopic.class);
        domainClassMap.put(User.class.getSimpleName(), User.class);
        domainClassMap.put(RawUser.class.getSimpleName(), RawUser.class);
        domainClassMap.put(RawDataSource.class.getSimpleName(), RawDataSource.class);
        domainClassMap.put(Organization.class.getSimpleName(), Organization.class);
    }

    protected boolean checkPermission(Authentication authentication, Object domainObject, Object permissions) {

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
    public boolean hasPermission(Authentication authentication, Object domainObject, Object permissions) {

        Object refreshedDomainObject = domainObjectLoaderFactory.getDomainObjectLoader(domainObject.getClass())
            .withTargetId(domainObject)
            .loadDomainObject();

        // For case when there is no persisted entity yet, e.g. when entity is being created
        refreshedDomainObject = ObjectUtils.firstNonNull(refreshedDomainObject, domainObject);

        return checkPermission(authentication, refreshedDomainObject, permissions);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId,
                                 String targetType, Object permission) {

        Object domainObject = domainObjectLoaderFactory.getDomainObjectLoader(domainClassMap.get(targetType))
                .withTargetId(targetId)
                .loadDomainObject();

        return Objects.nonNull(domainObject)
                && checkPermission(authentication, domainObject, permission);
    }

    protected PermissionDsl studyRules(Object domainObject, ArachneUser user) {

        return domainObject(domainObject)
                .when(instanceOf(Study.class))
                .then(study -> getArachnePermissions(secureService.getRolesByStudy(user, study))).apply();
    }

    protected PermissionDsl analysisRules(Object domainObject, ArachneUser user) {

        return domainObject(domainObject)
                .when(instanceOf(Analysis.class))
                .then(analysis -> getArachnePermissions(secureService.getRolesByAnalysis(user, analysis)))
                .filter((analysis, permission) -> !(ArachnePermission.DELETE_ANALYSIS.equals(permission)
                        && (
                            Objects.isNull(analysis.getAuthor())
                                    || !(Objects.equals(user.getId(), analysis.getAuthor().getId()) || AnalysisPredicates.userIsLeadInvestigator(user).test(analysis))
                                    || (Objects.nonNull(analysis.getFiles()) && !analysis.getFiles().isEmpty())
                                    || (Objects.nonNull(analysis.getSubmissions()) && !analysis.getSubmissions().isEmpty())
                            )
                )).apply()
                .when(instanceOf(Analysis.class).and(analysisAuthorIs(user)))
                .then(analysis -> Collections.singleton(DELETE_ANALYSIS_FILES)).apply();
    }

    protected PermissionDsl submissionRules(Object domainObject, ArachneUser user) {

        return domainObject(domainObject).when(instanceOf(Submission.class))
                .then(submission -> getArachnePermissions(secureService.getRolesBySubmission(user, submission))).apply();
    }

    protected PermissionDsl analysisFileRules(Object domainObject, ArachneUser user) {

        return domainObject(domainObject)
                .when(instanceOf(AnalysisFile.class).and(analysisFileAuthorIs(user)))
                .then(file -> Collections.singleton(DELETE_ANALYSIS_FILES)).apply()
                .when(instanceOf(AnalysisFile.class).and(AnalysisFilePredicates.userIsLeadInvestigator(user)))
                .then(file -> Collections.singleton(DELETE_ANALYSIS_FILES)).apply();
    }

    protected PermissionDsl dataSourceRules(Object domainObject, ArachneUser user) {

        return domainObject(domainObject).when(instanceOf(IDataSource.class))
                .then(dataSource -> getArachnePermissions(secureService.getRolesByDataSource(user, (D) dataSource))).apply()
                .when(instanceOf(IDataSource.class).and(hasRole(user, "ROLE_ADMIN")))
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
                .then(paper -> getArachnePermissions(secureService.getRolesByPaper(user, (T) paper))).apply()
                .when(instanceOf(Paper.class).and(paper -> paper.getPublishState() == PublishState.PUBLISHED))
                .then(paper -> Collections.singleton(ArachnePermission.ACCESS_PAPER))
                .apply();
    }

    protected PermissionDsl insightRules(Object domainObject, ArachneUser user) {

        return domainObject(domainObject).when(instanceOf(SubmissionInsight.class))
                .then(insight -> getArachnePermissions(secureService.getRolesByInsight(user, (SubmissionInsight) insight))).apply();
    }

    protected PermissionDsl topicRules(Object domainObject, ArachneUser user) {

        return domainObject(domainObject).when(instanceOf(CommentTopic.class))
                .then(topic -> getArachnePermissions(secureService.getRolesByCommentTopic(user, (CommentTopic) topic))).apply();
    }

    protected PermissionDsl userRules(Object domainObject, ArachneUser user) {

        return domainObject(domainObject).when(instanceOf(IUser.class))
                .then(targetUser -> secureService.getPermissionsForUser(user, targetUser))
                .apply();
    }

    protected PermissionDsl organizationRules(Object domainObject, ArachneUser user) {

        return domainObject(domainObject).when(instanceOf(Organization.class))
                .then(organization -> getArachnePermissions(secureService.getRolesByOrganization(user, (Organization) organization))).apply();
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
                .with(insightRules(domainObject, user))
                .with(topicRules(domainObject, user))
                .with(organizationRules(domainObject, user))
                .with(additionalRules(domainObject, user))
                .with(userRules(domainObject, user))
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

        if (hasPermissionsObj instanceof Analysis) {
            final Analysis analysis = (Analysis) hasPermissionsObj;
            final List<SubmissionGroup> submissionGroups = analysis.getSubmissionGroups();
            if (!CollectionUtils.isEmpty(submissionGroups)) {
                submissionGroups.forEach(submissionGroup -> submissionGroup.getSubmissions().forEach(submission -> {
                    final Set<ArachnePermission> submissionPermissions = getAllPermissions(submission, user);
                    submission.setPermissions(submissionPermissions);
                }));
            }
            final List<AnalysisFile> files = analysis.getFiles();
            if (!CollectionUtils.isEmpty(files)) {
                files.forEach(file -> {
                    final Set<ArachnePermission> filePermissions = getAllPermissions(file, user);
                    file.setPermissions(filePermissions);
                });
            }
        } else if (hasPermissionsObj instanceof Study) {
            final Study study = (Study) hasPermissionsObj;
            for (final Analysis analysis : study.getAnalyses()) {
                analysis.setPermissions(getAllPermissions(analysis, user));
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

    public boolean addPermissionsToSubmissions(ArachneUser user, Page<SubmissionGroup> submissionGroups) {

        for (SubmissionGroup submissionGroup : submissionGroups) {
            if (submissionGroup.getSubmissions() != null) {
                submissionGroup.getSubmissions().forEach(s -> s.setPermissions(getAllPermissions(s, user)));
            }
        }
        return true;
    }

    public boolean processPermissions(ArachneUser user, HasArachnePermissions hasPermissionsObj) {

        addPermissions(user, hasPermissionsObj);
        return true;
    }

}
