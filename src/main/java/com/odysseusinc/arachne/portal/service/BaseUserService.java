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
 * Created: September 19, 2017
 *
 */

package com.odysseusinc.arachne.portal.service;

import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.MetadataException;
import com.odysseusinc.arachne.portal.api.v1.dto.BatchOperationType;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.NotUniqueException;
import com.odysseusinc.arachne.portal.exception.PasswordValidationException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.exception.UserNotFoundException;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.exception.WrongFileFormatException;
import com.odysseusinc.arachne.portal.model.Country;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.Invitationable;
import com.odysseusinc.arachne.portal.model.Skill;
import com.odysseusinc.arachne.portal.model.StateProvince;
import com.odysseusinc.arachne.portal.model.UserLink;
import com.odysseusinc.arachne.portal.model.UserPublication;
import com.odysseusinc.arachne.portal.model.UserStudy;
import com.odysseusinc.arachne.portal.model.search.UserSearch;
import com.odysseusinc.arachne.portal.model.security.Tenant;
import com.odysseusinc.arachne.portal.service.impl.solr.FieldList;
import com.odysseusinc.arachne.portal.service.impl.solr.SearchResult;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;

public interface BaseUserService<U extends IUser, S extends Skill> {

    U getByUsername(final String username);

    U getByUsername(final String userOrigin, final String username);

    U getByEmail(String email);

    U getByEmailInAnyTenant(final String email);

    U getEnabledByIdInAnyTenant(final Long id);

    U getByIdInAnyTenant(final Long id);

    U getByUnverifiedEmail(final String email);

    U getByUnverifiedEmailInAnyTenant(final String email);

    U getByUnverifiedEmailIgnoreCaseInAnyTenant(final String email);

    U getByUsernameInAnyTenant(final String username);

    U getByUsernameInAnyTenant(final String username, boolean includeDeleted);

    @PreAuthorize("hasRole('ROLE_ADMIN') || #dataNode == authentication.principal || hasPermission(#id, 'RawUser', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).ACCESS_USER)")
    List<U> getByIdsInAnyTenant(List<Long> ids);

    void remove(Long id)
            throws ValidationException, UserNotFoundException, NotExistException, IOException, SolrServerException;

    U createWithEmailVerification(final @NotNull U user, String registrantToken, String callbackUrl) throws PasswordValidationException;

    void confirmUserEmail(U user)
            throws IOException, NotExistException,
            SolrServerException, NoSuchFieldException, IllegalAccessException;

    void confirmUserEmail(String activateCode)
            throws UserNotFoundException, IOException, NotExistException,
            SolrServerException, NoSuchFieldException, IllegalAccessException;

    void sendRegistrationEmail(U user, String registrantToken, String callbackUrl, boolean isAsync);

    void resendActivationEmail(String email) throws UserNotFoundException;

    U createWithValidation(@NotNull U user) throws NotUniqueException, NotExistException, PasswordValidationException;

    U create(@NotNull U user) throws PasswordValidationException;

    void sendRemindPasswordEmail(U user, String token, String registrantToken, String callbackUrl);

    void resendActivationEmail(U user);

    U getByIdInAnyTenantAndInitializeCollections(Long id);

    U getByUuidInAnyTenantAndInitializeCollections(String uuid);

    U getById(Long id);

    U update(U user)
            throws
            IllegalAccessException,
            SolrServerException,
            IOException,
            NotExistException,
            NoSuchFieldException;

    U updateInAnyTenant(U user) throws NotExistException;

    void saveUsers(List<U> users, Set<Tenant> tenants, boolean emailConfirmationRequired);

    @PreAuthorize("hasPermission(#uuid, 'User', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).ACCESS_USER)")
    U getByUuid(String uuid);

    @PreAuthorize("hasPermission(#uuid, 'User', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).ACCESS_USER)")
    U getByUuidAndInitializeCollections(String uuid);

    List<U> suggestUserFromAnyTenant(String query, List<String> emailsList, Integer limit);

    List<U> suggestUserToStudy(String query, Long studyId, int limit);

    List<U> suggestUserToPaper(String query, Long paperId, int limit);

    List<U> suggestNotAdmin(String query, Integer limit);

    List<U> getAllEnabledFromAllTenants();

    Page<U> getPage(Pageable pageable, UserSearch userSearch);

    List<U> getList(UserSearch userSearch);

    List<U> findUsersInAnyTenantByEmailIn(List<String> emails);

    void resetPassword(U user)
            throws UserNotFoundException, IllegalAccessException, NotExistException,
            NoSuchFieldException, SolrServerException, IOException;

    void updatePassword(U user, String oldPassword, String newPassword) throws ValidationException, PasswordValidationException;

    U addSkillToUser(Long userId, Long skillId)
            throws NotExistException,
            IllegalAccessException,
            SolrServerException,
            IOException,
            NoSuchFieldException;

    U removeSkillFromUser(Long userId, Long skillId)
            throws NotExistException,
            IllegalAccessException,
            SolrServerException,
            IOException,
            NoSuchFieldException;

    U addLinkToUser(Long userId, UserLink link)
            throws NotExistException, NotUniqueException, PermissionDeniedException;

    U removeLinkFromUser(Long userId, Long linkId) throws NotExistException;

    U addPublicationToUser(Long userId, UserPublication publication)
            throws NotExistException, NotUniqueException, PermissionDeniedException;

    U removePublicationFromUser(Long userId, Long publicationId) throws NotExistException;

    void saveAvatar(U user, MultipartFile file)
            throws IOException, WrongFileFormatException, ImageProcessingException, MetadataException, IllegalAccessException, SolrServerException, NoSuchFieldException;

    List<? extends Invitationable> getCollaboratorInvitations(U user);

    List<? extends Invitationable> getDataSourceInvitations(U user);

    List<? extends Invitationable> getInvitationsForStudy(U user, final Long studyId);

    UserStudy processInvitation(U user, Long id, Boolean accepted, String comment);

    UserStudy getByIdAndStatusPendingAndToken(Long userStudyId, String token) throws NotExistException;

    FieldList getSolrFields();

    void indexBySolr(U user)
            throws IllegalAccessException, IOException, SolrServerException, NotExistException, NoSuchFieldException;

    void indexAllBySolr()
            throws IOException,
            NotExistException,
            SolrServerException,
            NoSuchFieldException,
            IllegalAccessException;

    SearchResult<U> search(SolrQuery solrQuery) throws IOException, SolrServerException, NoSuchFieldException;

    List<Country> suggestCountry(String query, Integer limit, Long includeId);

    List<StateProvince> suggestStateProvince(String query, Long countryId, Integer limit, Long includeId);

    U getUser(Principal principal) throws PermissionDeniedException;

    U getCurrentUser() throws PermissionDeniedException;

    List<U> getAllAdmins(String sortBy, Boolean sortAsc);

    void addUserToAdmins(Long id);

    void removeUserFromAdmins(Long id);

    List<U> getUsersByUserNames(List<String> userNames);

    List<? extends Invitationable> getUnlockAnalysisRequests(U user);

    U findOne(Long participantId);

    List<U> findUsersByUuidsIn(List<String> dataOwnerIds);

    List<U> findUsersApprovedInDataSource(Long id);

    void putAvatarToResponse(HttpServletResponse response, U user) throws IOException;

    void setActiveTenant(U user, Long tenantId);

    void makeLinksWithStudiesDeleted(Long tenantId, Long userId);

    U getRawUser(Long userId);

    void makeLinksWithPapersDeleted(Long tenantId, Long userId);

    void revertBackUserToPapers(Long tenantId, Long userId);

    List<U> findByIdsInAnyTenant(Set<Long> userIds);

    void performBatchOperation(List<String> ids, BatchOperationType type);
}
