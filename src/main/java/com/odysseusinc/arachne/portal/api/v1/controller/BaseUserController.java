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
 * Created: September 19, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.controller;

import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.MetadataException;
import com.odysseusinc.arachne.commons.api.v1.dto.ArachnePasswordInfoDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonArachneUserStatusDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonLinkUserToDataNodeDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonUserDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonUserRegistrationDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult;
import com.odysseusinc.arachne.commons.utils.UserIdUtils;
import com.odysseusinc.arachne.portal.api.v1.dto.ApproveDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.ChangePasswordDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.CountryDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.InvitationActionDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.InvitationActionWithTokenDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.InvitationDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.InvitationType;
import com.odysseusinc.arachne.portal.api.v1.dto.RemindPasswordDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.StateProvinceDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UserLinkDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UserProfileDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UserProfileGeneralDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UserPublicationDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UserSettingsDTO;
import com.odysseusinc.arachne.portal.exception.AlreadyExistException;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.NotUniqueException;
import com.odysseusinc.arachne.portal.exception.PasswordValidationException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.exception.UserNotFoundException;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.exception.WrongFileFormatException;
import com.odysseusinc.arachne.portal.model.AbstractUserStudyListItem;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.AnalysisUnlockRequest;
import com.odysseusinc.arachne.portal.model.Country;
import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.model.DataNodeUser;
import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.Invitationable;
import com.odysseusinc.arachne.portal.model.Paper;
import com.odysseusinc.arachne.portal.model.Skill;
import com.odysseusinc.arachne.portal.model.StateProvince;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.StudyDataSourceLink;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.model.UserLink;
import com.odysseusinc.arachne.portal.model.UserPublication;
import com.odysseusinc.arachne.portal.model.UserStudy;
import com.odysseusinc.arachne.portal.model.search.PaperSearch;
import com.odysseusinc.arachne.portal.model.search.StudySearch;
import com.odysseusinc.arachne.portal.security.passwordvalidator.ArachnePasswordValidator;
import com.odysseusinc.arachne.portal.service.AnalysisUnlockRequestService;
import com.odysseusinc.arachne.portal.service.BaseDataNodeService;
import com.odysseusinc.arachne.portal.service.BasePaperService;
import com.odysseusinc.arachne.portal.service.BaseStudyService;
import com.odysseusinc.arachne.portal.service.BaseUserService;
import com.odysseusinc.arachne.portal.service.analysis.BaseAnalysisService;
import com.odysseusinc.arachne.portal.service.submission.BaseSubmissionService;
import com.odysseusinc.arachne.portal.util.UserUtils;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.solr.client.solrj.SolrServerException;
import org.ohdsi.authenticator.service.authentication.Authenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult.ErrorCode.NO_ERROR;
import static com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult.ErrorCode.VALIDATION_ERROR;
import static com.odysseusinc.arachne.portal.api.v1.controller.util.ControllerUtils.emulateEmailSent;

public abstract class BaseUserController<
        U extends IUser,
        S extends Study,
        DS extends IDataSource,
        SS extends StudySearch,
        SU extends AbstractUserStudyListItem,
        DN extends DataNode,
        P extends Paper,
        PS extends PaperSearch,
        SK extends Skill,
        A extends Analysis,
        SB extends Submission> extends BaseController<DN, U> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseUserController.class);
    private static final String AVATAR_CONTENT_TYPE = "image/*";
    private static final String DATA_NODE_NOT_FOUND_EXCEPTION = "dataNode %s not found";
    private static final String INVITATION_HOME_PAGE = "/study-manager/studies/";

    protected final BaseUserService<U, SK> userService;
    protected final BaseStudyService<S, DS, SS, SU> studyService;
    protected final GenericConversionService conversionService;
    protected final BaseDataNodeService<DN> baseDataNodeService;
    protected final BaseAnalysisService<A> analysisService;
    protected final AnalysisUnlockRequestService analysisUnlockRequestService;
    protected final BasePaperService<P, PS, S, DS, SS, SU> paperService;
    protected final BaseSubmissionService<SB, A> submissionService;
    protected final ArachnePasswordValidator passwordValidator;
    protected final Authenticator authenticator;
    @Value("${arachne.token.header}")
    private String tokenHeader;

    public BaseUserController(BaseUserService<U, SK> userService,
                              BaseStudyService<S, DS, SS, SU> studyService,
                              GenericConversionService conversionService,
                              BaseDataNodeService<DN> baseDataNodeService,
                              BaseAnalysisService<A> analysisService,
                              AnalysisUnlockRequestService analysisUnlockRequestService,
                              BasePaperService<P, PS, S, DS, SS, SU> paperService,
                              BaseSubmissionService<SB, A> submissionService,
                              ArachnePasswordValidator passwordValidator,
                              Authenticator authenticator) {

        this.userService = userService;
        this.studyService = studyService;
        this.conversionService = conversionService;
        this.baseDataNodeService = baseDataNodeService;
        this.analysisService = analysisService;
        this.analysisUnlockRequestService = analysisUnlockRequestService;
        this.paperService = paperService;
        this.submissionService = submissionService;
        this.passwordValidator = passwordValidator;
        this.authenticator = authenticator;
    }

    @ApiOperation("Password restrictions")
    @GetMapping(value = "/api/v1/auth/password-policies")
    public ArachnePasswordInfoDTO getPasswordPolicies() {

        return conversionService.convert(passwordValidator.getPasswordInfo(), ArachnePasswordInfoDTO.class);
    }

    @ApiOperation("Register new user via form.")
    @PostMapping(value = "/api/v1/auth/registration")
    public void register(@RequestBody @Valid CommonUserRegistrationDTO dto)
            throws NotExistException, PermissionDeniedException, PasswordValidationException, InterruptedException {

        try {
            U user = convertRegistrationDTO(dto);
            user.setUsername(user.getEmail());
            user.setEnabled(false);
            userService.createWithEmailVerification(user, dto.getRegistrantToken(), dto.getCallbackUrl());
        } catch (NotUniqueException ex) {
            // If user with such email already exists,
            // mute exception to prevent "Unauthenticated Email Address Enumeration" security issue
            emulateEmailSent();
        }
    }

    protected abstract U convertRegistrationDTO(CommonUserRegistrationDTO dto);

    @ApiOperation("Resend registration email for not enabled user")
    @PostMapping(value = "/api/v1/auth/resend-activation-email")
    public JsonResult<Map<String, Object>> resendActivationEmail(@RequestBody @Valid RemindPasswordDTO request, BindingResult binding)
            throws UserNotFoundException {

        JsonResult<Map<String, Object>> result;
        if (binding.hasErrors()) {
            result = new JsonResult<>(JsonResult.ErrorCode.VALIDATION_ERROR);
            binding.getFieldErrors()
                    .forEach(e -> result.getValidatorErrors().put(e.getField(), e.getDefaultMessage()));
        } else {
            userService.resendActivationEmail(request.getEmail());
            result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
        }
        return result;
    }

    @ApiOperation("Get status of registered user")
    @GetMapping(value = "/api/v1/auth/status/{userUuid}")
    public JsonResult<CommonArachneUserStatusDTO> findUserStatus(@PathVariable("userUuid") String uuid)
            throws UserNotFoundException {

        JsonResult<CommonArachneUserStatusDTO> result;
        IUser user = userService.getByUuid(uuid);
        if (user == null) {
            throw new UserNotFoundException("userUuid", "user not found");
        } else {
            result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
            result.setResult(user.getEnabled()
                    ? CommonArachneUserStatusDTO.APPROVED : CommonArachneUserStatusDTO.PENDING);
        }
        return result;
    }

    @ApiOperation("Register user via activation code.")
    @GetMapping(value = "/api/v1/user-management/activation/{activationCode}")
    public void activate(
            Principal principal,
            HttpServletRequest request,
            @PathVariable("activationCode") String activateCode,
            @RequestParam(value = "callbackUrl", required = false) String callbackUrl,
            HttpServletResponse response)
            throws IOException, UserNotFoundException, NotExistException,
            NoSuchFieldException, IllegalAccessException, SolrServerException, URISyntaxException {

        getAuthToken(request).forEach(authenticator::invalidateToken);

        Boolean activated;
        try {
            userService.confirmUserEmail(activateCode);
            activated = true;
        } catch (UserNotFoundException ex) {
            activated = false;
        }

        String safeCallback = callbackUrl != null ? callbackUrl : "/auth/login";
        URIBuilder redirectURIBuilder = new URIBuilder(safeCallback);
        redirectURIBuilder.addParameter(
                "message",
                activated ? "email-confirmed" : "email-not-confirmed"
        );

        response.sendRedirect(redirectURIBuilder.build().toString());
    }

    @ApiOperation("Upload user avatar")
    @PostMapping(value = "/api/v1/user-management/users/avatar")
    public JsonResult<Boolean> saveUserAvatar(
            Authentication principal,
            @RequestParam(name = "file") MultipartFile[] file)
            throws IOException, WrongFileFormatException, ValidationException, ImageProcessingException, MetadataException, IllegalAccessException, SolrServerException, NoSuchFieldException {

        JsonResult<Boolean> result;
        U user = getCurrentUser(principal);
        if (file != null && file.length > 0) {
            userService.saveAvatar(user, file[0]);
        } else {
            throw new ValidationException("no files");
        }
        result = new JsonResult<>(NO_ERROR);
        result.setResult(Boolean.TRUE);

        return result;
    }

    @ApiOperation("Download user avatar")
    @GetMapping(value = "/api/v1/user-management/users/avatar")
    public void getUserAvatar(
            Authentication principal,
            HttpServletResponse response) throws IOException {

        U user = getCurrentUser(principal);
        userService.putAvatarToResponse(response, user);
    }

    @ApiOperation("Download user avatar")
    @GetMapping(value = "/api/v1/user-management/users/{id}/avatar")
    public void getUserAvatar(
            @PathVariable("id") String id,
            HttpServletResponse response) throws IOException {

        U user = userService.getByIdInAnyTenant(UserIdUtils.uuidToId(id));
        userService.putAvatarToResponse(response, user);
    }

    @ApiOperation("Save user profile")
    @PostMapping(value = "/api/v1/user-management/users/profile")
    public JsonResult<UserProfileDTO> saveProfile(
            Authentication principal,
            @RequestBody @Valid UserProfileGeneralDTO dto,
            BindingResult binding)
            throws
            IllegalAccessException,
            SolrServerException,
            IOException,
            NotExistException,
            NoSuchFieldException {

        JsonResult<UserProfileDTO> result;
        U owner = getCurrentUser(principal);
        if (binding.hasErrors()) {
            result = setValidationErrors(binding);
        } else {
            U user = convertUserProfileGeneralDTO(dto);
            user.setId(owner.getId());
            user = userService.update(user);
            result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
            result.setResult(conversionService.convert(user, UserProfileDTO.class));
        }
        return result;
    }

    protected abstract U convertUserProfileGeneralDTO(UserProfileGeneralDTO dto);

    @ApiOperation("Change user password")
    @PostMapping(value = "/api/v1/user-management/users/changepassword")
    public JsonResult changePassword(@RequestBody @Valid ChangePasswordDTO changePasswordDTO,
                                     Authentication principal
    ) throws ValidationException, PasswordValidationException {

        JsonResult result;
        U loggedUser = getCurrentUser(principal);
        try {
            userService.updatePassword(loggedUser, changePasswordDTO.getOldPassword(), changePasswordDTO.getNewPassword());
            result = new JsonResult<>(NO_ERROR);
        } catch (ValidationException ex) {
            result = new JsonResult<>(VALIDATION_ERROR);
            result.setErrorMessage(ex.getMessage());
        }
        return result;
    }

    @ApiOperation("Add skill to user profile.")
    @PostMapping(value = "/api/v1/user-management/users/skills/{skillId}")
    public JsonResult<UserProfileDTO> addSkill(
            Authentication principal,
            @PathVariable("skillId") Long skillId
    ) throws NotExistException, IllegalAccessException, SolrServerException, IOException, NoSuchFieldException {

        JsonResult<UserProfileDTO> result;
        U user = getCurrentUser(principal);
        user = userService.addSkillToUser(user.getId(), skillId);
        UserProfileDTO userProfileDTO = conversionService.convert(user, UserProfileDTO.class);
        result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
        result.setResult(userProfileDTO);
        return result;
    }

    @ApiOperation("Remove skill from user profile.")
    @DeleteMapping(value = "/api/v1/user-management/users/skills/{skillId}")
    public JsonResult<UserProfileDTO> removeSkill(
            Authentication principal,
            @PathVariable("skillId") Long skillId
    ) throws NotExistException, IllegalAccessException, SolrServerException, IOException, NoSuchFieldException {

        JsonResult<UserProfileDTO> result;
        U user = getCurrentUser(principal);
        user = userService.removeSkillFromUser(user.getId(), skillId);
        UserProfileDTO userProfileDTO = conversionService.convert(user, UserProfileDTO.class);
        result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
        result.setResult(userProfileDTO);
        return result;
    }

    @ApiOperation("Add link to user profile.")
    @PostMapping(value = "/api/v1/user-management/users/links")
    public JsonResult<UserProfileDTO> addLink(
            Authentication principal,
            @Valid @RequestBody UserLinkDTO userLinkDTO,
            BindingResult binding
    ) throws NotExistException, PermissionDeniedException, NotUniqueException {

        JsonResult<UserProfileDTO> result;
        if (binding.hasErrors()) {
            result = new JsonResult<>(VALIDATION_ERROR);
            for (FieldError fieldError : binding.getFieldErrors()) {
                result.getValidatorErrors().put(fieldError.getField(), fieldError.getDefaultMessage());
            }
        } else {
            U user = getCurrentUser(principal);
            user = userService.addLinkToUser(user.getId(), conversionService.convert(userLinkDTO, UserLink.class));

            UserProfileDTO userProfileDTO = conversionService.convert(user, UserProfileDTO.class);
            result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
            result.setResult(userProfileDTO);
        }
        return result;
    }

    @ApiOperation("Remove link from user profile.")
    @DeleteMapping(value = "/api/v1/user-management/users/links/{linkId}")
    public JsonResult<UserProfileDTO> removeLink(
            Authentication principal,
            @PathVariable("linkId") Long linkId
    ) throws NotExistException {

        JsonResult<UserProfileDTO> result;
        U user = getCurrentUser(principal);
        user = userService.removeLinkFromUser(user.getId(), linkId);
        UserProfileDTO userProfileDTO = conversionService.convert(user, UserProfileDTO.class);
        result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
        result.setResult(userProfileDTO);
        return result;
    }

    @ApiOperation("Add user's publication.")
    @PostMapping(value = "/api/v1/user-management/users/publications")
    public JsonResult<UserProfileDTO> addPublication(
            Authentication principal,
            @Valid @RequestBody UserPublicationDTO userPublicationDTO,
            BindingResult binding
    ) throws NotExistException, PermissionDeniedException, NotUniqueException {

        JsonResult<UserProfileDTO> result;
        if (binding.hasErrors()) {
            result = new JsonResult<>(VALIDATION_ERROR);
            for (FieldError fieldError : binding.getFieldErrors()) {
                result.getValidatorErrors().put(fieldError.getField(), fieldError.getDefaultMessage());
            }
        } else {

            U user = getCurrentUser(principal);
            user = userService.addPublicationToUser(user.getId(), conversionService.convert(userPublicationDTO,
                    UserPublication.class));
            UserProfileDTO userProfileDTO = conversionService.convert(user, UserProfileDTO.class);
            result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
            result.setResult(userProfileDTO);
        }
        return result;
    }

    @ApiOperation("Remove user's publication.")
    @DeleteMapping(value = "/api/v1/user-management/users/publications/{publicationId}")
    public JsonResult<UserProfileDTO> removePublication(
            Authentication principal,
            @PathVariable("publicationId") Long publicationId
    ) throws NotExistException {

        JsonResult<UserProfileDTO> result;
        U user = getCurrentUser(principal);
        user = userService.removePublicationFromUser(user.getId(), publicationId);
        UserProfileDTO userProfileDTO = conversionService.convert(user, UserProfileDTO.class);
        result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
        result.setResult(userProfileDTO);
        return result;
    }

    @ApiOperation("Get user's invitations.")
    @GetMapping("/api/v1/user-management/users/invitations")
    public JsonResult<List<InvitationDTO>> invitations(Authentication principal) throws NotExistException {

        U user = getCurrentUser(principal);

        Stream<? extends Invitationable> invitationables = getInvitations(user).stream().flatMap(Collection::stream);
        Stream<InvitationDTO> invitationStream = invitationables
                .map(inv -> conversionService.convert(inv, InvitationDTO.class))
                .sorted(Comparator.comparing(InvitationDTO::getDate).reversed());

        return new JsonResult<>(NO_ERROR, invitationStream.collect(Collectors.toList()));
    }

    private List<Collection> getInvitations(U user) {

        return Arrays.asList(
                userService.getCollaboratorInvitations(user),
                analysisService.getWaitingForApprovalSubmissions(user),
                userService.getDataSourceInvitations(user),
                userService.getUnlockAnalysisRequests(user)
        );
    }

    @ApiOperation("Accept invitations via mail.")
    @GetMapping(value = "/api/v1/user-management/users/invitations/mail")
    public JsonResult<UserProfileDTO> invitationAcceptViaMail(
            @RequestParam("id") Long id,
            @RequestParam("accepted") Boolean accepted,
            @RequestParam("type") String type,
            @RequestParam("token") String token,
            @RequestParam(value = "userUuid", required = false) String userUuid,
            HttpServletResponse response
    ) throws NotExistException, AlreadyExistException, IOException {

        InvitationActionWithTokenDTO dto = new InvitationActionWithTokenDTO(id, type, accepted, token);

        String redirectLink;
        U user;

        try {
            user = getUserFromInvitationDto(dto, userUuid);
            redirectLink = getRedirectLinkFromInvitationDto(dto, id, token);
        } catch (NotExistException ex) {
            JsonResult result = new JsonResult<>(VALIDATION_ERROR);
            result.setErrorMessage(ex.getMessage());
            response.sendRedirect(INVITATION_HOME_PAGE);
            return result;
        }
        response.sendRedirect(redirectLink);
        return invitationAccept(dto, user);
    }

    private String getRedirectLinkFromInvitationDto(InvitationActionWithTokenDTO dto, Long id, String token) {

        String redirectLink = INVITATION_HOME_PAGE;

        switch (dto.getType()) {
            case InvitationType.COLLABORATOR: {
                UserStudy userStudy = userService.getByIdAndStatusPendingAndToken(dto.getId(), dto.getToken());
                redirectLink += userStudy.getStudy().getId();
                break;
            }
            case InvitationType.DATA_OWNER: {
                StudyDataSourceLink link = studyService.getByIdAndStatusPendingAndToken(dto.getId(), dto.getToken());
                redirectLink += link.getStudy().getId();
                break;
            }
            case InvitationType.UNLOCK_ANALYSIS: {
                final AnalysisUnlockRequest unlockRequest
                        = analysisUnlockRequestService.getByIdAndTokenAndStatusPending(dto.getId(), dto.getToken());
                redirectLink = "/analysis-execution/analyses/" + unlockRequest.getAnalysis().getId();
                break;
            }
            case InvitationType.APPROVE_PUBLISH_SUBMISSION:
            case InvitationType.APPROVE_EXECUTE_SUBMISSION: {
                final Submission submission = submissionService.getSubmissionByIdAndToken(id, token);
                redirectLink = "/analysis-execution/analyses/" + submission.getAnalysis().getId();
                break;
            }
            default: {
                throw new IllegalArgumentException();
            }
        }

        return redirectLink;
    }

    private U getUserFromInvitationDto(InvitationActionWithTokenDTO dto, String userUuid) {

        U user = createNewUser();

        switch (dto.getType()) {
            case InvitationType.COLLABORATOR: {
                UserStudy userStudy = userService.getByIdAndStatusPendingAndToken(dto.getId(), dto.getToken());
                user = (U) userStudy.getUser();
                break;
            }
            case InvitationType.DATA_OWNER:
            case InvitationType.UNLOCK_ANALYSIS:
            case InvitationType.APPROVE_PUBLISH_SUBMISSION:
            case InvitationType.APPROVE_EXECUTE_SUBMISSION: {
                user = userService.getByUuidInAnyTenantAndInitializeCollections(userUuid);
                break;
            }
            default: {
                throw new IllegalArgumentException();
            }
        }

        return user;
    }

    protected abstract U createNewUser();

    @ApiOperation("Accept invitations.")
    @PostMapping("/api/v1/user-management/users/invitations")
    public JsonResult<UserProfileDTO> invitationAcceptViaInvitation(
            Authentication principal,
            @Valid @RequestBody InvitationActionDTO invitationActionDTO
    ) throws NotExistException, AlreadyExistException, IOException {

        U user = getCurrentUser(principal);
        return invitationAccept(invitationActionDTO, user);
    }

    private JsonResult<UserProfileDTO> invitationAccept(
            InvitationActionDTO invitationActionDTO,
            U user
    ) throws NotExistException, AlreadyExistException, IOException {

        checkIfUserExists(user);

        final Boolean invitationAccepted = invitationActionDTO.getAccepted();
        final Long invitationId = invitationActionDTO.getId();
        switch (invitationActionDTO.getType()) {
            case InvitationType.COLLABORATOR: {
                userService.processInvitation(user, invitationId, invitationAccepted, invitationActionDTO.getComment());
                break;
            }
            case InvitationType.DATA_OWNER: {
                studyService.processDataSourceInvitation(user, invitationId, invitationAccepted,
                        invitationActionDTO.getComment());
                break;
            }
            case InvitationType.UNLOCK_ANALYSIS: {
                analysisService.processAnalysisUnlockRequest(user, invitationId, invitationAccepted);
                break;
            }
            case InvitationType.APPROVE_EXECUTE_SUBMISSION:
                submissionService.approveSubmission(invitationId, invitationAccepted, invitationActionDTO.getComment(),
                        user);
                break;
            case InvitationType.APPROVE_PUBLISH_SUBMISSION:
                ApproveDTO dto = new ApproveDTO(invitationId, invitationAccepted, Boolean.TRUE,
                        invitationActionDTO.getComment());
                submissionService.approveSubmissionResult(invitationId, dto, user);
                break;
            default: {
                throw new IllegalArgumentException();
            }
        }
        return new JsonResult<>(NO_ERROR,
                conversionService.convert(userService.getByIdInAnyTenantAndInitializeCollections(user.getId()),
                        UserProfileDTO.class));
    }

    private void checkIfUserExists(U user) {

        if (user == null || user.getId() == null || userService.findOne(user.getId()) == null) {

            throw new UserNotFoundException("userId", "user not found");
        }
    }

    @ApiOperation("Suggests country.")
    @GetMapping(value = "/api/v1/user-management/countries/search")
    public JsonResult<List<CountryDTO>> suggestCountries(
            @RequestParam("query") String query,
            @RequestParam("limit") Integer limit,
            @RequestParam(value = "includeId", required = false) Long includeId

    ) {

        JsonResult<List<CountryDTO>> result;
        List<Country> countries = userService.suggestCountry(query, limit, includeId);
        List<CountryDTO> countriesDTOs = new LinkedList<>();
        countries
                .forEach(country -> countriesDTOs.add(conversionService.convert(country, CountryDTO.class)));
        result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
        result.setResult(countriesDTOs);
        return result;
    }

    @ApiOperation("Suggests state or province.")
    @GetMapping(value = "/api/v1/user-management/state-province/search")
    public JsonResult<List<StateProvinceDTO>> suggestStateProvince(
            @RequestParam("countryId") String countryIdParam,
            @RequestParam("query") String query,
            @RequestParam("limit") Integer limit,
            @RequestParam(value = "includeId", required = false) String includeIdParam
    ) {

        JsonResult<List<StateProvinceDTO>> result;
        Long countryId = null;
        if (NumberUtils.isCreatable(countryIdParam)) {
            countryId = NumberUtils.createLong(countryIdParam);
        } else if (StringUtils.isNotBlank(countryIdParam)) {
            Country country = Optional.ofNullable(userService.findCountryByCode(countryIdParam))
                    .orElseThrow(() -> new NotExistException(Country.class));
            countryId = country.getId();
        }
        Long includeId = null;
        if (NumberUtils.isCreatable(includeIdParam)) {
            includeId = NumberUtils.createLong(includeIdParam);
        } else if (StringUtils.isNotBlank(includeIdParam)) {
            StateProvince stateProvince = Optional.ofNullable(userService.findStateProvinceByCode(includeIdParam))
                    .orElseThrow(() -> new NotExistException(StateProvince.class));
            includeId = stateProvince.getId();
        }
        List<StateProvince> countries = userService.suggestStateProvince(query, countryId, limit, includeId);
        List<StateProvinceDTO> countriesDTOs = new LinkedList<>();
        countries
                .forEach(country -> countriesDTOs.add(conversionService.convert(country, StateProvinceDTO.class)));
        result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
        result.setResult(countriesDTOs);
        return result;
    }

    @ApiOperation("List DataNode users")
    @GetMapping(value = "/api/v1/user-management/datanodes/{dataNodeId}/users")
    public List<CommonUserDTO> listDataNodeUsers(@PathVariable("dataNodeId") Long dataNodeId) {

        final DN dataNode = baseDataNodeService.getById(dataNodeId);
        List<DataNodeUser> users = baseDataNodeService.listOwners(dataNode);
        return users.stream()
                .map(user -> conversionService.convert(user.getUser(), CommonUserDTO.class))
                .collect(Collectors.toList());
    }

    @ApiOperation("Link U to DataNode")
    @PostMapping(value = "/api/v1/user-management/datanodes/{datanodeSid}/users")
    public JsonResult linkUserToDataNode(@PathVariable("datanodeSid") Long datanodeId,
                                         @RequestBody CommonLinkUserToDataNodeDTO linkUserToDataNode
    ) throws NotExistException, AlreadyExistException {

        final DN dataNode = Optional.ofNullable(baseDataNodeService.getById(datanodeId)).orElseThrow(() ->
                new NotExistException(String.format(DATA_NODE_NOT_FOUND_EXCEPTION, datanodeId),
                        DataNode.class));
        final U user = userService.getByUsernameInAnyTenant(linkUserToDataNode.getUserName());
        if (Objects.isNull(user)) {
            throw new NotExistException(String.format("User %s not found", linkUserToDataNode.getUserName()), IUser.class);
        }
        baseDataNodeService.linkUserToDataNode(dataNode, user);
        return new JsonResult<>(NO_ERROR);
    }

    @ApiOperation("Unlink User to DataNode")
    @DeleteMapping(value = "/api/v1/user-management/datanodes/{datanodeId}/users")
    public JsonResult unlinkUserToDataNode(@PathVariable("datanodeId") Long datanodeId,
                                           @RequestBody CommonLinkUserToDataNodeDTO linkUserToDataNode
    ) throws NotExistException {

        final DN datanode = Optional.ofNullable(baseDataNodeService.getById(datanodeId)).orElseThrow(() ->
                new NotExistException(String.format(DATA_NODE_NOT_FOUND_EXCEPTION, datanodeId), DataNode.class));
        final U user = userService.getByUsernameInAnyTenant(linkUserToDataNode.getUserName(), true);
        if (user != null) {
            baseDataNodeService.unlinkUserToDataNode(datanode, user);
        }
        return new JsonResult(NO_ERROR);
    }

    @ApiOperation("Relink all Users to DataNode")
    @PutMapping(value = "/api/v1/user-management/datanodes/{datanodeId}/users")
    public JsonResult<List<CommonUserDTO>> relinkAllUsersToDataNode(@PathVariable("datanodeId") final Long dataNodeId,
                                                                    @RequestBody final List<CommonLinkUserToDataNodeDTO> linkUserToDataNodes
    ) throws NotExistException {

        final DN dataNode = baseDataNodeService.getById(dataNodeId);

        final Set<DataNodeUser> users = linkUserToDataNodes.stream()
                .map(link -> new DataNodeUser(userService.getByUsernameInAnyTenant(link.getUserName()), dataNode))
                .collect(Collectors.toSet());

        baseDataNodeService.relinkAllUsersToDataNode(dataNode, users);

        final List<CommonUserDTO> userDTOs = users.stream()
                .map(user -> conversionService.convert(user.getUser(), CommonUserDTO.class))
                .collect(Collectors.toList());

        return new JsonResult<>(NO_ERROR, userDTOs);
    }

    @ApiOperation("Create new user")
    @PostMapping(value = "/api/v1/admin/users")
    public CommonUserDTO create(@RequestBody @Valid CommonUserRegistrationDTO dto) throws PasswordValidationException {

        U user = convertRegistrationDTO(dto);

        user = userService.createWithEmailVerification(user, dto.getRegistrantToken(), dto.getCallbackUrl());
        return conversionService.convert(user, CommonUserDTO.class);
    }

    @ApiOperation("Remove user")
    @DeleteMapping(value = "/api/v1/admin/users/{uuid}")
    public Map<String, Boolean> delete(@PathVariable("uuid") String uuid)
            throws ValidationException, IOException, SolrServerException {

        userService.remove(UserIdUtils.uuidToId(uuid));
        return Collections.singletonMap("result", true);
    }

    @ApiOperation("Toggle user email confirmation")
    @PostMapping(value = "/api/v1/admin/users/{uuid}/confirm-email/{confirmed}")
    public CommonUserDTO confirmEmail(@PathVariable("uuid") String userUuid,
                                      @PathVariable("confirmed") Boolean confirm)
            throws IOException, NoSuchFieldException, SolrServerException, IllegalAccessException {

        U user = userService.getByIdInAnyTenantAndInitializeCollections(UserIdUtils.uuidToId(userUuid));
        user.setEmailConfirmed(confirm);
        if (!confirm) {
            user.setRegistrationCode(UUID.randomUUID().toString());
        }
        userService.updateInAnyTenant(user);
        return conversionService.convert(user, CommonUserDTO.class);
    }

    @ApiOperation("Set user's preferences")
    @PutMapping(value = "/api/v1/user-management/users/settings")
    public void updateUser(
            Principal principal,
            @RequestBody UserSettingsDTO dto
    ) throws PermissionDeniedException {

        U user = getUser(principal);
        if (dto.getActiveTenantId() != null) {
            userService.setActiveTenant(user, dto.getActiveTenantId());
        }
    }

    protected List<String> getAuthToken(HttpServletRequest request) {

        List<String> tokens = new ArrayList<>();

        // Get token from header
        String headerToken = request.getHeader(tokenHeader);
        if (headerToken != null) {
            tokens.add(headerToken);
        }

        // Get token from cookie
        if (request.getCookies() != null) {
            Arrays.stream(request.getCookies())
                    .filter(cookie -> cookie.getName().equalsIgnoreCase(tokenHeader) && cookie.getValue() != null)
                    .findFirst()
                    .ifPresent(cookie -> tokens.add(cookie.getValue()));
        }

        return tokens;
    }

    private U getCurrentUser(Authentication principal) {
        return userService.getById(UserUtils.getCurrentUser(principal).getId());
    }

}
