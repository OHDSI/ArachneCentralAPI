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
 * Created: October 19, 2016
 *
 */

package com.odysseusinc.arachne.portal.service.impl;

import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.Skill;
import com.odysseusinc.arachne.portal.repository.AnalysisUnlockRequestRepository;
import com.odysseusinc.arachne.portal.repository.BaseRawUserRepository;
import com.odysseusinc.arachne.portal.repository.BaseUserRepository;
import com.odysseusinc.arachne.portal.repository.CountryRepository;
import com.odysseusinc.arachne.portal.repository.RoleRepository;
import com.odysseusinc.arachne.portal.repository.StateProvinceRepository;
import com.odysseusinc.arachne.portal.repository.StudyDataSourceLinkRepository;
import com.odysseusinc.arachne.portal.repository.UserStudyRepository;
import com.odysseusinc.arachne.portal.security.passwordvalidator.ArachnePasswordValidator;
import com.odysseusinc.arachne.portal.service.ProfessionalTypeService;
import com.odysseusinc.arachne.portal.service.SkillService;
import com.odysseusinc.arachne.portal.service.SolrService;
import com.odysseusinc.arachne.portal.service.TenantService;
import com.odysseusinc.arachne.portal.service.UserLinkService;
import com.odysseusinc.arachne.portal.service.UserPublicationService;
import com.odysseusinc.arachne.portal.service.UserRegistrantService;
import com.odysseusinc.arachne.portal.service.UserService;
import com.odysseusinc.arachne.portal.service.impl.solr.SolrField;
import com.odysseusinc.arachne.portal.service.mail.ArachneMailSender;
import org.springframework.context.MessageSource;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@SuppressWarnings("unused")
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl extends BaseUserServiceImpl<IUser, Skill, SolrField> implements UserService {
    public UserServiceImpl(StateProvinceRepository stateProvinceRepository,
                           MessageSource messageSource,
                           ProfessionalTypeService professionalTypeService,
                           JavaMailSender javaMailSender,
                           ArachnePasswordValidator passwordValidator,
                           BaseUserRepository userRepository,
                           CountryRepository countryRepository,
                           SolrService solrService,
                           ArachneMailSender arachneMailSender,
                           UserStudyRepository userStudyRepository,
                           UserPublicationService userPublicationService,
                           UserRegistrantService userRegistrantService,
                           StudyDataSourceLinkRepository studyDataSourceLinkRepository,
                           GenericConversionService conversionService,
                           AnalysisUnlockRequestRepository analysisUnlockRequestRepository,
                           SkillService skillService,
                           RoleRepository roleRepository,
                           UserLinkService userLinkService,
                           TenantService tenantService,
                           BaseRawUserRepository rawUserRepository) {

        super(stateProvinceRepository,
                messageSource,
                professionalTypeService,
                javaMailSender,
                passwordValidator,
                userRepository,
                countryRepository,
                solrService,
                arachneMailSender,
                userStudyRepository,
                userPublicationService,
                userRegistrantService,
                studyDataSourceLinkRepository,
                conversionService,
                analysisUnlockRequestRepository,
                skillService,
                roleRepository,
                userLinkService,
                tenantService,
                rawUserRepository);
    }
}
