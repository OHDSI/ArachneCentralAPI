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
 * Created: January 13, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.controller;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonUserRegistrationDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UserProfileGeneralDTO;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.Paper;
import com.odysseusinc.arachne.portal.model.Skill;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.StudyViewItem;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.model.search.PaperSearch;
import com.odysseusinc.arachne.portal.model.search.StudySearch;
import com.odysseusinc.arachne.portal.security.passwordvalidator.ArachnePasswordValidator;
import com.odysseusinc.arachne.portal.service.AnalysisUnlockRequestService;
import com.odysseusinc.arachne.portal.service.DataNodeService;
import com.odysseusinc.arachne.portal.service.PaperService;
import com.odysseusinc.arachne.portal.service.StudyService;
import com.odysseusinc.arachne.portal.service.UserService;
import com.odysseusinc.arachne.portal.service.analysis.AnalysisService;
import com.odysseusinc.arachne.portal.service.submission.SubmissionService;
import org.ohdsi.authenticator.service.authentication.Authenticator;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SuppressWarnings("unused")
public class UserController extends BaseUserController<IUser, Study, IDataSource, StudySearch, StudyViewItem, DataNode, Paper, PaperSearch, Skill, Analysis, Submission> {


    public UserController(UserService userService,
                          StudyService studyService,
                          GenericConversionService conversionService,
                          DataNodeService dataNodeService,
                          AnalysisService analysisService,
                          AnalysisUnlockRequestService analysisUnlockRequestService,
                          PaperService paperService,
                          SubmissionService submissionService,
                          ArachnePasswordValidator passwordValidator,
                          Authenticator authenticator
    ) {

        super(  userService,
                studyService,
                conversionService,
                dataNodeService,
                analysisService,
                analysisUnlockRequestService,
                paperService,
                submissionService,
                passwordValidator,
                authenticator);
    }

    @Override
    protected User convertRegistrationDTO(CommonUserRegistrationDTO dto) {

        return conversionService.convert(dto, User.class);
    }

    @Override
    protected User convertUserProfileGeneralDTO(UserProfileGeneralDTO dto) {

        return conversionService.convert(dto, User.class);
    }

    @Override
    protected User createNewUser() {

        return new User();
    }
}
