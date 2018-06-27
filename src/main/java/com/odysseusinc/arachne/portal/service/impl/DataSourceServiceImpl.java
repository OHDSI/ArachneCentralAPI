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
 * Created: November 15, 2016
 *
 */

package com.odysseusinc.arachne.portal.service.impl;

import com.odysseusinc.arachne.portal.model.DataSource;
import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.repository.BaseDataSourceRepository;
import com.odysseusinc.arachne.portal.repository.BaseRawDataSourceRepository;
import com.odysseusinc.arachne.portal.repository.StudyDataSourceLinkRepository;
import com.odysseusinc.arachne.portal.service.DataSourceService;
import com.odysseusinc.arachne.portal.service.SolrService;
import com.odysseusinc.arachne.portal.service.TenantService;
import com.odysseusinc.arachne.portal.service.UserService;
import com.odysseusinc.arachne.portal.service.impl.solr.SolrField;
import com.odysseusinc.arachne.portal.service.mail.ArachneMailSender;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@Service
@SuppressWarnings("unused")
@Transactional(rollbackFor = Exception.class)
public class DataSourceServiceImpl extends BaseDataSourceServiceImpl<IDataSource, SolrField> implements DataSourceService {

    public DataSourceServiceImpl(SolrService solrService,
                                 BaseDataSourceRepository dataSourceRepository,
                                 GenericConversionService conversionService,
                                 TenantService tenantService,
                                 BaseRawDataSourceRepository rawDataSourceRepository,
                                 UserService userService,
                                 ArachneMailSender arachneMailSender,
                                 EntityManager entityManager,
                                 StudyDataSourceLinkRepository studyDataSourceLinkRepository) {

        super(solrService, dataSourceRepository, conversionService, tenantService, rawDataSourceRepository, userService, arachneMailSender, entityManager, studyDataSourceLinkRepository);
    }

    @Override
    protected Class<?> getType() {

        return DataSource.class;
    }
}
