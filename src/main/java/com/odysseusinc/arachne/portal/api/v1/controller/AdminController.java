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

import com.odysseusinc.arachne.commons.utils.ConverterUtils;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.Paper;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.StudyViewItem;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.model.search.PaperSearch;
import com.odysseusinc.arachne.portal.model.search.StudySearch;
import com.odysseusinc.arachne.portal.service.AdminService;
import com.odysseusinc.arachne.portal.service.DataSourceService;
import com.odysseusinc.arachne.portal.service.PaperService;
import com.odysseusinc.arachne.portal.service.ProfessionalTypeService;
import com.odysseusinc.arachne.portal.service.StudyService;
import com.odysseusinc.arachne.portal.service.TenantService;
import com.odysseusinc.arachne.portal.service.analysis.AnalysisService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Validator;

@Api(hidden = true)
@RestController
public class AdminController extends BaseAdminController<IUser, Study, IDataSource, StudySearch, StudyViewItem, Analysis, Paper, PaperSearch, Submission> {

    @Autowired
    public AdminController(final DataSourceService dataSourceService,
                           final ProfessionalTypeService professionalTypeService,
                           final AdminService adminService,
                           final StudyService studyService,
                           final AnalysisService analysisService,
                           final PaperService paperService,
                           final TenantService tenantService,
                           final ConverterUtils converterUtils,
                           final Validator validator,
                           final MessageSource messageSource) {

        super(dataSourceService, professionalTypeService, adminService,
                studyService, analysisService, paperService,
                tenantService, converterUtils, validator,
                messageSource);
    }

    @Override
    protected Class getUser() {

        return User.class;
    }
}
