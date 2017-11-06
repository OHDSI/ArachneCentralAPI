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

package com.odysseusinc.arachne.portal.service.analysis.impl;

import static com.odysseusinc.arachne.portal.model.SubmissionStatus.EXECUTED;
import static com.odysseusinc.arachne.portal.model.SubmissionStatus.EXECUTED_PUBLISHED;
import static com.odysseusinc.arachne.portal.model.SubmissionStatus.EXECUTED_REJECTED;
import static com.odysseusinc.arachne.portal.model.SubmissionStatus.FAILED;
import static com.odysseusinc.arachne.portal.model.SubmissionStatus.FAILED_PUBLISHED;
import static com.odysseusinc.arachne.portal.model.SubmissionStatus.FAILED_REJECTED;
import static com.odysseusinc.arachne.portal.model.SubmissionStatus.IN_PROGRESS;
import static com.odysseusinc.arachne.portal.model.SubmissionStatus.NOT_APPROVED;
import static com.odysseusinc.arachne.portal.model.SubmissionStatus.PENDING;
import static com.odysseusinc.arachne.portal.service.impl.submission.SubmissionActionType.EXECUTE;
import static com.odysseusinc.arachne.portal.service.impl.submission.SubmissionActionType.PUBLISH;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphUtils;
import com.google.common.base.Objects;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
import com.odysseusinc.arachne.commons.utils.CommonFileUtils;
import com.odysseusinc.arachne.portal.api.v1.dto.ApproveDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.FileContentDTO;
import com.odysseusinc.arachne.portal.config.WebSecurityConfig;
import com.odysseusinc.arachne.portal.exception.AlreadyExistException;
import com.odysseusinc.arachne.portal.exception.IORuntimeException;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.NotUniqueException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.model.AbstractUserStudyListItem;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.AnalysisFile;
import com.odysseusinc.arachne.portal.model.AnalysisUnlockRequest;
import com.odysseusinc.arachne.portal.model.AnalysisUnlockRequestStatus;
import com.odysseusinc.arachne.portal.model.ArachneFile;
import com.odysseusinc.arachne.portal.model.CommentTopic;
import com.odysseusinc.arachne.portal.model.DataReference;
import com.odysseusinc.arachne.portal.model.DataSource;
import com.odysseusinc.arachne.portal.model.Invitationable;
import com.odysseusinc.arachne.portal.model.ResultFile;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.model.SubmissionFile;
import com.odysseusinc.arachne.portal.model.SubmissionInsight;
import com.odysseusinc.arachne.portal.model.SubmissionInsightSubmissionFile;
import com.odysseusinc.arachne.portal.model.SubmissionStatus;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.model.search.StudySearch;
import com.odysseusinc.arachne.portal.model.statemachine.study.StudyState;
import com.odysseusinc.arachne.portal.model.statemachine.study.StudyStateMachine;
import com.odysseusinc.arachne.portal.repository.AnalysisFileRepository;
import com.odysseusinc.arachne.portal.repository.AnalysisUnlockRequestRepository;
import com.odysseusinc.arachne.portal.repository.BaseAnalysisRepository;
import com.odysseusinc.arachne.portal.repository.ResultFileRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionFileRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionInsightRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionInsightSubmissionFileRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionResultFileRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionStatusHistoryRepository;
import com.odysseusinc.arachne.portal.repository.submission.BaseSubmissionRepository;
import com.odysseusinc.arachne.portal.service.BaseStudyService;
import com.odysseusinc.arachne.portal.service.CommentService;
import com.odysseusinc.arachne.portal.service.StudyFileService;
import com.odysseusinc.arachne.portal.service.analysis.BaseAnalysisService;
import com.odysseusinc.arachne.portal.service.impl.AnalysisPreprocessorService;
import com.odysseusinc.arachne.portal.service.impl.CRUDLServiceImpl;
import com.odysseusinc.arachne.portal.service.impl.submission.SubmissionAction;
import com.odysseusinc.arachne.portal.service.impl.submission.SubmissionActionType;
import com.odysseusinc.arachne.portal.service.mail.ArachneMailSender;
import com.odysseusinc.arachne.portal.service.mail.UnlockAnalysisRequestMailMessage;
import com.odysseusinc.arachne.portal.util.AnalysisHelper;
import com.odysseusinc.arachne.portal.util.FileUtils;
import com.odysseusinc.arachne.portal.util.LegacyAnalysisHelper;
import com.odysseusinc.arachne.portal.util.ZipUtil;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipOutputStream;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

public abstract class BaseAnalysisServiceImpl<
        A extends Analysis,
        S extends Study,
        DS extends DataSource,
        SS extends StudySearch,
        SU extends AbstractUserStudyListItem> extends CRUDLServiceImpl<A>
        implements BaseAnalysisService<A> {

    public static final String ILLEGAL_SUBMISSION_STATE_EXCEPTION = "Submission must be in EXECUTED or FAILED state before approve result";
    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseAnalysisServiceImpl.class);
    private static final String CREATING_INSIGHT_LOG = "Creating Insight for Submission with id='{}'";
    private static final String UPDATING_INSIGHT_LOG = "Updating Insight for Submission with id='{}'";
    private static final String SUBMISSION_NOT_EXIST_EXCEPTION = "Submission with id='%s' does not exist";
    private static final String INSIGHT_NOT_EXIST_EXCEPTION = "Insight for Submission with id='%s' does not exist";
    private static final String INSIGHT_ALREADY_EXISTS_EXCEPTION = "Insight for Submission with id='%s' already exists";
    private static final String ANALYSIS_NOT_FOUND_EXCEPTION = "Analysis with id='%s' is not found";
    private static final String RESULT_FILE_NOT_EXISTS_EXCEPTION = "Result file with uuid='%s' for submission with "
            + "id='%s' does not exist";
    private static final String FILE_NOT_UPLOADED_MANUALLY_EXCEPTION = "File %s was not uploaded manually";
    private static final String UNLOCK_REQUEST_ALREADY_EXISTS_EXCEPTION
            = "Unlock request for Analysis with id='%s' was already created by User with id='%s'";
    private static final String UNLOCK_REQUEST_NOT_EXIST_EXCEPTION
            = "Unlock request with id='%s' for User with id='%s' does not exist";

    protected final GenericConversionService conversionService;
    protected final BaseAnalysisRepository<A> analysisRepository;
    protected final BaseSubmissionRepository<Submission> submissionRepository;
    protected final AnalysisFileRepository analysisFileRepository;
    protected final SubmissionFileRepository submissionFileRepository;
    protected final SubmissionResultFileRepository submissionResultFileRepository;
    protected final ResultFileRepository resultFileRepository;
    protected final SubmissionStatusHistoryRepository submissionStatusHistoryRepository;
    protected final SubmissionInsightRepository submissionInsightRepository;
    protected final SubmissionInsightSubmissionFileRepository submissionInsightSubmissionFileRepository;
    protected final CommentService commentService;
    protected final RestTemplate restTemplate;
    protected final LegacyAnalysisHelper legacyAnalysisHelper;
    protected final AnalysisUnlockRequestRepository analysisUnlockRequestRepository;
    protected final ArachneMailSender mailSender;
    protected final SimpMessagingTemplate wsTemplate;
    protected final AnalysisPreprocessorService preprocessorService;
    protected final StudyStateMachine studyStateMachine;
    @Value("${files.store.path}")
    private String fileStorePath;
    protected final BaseStudyService<S, DS, SS, SU> studyService;
    protected final AnalysisHelper analysisHelper;
    protected final StudyFileService fileService;

    public BaseAnalysisServiceImpl(GenericConversionService conversionService,
                                   BaseAnalysisRepository<A> analysisRepository,
                                   BaseSubmissionRepository submissionRepository,
                                   AnalysisFileRepository analysisFileRepository,
                                   SubmissionFileRepository submissionFileRepository,
                                   SubmissionResultFileRepository submissionResultFileRepository,
                                   ResultFileRepository resultFileRepository,
                                   SubmissionStatusHistoryRepository submissionStatusHistoryRepository,
                                   SubmissionInsightRepository submissionInsightRepository,
                                   SubmissionInsightSubmissionFileRepository submissionInsightSubmissionFileRepository,
                                   CommentService commentService,
                                   @SuppressWarnings("SpringJavaAutowiringInspection")
                                   @Qualifier("restTemplate") RestTemplate restTemplate,
                                   LegacyAnalysisHelper legacyAnalysisHelper,
                                   AnalysisUnlockRequestRepository analysisUnlockRequestRepository,
                                   ArachneMailSender mailSender,
                                   SimpMessagingTemplate wsTemplate,
                                   AnalysisPreprocessorService preprocessorService,
                                   StudyStateMachine studyStateMachine,
                                   BaseStudyService<S, DS, SS, SU> studyService,
                                   AnalysisHelper analysisHelper,
                                   StudyFileService fileService) {

        this.conversionService = conversionService;
        this.analysisRepository = analysisRepository;
        this.submissionRepository = submissionRepository;
        this.analysisFileRepository = analysisFileRepository;
        this.submissionFileRepository = submissionFileRepository;
        this.submissionResultFileRepository = submissionResultFileRepository;
        this.resultFileRepository = resultFileRepository;
        this.submissionStatusHistoryRepository = submissionStatusHistoryRepository;
        this.submissionInsightRepository = submissionInsightRepository;
        this.submissionInsightSubmissionFileRepository = submissionInsightSubmissionFileRepository;
        this.commentService = commentService;
        this.restTemplate = restTemplate;
        this.legacyAnalysisHelper = legacyAnalysisHelper;
        this.analysisUnlockRequestRepository = analysisUnlockRequestRepository;
        this.mailSender = mailSender;
        this.wsTemplate = wsTemplate;
        this.preprocessorService = preprocessorService;
        this.studyStateMachine = studyStateMachine;
        this.studyService = studyService;
        this.analysisHelper = analysisHelper;
        this.fileService = fileService;
    }

    @Override
    public CrudRepository<A, Long> getRepository() {

        return analysisRepository;
    }

    @Override
    @PreAuthorize("hasPermission(#analysis, "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).CREATE_ANALYSIS)")
    @PostAuthorize("@ArachnePermissionEvaluator.addPermissions(principal, returnObject )")
    public A create(A analysis)
            throws NotUniqueException, PermissionDeniedException, NotExistException {

        if (analysis.getStudy() == null || analysis.getStudy().getId() == null) {
            throw new NotExistException("Study not exist", Analysis.class);
        }
        List<A> analyses = analysisRepository.findByTitleAndStudyId(analysis.getTitle(), analysis.getStudy().getId());
        if (!analyses.isEmpty()) {
            throw new NotUniqueException("title", "Not unique");
        }
        beforeCreate(analysis);

        final A saved = super.create(analysis);
        afterCreate(saved);

        return saved;
    }

    protected void beforeCreate(A analysis) {

        S foundStudy = studyService.getById(analysis.getStudy().getId());
        analysis.setStudy(foundStudy);
        Integer maxOrd = analysisRepository.getMaxOrd(analysis.getStudy().getId());
        analysis.setOrd(maxOrd != null ? maxOrd + 1 : 0);
        Date date = new Date();
        analysis.setCreated(date);
        analysis.setUpdated(date);

        foundStudy.getAnalyses().add(analysis);
    }

    protected void afterCreate(A analysis) {

        moveStudyToActiveStatusIfAnalysisIsFirst(analysis);
    }


    protected void moveStudyToActiveStatusIfAnalysisIsFirst(A saved) {

//        still trying to find more "declarative" way of doing such things

        Study study = saved.getStudy();
        if (CollectionUtils.size(analysisRepository.findByStudyOrderByOrd(study)) == 1) {
            studyStateMachine.moveToState(study, StudyState.ACTIVE.getStateName());
        }
    }

    @Override
    public A update(A analysis)
            throws NotUniqueException, NotExistException, ValidationException {

        A forUpdate = analysisRepository.findOne(analysis.getId());
        if (forUpdate == null) {
            throw new NotExistException("update: analysis with id=" + analysis.getId() + " not exist", Analysis.class);
        }
        if (analysis.getTitle() != null && !analysis.getTitle().equals(forUpdate.getTitle())) {
            List<A> analyses = analysisRepository.findByTitleAndStudyId(analysis.getTitle(), forUpdate.getStudy().getId());
            if (!analyses.isEmpty()) {
                throw new NotUniqueException("title", "Not unique");
            }
            forUpdate.setTitle(analysis.getTitle());
        }
        if (analysis.getDescription() != null) {
            forUpdate.setDescription(analysis.getDescription());
        }
        final CommonAnalysisType analysisType = analysis.getType();
        if (analysisType != null) {
            forUpdate.setType(analysisType);
        }

        return super.update(forUpdate);
    }

    @Override
    public void delete(Long id) throws NotExistException {

        super.delete(id);
    }

    @Override
    public A getById(Long id) throws NotExistException {

        return analysisRepository.findById(
                id,
                EntityGraphUtils.fromAttributePaths(
                        "submissions",
                        "submissions.author",
                        "submissions.submissionGroup",
                        "submissions.submissionInsight",
                        "submissions.dataSource")
        );
    }


    @Override
    public List<A> list(User user, Long studyId) throws PermissionDeniedException, NotExistException {

        Study study = studyService.getById(studyId);
        return analysisRepository.findByStudyOrderByOrd(study);
    }

    private List<A> list(Study study) {

        return analysisRepository.findByStudyOrderByOrd(study);
    }

    @Override
    public Boolean moveAnalysis(Long id, Integer index) {

        A analysis = analysisRepository.findOne(id);
        Study study = analysis.getStudy();
        List<A> list = list(study);
        list.remove(analysis);
        list.add(index, analysis);
        int ind = 0;
        for (Analysis an : list) {
            an.setOrd(ind++);
        }
        return true;
    }

    @Override
    public AnalysisFile saveFile(MultipartFile multipartFile, User user, A analysis, String label,
                                 Boolean isExecutable, DataReference dataReference) throws IOException {

        String originalFilename = multipartFile.getOriginalFilename();

        String fileNameLowerCase = UUID.randomUUID().toString();
        try {
            Path analysisPath = getAnalysisPath(analysis);
            Path targetPath = Paths.get(analysisPath.toString(), fileNameLowerCase);

            Files.copy(multipartFile.getInputStream(), targetPath, REPLACE_EXISTING);

            AnalysisFile analysisFile = new AnalysisFile();
            analysisFile.setDataReference(dataReference);
            analysisFile.setUuid(fileNameLowerCase);
            analysisFile.setAnalysis(analysis);
            analysisFile.setContentType(CommonFileUtils.getContentType(originalFilename, targetPath.toString()));
            analysisFile.setLabel(label);
            analysisFile.setAuthor(user);
            analysisFile.setUpdatedBy(user);
            analysisFile.setExecutable(false);
            analysisFile.setRealName(originalFilename);
            Date created = new Date();
            analysisFile.setCreated(created);
            analysisFile.setUpdated(created);
            analysisFile.setVersion(1);
            beforeSaveAnalysisFile(analysisFile);

            AnalysisFile saved = analysisFileRepository.save(analysisFile);
            analysis.getFiles().add(saved);
            afterSaveAnalysisFile(saved);

            if (Boolean.TRUE.equals(isExecutable)) {
                setIsExecutable(saved.getUuid());
            }

            preprocessorService.preprocessFile(analysis, analysisFile);

            return saved;

        } catch (IOException | RuntimeException ex) {
            String message = "error save file to disk, filename=" + fileNameLowerCase + " ex=" + ex.toString();
            LOGGER.error(message, ex);
            throw new IOException(message);
        }
    }

    protected void beforeSaveAnalysisFile(AnalysisFile file) {

    }

    protected void afterSaveAnalysisFile(AnalysisFile analysisFile) {

    }

    protected Path getAnalysisPath(Analysis analysis) throws IOException {

        Study study = analysis.getStudy();

        checkDirAndMakeIfNotExist(Paths.get(analysisHelper.getStoreFilesPath()));
        checkDirAndMakeIfNotExist(Paths.get(analysisHelper.getStoreFilesPath(), study.getId().toString()));

        Path desiredAnalysisDirectory = Paths.get(analysisHelper.getStoreFilesPath(),
                study.getId().toString(), analysis.getId().toString());
        checkDirAndMakeIfNotExist(desiredAnalysisDirectory);

        return desiredAnalysisDirectory;
    }

    private void checkDirAndMakeIfNotExist(Path path) throws IOException {

        File dir = path.toFile();
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("Can not create folder: " + dir.getAbsolutePath());
            }
        }
    }

    @Override
    public AnalysisFile saveFile(String link, User user, A analysis, String label, Boolean isExecutable)
            throws IOException {

        throwAccessDeniedExceptionIfLocked(analysis);
        Study study = analysis.getStudy();
        String fileNameLowerCase = UUID.randomUUID().toString();
        try {
            if (link == null) {
                throw new IORuntimeException("wrong url");
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
            HttpEntity<String> entity = new HttpEntity<>(headers);
            URL url = new URL(link);
            String fileName = FilenameUtils.getName(url.getPath());

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    link,
                    HttpMethod.GET, entity, byte[].class);

            if (response.getStatusCode() == HttpStatus.OK) {

                final String contentType = response.getHeaders().getContentType().toString();

                Path pathToAnalysis = getAnalysisPath(analysis);
                Path targetPath = Paths.get(pathToAnalysis.toString(), fileNameLowerCase);

                Files.copy(new ByteArrayInputStream(response.getBody()),
                        pathToAnalysis, REPLACE_EXISTING);
                AnalysisFile analysisFile = new AnalysisFile();
                analysisFile.setUuid(fileNameLowerCase);
                analysisFile.setAnalysis(analysis);
                analysisFile.setContentType(contentType);
                analysisFile.setLabel(label);
                analysisFile.setAuthor(user);
                analysisFile.setExecutable(Boolean.TRUE.equals(isExecutable));
                analysisFile.setRealName(fileName);

                analysisFile.setEntryPoint(fileName);

                Date created = new Date();
                analysisFile.setCreated(created);
                analysisFile.setUpdated(created);
                analysisFile.setVersion(1);
                return analysisFileRepository.save(analysisFile);
            }
        } catch (IOException | RuntimeException ex) {
            String message = "error save file to disk, filename=" + fileNameLowerCase + " ex=" + ex.toString();
            LOGGER.error(message, ex);
            throw new IOException(message);
        }
        return null;
    }

    @Override
    public Path getAnalysisFile(AnalysisFile analysisFile) throws FileNotFoundException {

        Optional.of(analysisFile).orElseThrow(FileNotFoundException::new);
        Path file = Paths.get(analysisHelper.getStoreFilesPath(),
                analysisFile.getAnalysis().getStudy().getId().toString(),
                analysisFile.getAnalysis().getId().toString(),
                analysisFile.getUuid());
        if (Files.notExists(file)) {
            throw new FileNotFoundException();
        }
        return file;
    }

    @Override
    @PreAuthorize("hasPermission(#analysisId,  'Analysis', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).ACCESS_STUDY)")
    public AnalysisFile getAnalysisFile(Long analysisId, String uuid) {

        return getAnalysisFileUnsecured(uuid);
    }

    @Override
    public AnalysisFile getAnalysisFileUnsecured(String uuid) {

        return analysisFileRepository.findByUuid(uuid);
    }

    @Override
    public void lockAnalysisFiles(Long analysisId, Boolean locked) throws NotExistException {

        final Optional<A> analysisOptional = Optional.of(analysisRepository.findOne(analysisId));
        final A analysis = analysisOptional.orElseThrow(() -> {
            String message = String.format(ANALYSIS_NOT_FOUND_EXCEPTION, analysisId);
            return new NotExistException(message, Analysis.class);
        });
        analysis.setLocked(locked);
        analysisRepository.save(analysis);
    }

    @Override
    public AnalysisUnlockRequest sendAnalysisUnlockRequest(
            Long analysisId,
            AnalysisUnlockRequest analysisUnlockRequest
    ) throws NotExistException, AlreadyExistException {

        final Optional<A> analysisOptional = analysisRepository.findByIdAndAndLockedTrue(analysisId);
        final Analysis analysis = analysisOptional.orElseThrow(() ->
                new NotExistException(ANALYSIS_NOT_FOUND_EXCEPTION, Analysis.class)
        );

        User user = analysisUnlockRequest.getUser();
        final AnalysisUnlockRequest existUnlockRequest
                = analysisUnlockRequestRepository.findByAnalysisAndStatus(analysis, AnalysisUnlockRequestStatus.PENDING);
        if (existUnlockRequest != null) {
            String message = String.format(UNLOCK_REQUEST_ALREADY_EXISTS_EXCEPTION, analysis.getId(), user.getId());
            throw new AlreadyExistException(message);
        }
        analysisUnlockRequest.setAnalysis(analysis);
        final AnalysisUnlockRequest savedUnlockRequest = analysisUnlockRequestRepository.save(analysisUnlockRequest);
        studyService.findLeads((S)savedUnlockRequest.getAnalysis().getStudy()).forEach(lead ->
                mailSender.send(new UnlockAnalysisRequestMailMessage(
                        WebSecurityConfig.portalHost.get(), lead, savedUnlockRequest)
                )
        );
        return savedUnlockRequest;
    }

    @Override
    public void processAnalysisUnlockRequest(User user, Long invitationId, Boolean invitationAccepted)
            throws NotExistException {

        final Long userId = user.getId();
        final AnalysisUnlockRequest exist
                = analysisUnlockRequestRepository.findOneByIdAndLeadId(invitationId, userId)
                .orElseThrow(() -> {
                    final String message = String.format(UNLOCK_REQUEST_NOT_EXIST_EXCEPTION, invitationId, userId);
                    return new NotExistException(message, AnalysisUnlockRequest.class);
                });
        if (invitationAccepted != null && invitationAccepted) {
            exist.setStatus(AnalysisUnlockRequestStatus.APPROVED);
            final A analysis = (A) exist.getAnalysis();
            analysis.setLocked(false);
            analysisRepository.save(analysis);
        } else {
            exist.setStatus(AnalysisUnlockRequestStatus.DECLINED);
        }
        analysisUnlockRequestRepository.save(exist);
    }

    @Override
    public Path getSubmissionFile(SubmissionFile submissionFile) throws FileNotFoundException {

        Optional.of(submissionFile).orElseThrow(FileNotFoundException::new);
        Path file = analysisHelper.getSubmissionFile(submissionFile);
        if (Files.notExists(file)) {
            try {
                Optional<Path> olderFile = legacyAnalysisHelper.getOldSubmissionFile(submissionFile);
                file = olderFile.orElseThrow(FileNotFoundException::new);
            } catch (IOException e) {
                LOGGER.error("Failed to get submission file", e);
                throw new FileNotFoundException(e.getMessage());
            }
        }
        return file;
    }

    @Override
    public Path getResultFile(ResultFile resultFile) throws FileNotFoundException {

        Optional.of(resultFile).orElseThrow(FileNotFoundException::new);
        Path file = analysisHelper.getResultFile(resultFile);
        if (Files.notExists(file)) {

            file = legacyAnalysisHelper.getOldResultFile(resultFile);
            if (Files.notExists(file)) {
                throw new FileNotFoundException();
            }
        }
        return file;
    }

    @Override
    public Boolean deleteAnalysisFile(A analysis,
                                      AnalysisFile analysisFile) {

        throwAccessDeniedExceptionIfLocked(analysis);
        return forceDeleteAnalysisFile(analysis, analysisFile);
    }

    @Override
    public Boolean forceDeleteAnalysisFile(A analysis, AnalysisFile analysisFile) {

        Study study = analysis.getStudy();
        analysisFileRepository.delete(analysisFile);
        return Paths.get(analysisHelper.getStoreFilesPath(), study.getId().toString(), analysis.getId().toString(),
                analysisFile.getUuid()).toFile().delete();
    }

    @Override
    public void updateFile(String uuid, MultipartFile file, Long analysisId, Boolean isExecutable)
            throws IOException {

        A analysis = analysisRepository.findOne(analysisId);
        throwAccessDeniedExceptionIfLocked(analysis);
        Study study = analysis.getStudy();
        try {
            AnalysisFile analysisFile = analysisFileRepository.findByUuid(uuid);

            if (file != null) {
                analysisFile.setContentType(file.getContentType());
                analysisFile.setRealName(file.getOriginalFilename());
                Path analysisFolder = analysisHelper.getAnalysisFolder(analysis);
                if (Files.notExists(analysisFolder)) {
                    Files.createDirectories(analysisFolder);
                }
                Files.copy(file.getInputStream(),
                        analysisFolder.resolve(uuid), REPLACE_EXISTING);
            }
            Date updated = new Date();
            analysisFile.setUpdated(updated);
            analysisFile.incrementVersion();
            analysisFile.setExecutable(isExecutable != null && isExecutable);
            analysisFileRepository.save(analysisFile);
        } catch (IOException | RuntimeException ex) {
            String message = "error save file to disk, filename=" + uuid + " ex=" + ex.toString();
            LOGGER.error(message, ex);
            throw new IOException(message);
        }
    }

    @Override
    public void writeToFile(
            AnalysisFile analysisFile,
            FileContentDTO fileContentDTO,
            User updatedBy) throws IOException {

        Analysis analysis = analysisFile.getAnalysis();
        throwAccessDeniedExceptionIfLocked(analysis);
        Study study = analysis.getStudy();
        try {
            Path analysisFolder = analysisHelper.getAnalysisFolder(analysis);
            if (Files.notExists(analysisFolder)) {
                Files.createDirectories(analysisFolder);
            }
            Path target = analysisFolder.resolve(analysisFile.getUuid());
            byte[] content = fileContentDTO.getContent().getBytes(StandardCharsets.UTF_8);
            try (final InputStream stream = new ByteArrayInputStream(content)) {
                Files.copy(stream, target, REPLACE_EXISTING);
            }
            analysisFile.setUpdated(new Date());
            analysisFile.setEntryPoint(analysisFile.getEntryPoint());
            analysisFile.setUpdatedBy(updatedBy);
            analysisFile.incrementVersion();
            analysisFileRepository.save(analysisFile);

        } catch (IOException | RuntimeException ex) {

            String message = "error save file to disk, filename=" + analysisFile.getUuid() + " ex=" + ex.toString();
            LOGGER.error(message, ex);
            throw new IOException(message);
        }
    }

    @Override
    public AnalysisFile saveAnalysisFile(AnalysisFile file) {

        return analysisFileRepository.save(file);
    }

    @Override
    public byte[] getAllBytes(ArachneFile arachneFile) throws IOException {

        Path path = getPath(arachneFile);
        return FileUtils.getBytes(path, checkIfBase64EncodingNeeded(arachneFile));
    }

    private boolean checkIfBase64EncodingNeeded(ArachneFile arachneFile) {

        String contentType = arachneFile.getContentType();
        return Stream.of(CommonFileUtils.TYPE_IMAGE, CommonFileUtils.TYPE_PDF)
                .anyMatch(type -> org.apache.commons.lang3.StringUtils.containsIgnoreCase(contentType, type));
    }

    @Override
    public void deleteSubmissionFile(SubmissionFile file) {

        try {
            Path path = getPath(file);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        submissionFileRepository.delete(file);
    }

    protected Path getPath(ArachneFile arachneFile) throws FileNotFoundException {

        if (arachneFile == null) {
            throw new FileNotFoundException();
        }
        Path path = null;
        if (arachneFile instanceof AnalysisFile) {
            path = analysisHelper.getAnalysisFolder(((AnalysisFile) arachneFile).getAnalysis())
                    .resolve(arachneFile.getUuid());
        } else if (arachneFile instanceof SubmissionFile) {
            path = analysisHelper.getSubmissionFile((SubmissionFile) arachneFile);
        } else if (arachneFile instanceof ResultFile) {
            path = analysisHelper.getResultFile((ResultFile) arachneFile);
        }
        if (Files.notExists(path)) {
            throw new FileNotFoundException();
        }
        return path;
    }

    /**
     * Updates isExecutable flag in a file with the given uuid and sets entry point
     *
     * @param uuid analysis file uuid
     */
    @Override
    public void setIsExecutable(String uuid) {

        AnalysisFile analysisFile = analysisFileRepository.findByUuid(uuid);
        Analysis analysis = analysisFile.getAnalysis();

        throwAccessDeniedExceptionIfLocked(analysis);

        setExecutableFileInAnalysis(analysis, uuid);
    }

    private void setExecutableFileInAnalysis(Analysis analysis, String uuid) {

        clearExecutableFilesInAnalysis(analysis);
        analysis.getFiles().stream()
                .filter(file -> Objects.equal(file.getUuid(), uuid))
                .findFirst().ifPresent(file -> {
            file.setExecutable(Boolean.TRUE);
            checkIfEntryPointIsEmpty(file);
            analysisFileRepository.save(file);
        });
    }

    private void checkIfEntryPointIsEmpty(AnalysisFile file) {

        if (StringUtils.isEmpty(file.getEntryPoint())) {
            file.setEntryPoint(file.getRealName());
        }
    }

    private void clearExecutableFilesInAnalysis(Analysis analysis) {

        analysis.getFiles().stream()
                .filter(AnalysisFile::getExecutable).forEach(file -> {
            file.setExecutable(Boolean.FALSE);
            analysisFileRepository.save(file);
        });
    }

    protected SubmissionStatus beforeApproveSubmissionResult(Submission submission, ApproveDTO approveDTO) {

        if (approveDTO.getIsApproved() == null) {
            throw new IllegalArgumentException("Approving must have not null isApproved parameter");
        }
        return EXECUTED;
    }

    protected SubmissionStatus runApproveSubmissionProcess(Submission submission, SubmissionStatus initialState,
                                                           ApproveDTO action) {

        checkApproveSubmissionResultInitialState(initialState);
        SubmissionStatus result;
        switch (initialState) {
            case EXECUTED:
                result = action.getIsApproved() ? EXECUTED_PUBLISHED : EXECUTED_REJECTED;
                break;
            case FAILED:
                result = action.getIsApproved() ? FAILED_PUBLISHED : FAILED_REJECTED;
                break;
            default:
                throw new IllegalArgumentException(ILLEGAL_SUBMISSION_STATE_EXCEPTION);
        }
        return result;
    }

    protected void checkApproveSubmissionResultInitialState(SubmissionStatus initialState) {

        if (Stream.of(EXECUTED, FAILED).noneMatch(s -> s.equals(initialState))) {
            throw new IllegalArgumentException(ILLEGAL_SUBMISSION_STATE_EXCEPTION);
        }
    }

    protected SubmissionStatus getApproveForExecutionSubmissionStatus(Submission submission, Boolean isApproved) {

        return isApproved ? IN_PROGRESS : NOT_APPROVED;
    }

    private String generateUUID() {

        return UUID.randomUUID().toString().replace("-", "");
    }

    protected SubmissionStatus calculateSubmissionStatusAccordingToDatasourceOwnership(DataSource dataSource, User user) {

        return PENDING;
    }

    @Override
    public List<SubmissionAction> getSubmissionActions(Submission submission) {

        // Approve execution
        SubmissionAction execApproveAction = getExecApproveAction(submission);

        // Manually upload files
        SubmissionAction manualResultUploadAction = getManualResultUploadAction(submission);

        // Publish submission
        SubmissionAction publishAction = getPublishAction(submission);

        return Arrays.asList(execApproveAction, manualResultUploadAction, publishAction);
    }

    protected SubmissionAction getPublishAction(Submission submission) {

        SubmissionAction publishAction = new SubmissionAction(PUBLISH.name());
        publishAction.setAvailable(
                Arrays.asList(EXECUTED, FAILED, IN_PROGRESS).contains(submission.getStatus()));
        switch (submission.getStatus()) {
            case EXECUTED_PUBLISHED:
            case FAILED_PUBLISHED:
                publishAction.setResult(true);
                break;
            case EXECUTED_REJECTED:
            case FAILED_REJECTED:
                publishAction.setResult(false);
                break;
            default:
                publishAction.setResult(null);
                break;
        }
        return publishAction;
    }

    protected SubmissionAction getExecApproveAction(Submission submission) {

        SubmissionAction execApproveAction = new SubmissionAction(EXECUTE.name());
        execApproveAction.setAvailable(submission.getStatus().equals(PENDING));
        switch (submission.getStatus()) {
            case PENDING:
                execApproveAction.setResult(null);
                break;
            case NOT_APPROVED:
                execApproveAction.setResult(false);
                break;
            default:
                execApproveAction.setResult(true);
                break;
        }
        return execApproveAction;
    }

    protected SubmissionAction getManualResultUploadAction(Submission submission) {

        SubmissionAction manualResultUploadAction = new SubmissionAction(SubmissionActionType.MANUAL_UPLOAD.name());
        manualResultUploadAction.setAvailable(
                submission.getStatus().equals(SubmissionStatus.IN_PROGRESS)
        );
        return manualResultUploadAction;
    }

    @Override
    public void getAnalysisAllFiles(Long analysisId, String archiveName, OutputStream os) throws IOException {

        Analysis analysis = analysisRepository.findOne(analysisId);
        Path storeFilesPath = analysisHelper.getAnalysisFolder(analysis);
        try (ZipOutputStream zos = new ZipOutputStream(os)) {
            for (AnalysisFile analysisFile : analysis.getFiles()) {
                String realName = analysisFile.getRealName();
                Path file = storeFilesPath.resolve(analysisFile.getUuid());
                ZipUtil.addZipEntry(zos, realName, file);
            }
        }
    }

    @Override
    public SubmissionInsight getSubmissionInsight(Long submissionId) throws NotExistException {

        final SubmissionInsight insight = submissionInsightRepository.findOneBySubmissionId(submissionId);
        throwNotExistExceptionIfNull(insight, submissionId);
        return insight;
    }

    @Override
    public Set<CommentTopic> getInsightComments(SubmissionInsight insight, Integer size, Sort sort) {

        final Set<CommentTopic> topics = extractCommentTopics(insight);
        return commentService.list(topics, size, sort);
    }

    @Override
    public SubmissionInsight createSubmissionInsight(Long submissionId, SubmissionInsight insight)
            throws AlreadyExistException, NotExistException {

        LOGGER.info(CREATING_INSIGHT_LOG, submissionId);
        final SubmissionInsight submissionInsight = submissionInsightRepository.findOneBySubmissionId(submissionId);
        if (submissionInsight != null) {
            final String message = String.format(INSIGHT_ALREADY_EXISTS_EXCEPTION, submissionId);
            throw new AlreadyExistException(message);
        }
        final List<String> allowedStatuses = Arrays.asList(EXECUTED_PUBLISHED.name(), FAILED_PUBLISHED.name());
        final Submission submission = submissionRepository.findByIdAndStatusIn(submissionId, allowedStatuses);
        throwNotExistExceptionIfNull(submission, submissionId);
        insight.setId(null);
        insight.setCreated(new Date());
        insight.setSubmission(submission);
        final SubmissionInsight savedInsight = submissionInsightRepository.save(insight);
        final List<SubmissionInsightSubmissionFile> submissionInsightSubmissionFiles
                = submission.getSubmissionGroup().getFiles()
                .stream()
                .map(sf -> new SubmissionInsightSubmissionFile(savedInsight, sf, new CommentTopic()))
                .collect(Collectors.toList());
        submissionInsightSubmissionFileRepository.save(submissionInsightSubmissionFiles);
        final List<ResultFile> resultFiles = submission.getResultFiles();
        resultFiles.forEach(resultFile -> resultFile.setCommentTopic(new CommentTopic()));
        submissionResultFileRepository.save(resultFiles);
        return savedInsight;
    }

    @Override
    public void deleteSubmissionInsightSubmissionFileLinks(List<SubmissionInsightSubmissionFile> links) {

        submissionInsightSubmissionFileRepository.delete(links);
    }

    @Override
    public SubmissionInsight updateSubmissionInsight(Long submissionId, SubmissionInsight insight)
            throws NotExistException {

        LOGGER.info(UPDATING_INSIGHT_LOG, submissionId);
        final SubmissionInsight exist = submissionInsightRepository.findOneBySubmissionId(submissionId);
        throwNotExistExceptionIfNull(exist, submissionId);
        if (insight.getName() != null) {
            exist.setName(insight.getName());
        }
        if (insight.getDescription() != null) {
            exist.setDescription(insight.getDescription());
        }
        return submissionInsightRepository.save(exist);
    }

    @Override
    public Page<SubmissionInsight> getInsightsByStudyId(Long studyId, Pageable pageable) {

        final Page<SubmissionInsight> page = submissionInsightRepository.findAllWithCommentsOrDescIsNotEmpty(studyId, pageable);
        final List<SubmissionInsight> insights = page.getContent();
        if (!insights.isEmpty()) {
            final List<Long> ids = insights.stream().map(SubmissionInsight::getId).collect(Collectors.toList());
            final Map<Long, Long> counts = submissionInsightRepository.countCommentsByTopicIds(ids)
                    .stream()
                    .collect(Collectors.toMap(o -> ((BigInteger) o[0]).longValue(), o -> ((BigInteger) o[1]).longValue()));
            insights.forEach(insight -> insight.setCommentsCount(counts.getOrDefault(insight.getId(), 0L)));
        }
        return page;
    }

    @Override
    public List<User> findLeads(Analysis analysis) {

        return studyService.findLeads((S)analysis.getStudy());
    }

    private void throwNotExistExceptionIfNull(SubmissionInsight submissionInsight, Long submissionId) throws NotExistException {

        if (submissionInsight == null) {
            final String message = String.format(INSIGHT_NOT_EXIST_EXCEPTION, submissionId);
            throw new NotExistException(message, SubmissionInsight.class);
        }
    }

    private void throwNotExistExceptionIfNull(Submission submission, Long submissionId) throws NotExistException {

        if (submission == null) {
            String message = String.format(SUBMISSION_NOT_EXIST_EXCEPTION, submissionId);
            throw new NotExistException(message, Submission.class);
        }
    }

    private void throwAccessDeniedExceptionIfLocked(Analysis analysis) {

        if (analysis.getLocked()) {
            final String ANALYSIS_LOCKE_EXCEPTION = "Analysis with id='%s' is locked, file access forbidden";
            final String message = String.format(ANALYSIS_LOCKE_EXCEPTION, analysis.getId());
            throw new AccessDeniedException(message);
        }
    }

    private Set<CommentTopic> extractCommentTopics(SubmissionInsight insight) {

        final Stream<CommentTopic> submissionFilesTopics = insight.getSubmissionInsightSubmissionFiles()
                .stream()
                .map(SubmissionInsightSubmissionFile::getCommentTopic);
        final Submission submission = insight.getSubmission();
        final Stream<CommentTopic> resultFileTopics = submission.getResultFiles()
                .stream()
                .map(ResultFile::getCommentTopic);
        return Stream.concat(submissionFilesTopics, resultFileTopics).map(commentTopic -> {
            commentTopic.setComments(new LinkedList<>());
            return commentTopic;
        }).collect(Collectors.toSet());
    }

    @Override
    public List<? extends Invitationable> getWaitingForApprovalSubmissions(User user) {

        return submissionRepository.findWaitingForApprovalSubmissionsByOwnerId(user.getId());
    }

    @Override
    public void fullDelete(List<A> analyses) {

        for (A analysis : analyses) {

            List<AnalysisFile> files = analysis.getFiles();
            analysisFileRepository.delete(files);
            for (AnalysisFile file : files) {
                deleteAnalysisFile(analysis, file);
            }
        }

        analysisRepository.delete(analyses);
    }

    @Override
    public List<A> findByStudyIds(List<Long> ids) {

        return analysisRepository.findByStudyIdIn(ids);
    }
}
