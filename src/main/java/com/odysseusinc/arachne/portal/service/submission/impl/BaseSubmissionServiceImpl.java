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
import static com.odysseusinc.arachne.portal.service.impl.submission.SubmissionActionType.EXECUTE;
import static com.odysseusinc.arachne.portal.service.impl.submission.SubmissionActionType.PUBLISH;
import static com.odysseusinc.arachne.portal.util.DataNodeUtils.isDataNodeOwner;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphUtils;
import com.odysseusinc.arachne.portal.api.v1.dto.ApproveDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UpdateNotificationDTO;
import com.odysseusinc.arachne.portal.config.WebSecurityConfig;
import com.odysseusinc.arachne.portal.exception.NoExecutableFileException;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.AnalysisFile;
import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.ResultFile;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.model.SubmissionFile;
import com.odysseusinc.arachne.portal.model.SubmissionGroup;
import com.odysseusinc.arachne.portal.model.SubmissionStatus;
import com.odysseusinc.arachne.portal.model.SubmissionStatusHistoryElement;
import com.odysseusinc.arachne.portal.model.search.ResultFileSearch;
import com.odysseusinc.arachne.portal.model.search.SubmissionGroupSearch;
import com.odysseusinc.arachne.portal.model.search.SubmissionGroupSpecification;
import com.odysseusinc.arachne.portal.model.search.SubmissionSpecification;
import com.odysseusinc.arachne.portal.repository.ResultFileRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionFileRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionGroupRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionInsightRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionResultFileRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionStatusHistoryRepository;
import com.odysseusinc.arachne.portal.repository.submission.BaseSubmissionRepository;
import com.odysseusinc.arachne.portal.service.BaseDataSourceService;
import com.odysseusinc.arachne.portal.service.UserService;
import com.odysseusinc.arachne.portal.service.impl.submission.SubmissionAction;
import com.odysseusinc.arachne.portal.service.impl.submission.SubmissionActionType;
import com.odysseusinc.arachne.portal.service.mail.ArachneMailSender;
import com.odysseusinc.arachne.portal.service.mail.InvitationApprovalSubmissionArachneMailMessage;
import com.odysseusinc.arachne.portal.service.submission.BaseSubmissionService;
import com.odysseusinc.arachne.portal.util.AnalysisHelper;
import com.odysseusinc.arachne.portal.util.ContentStorageHelper;
import com.odysseusinc.arachne.portal.util.DataNodeUtils;
import com.odysseusinc.arachne.portal.util.EntityUtils;
import com.odysseusinc.arachne.portal.util.LegacyAnalysisHelper;
import com.odysseusinc.arachne.portal.util.SubmissionHelper;
import com.odysseusinc.arachne.portal.util.UUIDGenerator;
import com.odysseusinc.arachne.portal.util.ZipUtil;
import com.odysseusinc.arachne.storage.model.ArachneFileMeta;
import com.odysseusinc.arachne.storage.model.QuerySpec;
import com.odysseusinc.arachne.storage.service.ContentStorageService;
import com.odysseusinc.arachne.storage.util.FileSaveRequest;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipOutputStream;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

public abstract class BaseSubmissionServiceImpl<
        T extends Submission,
        A extends Analysis,
        DS extends IDataSource>
        implements BaseSubmissionService<T, A> {

    public static final String SUBMISSION_NOT_EXIST_EXCEPTION = "Submission with id='%s' does not exist";
    public static final String ILLEGAL_SUBMISSION_STATE_EXCEPTION = "Submission must be in EXECUTED or FAILED state before approve result";
    public static final String RESULT_FILE_NOT_EXISTS_EXCEPTION = "Result file with id='%s' for submission with "
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
    protected final ContentStorageService contentStorageService;
    protected final UserService userService;
    protected final ContentStorageHelper contentStorageHelper;

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
                                        EntityManager entityManager,
                                        SubmissionHelper submissionHelper,
                                        ContentStorageService contentStorageService,
                                        UserService userService,
                                        ContentStorageHelper contentStorageHelper) {

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
        this.contentStorageService = contentStorageService;
        this.userService = userService;
        this.contentStorageHelper = contentStorageHelper;
    }

    @Override
    public T approveSubmissionResult(Long submissionId, ApproveDTO approveDTO, IUser user) {

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
    public T createSubmission(IUser user, A analysis, Long datasourceId,
                              SubmissionGroup submissionGroup)
            throws NotExistException, IOException {

        DS dataSource = dataSourceService.getByIdUnsecured(datasourceId);
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

    protected SubmissionStatus calculateSubmissionStatusAccordingToDatasourceOwnership(DS dataSource, IUser user) {

        return PENDING;
    }

    @Override
    public void notifyOwnersAboutNewSubmission(T submission) {

        Set<IUser> dnOwners = DataNodeUtils.getDataNodeOwners(submission.getDataSource().getDataNode());
        dnOwners.remove(submission.getAuthor());
        try {
            for (IUser owner : dnOwners) {
                mailSender.send(new InvitationApprovalSubmissionArachneMailMessage(
                        WebSecurityConfig.getDefaultPortalURI(), owner, submission)
                );
            }
        } catch (Exception ignore) {
            LOGGER.error(ignore.getLocalizedMessage());
        }

        notifyOwnersAboutSubmissionUpdateViaSocket(submission);
    }

    @Override
    public void notifyOwnersAboutSubmissionUpdateViaSocket(T submission) {

        Set<IUser> dnOwners = DataNodeUtils.getDataNodeOwners(submission.getDataSource().getDataNode());

        for (IUser owner : dnOwners) {
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
    public T moveSubmissionToNewStatus(T submission, SubmissionStatus status, IUser user, String comment) {

        List<SubmissionStatusHistoryElement> statusHistory = submission.getStatusHistory();
        statusHistory.add(new SubmissionStatusHistoryElement(new Date(), status, user, submission, comment));
        submission.setStatusHistory(statusHistory);
        return saveSubmission(submission);
    }

    @Override
    public T getSubmissionByIdUnsecured(Long id) throws NotExistException {

        T submission = submissionRepository.findOne(id);
        throwNotExistExceptionIfNull(submission, id);
        return submission;
    }

    @Override
    @PreAuthorize("hasPermission(#id,  'Submission', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).ACCESS_STUDY)")
    @PostAuthorize("@ArachnePermissionEvaluator.addPermissions(principal, returnObject )")
    public T getSubmissionById(Long id) throws NotExistException {

        return getSubmissionByIdUnsecured(id);
    }

    @Override
    public T getSubmissionById(Long id, EntityGraph entityGraph) throws NotExistException {

        T submission = submissionRepository.findById(id, entityGraph);
        throwNotExistExceptionIfNull(submission, id);
        return submission;
    }

    @Override
    public T getSubmissionByIdAndStatus(Long id, SubmissionStatus status) throws NotExistException {

        return throwNotExistExceptionIfNull(submissionRepository.findByIdAndStatusIn(id,
                Collections.singletonList(status.name())), id);
    }

    @Override
    public T getSubmissionByIdAndStatus(Long id, List<SubmissionStatus> statusList) throws NotExistException {

        return throwNotExistExceptionIfNull(submissionRepository.findByIdAndStatusIn(id,
                statusList.stream().map(Enum::name).collect(Collectors.toList())), id);
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
    public SubmissionGroup createSubmissionGroup(IUser user, Analysis analysis) throws IOException, NoExecutableFileException {

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
            submissionFile.setAntivirusStatus(analysisFile.getAntivirusStatus());
            submissionFile.setAntivirusDescription(analysisFile.getAntivirusDescription());
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

    @Override
    @PreAuthorize("hasPermission(#submissoinGroupSearch.analysisId,  'Analysis', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).ACCESS_STUDY)")
    @PostAuthorize("@ArachnePermissionEvaluator.addPermissionsToSubmissions(principal, returnObject )")
    public Page<SubmissionGroup> getSubmissionGroups(SubmissionGroupSearch submissoinGroupSearch) {

        final SubmissionGroupSpecification submissionGroupSpecification = new SubmissionGroupSpecification(submissoinGroupSearch);
        final Integer page = submissoinGroupSearch.getPage();
        final PageRequest pageRequest = new PageRequest(page == null ? 0 : page - 1, submissoinGroupSearch.getPageSize(), new Sort(Sort.Direction.DESC, "created"));
        final Page<SubmissionGroup> submissionGroups = submissionGroupRepository.findAll(submissionGroupSpecification, pageRequest);
        final List<SubmissionGroup> content = submissionGroups.getContent();
        final Map<Long, SubmissionGroup> submissionGroupMap = content.stream().collect(Collectors.toMap(SubmissionGroup::getId, sg -> {
            sg.setSubmissions(new ArrayList<>());
            return sg;
        }));

        final Set<Long> submissionGroupIds = submissionGroupMap.keySet();
        if (!CollectionUtils.isEmpty(submissionGroupIds)) {
            final SubmissionSpecification<T> submissionSpecification = SubmissionSpecification.<T>builder(submissionGroupIds)
                    .withStatuses(submissoinGroupSearch.getSubmissionStatuses())
                    .withDataSourceIds(submissoinGroupSearch.getDataSourceIds())
                    .hasInsight(submissoinGroupSearch.getHasInsight())
                    .showHidden(submissoinGroupSearch.getShowHidden())
                    .build();
            submissionRepository.findAll(submissionSpecification)
                    .forEach(s -> submissionGroupMap.get(s.getSubmissionGroup().getId()).getSubmissions().add(s));
        }
        return submissionGroups;
    }

    protected void checkBeforeCreateSubmissionGroup(Analysis analysis) throws NoExecutableFileException {

    }

    @Override
    public List<SubmissionStatusHistoryElement> getSubmissionStatusHistory(Long analysisId, Long submissionId) {

        return submissionStatusHistoryRepository.findBySubmissionIdOrderByDate(submissionId);
    }

    @Override
    public boolean deleteSubmissionResultFile(Long submissionId, ResultFile resultFile)
            throws NotExistException, ValidationException {

        final T submission = submissionRepository.findByIdAndStatusIn(submissionId,
                Collections.singletonList(IN_PROGRESS.name()));
        throwNotExistExceptionIfNull(submission, submissionId);
        Optional.ofNullable(resultFile).orElseThrow(() ->
                new NotExistException(String.format(RESULT_FILE_NOT_EXISTS_EXCEPTION,
                        resultFile.getId(), submission.getId()), ResultFile.class));
        ArachneFileMeta fileMeta = contentStorageService.getFileByPath(resultFile.getPath());
        if (fileMeta.getCreatedBy() == null) { // not manually uploaded
            throw new ValidationException(String.format(FILE_NOT_UPLOADED_MANUALLY_EXCEPTION, resultFile.getId()));
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

    @Override
    public T getSubmissionByIdAndToken(Long id, String token) throws NotExistException {

        return submissionRepository.findByIdAndToken(id, token).orElseThrow(() -> new NotExistException(Submission.class));
    }

    @Override
    public ResultFile uploadResultsByDataOwner(Long submissionId, String name, MultipartFile file) throws NotExistException, IOException {

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        IUser user = userService.getByUsername(userDetails.getUsername());

        T submission = submissionRepository.findByIdAndStatusIn(submissionId,
                Collections.singletonList(IN_PROGRESS.name()));
        throwNotExistExceptionIfNull(submission, submissionId);

        File tempFile = File.createTempFile("manual-upload", name);
        file.transferTo(tempFile);

        ResultFile resultFile = createResultFile(
                tempFile.toPath(),
                ObjectUtils.firstNonNull(name, file.getOriginalFilename()),
                submission,
                user.getId()
        );
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
    public T approveSubmission(Long submissionId, Boolean isApproved, String comment, IUser user)
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
    @PreAuthorize("hasPermission(#submission, "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).UPDATE_SUBMISSION)")
    public T updateSubmission(T submission) {

        final Long id = submission.getId();
        final T existingSubmission = getSubmissionByIdUnsecured(id);

        final Boolean hidden = submission.getHidden();
        if (hidden != null) {
            final SubmissionAction hideAction = getHideAction(existingSubmission);
            if (!hideAction.getAvailable()) {
                final String message = String.format("Status of Submission with id: '%s' does not allow hide this one", id);
                throw new IllegalStateException(message);
            }
            existingSubmission.setHidden(hidden);
        }
        return submissionRepository.save(existingSubmission);
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
    public List<T> getByIdIn(List<Long> ids) {

        return submissionRepository.findByIdIn(ids);
    }

    @Override
    public List<SubmissionStatusHistoryElement> getSubmissionStatusHistoryElementsByIdsIn(List<Long> ids) {

        return submissionStatusHistoryRepository.findByIdIn(ids);
    }


    @Override
    @Transactional
    public List<ResultFile> createResultFilesBatch(
            List<FileSaveRequest> fileSaveRequests,
            Submission submission,
            Long createById
    ) throws IOException {

        fileSaveRequests.forEach(entry ->
                entry.setDestinationFilepath(contentStorageHelper.getResultFilesDir(submission, entry.getDestinationFilepath()))
        );

        List<ArachneFileMeta> metaList = contentStorageService.saveBatch(fileSaveRequests, createById);

        return metaList.stream().map(fm -> {

            ResultFile resultFile = new ResultFile();
            resultFile.setSubmission(submission);

            resultFile.setPath(fm.getPath());

            return resultFile;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ResultFile createResultFile(
            Path filePath,
            String name,
            Submission submission,
            Long createById
    ) throws IOException {

        ResultFile resultFile = new ResultFile();
        resultFile.setSubmission(submission);

        ArachneFileMeta fileMeta = contentStorageService.saveFile(
                filePath.toFile(), contentStorageHelper.getResultFilesDir(submission, name),
                createById
        );

        resultFile.setPath(fileMeta.getPath());

        return resultFile;
    }

    @Override
    public List<ArachneFileMeta> getResultFiles(IUser user, Long submissionId, ResultFileSearch resultFileSearch) throws PermissionDeniedException {

        Submission submission = submissionRepository.findById(submissionId, EntityUtils.fromAttributePaths("dataSource", "dataSource.dataNode"));
        checkSubmissionPermission(user, submission);

        String resultFilesPath = contentStorageHelper.getResultFilesDir(submission, resultFileSearch.getPath());

        QuerySpec querySpec = new QuerySpec();
        querySpec.setName(resultFileSearch.getRealName());
        querySpec.setPath(resultFilesPath);

        return contentStorageService.searchFiles(querySpec);
    }

    @Override
    public ArachneFileMeta getResultFileAndCheckPermission(IUser user, Submission submission, Long analysisId,
                                                           String fileUuid) throws PermissionDeniedException {

        ArachneFileMeta byFileUuid = contentStorageService.getFileByIdentifier(fileUuid);
        checkSubmissionPermission(user, submission);
        return byFileUuid;
    }

    private void checkSubmissionPermission(IUser user, Submission submission) throws PermissionDeniedException {

        if (!(EXECUTED_PUBLISHED.equals(submission.getStatus()) || FAILED_PUBLISHED.equals(submission.getStatus()))) {
            if (user == null || !isDataNodeOwner(submission.getDataSource().getDataNode(), user)) {
                throw new PermissionDeniedException();
            }
        }
    }

    public ResultFile getResultFileByPath(String path) {

        return resultFileRepository.findByPath(path);
    }

    public ResultFile getResultFileById(Long fileId) {

        return resultFileRepository.findById(fileId);
    }

    @Override
    public void getSubmissionResultAllFiles(
            IUser user,
            Long analysisId,
            Long submissionId,
            String archiveName,
            OutputStream os) throws
            IOException, PermissionDeniedException {

        Submission submission = submissionRepository.findOne(submissionId);
        checkSubmissionPermission(user, submission);

        Path resultFilesPath = Paths.get(contentStorageHelper.getResultFilesDir(submission));
        try (ZipOutputStream zos = new ZipOutputStream(os)) {
            for (ResultFile resultFile : submission.getResultFiles()) {
                Path relativePath = resultFilesPath.relativize(Paths.get(resultFile.getPath()));
                ZipUtil.addZipEntry(zos, relativePath.toString(), contentStorageService.getContentByFilepath(resultFile.getPath()));
            }
        }
    }

    @Override
    public List<SubmissionFile> getSubmissionFiles(Long submissionGroupId) {

        return submissionFileRepository.findBySubmissionGroupId(submissionGroupId);
    }

    @Override
    public SubmissionFile getSubmissionFile(Long submissionGroupId, Long fileId) {

        return submissionFileRepository.findBySubmissionGroupIdAndId(submissionGroupId, fileId);
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

    @Override
    public List<SubmissionAction> getSubmissionActions(Submission submission) {

        // Approve execution
        SubmissionAction execApproveAction = getExecApproveAction(submission);

        // Manually upload files
        SubmissionAction manualResultUploadAction = getManualResultUploadAction(submission);

        // Publish submission
        SubmissionAction publishAction = getPublishAction(submission);

        SubmissionAction hideAction = getHideAction(submission);

        return Arrays.asList(execApproveAction, manualResultUploadAction, publishAction, hideAction);
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

    private SubmissionAction getHideAction(Submission submission) {

        SubmissionAction hideAction = new SubmissionAction(SubmissionActionType.HIDE.name());
        List<SubmissionStatus> availableForStatuses = Arrays.asList(
                NOT_APPROVED, EXECUTED_REJECTED, FAILED_REJECTED, EXECUTED_PUBLISHED, FAILED_PUBLISHED
        );
        hideAction.setAvailable(availableForStatuses.contains(submission.getStatus()));
        return hideAction;
    }
}
