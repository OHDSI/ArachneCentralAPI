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
 * Created: May 19, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.impl;

import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.StudyViewItem;
import com.odysseusinc.arachne.portal.model.achilles.AchillesFile;
import com.odysseusinc.arachne.portal.model.achilles.AchillesReport;
import com.odysseusinc.arachne.portal.model.achilles.Characterization;
import com.odysseusinc.arachne.portal.model.search.StudySearch;
import com.odysseusinc.arachne.portal.repository.AchillesFileRepository;
import com.odysseusinc.arachne.portal.repository.AchillesReportRepository;
import com.odysseusinc.arachne.portal.repository.CharacterizationRepository;
import com.odysseusinc.arachne.portal.service.AchillesImportService;
import com.odysseusinc.arachne.portal.service.AchillesService;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AchillesServiceImpl extends BaseAchillesService<IDataSource, Study, StudySearch, StudyViewItem> implements AchillesService<IDataSource> {

    public AchillesServiceImpl(CharacterizationRepository characterizationRepository, AchillesFileRepository achillesFileRepository, AchillesReportRepository achillesReportRepository, AchillesImportService achillesHelperService) {

        super(characterizationRepository, achillesFileRepository, achillesReportRepository, achillesHelperService);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Characterization> getCharacterizations(IDataSource dataSource) {

        return super.getCharacterizations(dataSource);
    }

    @Override
    public Optional<Characterization> getCharacterization(IDataSource dataSource, Long characterizationId) {

        return super.getCharacterization(dataSource, characterizationId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Characterization> getLatestCharacterization(IDataSource dataSource) {

        return super.getLatestCharacterization(dataSource);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AchillesReport> getReports(AchillesFile file) {

        return super.getReports(file);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AchillesReport> getReports(IDataSource dataSource) {

        return super.getReports(dataSource);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AchillesFile> getAchillesFile(Long characterizationId, String filePath) {

        return super.getAchillesFile(characterizationId, filePath);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getLatestCharacterizationId(IDataSource dataSource) throws NotExistException {

        return super.getLatestCharacterizationId(dataSource);
    }
}
