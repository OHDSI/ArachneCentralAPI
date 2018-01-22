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
 * Created: September 19, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.impl;

import static com.odysseusinc.arachne.portal.model.ParticipantStatus.APPROVED;
import static com.odysseusinc.arachne.portal.model.ParticipantStatus.DECLINED;
import static com.odysseusinc.arachne.portal.repository.UserSpecifications.emailConfirmed;
import static com.odysseusinc.arachne.portal.repository.UserSpecifications.userEnabled;
import static com.odysseusinc.arachne.portal.repository.UserSpecifications.withNameOrEmailLike;
import static com.odysseusinc.arachne.portal.service.RoleService.ROLE_ADMIN;
import static java.lang.Boolean.TRUE;
import static org.springframework.data.jpa.domain.Specifications.where;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphUtils;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.odysseusinc.arachne.commons.utils.CommonFileUtils;
import com.odysseusinc.arachne.portal.api.v1.dto.SearchExpertListDTO;
import com.odysseusinc.arachne.portal.config.WebSecurityConfig;
import com.odysseusinc.arachne.portal.exception.ArachneSystemRuntimeException;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.NotUniqueException;
import com.odysseusinc.arachne.portal.exception.PasswordValidationException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.exception.UserNotFoundException;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.exception.WrongFileFormatException;
import com.odysseusinc.arachne.portal.model.Country;
import com.odysseusinc.arachne.portal.model.DataSourceStatus;
import com.odysseusinc.arachne.portal.model.Invitationable;
import com.odysseusinc.arachne.portal.model.ParticipantStatus;
import com.odysseusinc.arachne.portal.model.ProfessionalType;
import com.odysseusinc.arachne.portal.model.Role;
import com.odysseusinc.arachne.portal.model.Skill;
import com.odysseusinc.arachne.portal.model.StateProvince;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.model.UserLink;
import com.odysseusinc.arachne.portal.model.UserPublication;
import com.odysseusinc.arachne.portal.model.UserRegistrant;
import com.odysseusinc.arachne.portal.model.UserStudy;
import com.odysseusinc.arachne.portal.model.search.UserSearch;
import com.odysseusinc.arachne.portal.repository.AnalysisUnlockRequestRepository;
import com.odysseusinc.arachne.portal.repository.BaseUserRepository;
import com.odysseusinc.arachne.portal.repository.CountryRepository;
import com.odysseusinc.arachne.portal.repository.RoleRepository;
import com.odysseusinc.arachne.portal.repository.StateProvinceRepository;
import com.odysseusinc.arachne.portal.repository.StudyDataSourceLinkRepository;
import com.odysseusinc.arachne.portal.repository.UserSpecifications;
import com.odysseusinc.arachne.portal.repository.UserStudyRepository;
import com.odysseusinc.arachne.portal.security.passwordvalidator.ArachnePasswordData;
import com.odysseusinc.arachne.portal.security.passwordvalidator.ArachnePasswordValidator;
import com.odysseusinc.arachne.portal.service.BaseSkillService;
import com.odysseusinc.arachne.portal.service.BaseSolrService;
import com.odysseusinc.arachne.portal.service.BaseUserService;
import com.odysseusinc.arachne.portal.service.ProfessionalTypeService;
import com.odysseusinc.arachne.portal.service.UserLinkService;
import com.odysseusinc.arachne.portal.service.UserPublicationService;
import com.odysseusinc.arachne.portal.service.UserRegistrantService;
import com.odysseusinc.arachne.portal.service.impl.solr.FieldList;
import com.odysseusinc.arachne.portal.service.impl.solr.SearchResult;
import com.odysseusinc.arachne.portal.service.impl.solr.SolrField;
import com.odysseusinc.arachne.portal.service.mail.ArachneMailSender;
import com.odysseusinc.arachne.portal.service.mail.RegistrationMailMessage;
import com.odysseusinc.arachne.portal.service.mail.RemindPasswordMailMessage;
import com.odysseusinc.arachne.portal.util.UUIDGenerator;
import edu.vt.middleware.password.Password;
import edu.vt.middleware.password.PasswordValidator;
import edu.vt.middleware.password.RuleResult;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import org.apache.commons.io.FilenameUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;


public abstract class BaseUserServiceImpl<U extends User, S extends Skill, SF extends SolrField> implements BaseUserService<U, S> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseUserServiceImpl.class);
    private static final String USERS_DIR = "users";
    private static final String AVATAR_FILE_NAME = "avatar.jpg";
    private static final String PASSWORD_NOT_MATCH_EXC = "Old password is incorrect";
    private final MessageSource messageSource;
    protected final ProfessionalTypeService professionalTypeService;
    private final JavaMailSender javaMailSender;
    protected final BaseUserRepository<U> userRepository;
    private final CountryRepository countryRepository;
    private final StateProvinceRepository stateProvinceRepository;
    private final UserStudyRepository userStudyRepository;
    private final StudyDataSourceLinkRepository studyDataSourceLinkRepository;
    private final BaseSkillService<S> skillService;
    private final UserLinkService userLinkService;
    private final UserPublicationService userPublicationService;
    private final RoleRepository roleRepository;
    private final BaseSolrService<SF> solrService;
    private final GenericConversionService conversionService;
    private final AnalysisUnlockRequestRepository analysisUnlockRequestRepository;
    private final ArachneMailSender arachneMailSender;
    private final UserRegistrantService userRegistrantService;
    private final PasswordValidator passwordValidator;
    @Value("${files.store.path}")
    private String fileStorePath;
    @Value("${user.enabled.default}")
    private boolean userEnableDefault;
    private Resource defaultAvatar = new ClassPathResource("avatar.svg");
    @Value("${portal.authMethod}")
    protected String userOrigin;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public BaseUserServiceImpl(StateProvinceRepository stateProvinceRepository,
                               MessageSource messageSource,
                               ProfessionalTypeService professionalTypeService,
                               JavaMailSender javaMailSender,
                               @Qualifier("passwordValidator") ArachnePasswordValidator passwordValidator,
                               BaseUserRepository<U> userRepository,
                               CountryRepository countryRepository,
                               BaseSolrService<SF> solrService,
                               ArachneMailSender arachneMailSender,
                               UserStudyRepository userStudyRepository,
                               UserPublicationService userPublicationService,
                               UserRegistrantService userRegistrantService,
                               StudyDataSourceLinkRepository studyDataSourceLinkRepository,
                               GenericConversionService conversionService,
                               AnalysisUnlockRequestRepository analysisUnlockRequestRepository,
                               BaseSkillService<S> skillService,
                               RoleRepository roleRepository,
                               UserLinkService userLinkService) {

        this.stateProvinceRepository = stateProvinceRepository;
        this.messageSource = messageSource;
        this.professionalTypeService = professionalTypeService;
        this.javaMailSender = javaMailSender;
        this.passwordValidator = passwordValidator;
        this.userRepository = userRepository;
        this.countryRepository = countryRepository;
        this.solrService = solrService;
        this.arachneMailSender = arachneMailSender;
        this.userStudyRepository = userStudyRepository;
        this.userPublicationService = userPublicationService;
        this.userRegistrantService = userRegistrantService;
        this.studyDataSourceLinkRepository = studyDataSourceLinkRepository;
        this.conversionService = conversionService;
        this.analysisUnlockRequestRepository = analysisUnlockRequestRepository;
        this.skillService = skillService;
        this.roleRepository = roleRepository;
        this.userLinkService = userLinkService;
    }

    @Override
    public U getByUsername(final String username) {

        return getByUsername(this.userOrigin, username);
    }

    @Override
    public U getByUsername(final String userOrigin, final String username) {

        return userRepository.findByEmailAndEnabledTrue(username);
    }

    @Override
    public U getByEmail(final String email) {

        return getByUsername(this.userOrigin, email);
        // return email != null ? userRepository.findByEmailAndEnabledTrue(email) : null;
    }

    @Override
    public U getByUnverifiedEmail(final String email) {

        return userRepository.findByEmail(email, EntityGraphUtils.fromAttributePaths("roles", "professionalType"));
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void remove(Long id) throws ValidationException, UserNotFoundException, NotExistException, IOException, SolrServerException {

        if (id == null) {
            throw new ValidationException("remove user: id must be not null");
        }
        U user = userRepository.findOne(id);
        if (user == null) {
            throw new UserNotFoundException("removeUser", "remove user: user not found");
        }
        solrService.deleteByQuery(SolrServiceImpl.USER_COLLECTION, "id:" + user.getId());
        userRepository.delete(user);
    }

    @Override
    public U create(final @NotNull U user)
            throws NotUniqueException, NotExistException, PasswordValidationException {

        U byEmail = getByUnverifiedEmail(user.getEmail());
        if (byEmail != null) {
            throw new NotUniqueException(
                    "email",
                    messageSource.getMessage("validation.email.already.used", null, null)
            );
        }
        user.setUsername(user.getEmail());
        if (Objects.isNull(user.getEnabled())) {
            user.setEnabled(userEnableDefault);
        }
        Date date = new Date();
        user.setCreated(date);
        user.setUpdated(date);
        user.setLastPasswordReset(date);

        user.setProfessionalType(professionalTypeService.getById(user.getProfessionalType().getId()));
        String password = user.getPassword();
        final String username = user.getUsername();
        final String firstName = user.getFirstname();
        final String lastName = user.getLastname();
        final String middleName = user.getMiddlename();
        validatePassword(username, firstName, lastName, middleName, password);
        user.setPassword(passwordEncoder.encode(password));

        return userRepository.save(user);
    }

    @Override
    public U register(final @NotNull U user, String registrantToken, String callbackUrl)
            throws NotUniqueException, NotExistException, PasswordValidationException {

        user.setEnabled(false);
        user.setEmailConfirmed(false);
        user.setRegistrationCode(UUID.randomUUID().toString());

        U savedUser = create(user);

        Optional<UserRegistrant> userRegistrant = userRegistrantService.findByToken(registrantToken);
        sendRegistrationEmail(savedUser, userRegistrant, callbackUrl);
        return savedUser;
    }


    @Override
    public void confirmUserEmail(U user)
            throws IOException, NotExistException,
            SolrServerException, NoSuchFieldException, IllegalAccessException {

        user.setEmailConfirmed(true);
        user.setRegistrationCode("");
        user.setUpdated(new Date());
        user.setEnabled(userEnableDefault);
        U savedUser = userRepository.save(user);
        indexBySolr(savedUser);
    }

    @Override
    public void confirmUserEmail(String activateCode)
            throws UserNotFoundException, IOException, NotExistException,
            SolrServerException, NoSuchFieldException, IllegalAccessException {

        U user = userRepository.findByRegistrationCode(activateCode);
        if (user == null) {
            throw new UserNotFoundException("activationCode", "user not found by registration code " + activateCode);
        }
        confirmUserEmail(user);
    }

    @Override
    public void resendActivationEmail(String email) throws UserNotFoundException {

        final U user = userRepository.findByEmailAndEnabledFalse(email);
        if (user == null) {
            throw new UserNotFoundException("email", "not enabled user is not found by email " + email);
        }
        sendRegistrationEmail(user);
    }

    @Override
    public U getByIdAndInitializeCollections(Long id) {

        return initUserCollections(ensureUuidAssigned(getById(id)));
    }

    @Override
    public U getById(Long id) {

        return userRepository.findOne(id);
    }


    @Override
    public List<U> getAllByIDs(List<Long> ids) {

        return userRepository.findByIdIn(ids);
    }

    @Override
    public U update(final U user)
            throws IllegalAccessException, SolrServerException, IOException, NotExistException, NoSuchFieldException {

        U forUpdate = userRepository.findOne(user.getId());
        final Date date = new Date();
        forUpdate.setId(user.getId());
        if (user.getFirstname() != null) {
            forUpdate.setFirstname(user.getFirstname());
        }
        if (user.getMiddlename() != null) {
            forUpdate.setMiddlename(user.getMiddlename());
        }
        if (user.getLastname() != null) {
            forUpdate.setLastname(user.getLastname());
        }
        forUpdate.setEnabled(user.getEnabled() != null ? user.getEnabled() : forUpdate.getEnabled());
        forUpdate.setUpdated(date);
        if (user.getProfessionalType() != null) {
            ProfessionalType professionalType = professionalTypeService.getById(user.getProfessionalType().getId());
            if (professionalType != null) {
                forUpdate.setProfessionalType(professionalType);
            }
        }
        if (user.getPhone() != null) {
            forUpdate.setPhone(user.getPhone());
        }
        if (user.getMobile() != null) {
            forUpdate.setMobile(user.getMobile());
        }
        if (user.getAddress1() != null) {
            forUpdate.setAddress1(user.getAddress1());
        }
        if (user.getAddress2() != null) {
            forUpdate.setAddress2(user.getAddress2());
        }
        if (user.getCity() != null) {
            forUpdate.setCity(user.getCity());
        }
        if (user.getZipCode() != null) {
            forUpdate.setZipCode(user.getZipCode());
        }
        if (user.getCountry() != null) {
            Country country = countryRepository.findOne(user.getCountry().getId());
            if (country != null) {
                forUpdate.setCountry(country);
            }
        }
        if (user.getStateProvince() != null) {
            StateProvince stateProvince = stateProvinceRepository.findOne(user.getStateProvince().getId());
            if (stateProvince != null) {
                forUpdate.setStateProvince(stateProvince);
            }
        }
        if (user.getAffiliation() != null) {
            forUpdate.setAffiliation(user.getAffiliation());
        }
        if (user.getPersonalSummary() != null) {
            forUpdate.setPersonalSummary(user.getPersonalSummary());
        }
        if (user.getContactEmail() != null) {
            forUpdate.setContactEmail(user.getContactEmail());
        }
        U savedUser = initUserCollections(userRepository.save(forUpdate));

        if (savedUser.getEnabled()) {
            indexBySolr(savedUser);
        } else {
            solrService.deleteByQuery(SolrServiceImpl.USER_COLLECTION, "id:" + user.getId());
        }
        return savedUser;
    }

    @Override
    public U getByUuid(String uuid) {

        if (uuid != null && !uuid.isEmpty()) {
            return userRepository.findByUuid(uuid);
        } else {
            throw new IllegalArgumentException("Given uuid is blank");
        }
    }

    @Override
    public U getByUuidAndInitializeCollections(String uuid) {

        return initUserCollections(getByUuid(uuid));
    }

    @Override
    public List<U> suggestUser(final String query, List<String> emailsList, final Integer limit) {

        final String preparedQuery = prepareQuery(query);
        return userRepository.suggest(preparedQuery, emailsList, limit);
    }

    @Override
    public List<U> suggestUserToStudy(final String query, final Long studyId, int limit) {

        final String preparedQuery = prepareQuery(query);
        return userRepository.suggestToStudy(preparedQuery, studyId, limit);
    }

    @Override
    public List<U> suggestUserToPaper(String query, Long paperId, int limit) {

        final String preparedQuery = prepareQuery(query);
        return userRepository.suggestToPaper(preparedQuery, paperId, limit);
    }

    @Override
    public List<U> suggestNotAdmin(final String query, Integer limit) {

        String[] split = query.trim().split(" ");
        StringBuilder suggestRequest = new StringBuilder("%(");
        for (String s : split) {
            suggestRequest.append(s.toLowerCase()).append("|");
        }
        suggestRequest.delete(suggestRequest.length() - 1, suggestRequest.length());
        suggestRequest.append(")%");
        return userRepository.suggestNotAdmin(suggestRequest.toString(), limit);
    }

    @Override
    public List<U> getAllEnabled() {

        return userRepository.findAllByEnabledIsTrue();
    }

    @Override
    public Page<U> getAll(Pageable pageable, UserSearch userSearch) {

        Specifications<U> spec = where(UserSpecifications.hasEmail());
        if (userSearch.getEmailConfirmed() != null && userSearch.getEmailConfirmed()) {
            spec = spec.and(emailConfirmed());
        }
        if (userSearch.getEnabled() != null && userSearch.getEnabled()) {
            spec = spec.and(userEnabled());
        }
        if (!StringUtils.isEmpty(userSearch.getQuery())) {
            String pattern = userSearch.getQuery() + "%";
            spec = spec.and(withNameOrEmailLike(pattern));
        }
        return userRepository.findAll(spec, pageable);
    }

    private void sendRegistrationEmail(final U user) {

        sendRegistrationEmail(user, Optional.empty(), null);
    }

    private void sendRegistrationEmail(U user, Optional<UserRegistrant> userRegistrant, String callbackUrl) {

        RegistrationMailMessage mail = new RegistrationMailMessage(
                user,
                WebSecurityConfig.portalUrl.get(),
                user.getRegistrationCode()
        );
        userRegistrant.ifPresent(registrant ->
                userRegistrantService.customizeUserRegistrantMailMessage(registrant, callbackUrl, mail));
        arachneMailSender.send(mail);
    }

    @Override
    public void resetPassword(U user)
            throws UserNotFoundException, IllegalAccessException, NotExistException,
            NoSuchFieldException, SolrServerException, IOException {

        U forUpdate = userRepository.findOne(user.getId());
        Date date = new Date();
        forUpdate.setId(user.getId());
        forUpdate.setPassword(passwordEncoder.encode(user.getPassword()));
        forUpdate.setUpdated(date);
        U saved = userRepository.save(forUpdate);
        if (saved.getRegistrationCode() != null && !saved.getRegistrationCode().isEmpty()) {
            confirmUserEmail(saved.getRegistrationCode());
        }
    }

    @Override
    public void updatePassword(U user, String oldPassword, String newPassword)
            throws ValidationException, PasswordValidationException {

        U exists = userRepository.findOne(user.getId());

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new ValidationException(PASSWORD_NOT_MATCH_EXC);
        }
        validatePassword(user.getUsername(), user.getFirstname(), user.getLastname(), user.getMiddlename(), newPassword);
        exists.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(exists);
    }

    @Override
    public U addSkillToUser(Long userId, Long skillId)
            throws NotExistException, IllegalAccessException, SolrServerException, IOException, NoSuchFieldException {

        U forUpdate = userRepository.findOne(userId);
        S skill = skillService.getById(skillId);
        forUpdate.getSkills().add(skill);
        U savedUser = initUserCollections(userRepository.save(forUpdate));
        indexBySolr(savedUser);

        return savedUser;
    }

    @Override
    public U removeSkillFromUser(Long userId, Long skillId)
            throws NotExistException, IllegalAccessException, SolrServerException, IOException, NoSuchFieldException {

        U forUpdate = userRepository.findOne(userId);
        Skill skill = skillService.getById(skillId);
        forUpdate.getSkills().remove(skill);
        U savedUser = initUserCollections(userRepository.save(forUpdate));
        indexBySolr(savedUser);

        return savedUser;
    }

    @Override
    public U addLinkToUser(Long userId, UserLink link)
            throws NotExistException, NotUniqueException, PermissionDeniedException {

        U forUpdate = userRepository.findOne(userId);
        link.setUser(forUpdate);
        userLinkService.create(link);
        return initUserCollections(forUpdate);
    }

    private U ensureUuidAssigned(final U user) {

        U updated = user;
        if (Objects.isNull(user.getUuid())) {
            updated.setUuid(UUIDGenerator.generateUUID());
            updated = userRepository.saveAndFlush(updated);
        }
        return updated;
    }

    private U initUserCollections(U user) {

        if (user != null) {
            user.setRoles(roleRepository.findByUser(user.getId()));
            user.setLinks(userLinkService.findByUser(user));
            user.setPublications(userPublicationService.findByUser(user));
        }
        return user;
    }

    @Override
    public U removeLinkFromUser(Long userId, Long linkId) throws NotExistException {

        userLinkService.delete(linkId);
        U user = userRepository.findOne(userId);
        user.getLinks().size();
        return initUserCollections(user);
    }

    @Override
    public U addPublicationToUser(Long userId, UserPublication publication)
            throws NotExistException, NotUniqueException, PermissionDeniedException {

        U forUpdate = userRepository.findOne(userId);
        publication.setUser(forUpdate);
        UserPublication userPublication = userPublicationService.create(publication);
        forUpdate.getPublications().add(userPublication);
        return initUserCollections((U) userPublication.getUser());
    }

    @Override
    public U removePublicationFromUser(Long userId, Long publicationId) throws NotExistException {

        userPublicationService.delete(publicationId);
        U user = userRepository.findOne(userId);
        user.getPublications().size();
        return initUserCollections(user);
    }

    @Override
    public void saveAvatar(U user, MultipartFile file)
            throws IOException, WrongFileFormatException, ImageProcessingException, MetadataException, IllegalAccessException, SolrServerException, NoSuchFieldException {

        String fileExt = FilenameUtils.getExtension(file.getOriginalFilename());
        BufferedImage img = ImageIO.read(file.getInputStream());
        if (img == null) {
            throw new WrongFileFormatException("file", "File format is not supported");
        }

        final File avatar = getUserAvatarFile(user);

        Metadata metadata = ImageMetadataReader.readMetadata(file.getInputStream());
        ExifIFD0Directory exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);

        int orientation = 1;
        try {
            orientation = exifIFD0Directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
        } catch (Exception ignore) {
            LOGGER.debug(ignore.getMessage(), ignore);
        }

        List<Scalr.Rotation> rotations = new LinkedList<>();

        switch (orientation) {
            case 1:
                break;
            case 2: // Flip X
                rotations.add(Scalr.Rotation.FLIP_HORZ);
                break;
            case 3: // PI rotation
                rotations.add(Scalr.Rotation.CW_180);
                break;
            case 4: // Flip Y
                rotations.add(Scalr.Rotation.FLIP_VERT);
                break;
            case 5: // - PI/2 and Flip X
                rotations.add(Scalr.Rotation.CW_90);
                rotations.add(Scalr.Rotation.FLIP_HORZ);
                break;
            case 6: // -PI/2 and -width
                rotations.add(Scalr.Rotation.CW_90);
                break;
            case 7: // PI/2 and Flip
                rotations.add(Scalr.Rotation.CW_90);
                rotations.add(Scalr.Rotation.FLIP_VERT);
                break;
            case 8: // PI / 2
                rotations.add(Scalr.Rotation.CW_270);
                break;
            default:
                break;
        }

        for (Scalr.Rotation rotation : rotations) {
            img = Scalr.rotate(img, rotation);
        }
        BufferedImage thumbnail = Scalr.resize(img,
                Math.min(Math.max(img.getHeight(), img.getWidth()), 640),
                Scalr.OP_ANTIALIAS);
        ImageIO.write(thumbnail, fileExt, avatar);
        user.setUpdated(new Date());
        U savedUser = userRepository.save(user);
        indexBySolr(savedUser);


    }

    private File getUserAvatarFile(U user) {

        File filesStoreDir = new File(fileStorePath);
        if (!filesStoreDir.exists()) {
            filesStoreDir.mkdirs();
        }
        File userFilesDir = Paths.get(filesStoreDir.getPath(), USERS_DIR, user.getId().toString()).toFile();
        if (!userFilesDir.exists()) {
            userFilesDir.mkdirs();
        }
        return Paths.get(userFilesDir.getPath(), AVATAR_FILE_NAME).toFile();
    }

    @Override
    public List<? extends Invitationable> getCollaboratorInvitations(U user) {

        return userStudyRepository.findByUserAndStatus(user, ParticipantStatus.PENDING);
    }

    @Override
    public List<? extends Invitationable> getDataSourceInvitations(U user) {

        return studyDataSourceLinkRepository.findByOwnerAndStatus(user, DataSourceStatus.PENDING);
    }

    @Override
    public List<? extends Invitationable> getInvitationsForStudy(U user, final Long studyId) {

        List<? extends Invitationable> collaboratorInvitations = userStudyRepository.findByUserAndStudyIdAndStatus(
                user,
                studyId,
                ParticipantStatus.PENDING
        );
        List<? extends Invitationable> dataSourceInvitations = studyDataSourceLinkRepository.findByOwnerAndStudyIdAndStatus(
                user,
                studyId,
                DataSourceStatus.PENDING
        );

        return Stream
                .concat(collaboratorInvitations.stream(), dataSourceInvitations.stream())
                .collect(Collectors.toList());
    }

    @Override
    public UserStudy processInvitation(U user, Long id, Boolean accepted,
                                       String comment) {

        UserStudy userStudy = userStudyRepository.findByIdAndUser(id, user);
        if (userStudy != null) {
            ParticipantStatus status = TRUE.equals(accepted)
                    ? APPROVED
                    : DECLINED;
            userStudy.setStatus(status);
            userStudy.setComment(DECLINED == status ? comment : null);
            userStudyRepository.save(userStudy);
        }
        return userStudy;
    }

    public UserStudy getByIdAndStatusPendingAndToken(Long userStudyId, String token) throws NotExistException {

        UserStudy userStudy = userStudyRepository.findByIdAndStatusAndToken(userStudyId, ParticipantStatus.PENDING,
                token);
        if (userStudy == null) {
            throw new NotExistException("User study with id=" + userStudyId + " and pending status and token = "
                    + token + "is not found ", UserStudy.class);
        }
        return userStudy;
    }

    @Override
    public void sendRemindPasswordEmail(U user, String token, String registrantToken, String callbackUrl) {

        RemindPasswordMailMessage mail = new RemindPasswordMailMessage(
                user,
                WebSecurityConfig.portalUrl.get(),
                token);

        Optional<UserRegistrant> userRegistrant = userRegistrantService.findByToken(registrantToken);
        userRegistrant.ifPresent(registrant ->
                userRegistrantService.customizeUserRegistrantMailMessage(registrant, callbackUrl, mail));
        arachneMailSender.send(mail);
    }

    public FieldList getSolrFields() {

        FieldList fieldList = new FieldList();
        fieldList.addAll(solrService.getFieldsOfClass(User.class));
        fieldList.addAll(getExtraSolrFields());
        return fieldList;
    }

    protected List<SolrField> getExtraSolrFields() {

        return Collections.emptyList();
    }

    public void indexBySolr(U user)
            throws IllegalAccessException, IOException, SolrServerException, NotExistException, NoSuchFieldException {

        solrService.putDocument(
                SolrServiceImpl.USER_COLLECTION,
                user.getId(),
                solrService.getValuesByEntity(user)
        );
    }

    public void indexAllBySolr()
            throws IOException, NotExistException, SolrServerException, NoSuchFieldException, IllegalAccessException {

        solrService.deleteByQuery(SolrServiceImpl.USER_COLLECTION, "*:*");
        List<U> userList = getAllEnabled();
        for (U user : userList) {
            indexBySolr(user);
        }
    }

    public SearchResult<U> search(SolrQuery solrQuery) throws IOException, SolrServerException {

        List<U> userList;

        QueryResponse solrResponse = solrService.search(
                SolrServiceImpl.USER_COLLECTION,
                solrQuery
        );

        List<Long> docIdList = solrResponse
                .getResults()
                .stream()
                .map(solrDoc -> Long.parseLong(solrDoc.get("id").toString()))
                .collect(Collectors.toList());

        userList = userRepository.findByIdIn(docIdList);
        userList = userList.stream().map(this::ensureUuidAssigned)
                .sorted(Comparator.comparing(item -> docIdList.indexOf(item.getId())))
                .collect(Collectors.toList());

        SearchResult<U> searchResult = new SearchResult<>(solrQuery, solrResponse, userList);
        searchResult.setExcludedOptions(getExcludedOptions());
        return searchResult;
    }

    private Map<String, List<String>> getExcludedOptions() throws IOException, SolrServerException {

        SolrQuery solrQuery = conversionService.convert(new SearchExpertListDTO(true), SolrQuery.class);

        QueryResponse solrResponse = solrService.search(
                SolrServiceImpl.USER_COLLECTION,
                solrQuery
        );
        SearchResult<Long> searchResult = new SearchResult<>(solrQuery, solrResponse, Collections.<Long>emptyList());
        return searchResult.excludedOptions();
    }

    @Override
    public List<Country> suggestCountry(String query, Integer limit, Long includeId) {

        List<Country> result = new LinkedList<>();
        if (query != null) {
            result.addAll(
                    countryRepository.suggest(
                            "%" + query.toLowerCase() + "%",
                            limit,
                            includeId != null ? includeId : -1L));
        }
        return result;
    }

    @Override
    public List<StateProvince> suggestStateProvince(String query, Long countryId, Integer limit, Long includeId) {

        List<StateProvince> result = new LinkedList<>();
        if (query != null && countryId != null) {
            result.addAll(
                    stateProvinceRepository.suggest(
                            "%" + query.toLowerCase() + "%",
                            countryId,
                            limit,
                            includeId != null ? includeId : -1L));
        }
        return result;
    }

    @Override
    public U getUser(Principal principal) throws PermissionDeniedException {

        if (principal == null) {
            throw new PermissionDeniedException();
        }
        U user = getByEmail(principal.getName());
        if (user == null) {
            throw new PermissionDeniedException();
        }
        return user;
    }

    @Override
    public U getCurrentUser() throws PermissionDeniedException {

        Authentication principal = SecurityContextHolder.getContext().getAuthentication();
        return getUser(principal);
    }

    @Override
    public List<U> getAllAdmins(final String sortBy, final Boolean sortAsc) {

        Sort.Direction direction = sortAsc != null && sortAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        final Sort sort;
        if (sortBy == null || sortBy.isEmpty() || sortBy.equals("name")) {
            sort = new Sort(direction, "firstname", "lastname", "middlename");
        } else {
            sort = new Sort(direction, sortBy);
        }
        List<U> admins = userRepository.findByRoles_name(ROLE_ADMIN, sort);
        return admins;
    }

    @Override
    public void addUserToAdmins(Long id) {

        U user = userRepository.findOne(id);
        List<Role> roles = roleRepository.findByName(ROLE_ADMIN);
        if (roles != null && !roles.isEmpty()) {
            user.getRoles().add(roles.get(0));
            userRepository.save(user);
        } else {
            throw new ArachneSystemRuntimeException("ROLE_ADMIN not found");
        }
    }

    @Override
    public void removeUserFromAdmins(Long id) {

        U user = userRepository.findOne(id);
        List<Role> roles = roleRepository.findByName(ROLE_ADMIN);
        if (roles != null && !roles.isEmpty()) {
            user.getRoles().remove(roles.get(0));
            userRepository.save(user);
        } else {
            throw new ArachneSystemRuntimeException("ROLE_ADMIN not found");
        }
    }

    @Override
    public List<U> getUsersByUserNames(List<String> userNames) {

        return userRepository.findAllByUsernameInAndEnabledTrue(userNames);
    }

    @Override
    public List<? extends Invitationable> getUnlockAnalysisRequests(U user) {

        return analysisUnlockRequestRepository.findAllByLeadId(user.getId());
    }

    @Override
    public U findOne(Long participantId) {

        return userRepository.findOne(participantId);
    }

    @Override
    public List<U> findUsersByIdsIn(List<Long> ids) {

        return userRepository.findByIdIn(ids);
    }

    @Override
    public List<U> findUsersApprovedInDataSource(Long id) {

        return userRepository.listApprovedByDatasource(id);
    }

    private void validatePassword(String username, String firstName, String lastName, String middleName, String password) throws PasswordValidationException {

        ArachnePasswordData passwordData = new ArachnePasswordData(new Password(password));
        passwordData.setUsername(username);
        passwordData.setFirstName(firstName);
        passwordData.setLastName(lastName);
        passwordData.setMiddleName(middleName);
        RuleResult result = passwordValidator.validate(passwordData);
        if (!result.isValid()) {
            throw new PasswordValidationException(passwordValidator.getMessages(result));
        }
    }

    private String prepareQuery(String query) {

        String[] split = query.toLowerCase().trim().split(" ");
        return Stream.of(split).collect(Collectors.joining("|", "%(", ")%"));
    }

    @Override
    public void putAvatarToResponse(HttpServletResponse response, U user) throws IOException {

        try (final AvatarResolver res = new AvatarResolver(user)) {
            response.setContentType(res.getContentType());
            response.setHeader("Content-type", res.getContentType());
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
            response.setHeader("Content-Disposition", "attachment; filename=avatar");
            org.apache.commons.io.IOUtils.copy(res.getInputStream(), response.getOutputStream());
            response.flushBuffer();
        }
    }

    private class AvatarResolver implements AutoCloseable {

        final private String contentType;
        final InputStream inputStream;

        private AvatarResolver(final U user) throws IOException {

            final File userAvatarFile = getUserAvatarFile(user);
            if (user != null && userAvatarFile.exists()) {
                this.contentType = CommonFileUtils.getMimeType(userAvatarFile.getName(), userAvatarFile.getAbsolutePath());
                this.inputStream = new FileInputStream(userAvatarFile);
            } else {
                this.contentType = CommonFileUtils.getMimeType(defaultAvatar.getFilename(), defaultAvatar);
                this.inputStream = defaultAvatar.getInputStream();
            }
        }

        public String getContentType() {

            return this.contentType;
        }

        public InputStream getInputStream() {

            return this.inputStream;
        }

        @Override
        public void close() throws IOException {

            inputStream.close();
        }
    }
}
