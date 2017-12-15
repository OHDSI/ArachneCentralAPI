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

import static com.odysseusinc.arachne.portal.model.SubmissionStatus.EXECUTED;
import static com.odysseusinc.arachne.portal.model.SubmissionStatus.EXECUTED_PUBLISHED;
import static com.odysseusinc.arachne.portal.model.SubmissionStatus.EXECUTED_REJECTED;
import static com.odysseusinc.arachne.portal.model.SubmissionStatus.FAILED;
import static com.odysseusinc.arachne.portal.model.SubmissionStatus.FAILED_PUBLISHED;
import static com.odysseusinc.arachne.portal.model.SubmissionStatus.FAILED_REJECTED;
import static com.odysseusinc.arachne.portal.model.SubmissionStatus.IN_PROGRESS;
import static com.odysseusinc.arachne.portal.model.SubmissionStatus.NOT_APPROVED;
import static com.odysseusinc.arachne.portal.model.SubmissionStatus.PENDING;
import static com.odysseusinc.arachne.portal.model.SubmissionStatus.valueOf;
import static com.odysseusinc.arachne.portal.util.DataNodeUtils.isDataNodeOwner;

import com.odysseusinc.arachne.portal.api.v1.dto.ApproveDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UpdateNotificationDTO;
import com.odysseusinc.arachne.portal.config.WebSecurityConfig;
import com.odysseusinc.arachne.portal.exception.NoExecutableFileException;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.AnalysisFile;
import com.odysseusinc.arachne.portal.model.DataSource;
import com.odysseusinc.arachne.portal.model.ResultEntity;
import com.odysseusinc.arachne.portal.model.ResultFile;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.model.SubmissionFile;
import com.odysseusinc.arachne.portal.model.SubmissionGroup;
import com.odysseusinc.arachne.portal.model.SubmissionInsight;
import com.odysseusinc.arachne.portal.model.SubmissionStatus;
import com.odysseusinc.arachne.portal.model.SubmissionStatusHistoryElement;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.model.search.ResultEntitySpecification;
import com.odysseusinc.arachne.portal.model.search.ResultFileSearch;
import com.odysseusinc.arachne.portal.repository.ResultFileRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionFileRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionGroupRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionInsightRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionResultFileRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionStatusHistoryRepository;
import com.odysseusinc.arachne.portal.repository.submission.BaseSubmissionRepository;
import com.odysseusinc.arachne.portal.service.BaseDataSourceService;
import com.odysseusinc.arachne.portal.service.mail.ArachneMailSender;
import com.odysseusinc.arachne.portal.service.mail.InvitationApprovalSubmissionArachneMailMessage;
import com.odysseusinc.arachne.portal.service.submission.BaseSubmissionService;
import com.odysseusinc.arachne.portal.util.AnalysisHelper;
import com.odysseusinc.arachne.portal.util.AnalysisUtils;
import com.odysseusinc.arachne.portal.util.DataNodeUtils;
import com.odysseusinc.arachne.portal.util.LegacyAnalysisHelper;
import com.odysseusinc.arachne.portal.util.SubmissionHelper;
import com.odysseusinc.arachne.portal.util.UUIDGenerator;
import com.odysseusinc.arachne.portal.util.ZipUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipOutputStream;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

public abstract class BaseSubmissionServiceImpl<T extends Submission, A extends Analysis, DS extends DataSource>
        implements BaseSubmissionService<T, A> {

    public static final String SUBMISSION_NOT_EXIST_EXCEPTION = "Submission with id='%s' does not exist";
    public static final String ILLEGAL_SUBMISSION_STATE_EXCEPTION = "Submission must be in EXECUTED or FAILED state before approve result";
    public static final String DELETING_INSIGHT_LOG = "Deleting Insight for Submission with id='{}'";
    public static final String INSIGHT_NOT_EXIST_EXCEPTION = "Insight for Submission with id='%s' does not exist";
    public static final String RESULT_FILE_NOT_EXISTS_EXCEPTION = "Result file with uuid='%s' for submission with "
            + "id='%s' does not exist";
    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseSubmissionService.class);
    private static final String FILE_NOT_UPLOADED_MANUALLY_EXCEPTION = "File %s was not uploaded manually";
    protected final BaseSubmissionRepository<T> submissionRepository;
    protected final BaseDataSourceService<DS> dataSourceService;
    protected final ArachneMailSender mailSender;
    protected final AnalysisHelper analysisHelper;
    protected final SimpMessagingTemplate wsTemplate;
    protected final LegacyAnalysisHelper legacyAnalysisHelper;
    protected final SubmissionResultFileRepository submissionResultFileRepository;
    protected final SubmissionGroupRepository submissionGroupRepository;
    protected final SubmissionInsightRepository submissionInsightRepository;
    protected final SubmissionFileRepository submissionFileRepository;
    protected final ResultFileRepository resultFileRepository;
    protected final SubmissionStatusHistoryRepository submissionStatusHistoryRepository;
    protected final EntityManager entityManager;
    protected final SubmissionHelper submissionHelper;

    @Value("${files.store.path}")
    private String fileStorePath;

    protected BaseSubmissionServiceImpl(BaseSubmissionRepository<T> submissionRepository,
                                        BaseDataSourceService<DS> dataSourceService,
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
                                        EntityManager entityManager, SubmissionHelper submissionHelper) {

        this.submissionRepository = submissionRepository;
        this.dataSourceService = dataSourceService;
        this.mailSender = mailSender;
        this.analysisHelper = analysisHelper;
        this.wsTemplate = wsTemplate;
        this.legacyAnalysisHelper = legacyAnalysisHelper;
        this.submissionResultFileRepository = submissionResultFileRepository;
        this.submissionGroupRepository = submissionGroupRepository;
        this.submissionInsightRepository = submissionInsightRepository;
        this.submissionFileRepository = submissionFileRepository;
        this.resultFileRepository = resultFileRepository;
        this.submissionStatusHistoryRepository = submissionStatusHistoryRepository;
        this.entityManager = entityManager;
        this.submissionHelper = submissionHelper;
    }

    @Override
    public T approveSubmissionResult(Long submissionId, ApproveDTO approveDTO, User user) {

        T submission = submissionRepository.findOne(submissionId);
        SubmissionStatus status = runApproveSubmissionProcess(submission,
                beforeApproveSubmissionResult(submission, approveDTO), approveDTO);
        List<SubmissionStatusHistoryElement> statusHistory = submission.getStatusHistory();
        statusHistory.add(new SubmissionStatusHistoryElement(new Date(), status, user, submission, approveDTO.getComment()));
        submission.setStatusHistory(statusHistory);
        submissionHelper.updateSubmissionExtendedInfo(submission);
        submission = saveSubmission(submission);

        notifyOwnersAboutSubmissionUpdateViaSocket(submission);

        return submission;
    }

    @Override
    public T createSubmission(User user, A analysis, Long datasourceId,
                              SubmissionGroup submissionGroup)
            throws NotExistException, IOException {

        DataSource dataSource = dataSourceService.getByIdUnsecured(datasourceId);
        T submission = newSubmission();
        submission.setAuthor(user);
        Date created = new Date();
        submission.setCreated(created);
        submission.setUpdated(created);
        submission.setAnalysis(analysis);
        submission.setDataSource(dataSource);

        SubmissionStatus status = calculateSubmissionStatusAccordingToDatasourceOwnership(dataSource, user);

        List<SubmissionStatusHistoryElement> statusHistory = new LinkedList<>();
        SubmissionStatusHistoryElement statusHistoryElement =
                new SubmissionStatusHistoryElement(created, status, user, submission, null);

        statusHistory.add(statusHistoryElement);
        submission.setStatusHistory(statusHistory);
        submission.setSubmissionGroup(submissionGroup);

        submission.setToken(UUIDGenerator.generateUUID());

        submission.setUpdatePassword(UUIDGenerator.generateUUID());
        beforeCreateSubmission(submission);
        submission = saveSubmissionAndFlush(submission);

        afterCreateSubmission(submission);
        return submission;
    }

    protected abstract T newSubmission();

    protected void beforeCreateSubmission(Submission submission) {

    }

    protected void afterCreateSubmission(Submission submission) {

    }

    protected SubmissionStatus calculateSubmissionStatusAccordingToDatasourceOwnership(DataSource dataSource, User user) {

        return PENDING;
    }

    @Override
    public void notifyOwnersAboutNewSubmission(T submission) {

        Set<User> dnOwners = DataNodeUtils.getDataNodeOwners(submission.getDataSource().getDataNode());

        try {
            for (User owner : dnOwners) {
                mailSender.send(new InvitationApprovalSubmissionArachneMailMessage(
                        WebSecurityConfig.portalHost.get(), owner, submission)
                );
            }
        } catch (Exception ignore) {
            LOGGER.error(ignore.getLocalizedMessage());
        }

        notifyOwnersAboutSubmissionUpdateViaSocket(submission);
    }

    @Override
    public void notifyOwnersAboutSubmissionUpdateViaSocket(T submission) {

        Set<User> dnOwners = DataNodeUtils.getDataNodeOwners(submission.getDataSource().getDataNode());

        for (User owner : dnOwners) {
            wsTemplate.convertAndSendToUser(
                    owner.getUsername(),
                    "/topic/invitations",
                    new UpdateNotificationDTO()
            );
        }
    }

    @Override
    public T saveSubmissionAndFlush(T submission) {

        return submissionRepository.saveAndFlush(submission);
    }

    @Override
    public T moveSubmissionToNewStatus(T submission, SubmissionStatus status, User user, String comment) {

        List<SubmissionStatusHistoryElement> statusHistory = submission.getStatusHistory();
        statusHistory.add(new SubmissionStatusHistoryElement(new Date(), status, user, submission, comment));
        submission.setStatusHistory(statusHistory);
        return saveSubmission(submission);
    }

    @Override
    public T changeSubmissionState(Long id, String status) {

        T submission = submissionRepository.findOne(id);
        SubmissionStatus submissionStatus = valueOf(status);
        List<SubmissionStatusHistoryElement> statusHistory = submission.getStatusHistory();
        statusHistory.add(new SubmissionStatusHistoryElement(new Date(), submissionStatus, null, submission, null));
        submission.setStatusHistory(statusHistory);
        return saveSubmission(submission);
    }

    @Override
    public T getSubmissionById(Long id) throws NotExistException {

        T submission = submissionRepository.findOne(id);
        throwNotExistExceptionIfNull(submission, id);
        return submission;
    }

    @Override
    public T getSubmissionByIdAndStatus(Long id, SubmissionStatus status) throws NotExistException {

        return throwNotExistExceptionIfNull(submissionRepository.findByIdAndStatusIn(id,
                Collections.singletonList(status.name())), id);
    }

    @Override
    public T getSubmissionByIdAndUpdatePasswordAndStatus(
            Long id, String updatePassword, List<SubmissionStatus> statusList) throws NotExistException {

        return throwNotExistExceptionIfNull(submissionRepository.findByIdAndUpdatePasswordAndStatusIn(id, updatePassword,
                statusList.stream().map(Enum::name).collect(Collectors.toList())), id);
    }

    @Override
    public T saveSubmission(T submission) {

        return submissionRepository.save(submission);
    }

    @Override
    public SubmissionGroup createSubmissionGroup(User user, Analysis analysis) throws IOException, NoExecutableFileException {

        checkBeforeCreateSubmissionGroup(analysis);

        List<SubmissionFile> files = new LinkedList<>();
        SubmissionGroup submissionGroup = new SubmissionGroup();
        submissionGroup.setAnalysis(analysis);
        submissionGroup.setAnalysisType(analysis.getType());
        submissionGroup.setAuthor(user);
        Date now = new Date();
        submissionGroup.setCreated(now);
        submissionGroup.setUpdated(now);
        submissionGroup.setFiles(files);
        submissionGroup = submissionGroupRepository.save(submissionGroup);

        Path submissionGroupFolder = analysisHelper.getSubmissionGroupFolder(submissionGroup);
        if (Files.notExists(submissionGroupFolder)) {
            Files.createDirectories(submissionGroupFolder);
        }
        for (AnalysisFile analysisFile : analysis.getFiles()) {
            SubmissionFile submissionFile = new SubmissionFile();
            String storeFilesPath = fileStorePath + File.separator + "content";
            String uuid = UUID.randomUUID().toString();

            submissionFile.setSubmissionGroup(submissionGroup);
            submissionFile.setContentType(analysisFile.getContentType());
            submissionFile.setCreated(analysisFile.getCreated());
            submissionFile.setUpdated(analysisFile.getUpdated());
            submissionFile.setLabel(analysisFile.getLabel());
            submissionFile.setRealName(analysisFile.getRealName());
            submissionFile.setEntryPoint(analysisFile.getEntryPoint());
            submissionFile.setUuid(uuid);
            submissionFile.setAuthor(analysisFile.getAuthor());
            submissionFile.setVersion(analysisFile.getVersion());
            Boolean isExecutable = analysisFile.getExecutable();
            submissionFile.setExecutable(isExecutable != null && isExecutable);
            Path analysisFileContent = Paths.get(storeFilesPath, analysis.getStudy().getId().toString(),
                    analysis.getId().toString(), analysisFile.getUuid());
            Path submissionFileContent = submissionGroupFolder.resolve(uuid);
            Path target = Files.copy(analysisFileContent, submissionFileContent, StandardCopyOption.REPLACE_EXISTING);
            try (InputStream in = Files.newInputStream(target)) {
                String checksum = DigestUtils.md5DigestAsHex(in);
                submissionFile.setChecksum(checksum);
            }
            files.add(submissionFile);
        }
        submissionFileRepository.save(files);
        submissionGroup.setChecksum(calculateMD5Hash(submissionGroupFolder, files));
        submissionGroupRepository.save(submissionGroup);
        return submissionGroup;
    }

    protected void checkBeforeCreateSubmissionGroup(Analysis analysis) throws NoExecutableFileException {
    }

    @Override
    public List<SubmissionStatusHistoryElement> getSubmissionStatusHistory(Long analysisId, Long submissionId) {

        return submissionStatusHistoryRepository.findBySubmissionIdOrderByDate(submissionId);
    }

    @Override
    @PreAuthorize("hasPermission(#submissionId,  'Submission', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).EDIT_ANALYSIS)")
    public void deleteSubmissionInsight(Long submissionId) throws NotExistException {

        LOGGER.info(DELETING_INSIGHT_LOG, submissionId);
        final T submission = submissionRepository.findOne(submissionId);
        throwNotExistExceptionIfNull(submission, submissionId);
        final SubmissionInsight submissionInsight = submissionInsightRepository.findOneBySubmissionId(submissionId);
        throwNotExistExceptionIfNull(submissionInsight, submissionId);
        final List<ResultFile> resultFiles = submission.getResultFiles();
        resultFiles.forEach(resultFile -> resultFile.setCommentTopic(null));
        submissionResultFileRepository.save(resultFiles);
        submissionInsightRepository.deleteBySubmissionId(submissionId);
    }

    @Override
    public void tryDeleteSubmissionInsight(Long submissionInsightId) {

        submissionInsightRepository.delete(submissionInsightId);
    }

    @Override
    public boolean deleteSubmissionResultFile(Long submissionId, String fileUuid)
            throws NotExistException, ValidationException {

        final T submission = submissionRepository.findByIdAndStatusIn(submissionId,
                Collections.singletonList(IN_PROGRESS.name()));
        throwNotExistExceptionIfNull(submission, submissionId);
        ResultFile resultFile = submissionResultFileRepository.findByUuid(fileUuid);
        Optional.ofNullable(resultFile).orElseThrow(() ->
                new NotExistException(String.format(RESULT_FILE_NOT_EXISTS_EXCEPTION,
                        fileUuid, submission.getId()), ResultFile.class));
        if (!resultFile.isManuallyUploaded()) {
            throw new ValidationException(String.format(FILE_NOT_UPLOADED_MANUALLY_EXCEPTION, fileUuid));
        }
        deleteSubmissionResultFile(resultFile);
        submission.getResultFiles().remove(resultFile);
        submission.setUpdated(new Date());
        saveSubmission(submission);
        return true;
    }

    @Override
    public void deleteSubmissionResultFile(ResultFile resultFile) {

        submissionResultFileRepository.delete(resultFile);
        Path resultPath = analysisHelper.getResultFile(resultFile);
        try {
            Files.deleteIfExists(resultPath);
        } catch (IOException e) {
            LOGGER.error("Failed to deleteComment result file", e);
        }
    }

    protected SubmissionStatus beforeApproveSubmissionResult(T submission, ApproveDTO approveDTO) {

        if (approveDTO.getIsApproved() == null) {
            throw new IllegalArgumentException("Approving must have not null isApproved parameter");
        }
        return EXECUTED;
    }

    protected SubmissionStatus runApproveSubmissionProcess(T submission, SubmissionStatus initialState,
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

    protected T throwNotExistExceptionIfNull(T submission, Long submissionId) throws NotExistException {

        if (Objects.isNull(submission)) {
            String message = String.format(SUBMISSION_NOT_EXIST_EXCEPTION, submissionId);
            throw new NotExistException(message, Submission.class);
        }
        return submission;
    }

    private void throwNotExistExceptionIfNull(SubmissionInsight submissionInsight, Long submissionId) throws NotExistException {

        if (submissionInsight == null) {
            final String message = String.format(INSIGHT_NOT_EXIST_EXCEPTION, submissionId);
            throw new NotExistException(message, SubmissionInsight.class);
        }
    }

    @Override
    public T getSubmissionByIdAndToken(Long id, String token) throws NotExistException {

        return submissionRepository.findByIdAndToken(id, token).orElseThrow(() -> new NotExistException(Submission.class));
    }

    @Override
    public ResultFile uploadResultsByDataOwner(Long submissionId, MultipartFile file) throws NotExistException, IOException {

        T submission = submissionRepository.findByIdAndStatusIn(submissionId,
                Collections.singletonList(IN_PROGRESS.name()));
        throwNotExistExceptionIfNull(submission, submissionId);
        Path zipArchiveResultPath = analysisHelper.getResultArchPath(submission);
        Path submissionResultPath = analysisHelper.getResultFolder(submission);
        writeSubmissionResultFiles(submission, zipArchiveResultPath, file);
        ResultFile resultFile = AnalysisUtils.createResultFile(submissionResultPath, zipArchiveResultPath, file.getOriginalFilename(),
                submission);
        resultFile.setManuallyUploaded(true);
        Date updated = new Date();
        List<ResultFile> resultFiles = submission.getResultFiles();
        resultFiles.add(resultFile);
        submission.setUpdated(updated);
        submissionResultFileRepository.save(resultFiles);
        saveSubmission(submission);
        return resultFile;
    }

    @Override
    public void getSubmissionAllFiles(Long submissionGroupId, String archiveName, OutputStream os) throws IOException {

        SubmissionGroup submissionGroup = submissionGroupRepository.findOne(submissionGroupId);
        Path storeFilesPath = analysisHelper.getSubmissionGroupFolder(submissionGroup);
        try (ZipOutputStream zos = new ZipOutputStream(os)) {
            for (SubmissionFile submissionFile : submissionGroup.getFiles()) {
                String realName = submissionFile.getRealName();
                Path file = storeFilesPath.resolve(submissionFile.getUuid());
                if (Files.notExists(file)) {
                    file = legacyAnalysisHelper.getOldSubmissionFile(submissionFile).orElseThrow(FileNotFoundException::new);
                }
                if (Files.exists(file)) {
                    ZipUtil.addZipEntry(zos, realName, file);
                }
            }
        }
    }

    @Override
    public Path getSubmissionArchiveChunk(Long id, String updatePassword, String fileName) throws FileNotFoundException {

        Path file;
        Submission submission = submissionRepository.findByIdAndUpdatePassword(id, updatePassword);
        if (submission != null) {
            file = analysisHelper.getSplittedFolder(submission.getSubmissionGroup())
                    .resolve(fileName);
            if (Files.notExists(file)) {
                throw new FileNotFoundException();
            }
        } else {
            throw new FileNotFoundException();
        }
        return file;
    }

    @Override
    public T approveSubmission(Long submissionId, Boolean isApproved, String comment, User user)
            throws IOException, NotExistException {

        T submission = getSubmissionByIdAndStatus(submissionId, PENDING);
        SubmissionStatus status = getApproveForExecutionSubmissionStatus(submission, isApproved);
        submission = moveSubmissionToNewStatus(submission, status, user, comment);

        notifyOwnersAboutSubmissionUpdateViaSocket(submission);

        return submission;
    }

    @Override
    public SubmissionGroup getSubmissionGroupById(Long id) throws NotExistException {

        return submissionGroupRepository.findOne(id);
    }

    @Override
    public void deleteSubmissionStatusHistory(List<SubmissionStatusHistoryElement> statusHistory) {

        submissionStatusHistoryRepository.delete(statusHistory);
    }

    @Override
    public SubmissionStatusHistoryElement getSubmissionStatusHistoryElementById(Long id) {

        return submissionStatusHistoryRepository.findOne(id);
    }

    @Override
    public void deleteSubmissions(List<T> submissions) {

        submissionRepository.delete(submissions);
    }

    @Override
    public void deleteSubmissionGroups(List<SubmissionGroup> groups) {

        submissionGroupRepository.delete(groups);
    }

    @Override
    public List<ResultEntity> getResultFiles(User user, Long submissionId, ResultFileSearch resultFileSearch) throws PermissionDeniedException {

        Submission submission = submissionRepository.findById(submissionId);
        if (!(EXECUTED_PUBLISHED.equals(submission.getStatus()) || FAILED_PUBLISHED.equals(submission.getStatus()))) {
            if (user == null || !isDataNodeOwner(submission.getDataSource().getDataNode(), user)) {
                throw new PermissionDeniedException();
            }
        }
        resultFileSearch.setSubmission(submission);

        ResultEntitySpecification spec = new ResultEntitySpecification(resultFileSearch);

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery criteriaQuery = criteriaBuilder.createQuery(ResultEntity.class);

        Root<ResultFile> root = criteriaQuery.from(ResultEntity.class);

        criteriaQuery.select(spec.getSelection(root, criteriaBuilder));
        criteriaQuery.where(spec.toPredicate(root, criteriaQuery, criteriaBuilder));
        criteriaQuery.orderBy(spec.getOrderBy(root, criteriaBuilder));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    @Override
    public ResultFile getResultFileAndCheckPermission(User user, Long analysisId, String uuid) throws PermissionDeniedException {

        ResultFile byUuid = getResultFileByUUID(uuid);
        Submission submission = byUuid.getSubmission();
        if (!(EXECUTED_PUBLISHED.equals(submission.getStatus()) || FAILED_PUBLISHED.equals(submission.getStatus()))) {
            if (user == null || !isDataNodeOwner(submission.getDataSource().getDataNode(), user)) {
                throw new PermissionDeniedException();
            }
        }
        return byUuid;
    }

    @Override
    public ResultFile getResultFileByUUID(String uuid) {

        return resultFileRepository.findByUuid(uuid);
    }

    @Override
    public void getSubmissionResultAllFiles(
            User user,
            Long analysisId,
            Long submissionId,
            String archiveName,
            OutputStream os) throws
            IOException, PermissionDeniedException {

        Submission submission = submissionRepository.findOne(submissionId);
        if (!(EXECUTED_PUBLISHED.equals(submission.getStatus()) || FAILED_PUBLISHED.equals(submission.getStatus()))) {
            if (user == null || !isDataNodeOwner(submission.getDataSource().getDataNode(), user)) {
                throw new PermissionDeniedException();
            }
        }
        Path storeFilesPath = analysisHelper.getSubmissionResultFolder(submission);
        if (Files.notExists(storeFilesPath)) {
            storeFilesPath = legacyAnalysisHelper.getOldSubmissionResultFolder(submission);
            if (Files.notExists(storeFilesPath)) {
                throw new FileNotFoundException();
            }
        }
        try (ZipOutputStream zos = new ZipOutputStream(os)) {
            for (ResultFile resultFile : submission.getResultFiles()) {
                String realName = resultFile.getRealName();
                Path file = storeFilesPath.resolve(resultFile.getUuid());
                if (Files.notExists(file)) {
                    file = legacyAnalysisHelper.getOldResultFile(resultFile);
                }
                if (Files.exists(file)) {
                    ZipUtil.addZipEntry(zos, realName, file);
                }
            }
        }
    }

    @Override
    public List<SubmissionFile> getSubmissionFiles(Long submissionGroupId) {

        return submissionFileRepository.findBySubmissionGroupId(submissionGroupId);
    }

    @Override
    public SubmissionFile getSubmissionFile(Long submissionGroupId, String uuid) {

        return submissionFileRepository.findBySubmissionGroupIdAndUuid(submissionGroupId, uuid);
    }

    protected SubmissionStatus getApproveForExecutionSubmissionStatus(Submission submission, Boolean isApproved) {

        return isApproved ? IN_PROGRESS : NOT_APPROVED;
    }

    protected void writeSubmissionResultFiles(Submission submission, Path destination,
                                              MultipartFile... files) throws IOException {

        for (MultipartFile file : files) {
            Files.copy(
                    file.getInputStream(),
                    destination.resolve(file.getOriginalFilename()),
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

    protected String calculateMD5Hash(Path folder, List<SubmissionFile> submissionFiles) throws IOException {

        List<Path> files = submissionFiles.stream()
                .map(submissionFile -> folder.resolve(submissionFile.getUuid()))
                .sorted().collect(Collectors.toList());
        List<InputStream> inputStreams = new ArrayList<>(files.size());
        for (Path path : files) {
            inputStreams.add(Files.newInputStream(path));
        }
        try (final SequenceInputStream in = new SequenceInputStream(Collections.enumeration(inputStreams))) {
            return DigestUtils.md5DigestAsHex(in);
        }
    }
}
