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
 * Created: September 18, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.analysis.impl;

import com.odysseusinc.arachne.portal.exception.AlreadyExistException;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.NotUniqueException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.AnalysisFile;
import com.odysseusinc.arachne.portal.model.AnalysisUnlockRequest;
import com.odysseusinc.arachne.portal.model.DataReference;
import com.odysseusinc.arachne.portal.model.DataSource;
import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.StudyViewItem;
import com.odysseusinc.arachne.portal.model.search.StudySearch;
import com.odysseusinc.arachne.portal.model.statemachine.study.StudyStateMachine;
import com.odysseusinc.arachne.portal.repository.AnalysisFileRepository;
import com.odysseusinc.arachne.portal.repository.AnalysisRepository;
import com.odysseusinc.arachne.portal.repository.AnalysisUnlockRequestRepository;
import com.odysseusinc.arachne.portal.repository.ResultFileRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionFileRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionStatusHistoryRepository;
import com.odysseusinc.arachne.portal.repository.submission.SubmissionRepository;
import com.odysseusinc.arachne.portal.service.SolrService;
import com.odysseusinc.arachne.portal.service.ToPdfConverter;
import com.odysseusinc.arachne.portal.service.StudyFileService;
import com.odysseusinc.arachne.portal.service.StudyService;
import com.odysseusinc.arachne.portal.service.analysis.AnalysisService;
import com.odysseusinc.arachne.portal.service.impl.AnalysisPreprocessorService;
import com.odysseusinc.arachne.portal.service.impl.solr.SolrField;
import com.odysseusinc.arachne.portal.service.mail.ArachneMailSender;
import com.odysseusinc.arachne.portal.util.AnalysisHelper;
import com.odysseusinc.arachne.portal.util.LegacyAnalysisHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;

@Service
@SuppressWarnings("unused")
@Transactional(rollbackFor = Exception.class)
public class AnalysisServiceImpl extends BaseAnalysisServiceImpl<Analysis, Study, IDataSource, StudySearch, StudyViewItem, SolrField> implements AnalysisService {

    @Autowired
    public AnalysisServiceImpl(GenericConversionService conversionService,
                               AnalysisRepository analysisRepository,
                               SubmissionRepository submissionRepository,
                               AnalysisFileRepository analysisFileRepository,
                               SubmissionFileRepository submissionFileRepository,
                               ResultFileRepository resultFileRepository,
                               SubmissionStatusHistoryRepository submissionStatusHistoryRepository,
                               @SuppressWarnings("SpringJavaAutowiringInspection")
                               @Qualifier("restTemplate") RestTemplate restTemplate,
                               LegacyAnalysisHelper legacyAnalysisHelper,
                               AnalysisUnlockRequestRepository analysisUnlockRequestRepository,
                               ArachneMailSender mailSender,
                               SimpMessagingTemplate wsTemplate,
                               AnalysisPreprocessorService preprocessorService,
                               StudyStateMachine studyStateMachine,
                               StudyService studyService,
                               AnalysisHelper analysisHelper,
                               StudyFileService fileService,
                               ToPdfConverter docToPdfConverter,
                               ApplicationEventPublisher eventPublisher,
                               SolrService solrService) {


        super(conversionService,
                analysisRepository,
                submissionRepository,
                analysisFileRepository,
                submissionFileRepository,
                resultFileRepository,
                submissionStatusHistoryRepository,
                restTemplate,
                legacyAnalysisHelper,
                analysisUnlockRequestRepository,
                mailSender,
                wsTemplate,
                preprocessorService,
                studyStateMachine,
                studyService,
                analysisHelper,
                fileService,
                docToPdfConverter,
                eventPublisher,
                solrService);
    }

    @Override
    public Class<Analysis> getType() {

        return Analysis.class;
    }


    @Override
    @PreAuthorize("hasPermission(#analysisFile, "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).DELETE_ANALYSIS_FILES)")
    public Boolean deleteAnalysisFile(Analysis analysis, AnalysisFile analysisFile) {

        return super.deleteAnalysisFile(analysis, analysisFile);
    }

    @Override
    @PreAuthorize("hasPermission(#analysisId,  'Analysis', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).UPLOAD_FILES)")
    public void updateFile(String uuid, MultipartFile file, Long analysisId, Boolean isExecutable) throws IOException {

        super.updateFile(uuid, file, analysisId, isExecutable);
    }

    @Override
    @PreAuthorize("hasPermission(#analysis, "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).CREATE_ANALYSIS)")
    @PostAuthorize("@ArachnePermissionEvaluator.addPermissions(principal, returnObject )")
    public Analysis update(Analysis analysis) throws NotUniqueException, NotExistException, ValidationException {

        return super.update(analysis);
    }

    @Override
    @PreAuthorize("hasPermission(#id, 'Analysis', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).DELETE_ANALYSIS)")
    public void delete(Long id) throws NotExistException {

        super.delete(id);
    }

    @Override
    @PreAuthorize("hasPermission(#id, 'Analysis', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).ACCESS_STUDY)")
    @PostAuthorize("@ArachnePermissionEvaluator.addPermissions(principal, returnObject )")
    public Analysis getById(Long id) throws NotExistException {

        return super.getById(id);
    }

    @Override
    @Secured({"ROLE_ADMIN"})
    public List<Analysis> list(IUser user, Long studyId) throws PermissionDeniedException, NotExistException {

        return super.list(user, studyId);
    }

    @Override
    @PreAuthorize("hasPermission(#id, 'Analysis', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).EDIT_STUDY)")
    public Boolean moveAnalysis(Long id, Integer index) {

        return super.moveAnalysis(id, index);
    }

    @Override
    @PreAuthorize("hasPermission(#analysis, "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).UPLOAD_ANALYSIS_FILES)")
    public AnalysisFile saveFile(MultipartFile multipartFile, IUser user, Analysis analysis,
                                 String label, Boolean isExecutable, DataReference dataReference)
            throws IOException, AlreadyExistException {

        return super.saveFile(multipartFile, user, analysis, label, isExecutable, dataReference);
    }

    @Override
    @PreAuthorize("hasPermission(#analysis, "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).UPLOAD_ANALYSIS_FILES)")
    public AnalysisFile saveFileByLink(String link, IUser user, Analysis analysis, String label,
                                       Boolean isExecutable) throws IOException, AlreadyExistException {

        return super.saveFileByLink(link, user, analysis, label, isExecutable);
    }

    @Override
    @PreAuthorize("hasPermission(#analysisFile.analysis, "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).ACCESS_STUDY)")
    public Path getAnalysisFile(AnalysisFile analysisFile) throws FileNotFoundException {

        return super.getAnalysisFile(analysisFile);
    }

    @Override
    @PreAuthorize("hasPermission(#analysisId,  'Analysis', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).ACCESS_STUDY)")
    public AnalysisFile getAnalysisFile(Long analysisId, String uuid) {

        return super.getAnalysisFile(analysisId, uuid);
    }

    @Override
    @PreAuthorize("hasPermission(#analysisId,  'Analysis', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).LOCK_ANALYSIS_FILE)")
    public void lockAnalysisFiles(Long analysisId, Boolean locked) throws NotExistException {

        super.lockAnalysisFiles(analysisId, locked);
    }

    @Override
    @PreAuthorize("hasPermission(#analysisId,  'Analysis', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).SENDING_UNLOCK_ANALYSIS_REQUEST)")
    public AnalysisUnlockRequest sendAnalysisUnlockRequest(Long analysisId, AnalysisUnlockRequest analysisUnlockRequest)
            throws NotExistException, AlreadyExistException {

        return super.sendAnalysisUnlockRequest(analysisId, analysisUnlockRequest);
    }

    @Override
    @PreAuthorize("hasPermission(#analysisId, 'Analysis', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).ACCESS_STUDY)")
    public void getAnalysisAllFiles(Long analysisId, String archiveName, OutputStream os) throws IOException {

        super.getAnalysisAllFiles(analysisId, archiveName, os);
    }
}
