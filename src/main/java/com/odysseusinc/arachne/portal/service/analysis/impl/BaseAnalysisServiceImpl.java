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

import static com.odysseusinc.arachne.portal.model.SubmissionStatus.EXECUTED;
import static com.odysseusinc.arachne.portal.model.SubmissionStatus.EXECUTED_PUBLISHED;
import static com.odysseusinc.arachne.portal.model.SubmissionStatus.EXECUTED_REJECTED;
import static com.odysseusinc.arachne.portal.model.SubmissionStatus.FAILED;
import static com.odysseusinc.arachne.portal.model.SubmissionStatus.FAILED_PUBLISHED;
import static com.odysseusinc.arachne.portal.model.SubmissionStatus.FAILED_REJECTED;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
import com.odysseusinc.arachne.commons.utils.CommonFileUtils;
import com.odysseusinc.arachne.portal.api.v1.dto.ApproveDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.FileDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UploadFileDTO;
import com.odysseusinc.arachne.portal.config.WebSecurityConfig;
import com.odysseusinc.arachne.portal.exception.AlreadyExistException;
import com.odysseusinc.arachne.portal.exception.IORuntimeException;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.NotUniqueException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.exception.ValidationRuntimeException;
import com.odysseusinc.arachne.portal.model.AbstractUserStudyListItem;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.AnalysisFile;
import com.odysseusinc.arachne.portal.model.AnalysisUnlockRequest;
import com.odysseusinc.arachne.portal.model.AnalysisUnlockRequestStatus;
import com.odysseusinc.arachne.portal.model.AntivirusStatus;
import com.odysseusinc.arachne.portal.model.ArachneFile;
import com.odysseusinc.arachne.portal.model.DataReference;
import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.Invitationable;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.model.SubmissionFile;
import com.odysseusinc.arachne.portal.model.SubmissionStatus;
import com.odysseusinc.arachne.portal.model.search.StudySearch;
import com.odysseusinc.arachne.portal.model.solr.SolrCollection;
import com.odysseusinc.arachne.portal.model.statemachine.study.StudyState;
import com.odysseusinc.arachne.portal.model.statemachine.study.StudyStateMachine;
import com.odysseusinc.arachne.portal.repository.AnalysisFileRepository;
import com.odysseusinc.arachne.portal.repository.AnalysisUnlockRequestRepository;
import com.odysseusinc.arachne.portal.repository.BaseAnalysisRepository;
import com.odysseusinc.arachne.portal.repository.ResultFileRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionFileRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionStatusHistoryRepository;
import com.odysseusinc.arachne.portal.repository.submission.BaseSubmissionRepository;
import com.odysseusinc.arachne.portal.service.BaseSolrService;
import com.odysseusinc.arachne.portal.service.BaseStudyService;
import com.odysseusinc.arachne.portal.service.StudyFileService;
import com.odysseusinc.arachne.portal.service.ToPdfConverter;
import com.odysseusinc.arachne.portal.service.analysis.BaseAnalysisService;
import com.odysseusinc.arachne.portal.service.impl.AnalysisPreprocessorService;
import com.odysseusinc.arachne.portal.service.impl.CRUDLServiceImpl;
import com.odysseusinc.arachne.portal.service.impl.antivirus.events.AntivirusJob;
import com.odysseusinc.arachne.portal.service.impl.antivirus.events.AntivirusJobAnalysisFileResponseEvent;
import com.odysseusinc.arachne.portal.service.impl.antivirus.events.AntivirusJobEvent;
import com.odysseusinc.arachne.portal.service.impl.antivirus.events.AntivirusJobFileType;
import com.odysseusinc.arachne.portal.service.impl.antivirus.events.AntivirusJobResponse;
import com.odysseusinc.arachne.portal.service.impl.solr.SolrField;
import com.odysseusinc.arachne.portal.service.mail.ArachneMailSender;
import com.odysseusinc.arachne.portal.service.mail.UnlockAnalysisRequestMailMessage;
import com.odysseusinc.arachne.portal.util.AnalysisHelper;
import com.odysseusinc.arachne.portal.util.EntityUtils;
import com.odysseusinc.arachne.portal.util.FileUtils;
import com.odysseusinc.arachne.portal.util.LegacyAnalysisHelper;
import com.odysseusinc.arachne.portal.util.ZipUtil;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipOutputStream;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.convert.support.GenericConversionService;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

public abstract class BaseAnalysisServiceImpl<
        A extends Analysis,
        S extends Study,
        DS extends IDataSource,
        SS extends StudySearch,
        SU extends AbstractUserStudyListItem,
        SF extends SolrField> extends CRUDLServiceImpl<A>
        implements BaseAnalysisService<A> {

    public static final String ILLEGAL_SUBMISSION_STATE_EXCEPTION = "Submission must be in EXECUTED or FAILED state before approve result";
    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseAnalysisServiceImpl.class);
    private static final String ANALYSIS_NOT_FOUND_EXCEPTION = "Analysis with id='%s' is not found";
    private static final String RESULT_FILE_NOT_EXISTS_EXCEPTION = "Result file with uuid='%s' for submission with "
            + "id='%s' does not exist";
    private static final String FILE_NOT_UPLOADED_MANUALLY_EXCEPTION = "File %s was not uploaded manually";
    private static final String UNLOCK_REQUEST_ALREADY_EXISTS_EXCEPTION
            = "Unlock request for Analysis with id='%s' was already created by User with id='%s'";
    private static final String UNLOCK_REQUEST_NOT_EXIST_EXCEPTION
            = "Unlock request with id='%s' for User with id='%s' does not exist";

    protected final ToPdfConverter docToPdfConverter;
    protected final GenericConversionService conversionService;
    protected final BaseAnalysisRepository<A> analysisRepository;
    protected final BaseSubmissionRepository<Submission> submissionRepository;
    protected final AnalysisFileRepository analysisFileRepository;
    protected final SubmissionFileRepository submissionFileRepository;
    protected final ResultFileRepository resultFileRepository;
    protected final SubmissionStatusHistoryRepository submissionStatusHistoryRepository;
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
    protected final ApplicationEventPublisher eventPublisher;
    protected final BaseSolrService<SF> solrService;

    public BaseAnalysisServiceImpl(final GenericConversionService conversionService,
                                   final BaseAnalysisRepository<A> analysisRepository,
                                   final BaseSubmissionRepository submissionRepository,
                                   final AnalysisFileRepository analysisFileRepository,
                                   final SubmissionFileRepository submissionFileRepository,
                                   final ResultFileRepository resultFileRepository,
                                   final SubmissionStatusHistoryRepository submissionStatusHistoryRepository,
                                   final @SuppressWarnings("SpringJavaAutowiringInspection")
                                   @Qualifier("restTemplate") RestTemplate restTemplate,
                                   final LegacyAnalysisHelper legacyAnalysisHelper,
                                   final AnalysisUnlockRequestRepository analysisUnlockRequestRepository,
                                   final ArachneMailSender mailSender,
                                   final SimpMessagingTemplate wsTemplate,
                                   final AnalysisPreprocessorService preprocessorService,
                                   final StudyStateMachine studyStateMachine,
                                   final BaseStudyService<S, DS, SS, SU> studyService,
                                   final AnalysisHelper analysisHelper,
                                   final StudyFileService fileService,
                                   final ToPdfConverter docToPdfConverter,
                                   final ApplicationEventPublisher eventPublisher,
                                   final BaseSolrService solrService
    ) {

        this.docToPdfConverter = docToPdfConverter;

        this.conversionService = conversionService;
        this.analysisRepository = analysisRepository;
        this.submissionRepository = submissionRepository;
        this.analysisFileRepository = analysisFileRepository;
        this.submissionFileRepository = submissionFileRepository;
        this.resultFileRepository = resultFileRepository;
        this.submissionStatusHistoryRepository = submissionStatusHistoryRepository;
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
        this.eventPublisher = eventPublisher;
        this.solrService = solrService;
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

        solrService.indexBySolr(analysis);

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
    @PreAuthorize("hasPermission(#analysis,  'Analysis', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).EDIT_ANALYSIS)")
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

        final A saved = super.update(forUpdate);
        solrService.indexBySolr(saved);
        return saved;
    }

    @Override
    public void delete(Long id) throws NotExistException {

        super.delete(id);
    }

    @Override
    public A getById(Long id) throws NotExistException {

        return analysisRepository.findById(
                id,
                EntityUtils.fromAttributePaths(
                        "study",
                        "submissions.author",
                        "submissions.submissionGroup",
                        "submissions.submissionInsight",
                        "submissions.dataSource")
        );
    }


    @Override
    public List<A> list(IUser user, Long studyId) throws PermissionDeniedException, NotExistException {

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

    protected boolean detectExecutable(CommonAnalysisType type, MultipartFile file) {

        return false;
    }

    @Transactional
    @Override
    public List<AnalysisFile> saveFiles(List<UploadFileDTO> files, IUser user, A analysis) throws IOException {

        List<String> errorFileMessages = new ArrayList<>();
        List<AnalysisFile> savedFiles = new ArrayList<>();
        for (UploadFileDTO f : files) {
            try {
                if (StringUtils.hasText(f.getLink())) {
                    savedFiles.add(saveFileByLink(f.getLink(), user, analysis, f.getLabel(), f.getExecutable()));
                } else if (f.getFile() != null) {
                    savedFiles.add(saveFile(f.getFile(), user, analysis, f.getLabel(), f.getExecutable(), null));
                } else {
                    errorFileMessages.add("Invalid file: \"" + f.getLabel() + "\"");
                }
            } catch (AlreadyExistException e) {
                errorFileMessages.add(e.getMessage());
            }
        }
        if (!errorFileMessages.isEmpty()) {
            throw new ValidationRuntimeException("Failed to save files", ImmutableMap.of("file", errorFileMessages));
        }
        return savedFiles;
    }

    @Transactional
    @Override
    public List<AnalysisFile> saveFiles(List<MultipartFile> multipartFiles, IUser user, A analysis, CommonAnalysisType analysisType,
                                        DataReference dataReference) throws IOException {

        List<MultipartFile> filteredFiles = multipartFiles.stream()
                .filter(f -> !CommonAnalysisType.COHORT.equals(analysisType) || !f.getName().endsWith(CommonFileUtils.OHDSI_JSON_EXT))
                .collect(Collectors.toList());
        List<AnalysisFile> savedFiles = new ArrayList<>();
        List<String> errorFileMessages = new ArrayList<>();
        for (MultipartFile f : filteredFiles) {
            try {
                savedFiles.add(saveFile(f, user, analysis, f.getName(), detectExecutable(analysisType, f), dataReference));
            } catch (AlreadyExistException e) {
                errorFileMessages.add(e.getMessage());
            }
        }
        if (!errorFileMessages.isEmpty()) {
            throw new ValidationRuntimeException("Failed to save files", ImmutableMap.of(dataReference.getGuid(), errorFileMessages));
        }
        return savedFiles;
    }

    @Override
    public AnalysisFile saveFile(MultipartFile multipartFile, IUser user, A analysis, String label,
                                 Boolean isExecutable, DataReference dataReference) throws IOException, AlreadyExistException {

        ensureLabelIsUnique(analysis.getId(), label);
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
            eventPublisher.publishEvent(new AntivirusJobEvent(this, new AntivirusJob(saved.getId(), saved.getRealName(), new FileInputStream(targetPath.toString()), AntivirusJobFileType.ANALYSIS_FILE)));
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
    public AnalysisFile saveFileByLink(String link, IUser user, A analysis, String label, Boolean isExecutable)
            throws IOException, AlreadyExistException {

        ensureLabelIsUnique(analysis.getId(), label);
        throwAccessDeniedExceptionIfLocked(analysis);
        String fileNameLowerCase = UUID.randomUUID().toString();
        try {
            if (link == null) {
                throw new IORuntimeException("wrong url");
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
            HttpEntity<String> entity = new HttpEntity<>(headers);
            URL url = new URL(link);

            String originalFileName = FilenameUtils.getName(url.getPath());

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    link,
                    HttpMethod.GET, entity, byte[].class);

            if (response.getStatusCode() == HttpStatus.OK) {

                final String contentType = response.getHeaders().getContentType().toString();

                Path pathToAnalysis = getAnalysisPath(analysis);
                Path targetPath = Paths.get(pathToAnalysis.toString(), fileNameLowerCase);

                Files.copy(new ByteArrayInputStream(response.getBody()),
                        targetPath, REPLACE_EXISTING);
                AnalysisFile analysisFile = new AnalysisFile();
                analysisFile.setUuid(fileNameLowerCase);
                analysisFile.setAnalysis(analysis);
                analysisFile.setContentType(contentType);
                analysisFile.setLabel(label);
                analysisFile.setAuthor(user);
                analysisFile.setExecutable(Boolean.TRUE.equals(isExecutable));
                analysisFile.setRealName(originalFileName);
                analysisFile.setEntryPoint(originalFileName);

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
    @PreAuthorize("hasPermission(#analysis,  "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).ACCESS_STUDY)")
    public List<AnalysisFile> findAnalysisFilesByDataReference(A analysis, DataReference dataReference) {

        return analysisFileRepository.findAllByAnalysisIdAndDataReferenceId(analysis.getId(), dataReference.getId());
    }

    @Override
    @PreAuthorize("hasPermission(#analysisId,  'Analysis', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).ACCESS_STUDY)")
    @PostAuthorize("@ArachnePermissionEvaluator.addPermissions(principal, returnObject )")
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

        IUser user = analysisUnlockRequest.getUser();
        final AnalysisUnlockRequest existUnlockRequest
                = analysisUnlockRequestRepository.findByAnalysisAndStatus(analysis, AnalysisUnlockRequestStatus.PENDING);
        if (existUnlockRequest != null) {
            String message = String.format(UNLOCK_REQUEST_ALREADY_EXISTS_EXCEPTION, analysis.getId(), user.getId());
            throw new AlreadyExistException(message);
        }
        analysisUnlockRequest.setAnalysis(analysis);
        final AnalysisUnlockRequest savedUnlockRequest = analysisUnlockRequestRepository.save(analysisUnlockRequest);
        studyService.findLeads((S) savedUnlockRequest.getAnalysis().getStudy()).forEach(lead ->
                mailSender.send(new UnlockAnalysisRequestMailMessage(
                        WebSecurityConfig.getDefaultPortalURI(), lead, savedUnlockRequest)
                )
        );
        return savedUnlockRequest;
    }

    @Override
    public void processAnalysisUnlockRequest(IUser user, Long invitationId, Boolean invitationAccepted)
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
                analysisFile.setRealName(file.getOriginalFilename());
                Path analysisFolder = analysisHelper.getAnalysisFolder(analysis);
                if (Files.notExists(analysisFolder)) {
                    Files.createDirectories(analysisFolder);
                }
                Path targetPath = analysisFolder.resolve(uuid);
                Files.copy(file.getInputStream(), targetPath, REPLACE_EXISTING);
                String contentType = CommonFileUtils.getContentType(file.getOriginalFilename(), targetPath.toString());
                analysisFile.setContentType(contentType);
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
    @PreAuthorize("hasPermission(#analysisFile, "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).DELETE_ANALYSIS_FILES)")
    public void updateCodeFile(
            final AnalysisFile analysisFile,
            final FileDTO fileDTO,
            final IUser updatedBy) throws IOException {

        final Analysis analysis = analysisFile.getAnalysis();
        
        throwAccessDeniedExceptionIfLocked(analysis);

        final String content = fileDTO.getContent();
        if (content != null) {
            writeContentAndCheckForViruses(analysisFile, updatedBy, content);
        }
        
        final String name = fileDTO.getName();
        if (!StringUtils.isEmpty(name)) {
            analysisFile.setLabel(name);
        }

        analysisFileRepository.saveAndFlush(analysisFile);
    }

    private void writeContentAndCheckForViruses(final AnalysisFile analysisFile, final IUser updatedBy, final String content) throws IOException {

        try {
            final Analysis analysis = analysisFile.getAnalysis();
            Path analysisFolder = analysisHelper.getAnalysisFolder(analysis);
            if (Files.notExists(analysisFolder)) {
                Files.createDirectories(analysisFolder);
            }
            Path targetPath = analysisFolder.resolve(analysisFile.getUuid());
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
            try (final InputStream stream = new ByteArrayInputStream(bytes)) {
                Files.copy(stream, targetPath, REPLACE_EXISTING);
            }
            analysisFile.setUpdated(new Date());
            analysisFile.setEntryPoint(analysisFile.getEntryPoint());
            analysisFile.setUpdatedBy(updatedBy);
            analysisFile.setContentType(CommonFileUtils.getContentType(analysisFile.getName(), targetPath.toString()));

            analysisFile.incrementVersion();
            analysisFile.setAntivirusStatus(AntivirusStatus.SCANNING);
            analysisFile.setAntivirusDescription(null);
            eventPublisher.publishEvent(new AntivirusJobEvent(this, new AntivirusJob(analysisFile.getId(), analysisFile.getName(), new FileInputStream(targetPath.toString()), AntivirusJobFileType.ANALYSIS_FILE)));

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
        return FileUtils.getBytes(path, arachneFile.getContentType());
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

    @Override
    public Path getPath(ArachneFile arachneFile) throws FileNotFoundException {

        if (arachneFile == null) {
            throw new FileNotFoundException();
        }
        Path path = null;
        if (arachneFile instanceof AnalysisFile) {
            path = analysisHelper.getAnalysisFolder(((AnalysisFile) arachneFile).getAnalysis())
                    .resolve(arachneFile.getUuid());
        } else if (arachneFile instanceof SubmissionFile) {
            path = analysisHelper.getSubmissionFile((SubmissionFile) arachneFile);
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
    public List<IUser> findLeads(Analysis analysis) {

        return studyService.findLeads((S) analysis.getStudy());
    }

    private void throwAccessDeniedExceptionIfLocked(Analysis analysis) {

        if (analysis.getLocked()) {
            final String ANALYSIS_LOCKE_EXCEPTION = "Analysis with id='%s' is locked, file access forbidden";
            final String message = String.format(ANALYSIS_LOCKE_EXCEPTION, analysis.getId());
            throw new AccessDeniedException(message);
        }
    }

    private void ensureLabelIsUnique(Long analysisId, String label) throws AlreadyExistException {

        if (!analysisFileRepository.findAllByAnalysisIdAndLabel(analysisId, label).isEmpty()) {
            throw new AlreadyExistException("File with such name " + label + " already exists");
        }
    }

    @Override
    public List<? extends Invitationable> getWaitingForApprovalSubmissions(IUser user) {

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

    @Override
    public List<A> getByIdIn(List<Long> ids) {

        return analysisRepository.findByIdIn(ids);
    }

    @Override
    public List<A> getByStudyId(Long id, EntityGraph graph) {

        return analysisRepository.findByStudyId(id, graph);
    }

    @EventListener
    @Transactional
    @Override
    public void processAntivirusResponse(AntivirusJobAnalysisFileResponseEvent event) {

        final AntivirusJobResponse antivirusJobResponse = event.getAntivirusJobResponse();
        final AnalysisFile analysisFile = analysisFileRepository.findOne(antivirusJobResponse.getFileId());
        if (analysisFile != null) {
            analysisFile.setAntivirusStatus(antivirusJobResponse.getStatus());
            analysisFile.setAntivirusDescription(antivirusJobResponse.getDescription());
            analysisFileRepository.save(analysisFile);
        }
    }

    @Override
    public void indexAllBySolr() throws IOException, NotExistException, SolrServerException, NoSuchFieldException, IllegalAccessException {

        solrService.deleteAll(SolrCollection.ANALYSES);

        final Map<Long, Study> map = studyService.findWithAnalysesInAnyTenant()
                .stream()
                .collect(Collectors.toMap(Study::getId, Function.identity()));
        final List<A> analyses = analysisRepository.findAll();
        for (final A analysis : analyses) {
            analysis.setStudy(map.get(analysis.getStudy().getId()));
        }
        solrService.indexBySolr(analyses);
    }

    @Override
    public void indexBySolr(final A analysis)
            throws IllegalAccessException, IOException, SolrServerException, NotExistException, NoSuchFieldException {

        solrService.indexBySolr(analysis);
    }
}
