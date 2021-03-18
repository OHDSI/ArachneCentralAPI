/*
 *
 * Copyright 2021 Odysseus Data Services, inc.
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
 * Authors: Yaroslav Molodkov, Alexandr Cumarav, Vitaliy Kulakov
 * Created: March 17, 2021
 *
 */


package com.odysseusinc.arachne.portal.service.impl;

import com.google.common.collect.Lists;
import com.odysseusinc.arachne.portal.api.v1.dto.converters.UserInfoToUserConverter;
import com.odysseusinc.arachne.portal.component.ldap.ImportResult;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.ProfessionalType;
import com.odysseusinc.arachne.portal.model.Skill;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.service.AuthenticationHelperService;
import com.odysseusinc.arachne.portal.service.BaseUserService;
import com.odysseusinc.arachne.portal.service.ProfessionalTypeService;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.authenticator.exception.AuthenticationException;
import org.ohdsi.authenticator.service.AuthService;
import org.ohdsi.authenticator.service.authentication.UserService;
import org.ohdsi.authenticator.service.directory.ad.AdAuthService;
import org.ohdsi.authenticator.service.directory.ldap.LdapAuthService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserImportService {

    private static Collection<String> supportedMethodTypes = Arrays.asList(LdapAuthService.AUTH_METHOD_NAME, AdAuthService.AUTH_METHOD_NAME);

    private BaseUserService<IUser, Skill> userService;

    private UserService authUserService;

    private ProfessionalTypeService professionalTypeService;

    private UserInfoToUserConverter converter;

    private AuthenticationHelperService authenticationHelperService;

    public UserImportService(BaseUserService<IUser, Skill> userService, UserService authUserService, ProfessionalTypeService professionalTypeService, UserInfoToUserConverter converter, AuthenticationHelperService authenticationHelperService) {

        this.userService = userService;
        this.authUserService = authUserService;
        this.professionalTypeService = professionalTypeService;
        this.converter = converter;
        this.authenticationHelperService = authenticationHelperService;
    }


    public List<ImportResult> doImport(ProfessionalType professionalType, List<User> users) throws NotExistException {

        return users.stream()
                .map(userForImport -> doImport(professionalType, userForImport))
                .collect(Collectors.toList());
    }

    public ImportResult doImport(ProfessionalType professionalType, User userForImport) {

        try {
            userForImport.setProfessionalType(professionalType);
            IUser existedUser = userService.getByUsernameInAnyTenant(userForImport.getUsername());

            if (existedUser != null) {
                userForImport.setId(existedUser.getId());
                userService.updateInAnyTenant(userForImport);
                return new ImportResult(userForImport.getUsername(), ImportResult.ImportResultState.UPDATED);
            } else {

                AuthService authService = authenticationHelperService.getCurrentAuthService();
                String authMethodType = authService.getMethodType();
                userForImport.setOrigin(authMethodType);
                userForImport.setPassword(generateRandomFakePasswordBecausePasswordCannotBeNullInBD());
                userForImport.setEmailConfirmed(true);

                //The createWithValidation method does not call the indexing of a new user, so we call it explicitly.
                IUser newUser = userService.createWithValidation(userForImport);
                userService.indexBySolr(newUser);
                return new ImportResult(userForImport.getUsername(), ImportResult.ImportResultState.CREATED);
            }
        } catch (Exception ex) {

            return new ImportResult(
                    userForImport.getUsername(),
                    ImportResult.ImportResultState.ERROR,
                    ex.getMessage()
            );
        }
    }

    private String generateRandomFakePasswordBecausePasswordCannotBeNullInBD() {

        return String.format("MockPass-%s", UUID.randomUUID().toString());
    }

    public ImportResult importOne(ProfessionalType professionalType, String username) {



        org.ohdsi.authenticator.model.User authUser = authUserService
                .findUser(authenticationHelperService.getCurrentMethodType(), username)
                .orElseThrow(() -> new IllegalStateException(String.format("Cannot synchronize user, there is no (%s) user", username)));

        User user = converter.convert(authUser);
        return doImport(professionalType, user);
    }

    public List<ImportResult> importAll(ProfessionalType defaultProfessionalType) {

        List<User> users = authUserService
                .findAllUsers(authenticationHelperService.getCurrentMethodType())
                .stream()
                .map(this.converter::convert)
                .collect(Collectors.toList());
        return doImport(defaultProfessionalType, users);
    }

    public void synchronizeUserWithExternalSourceIfPossible(String username) {

        AuthService authService = authenticationHelperService.getCurrentAuthService();
        String authMethodType = authService.getMethodType();
        if (!supportedMethodTypes.contains(StringUtils.upperCase(authMethodType))) {
            return;
        }

        ProfessionalType professionalType = getRandomProfessionalType()
                .orElseThrow(() -> new AuthenticationException("There is no any profession type."));
        ImportResult importResult = importOne(professionalType, username);
        if (importResult.getState() == ImportResult.ImportResultState.ERROR) {
            throw new UsernameNotFoundException("Cannot import user from an external source");
        }
    }

    private Optional<ProfessionalType> getRandomProfessionalType() {

        return Lists.newArrayList(professionalTypeService.list())
                .stream()
                .findAny();
    }

}
