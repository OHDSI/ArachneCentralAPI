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
 * Created: January 13, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.controller;

import com.odysseusinc.arachne.portal.security.TokenUtils;
import com.odysseusinc.arachne.portal.security.passwordvalidator.ArachnePasswordValidator;
import com.odysseusinc.arachne.portal.service.LoginAttemptService;
import com.odysseusinc.arachne.portal.service.PasswordResetService;
import com.odysseusinc.arachne.portal.service.ProfessionalTypeService;
import com.odysseusinc.arachne.portal.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationController extends BaseAuthenticationController {

    public AuthenticationController(AuthenticationManager authenticationManager,
                                    TokenUtils tokenUtils,
                                    UserService userService,
                                    UserDetailsService userDetailsService,
                                    PasswordResetService passwordResetService,
                                    ArachnePasswordValidator passwordValidator,
                                    ProfessionalTypeService professionalTypeService,
                                    LoginAttemptService loginAttemptService) {

        super(authenticationManager,
                tokenUtils,
                userService,
                userDetailsService,
                passwordResetService,
                passwordValidator,
                professionalTypeService,
                loginAttemptService);
    }
}
