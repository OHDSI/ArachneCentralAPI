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
 * Created: September 18, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.submission.impl;

import com.odysseusinc.arachne.portal.api.v1.dto.ApproveDTO;
import com.odysseusinc.arachne.portal.exception.NoExecutableFileException;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.DataSource;
import com.odysseusinc.arachne.portal.model.ResultFile;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.model.SubmissionFile;
import com.odysseusinc.arachne.portal.model.SubmissionGroup;
import com.odysseusinc.arachne.portal.model.SubmissionStatusHistoryElement;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.model.search.ResultFileSearch;
import com.odysseusinc.arachne.portal.repository.ResultFileRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionFileRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionGroupRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionInsightRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionResultFileRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionStatusHistoryRepository;
import com.odysseusinc.arachne.portal.repository.submission.BaseSubmissionRepository;
import com.odysseusinc.arachne.portal.service.BaseDataSourceService;
import com.odysseusinc.arachne.storage.model.ArachneFileMeta;
import com.odysseusinc.arachne.storage.service.ContentStorageService;
import com.odysseusinc.arachne.portal.service.UserService;
import com.odysseusinc.arachne.portal.service.mail.ArachneMailSender;
import com.odysseusinc.arachne.portal.service.submission.SubmissionService;
import com.odysseusinc.arachne.portal.util.AnalysisHelper;
import com.odysseusinc.arachne.portal.util.ContentStorageHelper;
import com.odysseusinc.arachne.portal.util.LegacyAnalysisHelper;
import com.odysseusinc.arachne.portal.util.SubmissionHelper;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(rollbackFor = Exception.class)
public class SubmissionServiceImpl extends BaseSubmissionServiceImpl<Submission, Analysis, DataSource>
        implements SubmissionService {

    @Autowired
    public SubmissionServiceImpl(BaseSubmissionRepository<Submission> submissionRepository,
                                 BaseDataSourceService<DataSource> dataSourceService,
                                 ArachneMailSender mailSender,
                                 AnalysisHelper analysisHelper,
                                 SimpMessagingTemplate wsTemplate,
                                 LegacyAnalysisHelper legacyAnalysisHelper,
                                 SubmissionResultFileRepository submissionResultFileRepository,
                                 SubmissionGroupRepository submissionGroupRepository,
                                 SubmissionInsightRepository submissionInsightRepository,
                                 SubmissionFileRepository submissionFileRepository,
                                 ResultFileRepository resultFileRepository,
                                 SubmissionStatusHistoryRepository submissionStatusHistoryRepository,
                                 EntityManager entityManager,
                                 SubmissionHelper submissionHelper,
                                 ContentStorageService contentStorageService,
                                 UserService userService,
                                 ContentStorageHelper contentStorageHelper) {

        super(submissionRepository,
                dataSourceService,
                mailSender,
                analysisHelper,
                wsTemplate,
                legacyAnalysisHelper,
                submissionResultFileRepository,
                submissionGroupRepository,
                submissionInsightRepository,
                submissionFileRepository,
                resultFileRepository,
                submissionStatusHistoryRepository,
                entityManager,
                submissionHelper,
                contentStorageService,
                userService,
                contentStorageHelper);
    }

    @Override
    protected Submission newSubmission() {

        return new Submission();
    }

    @Override
    @PreAuthorize("hasPermission(#submissionId, 'Submission', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).APPROVE_SUBMISSION)")
    @PostAuthorize("@ArachnePermissionEvaluator.addPermissions(principal, returnObject )")
    public Submission approveSubmissionResult(Long submissionId, ApproveDTO approveDTO, User user) {

        return super.approveSubmissionResult(submissionId, approveDTO, user);
    }

    @Override
    @PreAuthorize("hasPermission(#analysis, "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).CREATE_SUBMISSION)")
    public Submission createSubmission(User user, Analysis analysis, Long datasourceId, SubmissionGroup submissionGroup) throws NotExistException, IOException {

        return super.createSubmission(user, analysis, datasourceId, submissionGroup);
    }

    @Override
    @PreAuthorize("hasPermission(#analysis, "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).CREATE_SUBMISSION)")
    public SubmissionGroup createSubmissionGroup(User user, Analysis analysis) throws IOException, NoExecutableFileException {

        return super.createSubmissionGroup(user, analysis);
    }

    @Override
    @PreAuthorize("hasPermission(#submissionId, 'Submission', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).APPROVE_SUBMISSION)")
    public boolean deleteSubmissionResultFileByUuid(Long submissionId, String fileUuid) throws NotExistException, ValidationException {

        return super.deleteSubmissionResultFileByUuid(submissionId, fileUuid);
    }

    @Override
    @PreAuthorize("hasPermission(#submissionId, 'Submission', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).APPROVE_SUBMISSION)")
    public boolean deleteSubmissionResultFile(Long submissionId, Long fileId) throws NotExistException, ValidationException {

        return super.deleteSubmissionResultFile(submissionId, fileId);
    }

    @Override
    @PreAuthorize("hasPermission(#submissionId, 'Submission', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).APPROVE_SUBMISSION)")
    public ResultFile uploadResultsByDataOwner(Long submissionId, String name, MultipartFile file) throws NotExistException, IOException {

        return super.uploadResultsByDataOwner(submissionId, name, file);
    }

    @Override
    @PreAuthorize("hasPermission(#submissionId, 'Submission', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).APPROVE_SUBMISSION)")
    @PostAuthorize("@ArachnePermissionEvaluator.addPermissions(principal, returnObject )")
    public Submission approveSubmission(Long submissionId, Boolean isApproved, String comment, User user) throws IOException, NotExistException {

        return super.approveSubmission(submissionId, isApproved, comment, user);
    }

    @Override
    @PreAuthorize("hasPermission(#id, 'SubmissionGroup', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).ACCESS_STUDY)")
    public SubmissionGroup getSubmissionGroupById(Long id) throws NotExistException {

        return super.getSubmissionGroupById(id);
    }

    @Override
    @PreAuthorize("hasPermission(#analysisId, 'Analysis', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).ACCESS_STUDY)")
    public void getSubmissionResultAllFiles(User user, Long analysisId, Long submissionId, String archiveName, OutputStream os) throws IOException, PermissionDeniedException {

        super.getSubmissionResultAllFiles(user, analysisId, submissionId, archiveName, os);
    }

    @Override
    @PreAuthorize("hasPermission(#submissionId, 'Submission', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).ACCESS_STUDY)")
    public List<ArachneFileMeta> getResultFiles(User user, Long submissionId, ResultFileSearch resultFileSearch) throws PermissionDeniedException {

        return super.getResultFiles(user, submissionId, resultFileSearch);
    }

    @Override
    @PreAuthorize("hasPermission(#analysisId,  'Analysis', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).ACCESS_STUDY)")
    public ResultFile getResultFileAndCheckPermission(User user, Long analysisId, String uuid) throws PermissionDeniedException {

        return super.getResultFileAndCheckPermission(user, analysisId, uuid);
    }

    @Override
    @PreAuthorize("hasPermission(#submissionGroupId, 'SubmissionGroup', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).ACCESS_STUDY)")
    public void getSubmissionAllFiles(Long submissionGroupId, String archiveName, OutputStream os) throws IOException {

        super.getSubmissionAllFiles(submissionGroupId, archiveName, os);
    }

    @Override
    @PreAuthorize("hasPermission(#submissionGroupId,  'SubmissionGroup', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).ACCESS_STUDY)")
    public List<SubmissionFile> getSubmissionFiles(Long submissionGroupId) {

        return super.getSubmissionFiles(submissionGroupId);
    }

    @Override
    @PreAuthorize("hasPermission(#submissionGroupId,  'SubmissionGroup', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).ACCESS_STUDY)")
    public SubmissionFile getSubmissionFile(Long submissionGroupId, String uuid) {

        return super.getSubmissionFile(submissionGroupId, uuid);
    }

    @Override
    @PreAuthorize("hasPermission(#analysisId,  'Analysis', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).ACCESS_STUDY)")
    public List<SubmissionStatusHistoryElement> getSubmissionStatusHistory(Long analysisId, Long submissionId) {

        return super.getSubmissionStatusHistory(analysisId, submissionId);
    }
}
