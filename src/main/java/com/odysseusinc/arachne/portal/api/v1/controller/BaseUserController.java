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
 * Created: September 19, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.controller;

import static com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult.ErrorCode.NO_ERROR;
import static com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult.ErrorCode.VALIDATION_ERROR;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.MetadataException;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonArachneUserStatusDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonLinkUserToDataNodeDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonUserDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonUserRegistrationDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult;
import com.odysseusinc.arachne.portal.api.v1.dto.ApproveDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.ChangePasswordDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.CountryDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.ExpertListSearchResultDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.InvitationActionDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.InvitationActionWithTokenDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.InvitationDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.InvitationType;
import com.odysseusinc.arachne.portal.api.v1.dto.RemindPasswordDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.SearchExpertListDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.StateProvinceDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UserLinkDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UserProfileDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UserProfileGeneralDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UserPublicationDTO;
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
import com.odysseusinc.arachne.portal.model.DataNodeRole;
import com.odysseusinc.arachne.portal.model.DataNodeUser;
import com.odysseusinc.arachne.portal.model.DataSource;
import com.odysseusinc.arachne.portal.model.Paper;
import com.odysseusinc.arachne.portal.model.Skill;
import com.odysseusinc.arachne.portal.model.StateProvince;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.StudyDataSourceLink;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.model.UserLink;
import com.odysseusinc.arachne.portal.model.UserOrigin;
import com.odysseusinc.arachne.portal.model.UserPublication;
import com.odysseusinc.arachne.portal.model.UserStudy;
import com.odysseusinc.arachne.portal.model.search.PaperSearch;
import com.odysseusinc.arachne.portal.model.search.StudySearch;
import com.odysseusinc.arachne.portal.security.TokenUtils;
import com.odysseusinc.arachne.portal.service.AnalysisUnlockRequestService;
import com.odysseusinc.arachne.portal.service.BaseDataNodeService;
import com.odysseusinc.arachne.portal.service.BasePaperService;
import com.odysseusinc.arachne.portal.service.BaseStudyService;
import com.odysseusinc.arachne.portal.service.BaseUserService;
import com.odysseusinc.arachne.portal.service.analysis.BaseAnalysisService;
import com.odysseusinc.arachne.portal.service.impl.solr.SearchResult;
import com.odysseusinc.arachne.portal.service.submission.BaseSubmissionService;
import io.swagger.annotations.ApiOperation;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.apache.http.client.utils.URIBuilder;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

public abstract class BaseUserController<
        U extends User,
        S extends Study,
        DS extends DataSource,
        SS extends StudySearch,
        SU extends AbstractUserStudyListItem,
        DN extends DataNode,
        P extends Paper,
        PS extends PaperSearch,
        SK extends Skill,
        A extends Analysis,
        SB extends Submission> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseUserController.class);
    private static final String AVATAR_CONTENT_TYPE = "image/*";
    private static final String DATA_NODE_NOT_FOUND_EXCEPTION = "dataNode %s not found";

    protected final TokenUtils tokenUtils;
    protected final BaseUserService<U, SK> userService;
    protected final BaseStudyService<S, DS, SS, SU> studyService;
    protected final GenericConversionService conversionService;
    protected final BaseDataNodeService<DN> baseDataNodeService;
    protected final BaseAnalysisService<A> analysisService;
    protected final AnalysisUnlockRequestService analysisUnlockRequestService;
    protected final BasePaperService<P, PS> paperService;
    protected final BaseSubmissionService<SB, A> submissionService;

    public BaseUserController(TokenUtils tokenUtils,
                              BaseUserService<U, SK> userService,
                              BaseStudyService<S, DS, SS, SU> studyService,
                              GenericConversionService conversionService,
                              BaseDataNodeService<DN> baseDataNodeService,
                              BaseAnalysisService<A> analysisService,
                              AnalysisUnlockRequestService analysisUnlockRequestService,
                              BasePaperService<P, PS> paperService,
                              BaseSubmissionService<SB, A> submissionService) {

        this.tokenUtils = tokenUtils;
        this.userService = userService;
        this.studyService = studyService;
        this.conversionService = conversionService;
        this.baseDataNodeService = baseDataNodeService;
        this.analysisService = analysisService;
        this.analysisUnlockRequestService = analysisUnlockRequestService;
        this.paperService = paperService;
        this.submissionService = submissionService;
    }

    @ApiOperation("Register new user via form.")
    @RequestMapping(value = "/api/v1/auth/registration", method = RequestMethod.POST)
    public JsonResult<CommonUserDTO> register(@RequestBody @Valid CommonUserRegistrationDTO dto, BindingResult binding)
            throws NotExistException, NotUniqueException, PermissionDeniedException, PasswordValidationException {

        JsonResult<CommonUserDTO> result;
        if (binding.hasErrors()) {
            result = new JsonResult<>(VALIDATION_ERROR);
            for (FieldError fieldError : binding.getFieldErrors()) {
                result.getValidatorErrors().put(fieldError.getField(), fieldError.getDefaultMessage());
            }
        } else {
            U user = convertRegistrationDTO(dto);
            user.setUsername(user.getEmail());
            user.setOrigin(UserOrigin.NATIVE);
            user.setEmailConfirmed(false);
            user = userService.register(user, dto.getRegistrantToken(), dto.getCallbackUrl());
            result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
            result.setResult(conversionService.convert(user, CommonUserDTO.class));
        }
        return result;
    }

    protected abstract U convertRegistrationDTO(CommonUserRegistrationDTO dto);

    @ApiOperation("Resend registration email for not enabled user")
    @RequestMapping(value = "/api/v1/auth/resend-activation-email", method = RequestMethod.POST)
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
    @RequestMapping(value = "/api/v1/auth/status/{userUuid}", method = RequestMethod.GET)
    public JsonResult<CommonArachneUserStatusDTO> findUserStatus(@PathVariable("userUuid") String uuid)
            throws UserNotFoundException {

        JsonResult<CommonArachneUserStatusDTO> result;
        User user = userService.getByUuid(uuid);
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
    @RequestMapping("/api/v1/user-management/activation/{activationCode}")
    public void activate(
            Principal principal,
            HttpServletRequest request,
            @PathVariable("activationCode") String activateCode,
            @RequestParam(value = "callbackUrl", required = false) String callbackUrl,
            HttpServletResponse response)
            throws IOException, UserNotFoundException, NotExistException,
            NoSuchFieldException, IllegalAccessException, SolrServerException, URISyntaxException {

        tokenUtils
                .getAuthToken(request)
                .forEach(token -> tokenUtils.addInvalidateToken(token));

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
    @RequestMapping(value = "/api/v1/user-management/users/avatar", method = RequestMethod.POST)
    public JsonResult<Boolean> saveUserAvatar(
            Principal principal,
            @RequestParam(name = "file") MultipartFile[] file)
            throws IOException, WrongFileFormatException, ValidationException, ImageProcessingException, MetadataException {

        JsonResult<Boolean> result;
        U user = userService.getByEmail(principal.getName());
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
    @RequestMapping(value = "/api/v1/user-management/users/avatar", method = RequestMethod.GET)
    public void getUserAvatar(
            Principal principal,
            HttpServletResponse response) throws IOException {

        final Optional<String> userName = Optional.ofNullable(principal != null ? principal.getName() : null);
        U user = userName.map(userService::getByEmail).orElse(null);
        putAvatarToResponse(response, user);
    }

    @ApiOperation("Download user avatar")
    @RequestMapping(value = "/api/v1/user-management/users/{id}/avatar", method = RequestMethod.GET)
    public void getUserAvatar(
            @PathVariable("id") Long id,
            HttpServletResponse response) throws IOException {

        U user = userService.getById(id);
        putAvatarToResponse(response, user);
    }

    @ApiOperation("Save user profile")
    @RequestMapping(value = "/api/v1/user-management/users/profile", method = RequestMethod.POST)
    public JsonResult<UserProfileDTO> saveProfile(
            Principal principal,
            @RequestBody @Valid UserProfileGeneralDTO dto,
            BindingResult binding)
            throws
            IllegalAccessException,
            SolrServerException,
            IOException,
            NotExistException,
            NoSuchFieldException {

        JsonResult<UserProfileDTO> result;
        User owner = userService.getByEmail(principal.getName());
        if (binding.hasErrors()) {
            result = new JsonResult<>(VALIDATION_ERROR);
            for (FieldError fieldError : binding.getFieldErrors()) {
                result.getValidatorErrors().put(fieldError.getField(), fieldError.getDefaultMessage());
            }
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
    @RequestMapping(value = "/api/v1/user-management/users/changepassword", method = RequestMethod.POST)
    public JsonResult changePassword(@RequestBody @Valid ChangePasswordDTO changePasswordDTO,
                                     Principal principal
    ) throws ValidationException, PasswordValidationException {

        JsonResult result;
        U loggedUser = userService.getByEmail(principal.getName());
        try {
            userService.updatePassword(loggedUser, changePasswordDTO.getOldPassword(), changePasswordDTO.getNewPassword());
            result = new JsonResult<>(NO_ERROR);
        } catch (ValidationException ex) {
            result = new JsonResult<>(VALIDATION_ERROR);
            result.setErrorMessage(ex.getMessage());
        }
        return result;
    }

    @ApiOperation("View user profile.")
    @RequestMapping(value = "/api/v1/user-management/users/{userId}/profile", method = RequestMethod.GET)
    public JsonResult<UserProfileDTO> viewProfile(
            Principal principal,
            @PathVariable("userId") Long userId) {

        User logginedUser = userService.getByEmail(principal.getName());
        JsonResult<UserProfileDTO> result;
        User user = userService.getById(userId);
        UserProfileDTO userProfileDTO = conversionService.convert(user, UserProfileDTO.class);
        userProfileDTO.setIsEditable(logginedUser.getId().equals(userId));
        result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
        result.setResult(userProfileDTO);
        return result;
    }

    @ApiOperation("Suggests user according to query.")
    @RequestMapping(value = "/api/v1/user-management/users/search-user", method = RequestMethod.GET)
    public JsonResult<List<CommonUserDTO>> suggestUsers(
            @RequestParam(value = "studyId", required = false) Long studyId,
            @RequestParam(value = "paperId", required = false) Long paperId,
            @RequestParam("query") String query,
            @RequestParam(value = "size", defaultValue = "10") Integer size
    ) {

        JsonResult<List<CommonUserDTO>> result;
        List<U> users;
        if (studyId != null) {
            users = userService.suggestUserToStudy(query, studyId, size);
        } else if (paperId != null) {
            users = userService.suggestUserToPaper(query, paperId, size);
        } else {
            throw new javax.validation.ValidationException();
        }
        final List<CommonUserDTO> userDTOs = users.stream()
                .map(user -> conversionService.convert(user, CommonUserDTO.class))
                .collect(Collectors.toList());
        result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
        result.setResult(userDTOs);
        return result;
    }

    @ApiOperation("Suggests user according to query.")
    @RequestMapping(value = "/api/v1/user-management/users/suggests-user", method = RequestMethod.GET)
    public JsonResult<List<CommonUserDTO>> suggestUsersForDatanode(
            @RequestParam("query") String query,
            @RequestParam("email") List<String> emails,
            @RequestParam("limit") Integer limit
    ) {

        JsonResult<List<CommonUserDTO>> result;
        List<U> users = userService.suggestUser(query, emails, limit);
        List<CommonUserDTO> userDTOs = new LinkedList<>();
        for (U user : users) {
            userDTOs.add(conversionService.convert(user, CommonUserDTO.class));
        }
        result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
        result.setResult(userDTOs);
        return result;
    }

    @ApiOperation("Get user by id")
    @RequestMapping(value = "/api/v1/user-management/users/{id}", method = RequestMethod.GET)
    public JsonResult<CommonUserDTO> suggestUsersForDatanode(
            @PathVariable("id") Long id
    ) {

        JsonResult<CommonUserDTO> result;
        U user = userService.getById(id);
        CommonUserDTO userDTO = conversionService.convert(user, CommonUserDTO.class);
        result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
        result.setResult(userDTO);
        return result;
    }

    @ApiOperation("Add skill to user profile.")
    @RequestMapping(value = "/api/v1/user-management/users/skills/{skillId}", method = RequestMethod.POST)
    public JsonResult<UserProfileDTO> addSkill(
            Principal principal,
            @PathVariable("skillId") Long skillId
    ) throws NotExistException, IllegalAccessException, SolrServerException, IOException, NoSuchFieldException {

        JsonResult<UserProfileDTO> result;
        U user = userService.getByEmail(principal.getName());
        user = userService.addSkillToUser(user.getId(), skillId);
        UserProfileDTO userProfileDTO = conversionService.convert(user, UserProfileDTO.class);
        result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
        result.setResult(userProfileDTO);
        return result;
    }

    @ApiOperation("Remove skill from user profile.")
    @RequestMapping(value = "/api/v1/user-management/users/skills/{skillId}", method = RequestMethod.DELETE)
    public JsonResult<UserProfileDTO> removeSkill(
            Principal principal,
            @PathVariable("skillId") Long skillId
    ) throws NotExistException, IllegalAccessException, SolrServerException, IOException, NoSuchFieldException {

        JsonResult<UserProfileDTO> result;
        U user = userService.getByEmail(principal.getName());
        user = userService.removeSkillFromUser(user.getId(), skillId);
        UserProfileDTO userProfileDTO = conversionService.convert(user, UserProfileDTO.class);
        result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
        result.setResult(userProfileDTO);
        return result;
    }

    @ApiOperation("Add link to user profile.")
    @RequestMapping(value = "/api/v1/user-management/users/links", method = RequestMethod.POST)
    public JsonResult<UserProfileDTO> addLink(
            Principal principal,
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
            U user = userService.getByEmail(principal.getName());
            user = userService.addLinkToUser(user.getId(), conversionService.convert(userLinkDTO, UserLink.class));

            UserProfileDTO userProfileDTO = conversionService.convert(user, UserProfileDTO.class);
            result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
            result.setResult(userProfileDTO);
        }
        return result;
    }

    @ApiOperation("Remove link from user profile.")
    @RequestMapping(value = "/api/v1/user-management/users/links/{linkId}", method = RequestMethod.DELETE)
    public JsonResult<UserProfileDTO> removeLink(
            Principal principal,
            @PathVariable("linkId") Long linkId
    ) throws NotExistException {

        JsonResult<UserProfileDTO> result;
        U user = userService.getByEmail(principal.getName());
        user = userService.removeLinkFromUser(user.getId(), linkId);
        UserProfileDTO userProfileDTO = conversionService.convert(user, UserProfileDTO.class);
        result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
        result.setResult(userProfileDTO);
        return result;
    }

    @ApiOperation("Add user's publication.")
    @RequestMapping(value = "/api/v1/user-management/users/publications", method = RequestMethod.POST)
    public JsonResult<UserProfileDTO> addPublication(
            Principal principal,
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

            U user = userService.getByEmail(principal.getName());
            user = userService.addPublicationToUser(user.getId(), conversionService.convert(userPublicationDTO,
                    UserPublication.class));
            UserProfileDTO userProfileDTO = conversionService.convert(user, UserProfileDTO.class);
            result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
            result.setResult(userProfileDTO);
        }
        return result;
    }

    @ApiOperation("Remove user's publication.")
    @RequestMapping(value = "/api/v1/user-management/users/publications/{publicationId}", method = RequestMethod.DELETE)
    public JsonResult<UserProfileDTO> removePublication(
            Principal principal,
            @PathVariable("publicationId") Long publicationId
    ) throws NotExistException {

        JsonResult<UserProfileDTO> result;
        U user = userService.getByEmail(principal.getName());
        user = userService.removePublicationFromUser(user.getId(), publicationId);
        UserProfileDTO userProfileDTO = conversionService.convert(user, UserProfileDTO.class);
        result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
        result.setResult(userProfileDTO);
        return result;
    }

    @ApiOperation("Get user's invitations.")
    @RequestMapping(value = "/api/v1/user-management/users/invitations", method = RequestMethod.GET)
    public JsonResult<List<InvitationDTO>> invitations(
            Principal principal
    ) throws NotExistException {

        JsonResult<List<InvitationDTO>> result;
        U user = userService.getByEmail(principal.getName());

        Stream<InvitationDTO> invitationStream = getInvitations(user)
                .stream()
                .flatMap(Collection::stream)
                .map(o -> conversionService.convert(o, InvitationDTO.class))
                .sorted(Comparator.comparing(InvitationDTO::getDate).reversed());
        return new JsonResult<>(NO_ERROR, invitationStream.collect(Collectors.toList()));
    }

    private List<Collection> getInvitations(U user) {

        return Arrays.asList(
                userService.getInvitations(user),
                analysisService.getWaitingForApprovalSubmissions(user),
                userService.getDataSourceInvitations(user),
                userService.getUnlockAnalysisRequests(user)
        );
    }

    @ApiOperation("Accept invitations via mail.")
    @RequestMapping(value = "/api/v1/user-management/users/invitations/mail", method = RequestMethod.GET)
    public JsonResult<UserProfileDTO> invitationAcceptViaMail(
            @RequestParam("id") Long id,
            @RequestParam("accepted") Boolean accepted,
            @RequestParam("type") String type,
            @RequestParam("token") String token,
            @RequestParam(value = "userId", required = false) Long userId,
            HttpServletResponse response
    ) throws NotExistException, AlreadyExistException, IOException {

        InvitationActionWithTokenDTO dto = new InvitationActionWithTokenDTO(id, type, accepted, token);

        String redirectLink = "";
        U user;

        try {
            user = getUserFromInvitationDto(dto, userId);
            redirectLink = getRedirectLinkFromInvitationDto(dto, id, token);
        } catch (NotExistException ex) {
            JsonResult result = new JsonResult<>(VALIDATION_ERROR);
            result.setErrorMessage(ex.getMessage());
            response.sendRedirect(redirectLink);
            return result;
        }
        response.sendRedirect(redirectLink);
        return invitationAccept(dto, user);
    }

    private String getRedirectLinkFromInvitationDto(InvitationActionWithTokenDTO dto, Long id, String token) {

        String redirectLink = "/study-manager/studies/";

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

    private U getUserFromInvitationDto(InvitationActionWithTokenDTO dto, Long userId) {

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
                user = userService.getById(userId);
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
    @RequestMapping(value = "/api/v1/user-management/users/invitations", method = RequestMethod.POST)
    public JsonResult<UserProfileDTO> invitationAcceptViaInvitation(
            Principal principal,
            @Valid @RequestBody InvitationActionDTO invitationActionDTO
    ) throws NotExistException, AlreadyExistException, IOException {

        U user = userService.getByEmail(principal.getName());
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
                conversionService.convert(userService.getById(user.getId()), UserProfileDTO.class));
    }

    private void checkIfUserExists(U user) {

        if (user == null || user.getId() == null || userService.findOne(user.getId()) == null) {

            throw new UserNotFoundException("userId", "user not found");
        }
    }

    @ApiOperation("Get expert list")
    @RequestMapping(value = "/api/v1/user-management/users", method = RequestMethod.GET)
    public JsonResult<ExpertListSearchResultDTO> list(
            @ModelAttribute SearchExpertListDTO searchDTO
    ) throws IOException, SolrServerException {

        JsonResult result = new JsonResult<ExpertListSearchResultDTO>(NO_ERROR);

        SolrQuery solrQuery = conversionService.convert(searchDTO, SolrQuery.class);
        SearchResult searchResult = userService.search(solrQuery);

        result.setResult(
                this.conversionService.convert(
                        searchResult,
                        ExpertListSearchResultDTO.class
                )
        );
        return result;
    }

    @ApiOperation("Suggests country.")
    @RequestMapping(value = "/api/v1/user-management/countries/search", method = RequestMethod.GET)
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
    @RequestMapping(value = "/api/v1/user-management/state-province/search", method = RequestMethod.GET)
    public JsonResult<List<StateProvinceDTO>> suggestStateProvince(
            @RequestParam("countryId") Long countryId,
            @RequestParam("query") String query,
            @RequestParam("limit") Integer limit,
            @RequestParam(value = "includeId", required = false) Long includeId
    ) {

        JsonResult<List<StateProvinceDTO>> result;
        List<StateProvince> countries = userService.suggestStateProvince(query, countryId, limit, includeId);
        List<StateProvinceDTO> countriesDTOs = new LinkedList<>();
        countries
                .forEach(country -> countriesDTOs.add(conversionService.convert(country, StateProvinceDTO.class)));
        result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
        result.setResult(countriesDTOs);
        return result;
    }

    @ApiOperation("Link U to DataNode")
    @RequestMapping(value = "/api/v1/user-management/datanodes/{datanodeSid}/users", method = RequestMethod.POST)
    public JsonResult linkUserToDataNode(@PathVariable("datanodeSid") String datanodeSid,
                                         @RequestBody CommonLinkUserToDataNodeDTO linkUserToDataNode
    ) throws NotExistException, AlreadyExistException {

        final DN datanode = baseDataNodeService.getBySid(datanodeSid);
        Optional.ofNullable(datanode).orElseThrow(() ->
                new NotExistException(String.format(DATA_NODE_NOT_FOUND_EXCEPTION, datanodeSid),
                        DataNode.class));
        final U user = userService.getByUsername(linkUserToDataNode.getUserName());
        final Set<DataNodeRole> roles = linkUserToDataNode.getRoles()
                .stream()
                .map(role ->
                        DataNodeRole.valueOf(
                                role.getName().replace("ROLE_", "")
                        )
                )
                .collect(Collectors.toSet());
        baseDataNodeService.linkUserToDataNode(datanode, user, roles);
        return new JsonResult(NO_ERROR);
    }

    @ApiOperation("Unlink User to DataNode")
    @RequestMapping(value = "/api/v1/user-management/datanodes/{datanodeSid}/users", method = RequestMethod.DELETE)
    public JsonResult unlinkUserToDataNode(@PathVariable("datanodeSid") String datanodeSid,
                                           @RequestBody CommonLinkUserToDataNodeDTO linkUserToDataNode
    ) throws NotExistException {

        final DN datanode = baseDataNodeService.getBySid(datanodeSid);
        Optional.ofNullable(datanode).orElseThrow(() ->
                new NotExistException(String.format(DATA_NODE_NOT_FOUND_EXCEPTION, datanodeSid), DataNode.class));
        final U user = userService.getByUsername(linkUserToDataNode.getUserName());
        baseDataNodeService.unlinkUserToDataNode(datanode, user);
        return new JsonResult(NO_ERROR);
    }

    @ApiOperation("Relink all Users to DataNode")
    @RequestMapping(value = "/api/v1/user-management/datanodes/{datanodeSid}/users", method = RequestMethod.PUT)
    public JsonResult relinkAllUsersToDataNode(@PathVariable("datanodeSid") String datanodeSid,
                                               @RequestBody List<CommonLinkUserToDataNodeDTO> linkUserToDataNodes
    ) throws NotExistException {

        final DN datanode = baseDataNodeService.getBySid(datanodeSid);
        final Set<DataNodeUser> users = linkUserToDataNodes.stream()
                .map(link -> {
                            final U user = userService.getByUsername(link.getUserName());
                            final Set<DataNodeRole> roles = link.getRoles()
                                    .stream()
                                    .map(role ->
                                            DataNodeRole.valueOf(
                                                    role.getName().replace("ROLE_", "")
                                            )
                                    )
                                    .collect(Collectors.toSet());
                            final DataNodeUser dataNodeUser = new DataNodeUser();
                            dataNodeUser.setDataNode(datanode);
                            dataNodeUser.setUser(user);
                            dataNodeUser.setDataNodeRole(roles);
                            return dataNodeUser;
                        }
                )
                .collect(Collectors.toSet());
        baseDataNodeService.relinkAllUsersToDataNode(datanode, users);
        return new JsonResult(NO_ERROR);
    }

    private void putAvatarToResponse(HttpServletResponse response, U user) throws IOException {

        try (final InputStream is = userService.getUserAvatar(user)) {
            response.setContentType(AVATAR_CONTENT_TYPE);
            response.setHeader("Content-type", AVATAR_CONTENT_TYPE);
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
            response.setHeader("Content-Disposition", "attachment; filename=avatar");
            org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();
        }
    }

    @ApiOperation("Create new user")
    @RequestMapping(value = "/api/v1/admin/users", method = POST)
    public CommonUserDTO create(@RequestBody @Valid CommonUserRegistrationDTO dto) throws PasswordValidationException {

        CommonUserDTO result;
        U user = convertRegistrationDTO(dto);
        user.setEmailConfirmed(false);
        user = userService.create(user);
        result = conversionService.convert(user, CommonUserDTO.class);
        return result;
    }

    @ApiOperation("Remove user")
    @RequestMapping(value = "/api/v1/admin/users/{id}", method = DELETE)
    public Map<String, Boolean> delete(@PathVariable("id") Long userId) throws ValidationException {

        userService.remove(userId);
        return Collections.singletonMap("result", true);
    }

    @ApiOperation("Toggle user email confirmation")
    @RequestMapping(value = "/api/v1/admin/users/{id}/confirm-email/{confirmed}", method = POST)
    public CommonUserDTO confirmEmail(@PathVariable("id") Long userId,
                                      @PathVariable("confirmed") Boolean confirm)
            throws IOException, NoSuchFieldException, SolrServerException, IllegalAccessException {

        U user = userService.getById(userId);
        user.setEmailConfirmed(confirm);
        userService.update(user);
        return conversionService.convert(user, CommonUserDTO.class);
    }
}
