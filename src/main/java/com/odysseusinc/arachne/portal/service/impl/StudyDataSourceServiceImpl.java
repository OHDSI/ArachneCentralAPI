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
 * Created: August 25, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.impl;

import com.odysseusinc.arachne.portal.model.solr.SolrCollection;
import com.odysseusinc.arachne.portal.service.BaseDataSourceService;
import com.odysseusinc.arachne.portal.service.BaseSolrService;
import com.odysseusinc.arachne.portal.service.BaseStudyService;
import com.odysseusinc.arachne.portal.service.StudyDataSourceService;
import java.io.IOException;
import java.util.List;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudyDataSourceServiceImpl implements StudyDataSourceService {

    private final BaseStudyService studyService;
    private final BaseSolrService solrService;
    private final BaseDataSourceService dataSourceService;

    @Autowired
    public StudyDataSourceServiceImpl(BaseStudyService studyService,
                                      BaseSolrService solrService,
                                      BaseDataSourceService dataSourceService) {

        this.studyService = studyService;
        this.solrService = solrService;
        this.dataSourceService = dataSourceService;
    }

    @Transactional
    @Override
    public void softDeletingDataSource(final Long dataSourceId) throws IOException, SolrServerException {

        final List<Long> studyIds = studyService.getStudyIdsOfDataSource(dataSourceId);
        studyIds.forEach(studyId -> studyService.removeDataSourceUnsecured(studyId, dataSourceId));
        dataSourceService.delete(dataSourceId);
        solrService.delete(SolrCollection.DATA_SOURCES, String.valueOf(dataSourceId));
    }
}
