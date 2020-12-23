/*
 *
 * Copyright 2018 Odysseus Data Services, inc.
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

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
import com.odysseusinc.arachne.portal.api.v1.dto.FileDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UploadFileDTO;
import com.odysseusinc.arachne.portal.exception.AlreadyExistException;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.NotUniqueException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.AnalysisFile;
import com.odysseusinc.arachne.portal.model.AnalysisUnlockRequest;
import com.odysseusinc.arachne.portal.model.ArachneFile;
import com.odysseusinc.arachne.portal.model.DataReference;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.Invitationable;
import com.odysseusinc.arachne.portal.model.SubmissionFile;
import com.odysseusinc.arachne.portal.service.AnalysisPaths;
import com.odysseusinc.arachne.portal.service.CRUDLService;
import com.odysseusinc.arachne.portal.service.Indexable;
import com.odysseusinc.arachne.portal.service.impl.antivirus.events.AntivirusJobAnalysisFileResponseEvent;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;

public interface BaseAnalysisService<T extends Analysis> extends CRUDLService<T>, AnalysisPaths, Indexable {

    T create(T object) throws NotUniqueException, PermissionDeniedException, NotExistException;

    void delete(Long id) throws NotExistException;

    T update(T object) throws NotUniqueException, NotExistException, ValidationException;

    List<T> list(IUser user, Long studyId) throws PermissionDeniedException, NotExistException;

    Boolean moveAnalysis(Long id, Integer index);

    List<AnalysisFile> saveFiles(List<UploadFileDTO> files, IUser user, T analysis) throws IOException;

    AnalysisFile saveFile(MultipartFile multipartFile, IUser user, T analysis, String label,
                          Boolean isExecutable, DataReference dataReference) throws IOException, AlreadyExistException;

    AnalysisFile saveFileByLink(String link, IUser user, T analysis, String label, Boolean isExecutable)
            throws IOException, AlreadyExistException;

    Path getAnalysisFile(AnalysisFile analysisFile) throws FileNotFoundException;

    List<AnalysisFile> findAnalysisFilesByDataReference(T analysis, DataReference dataReference);

    AnalysisFile getAnalysisFile(Long analysisId, String uuid);

    AnalysisFile getAnalysisFileUnsecured(String uuid);

    void lockAnalysisFiles(Long analysisId, Boolean locked) throws NotExistException;

    Path getSubmissionFile(SubmissionFile submissionFile) throws FileNotFoundException;

    Boolean deleteAnalysisFile(T analysis,
                               AnalysisFile analysisFile);

    Boolean forceDeleteAnalysisFile(T analysis, AnalysisFile analysisFile);

    void updateFile(String uuid, MultipartFile file, Long analysisId, Boolean isExecutable)
            throws IOException;

    void updateCodeFile(AnalysisFile analysisFile,
                        FileDTO fileContentDTO, IUser updatedBy) throws IOException;

    void deleteSubmissionFile(SubmissionFile file);

    Path getPath(ArachneFile arachneFile) throws FileNotFoundException;

    void getAnalysisAllFiles(Long analysisId, String archiveName, OutputStream os) throws IOException;

    void processAnalysisUnlockRequest(IUser user, Long invitationId,
                                      Boolean invitationAccepted) throws NotExistException;

    void setIsExecutable(String uuid);

    AnalysisUnlockRequest sendAnalysisUnlockRequest(
            Long analysisId,
            AnalysisUnlockRequest analysisUnlockRequest
    ) throws NotExistException, AlreadyExistException;

    List<IUser> findLeads(T analysis);

    List<? extends Invitationable> getWaitingForApprovalSubmissions(IUser user);

    void fullDelete(List<T> analyses);

    List<T> getByStudyId(Long id, EntityGraph author);

    List<T> findByType(CommonAnalysisType type);

    void processAntivirusResponse(AntivirusJobAnalysisFileResponseEvent event);

    void indexBySolr(T analysis)
            throws IllegalAccessException, IOException, SolrServerException, NotExistException, NoSuchFieldException;
}
