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
 * Created: November 07, 2016
 *
 */

package com.odysseusinc.arachne.portal.service.impl;

import com.odysseusinc.arachne.portal.exception.AlreadyExistException;
import com.odysseusinc.arachne.portal.exception.FieldException;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.NotUniqueException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.model.AbstractUserStudyListItem;
import com.odysseusinc.arachne.portal.model.DataSource;
import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.ParticipantRole;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.StudyDataSourceLink;
import com.odysseusinc.arachne.portal.model.StudyFile;
import com.odysseusinc.arachne.portal.model.StudyViewItem;
import com.odysseusinc.arachne.portal.model.UserStudy;
import com.odysseusinc.arachne.portal.model.search.StudySearch;
import com.odysseusinc.arachne.portal.model.statemachine.study.StudyStateMachine;
import com.odysseusinc.arachne.portal.repository.FavouriteStudyRepository;
import com.odysseusinc.arachne.portal.repository.ResultFileRepository;
import com.odysseusinc.arachne.portal.repository.StudyDataSourceCommentRepository;
import com.odysseusinc.arachne.portal.repository.StudyDataSourceLinkRepository;
import com.odysseusinc.arachne.portal.repository.StudyFileRepository;
import com.odysseusinc.arachne.portal.repository.StudyRepository;
import com.odysseusinc.arachne.portal.repository.StudyViewItemRepository;
import com.odysseusinc.arachne.portal.repository.UserStudyExtendedRepository;
import com.odysseusinc.arachne.portal.repository.UserStudyGroupedRepository;
import com.odysseusinc.arachne.portal.repository.UserStudyRepository;
import com.odysseusinc.arachne.portal.service.BaseDataNodeService;
import com.odysseusinc.arachne.portal.service.DataSourceService;
import com.odysseusinc.arachne.portal.service.SolrService;
import com.odysseusinc.arachne.portal.service.StudyFileService;
import com.odysseusinc.arachne.portal.service.StudyService;
import com.odysseusinc.arachne.portal.service.StudyStatusService;
import com.odysseusinc.arachne.portal.service.StudyTypeService;
import com.odysseusinc.arachne.portal.service.UserService;
import com.odysseusinc.arachne.portal.service.impl.solr.SolrField;
import com.odysseusinc.arachne.portal.service.mail.ArachneMailSender;
import com.odysseusinc.arachne.portal.service.study.AddDataSourceStrategyFactory;
import com.odysseusinc.arachne.portal.util.StudyHelper;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.domain.Page;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(rollbackFor = Exception.class)
public class StudyServiceImpl extends BaseStudyServiceImpl<
        Study,
        IDataSource,
        StudySearch,
        StudyViewItem,
        SolrField> implements StudyService {
    public StudyServiceImpl(final UserStudyExtendedRepository userStudyExtendedRepository,
                            final StudyFileService fileService,
                            final StudyViewItemRepository userStudyPublicItemRepository,
                            final UserStudyGroupedRepository userStudyGroupedRepository,
                            final UserStudyRepository userStudyRepository,
                            final ArachneMailSender arachneMailSender,
                            final StudyRepository studyRepository,
                            final FavouriteStudyRepository favouriteStudyRepository,
                            final RestTemplate restTemplate,
                            final StudyTypeService studyTypeService,
                            final DataSourceService dataSourceService,
                            final StudyDataSourceLinkRepository studyDataSourceLinkRepository,
                            final ResultFileRepository resultFileRepository,
                            final StudyFileRepository studyFileRepository,
                            final StudyHelper studyHelper,
                            final StudyDataSourceCommentRepository dataSourceCommentRepository,
                            final UserService userService,
                            final SimpMessagingTemplate wsTemplate,
                            final StudyStatusService studyStatusService,
                            final BaseDataNodeService baseDataNodeService,
                            final JavaMailSender javaMailSender,
                            final GenericConversionService conversionService,
                            final StudyStateMachine studyStateMachine,
                            final AddDataSourceStrategyFactory addDataSourceStrategyFactory,
                            final ApplicationEventPublisher eventPublisher,
                            final SolrService solrService) {

        super(userStudyExtendedRepository,
                fileService,
                userStudyPublicItemRepository,
                userStudyGroupedRepository,
                userStudyRepository,
                arachneMailSender,
                studyRepository,
                favouriteStudyRepository,
                restTemplate,
                studyTypeService,
                dataSourceService,
                studyDataSourceLinkRepository,
                resultFileRepository,
                studyFileRepository,
                studyHelper,
                dataSourceCommentRepository,
                userService,
                wsTemplate,
                studyStatusService,
                baseDataNodeService,
                javaMailSender,
                conversionService,
                studyStateMachine,
                addDataSourceStrategyFactory,
                eventPublisher,
                solrService);
    }

    @Override
    public Class<Study> getType() {

        return Study.class;
    }

    @Override
    public Study createWorkspace(Long ownerId) {

        return createWorkspace(ownerId, new Study());
    }

    @Override
    @PreAuthorize("hasPermission(#studyId, 'Study', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).EDIT_STUDY)")
    public void delete(Long studyId) throws NotExistException {

        super.delete(studyId);
    }

    @Override
    @PostAuthorize("@ArachnePermissionEvaluator.addPermissions(principal, returnObject )")
    public Study getById(Long id) throws NotExistException {

        return super.getById(id);
    }

    @Override
    @PreAuthorize("hasPermission(#study, "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).EDIT_STUDY)")
    @PostAuthorize("@ArachnePermissionEvaluator.addPermissions(principal, returnObject )")
    public Study update(Study study) throws NotExistException, NotUniqueException, ValidationException {

        return super.update(study);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AbstractUserStudyListItem> findStudies(StudySearch studySearch) {

        return super.findStudies(studySearch);
    }

    @Override
    @PreAuthorize("hasPermission(#studyId, 'Study', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).ACCESS_STUDY)")
    @PostAuthorize("@ArachnePermissionEvaluator.addPermissions(principal, returnObject.study )")
    public StudyViewItem getStudy(IUser user, Long studyId) {

        return super.getStudy(user, studyId);
    }


    @Override
    @PreAuthorize("hasPermission(#studyId, 'Study', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).INVITE_CONTRIBUTOR)")
    public UserStudy addParticipant(IUser createdBy, Long studyId, Long participantId, ParticipantRole role) throws NotExistException, AlreadyExistException {

        return super.addParticipant(createdBy, studyId, participantId, role);
    }

    @Override
    @PreAuthorize("hasPermission(#studyId, 'Study', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).INVITE_CONTRIBUTOR)")
    public UserStudy updateParticipantRole(Long studyId, Long participantId, ParticipantRole role) throws NotExistException, AlreadyExistException, ValidationException {

        return super.updateParticipantRole(studyId, participantId, role);
    }

    @Override
    @PreAuthorize("hasPermission(#id, 'Study',"
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).EDIT_STUDY)")
    public void removeParticipant(Long id, Long participantId) throws NotExistException, PermissionDeniedException, ValidationException {

        super.removeParticipant(id, participantId);
    }

    @Override
    @PreAuthorize("hasPermission(#studyId, 'Study', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).UPLOAD_FILES)")
    public String saveFile(MultipartFile multipartFile, Long studyId, String label, IUser user) throws IOException {

        return super.saveFile(multipartFile, studyId, label, user);
    }

    @Override
    @PreAuthorize("hasPermission(#studyId, 'Study', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).UPLOAD_FILES)")
    public String saveFile(String link, Long studyId, String label, IUser user) throws IOException {

        return super.saveFile(link, studyId, label, user);
    }

    @Override
    @PreAuthorize("hasPermission(#studyId, 'Study', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).ACCESS_STUDY)")
    public StudyFile getStudyFile(Long studyId, String fileName) {

        return super.getStudyFile(studyId, fileName);
    }

    @Override
    @PreAuthorize("hasPermission(#studyId, 'Study', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).ACCESS_STUDY)")
    public Boolean getDeleteStudyFile(Long studyId, String uuid) throws FileNotFoundException {

        return super.getDeleteStudyFile(studyId, uuid);
    }

    @Override
    @PreAuthorize("hasPermission(#studyId, 'Study', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).ACCESS_STUDY)")
    public List<StudyDataSourceLink> listApprovedDataSources(Long studyId) {

        return super.listApprovedDataSources(studyId);
    }

    @Override
    //ordering annotations is important to check current participants before method invoke
    @PreAuthorize("hasPermission(#studyId, 'Study', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).INVITE_DATANODE)")
    public StudyDataSourceLink addDataSource(IUser createdBy, Long studyId, Long dataSourceId) throws NotExistException, AlreadyExistException {

        return super.addDataSource(createdBy, studyId, dataSourceId);
    }

    @Override
    @Transactional
    @PreAuthorize("hasPermission(#studyId, 'Study', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).INVITE_DATANODE)")
    public IDataSource addVirtualDataSource(IUser createdBy, Long studyId, String dataSourceName, List<String> dataOwnerIds)
            throws NotExistException, AlreadyExistException, NoSuchFieldException, IOException, ValidationException, FieldException, IllegalAccessException, SolrServerException {

        return super.addVirtualDataSource(createdBy, studyId, dataSourceName, dataOwnerIds);
    }

    @Override
    @PreAuthorize("hasPermission(#studyId, 'Study', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).UNLINK_DATASOURCE)")
    public void removeDataSource(Long studyId, Long dataSourceId) throws NotExistException {

        super.removeDataSource(studyId, dataSourceId);
    }

    @Override
    @PreAuthorize("hasPermission(#studyId, 'Study', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).ACCESS_STUDY)")
    public void getAllStudyFilesExceptLinks(Long studyId, String archiveName, OutputStream os) throws IOException {

        super.getAllStudyFilesExceptLinks(studyId, archiveName, os);
    }

    @Override
    @PostAuthorize("@ArachnePermissionEvaluator.addPermissions(principal, returnObject)")
    public Study findWorkspaceForUser(IUser user, final Long userId) throws NotExistException {
        
        return super.findWorkspaceForUser(user, userId);
    }

    @Override
    @PostAuthorize("@ArachnePermissionEvaluator.addPermissions(principal, returnObject )")
    public Study findOrCreateWorkspaceForUser(IUser user, Long userId) {

        return super.findOrCreateWorkspaceForUser(user, userId);
    }
}
