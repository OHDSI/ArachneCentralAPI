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
 * Created: September 18, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.analysis;

import com.odysseusinc.arachne.portal.api.v1.dto.FileContentDTO;
import com.odysseusinc.arachne.portal.exception.AlreadyExistException;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.NotUniqueException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.AnalysisFile;
import com.odysseusinc.arachne.portal.model.AnalysisUnlockRequest;
import com.odysseusinc.arachne.portal.model.ArachneFile;
import com.odysseusinc.arachne.portal.model.CommentTopic;
import com.odysseusinc.arachne.portal.model.DataReference;
import com.odysseusinc.arachne.portal.model.Invitationable;
import com.odysseusinc.arachne.portal.model.ResultFile;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.model.SubmissionFile;
import com.odysseusinc.arachne.portal.model.SubmissionInsight;
import com.odysseusinc.arachne.portal.model.SubmissionInsightSubmissionFile;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.service.AnalysisPaths;
import com.odysseusinc.arachne.portal.service.CRUDLService;
import com.odysseusinc.arachne.portal.service.impl.submission.SubmissionAction;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import org.apache.commons.compress.archivers.ArchiveException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;

public interface BaseAnalysisService<T extends Analysis> extends CRUDLService<T>, AnalysisPaths {
    T create(T object) throws NotUniqueException, PermissionDeniedException, NotExistException;

    void delete(Long id) throws NotExistException;

    T update(T object) throws NotUniqueException, NotExistException, ValidationException;

    List<T> list(User user, Long studyId) throws PermissionDeniedException, NotExistException;

    Boolean moveAnalysis(Long id, Integer index);

    AnalysisFile saveFile(MultipartFile multipartFile, User user, T analysis, String label,
                          Boolean isExecutable, DataReference dataReference) throws IOException;

    AnalysisFile saveFile(String link, User user, T analysis, String label, Boolean isExecutable)
            throws IOException;

    Path getAnalysisFile(AnalysisFile analysisFile) throws FileNotFoundException;

    AnalysisFile getAnalysisFile(Long analysisId, String uuid);

    void lockAnalysisFiles(Long analysisId, Boolean locked) throws NotExistException;

    AnalysisUnlockRequest sendAnalysisUnlockRequest(
            Long analysisId,
            AnalysisUnlockRequest analysisUnlockRequest
    ) throws NotExistException, AlreadyExistException;

    void processAnalysisUnlockRequest(User user, Long invitationId,
                                      Boolean invitationAccepted) throws NotExistException;

    Path getSubmissionFile(SubmissionFile submissionFile) throws FileNotFoundException;

    Path getResultFile(ResultFile resultFile) throws FileNotFoundException;

    Boolean deleteAnalysisFile(T analysis,
                               AnalysisFile analysisFile);

    Boolean forceDeleteAnalysisFile(T analysis, AnalysisFile analysisFile);

    void updateFile(String uuid, MultipartFile file, Long analysisId, Boolean isExecutable)
            throws IOException;

    void writeToFile(AnalysisFile analysisFile,
                     FileContentDTO fileContentDTO, User updatedBy) throws IOException;

    AnalysisFile saveAnalysisFile(AnalysisFile file);

    List<String> getPackratFiles(ArachneFile arachneFile) throws IOException, ArchiveException;

    byte[] getAllBytes(ArachneFile arachneFile) throws IOException;

    void deleteSubmissionFile(SubmissionFile file);

    void setIsExecutable(String uuid);

    List<String> getSubmissionFilesURLs(Submission source) throws IOException;

    List<SubmissionAction> getSubmissionActions(Submission submission);

    void getAnalysisAllFiles(Long analysisId, String archiveName, OutputStream os) throws IOException;

    SubmissionInsight getSubmissionInsight(Long submissionId) throws NotExistException;

    Set<CommentTopic> getInsightComments(SubmissionInsight insight, Integer size, Sort sort);

    SubmissionInsight createSubmissionInsight(Long submissionId, SubmissionInsight insight)
            throws AlreadyExistException, NotExistException;

    void deleteSubmissionInsightSubmissionFileLinks(List<SubmissionInsightSubmissionFile> links);

    SubmissionInsight updateSubmissionInsight(Long submissionId, SubmissionInsight insight)
            throws NotExistException;

    Page<SubmissionInsight> getInsightsByStudyId(Long studyId, Pageable pageable);

    List<User> findLeads(T analysis);

    List<? extends Invitationable> getWaitingForApprovalSubmissions(User user);

    void fullDelete(List<T> analyses);

    List<T> findByStudyIds(List<Long> ids);
}
