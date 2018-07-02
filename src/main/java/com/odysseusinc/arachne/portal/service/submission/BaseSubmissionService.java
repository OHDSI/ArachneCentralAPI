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

package com.odysseusinc.arachne.portal.service.submission;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.odysseusinc.arachne.portal.api.v1.dto.ApproveDTO;
import com.odysseusinc.arachne.portal.exception.NoExecutableFileException;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.ResultFile;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.model.SubmissionFile;
import com.odysseusinc.arachne.portal.model.SubmissionGroup;
import com.odysseusinc.arachne.portal.model.SubmissionStatus;
import com.odysseusinc.arachne.portal.model.SubmissionStatusHistoryElement;
import com.odysseusinc.arachne.portal.model.search.ResultFileSearch;
import com.odysseusinc.arachne.portal.model.search.SubmissionGroupSearch;
import com.odysseusinc.arachne.portal.service.impl.submission.SubmissionAction;
import com.odysseusinc.arachne.storage.model.ArachneFileMeta;
import com.odysseusinc.arachne.storage.util.FileSaveRequest;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;

public interface BaseSubmissionService<T extends Submission, A extends Analysis> {
    T approveSubmissionResult(Long submissionId, ApproveDTO approveDTO, IUser user);

    T approveSubmission(Long submissionId, Boolean isApproved, String comment, IUser user)
            throws IOException, NotExistException;

    T createSubmission(IUser user, A analysis, Long datasourceId,
                                SubmissionGroup submissionGroup)
            throws NotExistException, IOException;

    @PreAuthorize("hasPermission(#analysis, "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).CREATE_SUBMISSION)")
    SubmissionGroup createSubmissionGroup(IUser user, Analysis analysis) throws IOException, NoExecutableFileException;

    Page<SubmissionGroup> getSubmissionGroups(SubmissionGroupSearch submissoinGroupSearch);

    @PreAuthorize("hasPermission(#submissionId, 'Submission', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).APPROVE_SUBMISSION)")
    boolean deleteSubmissionResultFile(Long submissionId, ResultFile resultFile)
            throws NotExistException, ValidationException;

    void deleteSubmissionResultFile(ResultFile resultFile);

    T getSubmissionByIdAndToken(Long id, String token) throws NotExistException;

    T saveSubmission(T submission);

    T saveSubmissionAndFlush(T submission);

    T moveSubmissionToNewStatus(T submission, SubmissionStatus status, IUser user, String comment);

    T getSubmissionByIdUnsecured(Long id) throws NotExistException;

    T getSubmissionById(Long id) throws NotExistException;

    T getSubmissionById(Long id, EntityGraph entityGraph) throws NotExistException;

    T getSubmissionByIdAndStatus(Long id, SubmissionStatus status) throws NotExistException;

    T getSubmissionByIdAndStatus(Long id, List<SubmissionStatus> statusList) throws NotExistException;

    T getSubmissionByIdAndUpdatePasswordAndStatus(Long id, String updatePassword, List<SubmissionStatus> status)
            throws NotExistException;

    void notifyOwnersAboutNewSubmission(T submission);

    void notifyOwnersAboutSubmissionUpdateViaSocket(T submission);

    ResultFile uploadResultsByDataOwner(Long submissionId, String name, MultipartFile file) throws NotExistException, IOException;

    @PreAuthorize("hasPermission(#submissionId, 'Submission', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).ACCESS_STUDY)")
    List<ArachneFileMeta> getResultFiles(IUser user, Long submissionId, ResultFileSearch resultFileSearch) throws PermissionDeniedException;

    @PreAuthorize("hasPermission(#analysisId,  'Analysis', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).ACCESS_STUDY)")
    ArachneFileMeta getResultFileAndCheckPermission(IUser user, Submission submission, Long analysisId, String fileUuid)
            throws PermissionDeniedException;

    ResultFile getResultFileByPath(String path);

    ResultFile getResultFileById(Long fileId);

    void getSubmissionResultAllFiles(IUser user, Long analysisId, Long submissionId, String archiveName, OutputStream os)
            throws IOException, PermissionDeniedException;

    Path getSubmissionArchiveChunk(Long id, String updatePassword, String fileName) throws FileNotFoundException;

    void getSubmissionAllFiles(Long submissionGroupId, String archiveName, OutputStream os) throws IOException;

    List<SubmissionStatusHistoryElement> getSubmissionStatusHistory(Long analysisId, Long submissionId);

    List<SubmissionFile> getSubmissionFiles(Long submissionGroupId);

    SubmissionFile getSubmissionFile(Long submissionGroupId, Long fileId);

    SubmissionGroup getSubmissionGroupById(Long id) throws NotExistException;

    void deleteSubmissionStatusHistory(List<SubmissionStatusHistoryElement> statusHistory);

    SubmissionStatusHistoryElement getSubmissionStatusHistoryElementById(Long id);

    T updateSubmission(T submission);

    void deleteSubmissions(List<T> submission);

    void deleteSubmissionGroups(List<SubmissionGroup> groups);

    List<T> getByIdIn(List<Long> ids);

    List<SubmissionStatusHistoryElement> getSubmissionStatusHistoryElementsByIdsIn(List<Long> longs);

    List<ResultFile> createResultFilesBatch(
            List<FileSaveRequest> fileSaveRequests,
            Submission submission,
            Long createById
    ) throws IOException;

    ResultFile createResultFile(
            Path filePath,
            String name,
            Submission submission,
            Long createById
    ) throws IOException;

    List<SubmissionAction> getSubmissionActions(Submission submission);
}
