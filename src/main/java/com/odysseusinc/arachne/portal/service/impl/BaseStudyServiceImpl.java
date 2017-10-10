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
 * Created: September 17, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.impl;

import static com.odysseusinc.arachne.portal.model.DataSourceStatus.APPROVED;
import static com.odysseusinc.arachne.portal.model.DataSourceStatus.DECLINED;
import static com.odysseusinc.arachne.portal.model.DataSourceStatus.PENDING;
import static com.odysseusinc.arachne.portal.model.ParticipantRole.LEAD_INVESTIGATOR;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.singletonList;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonHealthStatus;
import com.odysseusinc.arachne.commons.utils.CommonFileUtils;
import com.odysseusinc.arachne.portal.exception.AlreadyExistException;
import com.odysseusinc.arachne.portal.exception.FieldException;
import com.odysseusinc.arachne.portal.exception.IORuntimeException;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.NotUniqueException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.model.AbstractUserStudyListItem;
import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.model.DataNodeUser;
import com.odysseusinc.arachne.portal.model.DataSource;
import com.odysseusinc.arachne.portal.model.DataSourceStatus;
import com.odysseusinc.arachne.portal.model.FavouriteStudy;
import com.odysseusinc.arachne.portal.model.ParticipantRole;
import com.odysseusinc.arachne.portal.model.ParticipantStatus;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.StudyDataSourceComment;
import com.odysseusinc.arachne.portal.model.StudyDataSourceLink;
import com.odysseusinc.arachne.portal.model.StudyFile;
import com.odysseusinc.arachne.portal.model.SuggestSearchRegion;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.model.UserStudy;
import com.odysseusinc.arachne.portal.model.UserStudyExtended;
import com.odysseusinc.arachne.portal.model.UserStudyGrouped;
import com.odysseusinc.arachne.portal.model.search.StudySearch;
import com.odysseusinc.arachne.portal.model.search.StudySpecification;
import com.odysseusinc.arachne.portal.repository.BaseStudyRepository;
import com.odysseusinc.arachne.portal.repository.BaseUserStudyLinkRepository;
import com.odysseusinc.arachne.portal.repository.FavouriteStudyRepository;
import com.odysseusinc.arachne.portal.repository.ResultFileRepository;
import com.odysseusinc.arachne.portal.repository.StudyDataSourceCommentRepository;
import com.odysseusinc.arachne.portal.repository.StudyDataSourceLinkRepository;
import com.odysseusinc.arachne.portal.repository.StudyFileRepository;
import com.odysseusinc.arachne.portal.repository.UserStudyExtendedRepository;
import com.odysseusinc.arachne.portal.repository.UserStudyGroupedRepository;
import com.odysseusinc.arachne.portal.repository.UserStudyRepository;
import com.odysseusinc.arachne.portal.service.BaseDataNodeService;
import com.odysseusinc.arachne.portal.service.BaseDataSourceService;
import com.odysseusinc.arachne.portal.service.BaseStudyService;
import com.odysseusinc.arachne.portal.service.BaseUserService;
import com.odysseusinc.arachne.portal.service.FileService;
import com.odysseusinc.arachne.portal.service.StudyStatusService;
import com.odysseusinc.arachne.portal.service.StudyTypeService;
import com.odysseusinc.arachne.portal.service.mail.ArachneMailSender;
import com.odysseusinc.arachne.portal.service.mail.InvitationCollaboratorMailSender;
import com.odysseusinc.arachne.portal.service.study.AddDataSourceStrategy;
import com.odysseusinc.arachne.portal.service.study.AddDataSourceStrategyFactory;
import com.odysseusinc.arachne.portal.util.BaseStudyHelper;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@SuppressWarnings("unused")
@Transactional(rollbackFor = Exception.class)
public abstract class BaseStudyServiceImpl<
        T extends Study,
        DS extends DataSource,
        SS extends StudySearch,
        SU extends AbstractUserStudyListItem> extends CRUDLServiceImpl<T>
        implements BaseStudyService<T, DS, SS, SU> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StudyServiceImpl.class);

    private static final String EX_STUDY_NOT_EXISTS = "The study does not exist";
    private static final String EX_USER_NOT_EXISTS = "The user does not exist";
    private static final String EX_LAST_LEAD_DELETE = "Last Lead Investigator cannot be deleted!";
    private static final String EX_PARTICIPANT_EXISTS = "Participant is already added";
    private static final String DATASOURCE_NOT_EXIST_EXCEPTION = "DataSource with id='%s' does not exist";
    private static final String VIRTUAL_DATASOURCE_OWNERS_IS_EMPTY_EXC = "Virtual Data Source must have at least one Data Owner";

    @Value("${portal.url}")
    private String portalUrl;
    private final JavaMailSender javaMailSender;
    private final UserStudyExtendedRepository userStudyExtendedRepository;
    private final FileService fileService;
    private final BaseUserStudyLinkRepository<SU> baseUserStudyLinkRepository;
    private final UserStudyRepository userStudyRepository;
    private final BaseStudyRepository<T> studyRepository;
    private final RestTemplate restTemplate;
    private final StudyFileRepository studyFileRepository;
    private final BaseUserService userService;
    private final BaseDataSourceService<DS> dataSourceService;
    private final BaseDataNodeService<DataNode> baseDataNodeService;
    private final StudyDataSourceLinkRepository studyDataSourceLinkRepository;
    private final StudyStatusService studyStatusService;
    private final StudyTypeService studyTypeService;
    private final BaseStudyHelper<DataNode, DS> studyHelper;
    private final ResultFileRepository resultFileRepository;
    private final FavouriteStudyRepository favouriteStudyRepository;
    private final StudyDataSourceCommentRepository dataSourceCommentRepository;
    private final ArachneMailSender arachneMailSender;
    private final SimpMessagingTemplate wsTemplate;
    protected final GenericConversionService conversionService;
    protected final AddDataSourceStrategyFactory<DS> addDataSourceStrategyFactory;
    private final Map<String, String[]> studySortPaths = new HashMap<>();

    public BaseStudyServiceImpl(UserStudyExtendedRepository userStudyExtendedRepository,
                                FileService fileService,
                                BaseUserStudyLinkRepository<SU> baseUserStudyLinkRepository,
                                UserStudyGroupedRepository userStudyGroupedRepository,
                                UserStudyRepository userStudyRepository,
                                ArachneMailSender arachneMailSender,
                                BaseStudyRepository<T> studyRepository,
                                FavouriteStudyRepository favouriteStudyRepository,
                                @Qualifier("restTemplate") RestTemplate restTemplate,
                                StudyTypeService studyTypeService,
                                BaseDataSourceService<DS> dataSourceService,
                                StudyDataSourceLinkRepository studyDataSourceLinkRepository,
                                ResultFileRepository resultFileRepository,
                                StudyFileRepository studyFileRepository,
                                BaseStudyHelper<DataNode, DS> studyHelper,
                                StudyDataSourceCommentRepository dataSourceCommentRepository,
                                BaseUserService userService,
                                SimpMessagingTemplate wsTemplate,
                                StudyStatusService studyStatusService,
                                BaseDataNodeService dataNodeService,
                                JavaMailSender javaMailSender,
                                GenericConversionService conversionService,
                                AddDataSourceStrategyFactory<DS> addDataSourceStrategyFactory) {

        this.javaMailSender = javaMailSender;
        this.userStudyExtendedRepository = userStudyExtendedRepository;
        this.fileService = fileService;
        this.baseUserStudyLinkRepository = baseUserStudyLinkRepository;
        this.userStudyRepository = userStudyRepository;
        this.arachneMailSender = arachneMailSender;
        this.studyRepository = studyRepository;
        this.favouriteStudyRepository = favouriteStudyRepository;
        this.restTemplate = restTemplate;
        this.studyTypeService = studyTypeService;
        this.dataSourceService = dataSourceService;
        this.studyDataSourceLinkRepository = studyDataSourceLinkRepository;
        this.resultFileRepository = resultFileRepository;
        this.studyFileRepository = studyFileRepository;
        this.studyHelper = studyHelper;
        this.dataSourceCommentRepository = dataSourceCommentRepository;
        this.userService = userService;
        this.wsTemplate = wsTemplate;
        this.studyStatusService = studyStatusService;
        this.baseDataNodeService = dataNodeService;
        this.conversionService = conversionService;
        this.addDataSourceStrategyFactory = addDataSourceStrategyFactory;
    }

    public abstract Class<T> getType();

    // sort detection logic cab be moved out into its own class smth like SortDetecter
    @PostConstruct
    private void init() {
        this.studySortPaths.put("title",new String[]{"study.title"});
        this.studySortPaths.put("leadList",new String[]{"firstLead.firstname", "firstLead.middlename", "firstLead.lastname"});
        this.studySortPaths.put("role",new String[]{"role"});
        this.studySortPaths.put("created",new String[]{"study.created"});
        this.studySortPaths.put("type",new String[]{"study.type.name"});
        this.studySortPaths.put("status",new String[]{"study.status"});

        this.studySortPaths.putAll(getAdditionalSortPaths());
    }

    protected Map<String, String[]> getAdditionalSortPaths() {
        return new HashMap<>();
    }

    @Override
    public T create(User owner, T study) throws NotUniqueException, NotExistException {

        List<T> studies = studyRepository.findByTitle(study.getTitle());
        if (!studies.isEmpty()) {
            throw new NotUniqueException("title", "not unique");
        }
        Date date = new Date();
        study.setCreated(date);
        study.setUpdated(date);
        Date startDate = new Date(System.currentTimeMillis());
        study.setStartDate(startDate);
        study.setType(studyTypeService.getById(study.getType().getId()));
        study.setStatus(studyStatusService.findByName("Initiate"));

        beforeStudySave(study);
        T savedStudy = studyRepository.save(study);
        afterStudySave(study);

        // Set Lead of the Study
        addDefaultLead(savedStudy, owner);

        return savedStudy;
    }

    protected void afterStudySave(T study) {

    }

    protected void beforeStudySave(T study) {

    }

    private UserStudy addDefaultLead(Study study, User owner) {

        UserStudy leadStudyLink = new UserStudy();
        leadStudyLink.setCreatedBy(owner);
        leadStudyLink.setUser(owner);
        leadStudyLink.setStudy(study);
        leadStudyLink.setRole(LEAD_INVESTIGATOR);
        leadStudyLink.setCreated(new Date());
        leadStudyLink.setStatus(ParticipantStatus.APPROVED);
        leadStudyLink.setToken("");
        userStudyRepository.save(leadStudyLink);

        return leadStudyLink;
    }

    @Override
    @PreAuthorize("hasPermission(#studyId, 'Study', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).EDIT_STUDY)")
    public void delete(Long studyId) throws NotExistException {

        if (studyId == null) {
            throw new NotExistException("id is null", getType());
        }
        if (!studyRepository.exists(studyId)) {
            throw new NotExistException(getType());
        }
        studyRepository.delete(studyId);
    }

    @Override
    public CrudRepository<T, Long> getRepository() {

        return studyRepository;
    }

    @Override
    @PostAuthorize("@ArachnePermissionEvaluator.addPermissions(principal, returnObject )")
    public T getById(Long id) throws NotExistException {

        T study = super.getById(id);
        if (study == null) {
            throw new NotExistException(getType());
        }
        Hibernate.initialize(study.getParticipants());
        return study;
    }


    @Override
    @PreAuthorize("hasPermission(#study, "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).EDIT_STUDY)")
    @PostAuthorize("@ArachnePermissionEvaluator.addPermissions(principal, returnObject )")
    public T update(T study)
            throws NotExistException, NotUniqueException, ValidationException {

        if (study.getId() == null) {
            throw new NotExistException("id is null", getType());
        }
        List<T> byTitle = studyRepository.findByTitle(study.getTitle());
        if (!byTitle.isEmpty()) {
            throw new NotUniqueException("title", "not unique");
        }
        T forUpdate = studyRepository.findOne(study.getId());
        if (forUpdate == null) {
            throw new NotExistException(getType());
        }
        if (study.getType() != null && study.getType().getId() != null) {
            forUpdate.setType(studyTypeService.getById(study.getType().getId()));
        }
        if (study.getStatus() != null && study.getStatus().getId() != null) {
            forUpdate.setStatus(studyStatusService.getById(study.getStatus().getId()));
        }
        forUpdate.setTitle(study.getTitle() != null ? study.getTitle() : forUpdate.getTitle());
        forUpdate.setDescription(study.getDescription() != null ? study.getDescription() : forUpdate.getDescription());

        forUpdate.setStartDate(study.getStartDate() != null ? study.getStartDate() : forUpdate.getStartDate());
        forUpdate.setEndDate(study.getEndDate() != null ? study.getEndDate() : forUpdate.getEndDate());
        if (forUpdate.getStartDate() != null && forUpdate.getEndDate() != null
                && forUpdate.getStartDate().getTime() > forUpdate.getEndDate().getTime()) {
            throw new ValidationException("end date before start date ");
        }

        forUpdate.setUpdated(new Date());

        beforeStudyUpdate(forUpdate, study);
        T updatedStudy = studyRepository.save(forUpdate);
        afterStudyUpdate(forUpdate, study);

        return updatedStudy;
    }

    protected void beforeStudyUpdate(T forUpdate, T willBeUpdated) {

    }

    protected void afterStudyUpdate(T exists, T wilBeUpdated) {

    }

    @Override
    public void setFavourite(Long userId, Long studyId, Boolean isFavourite) throws NotExistException {

        Study study = studyRepository.findOne(studyId);
        if (study == null) {
            throw new NotExistException("study not exist", Study.class);
        }
        if (isFavourite) {
            favouriteStudyRepository.save(new FavouriteStudy(userId, studyId));
        } else {
            favouriteStudyRepository.deleteByUserIdAndStudyId(userId, studyId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AbstractUserStudyListItem> findStudies(final SS studySearch) {

        Sort sort = getSort(studySearch.getSortBy(), studySearch.getSortAsc());
        StudySpecification<SU> studyFilteredListSpecification = new StudySpecification<>(studySearch);

        Page<SU> resultPage = baseUserStudyLinkRepository.findAll(
                studyFilteredListSpecification,
                new PageRequest(studySearch.getPage() - 1, studySearch.getPagesize(), sort));

        return resultPage.map(s -> (AbstractUserStudyListItem) s);
    }

    @Override
    public SU getStudy(final User user, final Long studyId) {

        if (user == null || user.getId() == null || studyId == null) {
            throw new IllegalArgumentException("Method arguments must not be null");
        }
        final SU userStudyItem = baseUserStudyLinkRepository
                .findFirstByUserIdAndStudyId(user.getId(), studyId).orElseThrow(
                        () -> new NotExistException(UserStudyGrouped.class)
                );
        Hibernate.initialize(userStudyItem.getStudy().getParticipants());
        return userStudyItem;
    }

    public List<User> findLeads(Study study) {

        List<UserStudyExtended> leadStudyLinkList = userStudyExtendedRepository.findByStudyAndRoleAndStatus(
                study, LEAD_INVESTIGATOR, ParticipantStatus.APPROVED);
        return leadStudyLinkList.stream()
                .sorted(Comparator.comparingLong(UserStudyExtended::getId))
                .map(UserStudyExtended::getUser)
                .collect(Collectors.toList());
    }

    protected final Sort getSort(String sortBy, Boolean sortAsc) {

        String defaultSort = "study.title";

        return new Sort(
                sortAsc == null || sortAsc ? Sort.Direction.ASC : Sort.Direction.DESC,
                studySortPaths.getOrDefault(sortBy, new String[]{defaultSort})
        );
    }


    @Override
    @PreAuthorize("hasPermission(#studyId, 'Study', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).INVITE_CONTRIBUTOR)")
    public UserStudy addParticipant(
            User createdBy,
            Long studyId,
            Long participantId,
            ParticipantRole role
    ) throws NotExistException, AlreadyExistException {

        Study study = Optional.ofNullable(studyRepository.findOne(studyId))
                .orElseThrow(() -> new NotExistException(EX_STUDY_NOT_EXISTS, Study.class));

        User participant = Optional.ofNullable(userService.findOne(participantId))
                .orElseThrow(() -> new NotExistException(EX_USER_NOT_EXISTS, User.class));

        UserStudy studyLink = userStudyRepository.findOneByStudyIdAndUserId(study.getId(), participant.getId());

        if (studyLink == null) {
            // If user is invited for first time - create new link
            studyLink = new UserStudy();
        } else if (studyLink.getStatus().isPendingOrApproved()) {
            // If user declined invitation or he is deleted - we can resend it again.
            // Otherwise - invitation is pending or was already accepted. Cannot change or recreate
            throw new AlreadyExistException(EX_PARTICIPANT_EXISTS);
        }

        studyLink.setCreatedBy(createdBy);
        studyLink.setUser(participant);
        studyLink.setStudy(study);
        studyLink.setRole(role);
        studyLink.setCreated(new Date());
        studyLink.setStatus(ParticipantStatus.PENDING);
        studyLink.setDeletedAt(null);
        studyLink.setComment(null);
        studyLink.setToken(UUID.randomUUID().toString().replace("-", ""));

        userStudyRepository.save(studyLink);
        arachneMailSender.send(new InvitationCollaboratorMailSender(portalUrl, participant, studyLink));
        return studyLink;
    }

    @Override
    @PreAuthorize("hasPermission(#studyId, 'Study', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).INVITE_CONTRIBUTOR)")
    public UserStudy updateParticipantRole(Long studyId, Long participantId,
                                           ParticipantRole role)
            throws NotExistException, AlreadyExistException, ValidationException {

        Study study = Optional.ofNullable(studyRepository.findOne(studyId))
                .orElseThrow(() -> new NotExistException(EX_STUDY_NOT_EXISTS, Study.class));
        User participant = Optional.ofNullable(userService.findOne(participantId))
                .orElseThrow(() -> new NotExistException(EX_USER_NOT_EXISTS, User.class));

        UserStudy studyLink = Optional.ofNullable(
                userStudyRepository.findOneByStudyAndUser(study, participant))
                .orElseThrow(() -> new NotExistException(UserStudy.class));

        checkLastLeadInvestigator(studyLink, study);

        studyLink.setRole(role);
        return userStudyRepository.save(studyLink);
    }

    @Override
    @PreAuthorize("hasPermission(#id, 'Study',"
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).EDIT_STUDY)")
    public void removeParticipant(Long id, Long participantId)
            throws NotExistException, PermissionDeniedException, ValidationException {

        Study study = getById(id);
        User participant = userService.findOne(participantId);
        UserStudy studyLink = Optional.ofNullable(
                userStudyRepository.findOneByStudyAndUser(study, participant))
                .orElseThrow(() -> new NotExistException(UserStudy.class));
        checkLastLeadInvestigator(studyLink, study);
        if (userStudyRepository.hardRemoveIfNotTracked(id, participantId) == 0) {
            userStudyRepository.delete(studyLink);
        }
    }

    private void checkLastLeadInvestigator(UserStudy studyLink, Study study) throws ValidationException {

        if (LEAD_INVESTIGATOR == studyLink.getRole()) {
            List<UserStudy> leadLinkList = userStudyRepository.findByStudyAndRole(study, LEAD_INVESTIGATOR);
            if (leadLinkList.size() <= 1) {
                throw new ValidationException(EX_LAST_LEAD_DELETE);
            }
        }
    }

    @Override
    @PreAuthorize("hasPermission(#studyId, 'Study', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).UPLOAD_FILES)")
    public String saveFile(MultipartFile multipartFile, Long studyId, String label, User user)
            throws IOException {

        Study study = studyRepository.findOne(studyId);
        String fileNameLowerCase = UUID.randomUUID().toString();
        try {

            StudyFile studyFile = new StudyFile();
            studyFile.setUuid(fileNameLowerCase);
            studyFile.setStudy(study);
            studyFile.setLabel(label);
            studyFile.setRealName(multipartFile.getOriginalFilename());
            Date created = new Date();
            studyFile.setCreated(created);
            studyFile.setUpdated(created);
            studyFile.setAuthor(user);

            // Save study after uuid and Study were set
            fileService.saveFile(multipartFile, studyFile);

            // Detect file content type (requires file to exist)
            String contentType = CommonFileUtils.getContentType(
                    multipartFile.getOriginalFilename(),
                    fileService.getStudyFilePath(studyFile).toAbsolutePath().toString()
            );
            studyFile.setContentType(contentType);

            // Save entity
            studyFileRepository.save(studyFile);
            return fileNameLowerCase;
        } catch (IOException | RuntimeException ex) {
            String message = "error save file to disk, filename=" + fileNameLowerCase + " ex=" + ex.toString();
            LOGGER.debug(message, ex);
            throw new IOException(message);
        }
    }

    @Override
    @PreAuthorize("hasPermission(#studyId, 'Study', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).UPLOAD_FILES)")
    public String saveFile(String link, Long studyId, String label, User user) throws IOException {

        Study study = studyRepository.findOne(studyId);
        String fileNameLowerCase = UUID.randomUUID().toString();
        try {

            if (link == null) {
                throw new IORuntimeException("wrong url");
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(singletonList(MediaType.APPLICATION_OCTET_STREAM));
            HttpEntity<String> entity = new HttpEntity<>(headers);
            URL url = new URL(link);
            String fileName = FilenameUtils.getName(url.getPath());

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    link.toString(),
                    HttpMethod.GET, entity, byte[].class);

            if (response.getStatusCode() == HttpStatus.OK) {
                String contentType = CommonFileUtils.TYPE_LINK;
                StudyFile studyFile = new StudyFile();
                studyFile.setUuid(fileNameLowerCase);
                studyFile.setContentType(contentType);
                studyFile.setStudy(study);
                studyFile.setLabel(label);
                studyFile.setRealName(fileName);
                studyFile.setLink(link);
                Date created = new Date();
                studyFile.setCreated(created);
                studyFile.setUpdated(created);
                studyFile.setAuthor(user);
                studyFileRepository.save(studyFile);
                return fileNameLowerCase;
            }
            return null;
        } catch (IOException | RuntimeException ex) {
            String message = "error save file to disk, filename=" + fileNameLowerCase + " ex=" + ex.toString();
            LOGGER.debug(message, ex);
            throw new IOException(message);
        }
    }

    @Override
    @PreAuthorize("hasPermission(#studyId, 'Study', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).ACCESS_STUDY)")
    public StudyFile getStudyFile(Long studyId, String fileName) {

        return studyFileRepository.findByUuid(fileName);
    }

    @Override
    @PreAuthorize("hasPermission(#studyId, 'Study', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).ACCESS_STUDY)")
    public Boolean getDeleteStudyFile(Long studyId, String uuid) throws FileNotFoundException {

        StudyFile studyFile = studyFileRepository.findByUuid(uuid);
        studyFileRepository.delete(studyFile);
        fileService.delete(studyFile);
        return true;
    }


    @Override
    @PreAuthorize("hasPermission(#studyId, 'Study', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).ACCESS_STUDY)")
    public List<StudyDataSourceLink> listApprovedDataSources(Long studyId) {

        return studyDataSourceLinkRepository.findApprovedNotDeletedByStudyId(studyId);
    }

    @Override
    //ordering annotations is important to check current participants before method invoke
    @PreAuthorize("hasPermission(#studyId, 'Study', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).INVITE_DATANODE)")
    public StudyDataSourceLink addDataSource(User createdBy, Long studyId, Long dataSourceId)
            throws NotExistException, AlreadyExistException {

        T study = studyRepository.findOne(studyId);
        if (study == null) {
            throw new NotExistException("study not exist", Study.class);
        }
        DS dataSource = dataSourceService.getNotDeletedById(dataSourceId);
        if (dataSource == null) {
            throw new NotExistException("dataSource not exist", DataSource.class);
        }
        StudyDataSourceLink studyDataSourceLink =
                studyDataSourceLinkRepository.findByDataSourceIdAndStudyId(dataSource.getId(), study.getId());
        if (studyDataSourceLink == null) {
            studyDataSourceLink = new StudyDataSourceLink();
        } else if (studyDataSourceLink.getStatus().isPendingOrApproved()) {
            throw new AlreadyExistException();
        }

        studyDataSourceLink.setStudy(study);
        studyDataSourceLink.setDataSource(dataSource);
        studyDataSourceLink.setCreated(new Date());
        studyDataSourceLink.setToken(UUID.randomUUID().toString());
        studyDataSourceLink.setCreatedBy(createdBy);
        studyDataSourceLink.setDeletedAt(null);

        AddDataSourceStrategy<DS> strategy = addDataSourceStrategyFactory.getStrategy(dataSource);
        strategy.addDataSourceToStudy(createdBy, dataSource, studyDataSourceLink);
        return studyDataSourceLink;
    }

    @Override
    @Transactional
    @PreAuthorize("hasPermission(#studyId, 'Study', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).INVITE_DATANODE)")
    public DS addVirtualDataSource(
            User createdBy,
            Long studyId,
            String dataSourceName,
            List<Long> dataOwnerIds
    )
            throws NotExistException, AlreadyExistException, NoSuchFieldException, IOException, ValidationException,
            FieldException, IllegalAccessException, SolrServerException {

        Study study = studyRepository.findOne(studyId);
        if (study == null) {
            throw new NotExistException("study not exist", Study.class);
        }
        if (CollectionUtils.isEmpty(dataOwnerIds)) {
            throw new IllegalArgumentException(VIRTUAL_DATASOURCE_OWNERS_IS_EMPTY_EXC);
        }
        final DataNode dataNode = studyHelper.getVirtualDataNode(study.getTitle(), dataSourceName);
        final DataNode registeredDataNode = baseDataNodeService.register(dataNode);

        final List<User> dataOwners = userService.findUsersByIdsIn(dataOwnerIds);
        final Set<DataNodeUser> dataNodeUsers = studyHelper.usersToDataNodeAdmins(dataOwners, registeredDataNode);
        registeredDataNode.setDataNodeUsers(dataNodeUsers);
        final Authentication savedAuth = studyHelper.loginByNode(registeredDataNode);
        baseDataNodeService.relinkAllUsersToDataNode(registeredDataNode, dataNodeUsers);
        SecurityContextHolder.getContext().setAuthentication(savedAuth);

        final DS dataSource = studyHelper.getVirtualDataSource(registeredDataNode, dataSourceName);
        dataSource.setHealthStatus(CommonHealthStatus.GREEN);
        dataSource.setHealthStatusDescription("Virtual DataSources are always GREEN");
        final DS registeredDataSource = dataSourceService.createOrRestoreDataSource(dataSource);
        addDataSource(createdBy, studyId, registeredDataSource.getId());
        return registeredDataSource;
    }

    @Override
    @PreAuthorize("hasPermission(#studyId, 'Study', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).UNLINK_DATASOURCE)")
    public void removeDataSource(Long studyId, Long dataSourceId) throws NotExistException {

        removeDataSourceUnsecured(studyId, dataSourceId);
    }

    @Override
    public void removeDataSourceUnsecured(Long studyId, Long dataSourceId) {

        Study study = studyRepository.findOne(studyId);
        if (study == null) {
            throw new NotExistException("study does not exist.", Study.class);
        }
        StudyDataSourceLink studyDataSourceLink = studyDataSourceLinkRepository.findByStudyIdAndDataSourceId(
                study.getId(),
                dataSourceId
        );
        if (studyDataSourceLink == null) {
            throw new NotExistException("studyDataSourceLink does not exist.", StudyDataSourceLink.class);
        }
        studyDataSourceLinkRepository.delete(studyDataSourceLink.getId());
    }

    @Override
    public void processDataSourceInvitation(User user,
                                            Long id, Boolean accepted, String comment) {

        StudyDataSourceLink studyDataSourceLink = studyDataSourceLinkRepository.findByIdAndOwner(id, user);
        if (studyDataSourceLink != null) {
            DataSourceStatus status = TRUE.equals(accepted) ? APPROVED : DECLINED;
            studyDataSourceLink.setStatus(status);
            if (DECLINED == status) {
                if (!StringUtils.isEmpty(comment)) {
                    dataSourceCommentRepository.save(
                            new StudyDataSourceComment(studyDataSourceLink.getId(), user.getId(), comment));
                }
            }
        }
    }

    @Override
    public StudyDataSourceLink getByIdAndStatusPendingAndToken(Long studyDataSourceId, String token)
            throws NotExistException {

        StudyDataSourceLink link = studyDataSourceLinkRepository.findByIdAndStatusAndToken(studyDataSourceId,
                PENDING, token);
        if (link == null) {
            throw new NotExistException("StudyDataSourceLink with id=" + studyDataSourceId
                    + " and pending status and token = " + token + " is not found", StudyDataSourceLink.class);
        }
        return link;
    }

    @Override
    public List<User> getApprovedUsers(DataSource dataSource) {

        return userService.findUsersApprovedInDataSource(dataSource.getId());
    }

    @Override
    public List<T> getStudiesUsesDataSource(Long dataSourceId) {

        return studyDataSourceLinkRepository.findNotDeletedByDataSourceId(dataSourceId).stream()
                .map(StudyDataSourceLink::getStudy)
                .map(s -> (T) s) // there is no way while we are dealing with transitive calls
                .collect(Collectors.toList());
    }

    @Override
    public boolean fullDelete(List<T> studies) {

        for (T study : studies) {

            List<StudyFile> files = study.getFiles();

            fileService.delete(files);
            studyFileRepository.delete(files);

            studyDataSourceLinkRepository.delete(study.getDataSources());

            studyHelper.tryDeleteStudyFolder(study);
        }

        studyRepository.delete(studies);

        return Boolean.TRUE;
    }

    @Override
    public List<T> getByIds(List<Long> studyIds) {

        return studyRepository.findByIdIn(studyIds);
    }

    @Override
    public Iterable<T> suggestStudy(String query, User owner, Long id, SuggestSearchRegion region) {

        Iterable<T> suggest;
        final String suggestRequest = "%" + query.toLowerCase() + "%";
        switch (region) {
            case PARTICIPANT: {
                suggest = studyRepository.suggestByParticipantId(suggestRequest, owner.getId(), id);
                break;
            }

            case DATASOURCE: {
                suggest = studyRepository.suggestByDatasourceId(suggestRequest, owner.getId(), id);
                break;
            }

            default: {
                throw new IllegalArgumentException("Search region is not recognized region=" + region);
            }
        }
        return suggest;
    }

    @Override
    @PreAuthorize("hasPermission(#studyId, 'Study', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).ACCESS_STUDY)")
    public void getStudyAllFiles(Long studyId, String archiveName, OutputStream os) throws IOException {

        T study = studyRepository.findOne(studyId);
        Path storeFilesPath = fileService.getPath(study);
        fileService.archiveFiles(os, storeFilesPath, study.getFiles());
    }

    private StudyDataSourceLink saveStudyDataSourceLinkWithStatus(StudyDataSourceLink studyDataSourceLink,
                                                                  DataSourceStatus status) {

        studyDataSourceLink.setStatus(status);
        return studyDataSourceLinkRepository.save(studyDataSourceLink);
    }


}
