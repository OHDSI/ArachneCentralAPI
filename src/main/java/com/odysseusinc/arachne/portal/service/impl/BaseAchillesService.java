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
 * Created: September 17, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.impl;

import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.model.AbstractUserStudyListItem;
import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.achilles.AchillesFile;
import com.odysseusinc.arachne.portal.model.achilles.AchillesReport;
import com.odysseusinc.arachne.portal.model.achilles.Characterization;
import com.odysseusinc.arachne.portal.model.search.StudySearch;
import com.odysseusinc.arachne.portal.repository.AchillesFileRepository;
import com.odysseusinc.arachne.portal.repository.AchillesReportRepository;
import com.odysseusinc.arachne.portal.repository.CharacterizationRepository;
import com.odysseusinc.arachne.portal.service.AchillesImportService;
import com.odysseusinc.arachne.portal.service.AchillesService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

public abstract class BaseAchillesService<DS extends IDataSource, S extends Study, SS extends StudySearch, SU extends AbstractUserStudyListItem> implements AchillesService<DS> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AchillesService.class);
    protected final CharacterizationRepository characterizationRepository;
    protected final AchillesFileRepository achillesFileRepository;
    protected final AchillesReportRepository achillesReportRepository;
    protected final AchillesImportService achillesHelperService;

    public BaseAchillesService(CharacterizationRepository characterizationRepository, AchillesFileRepository achillesFileRepository, AchillesReportRepository achillesReportRepository,AchillesImportService achillesHelperService) {

        this.characterizationRepository = characterizationRepository;
        this.achillesFileRepository = achillesFileRepository;
        this.achillesReportRepository = achillesReportRepository;
        this.achillesHelperService = achillesHelperService;
    }

    @Override
    @PreAuthorize("#ds.dataNode == authentication.principal or " +
            "hasPermission(#ds, T(com.odysseusinc.arachne.portal.security.ArachnePermission).UPLOAD_ACHILLES_REPORTS)")
    public void createCharacterization(@P("ds") DS dataSource, MultipartFile data) throws IOException {

        final File tempFile = Files.createTempFile("achilles", ".zip").toFile();
        data.transferTo(tempFile);
        achillesHelperService.importData(dataSource, tempFile);
    }

    @Override
    public List<Characterization> getCharacterizations(DS dataSource) {

        return characterizationRepository.findByDataSource(dataSource);
    }

    @Override
    public Optional<Characterization> getCharacterization(DS dataSource, Long characterizationId) {

        return characterizationRepository.findByIdAndDataSource(characterizationId, dataSource);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Characterization> getLatestCharacterization(DS dataSource) {

        return characterizationRepository.findTopByDataSourceOrderByDateDesc(dataSource);
    }

    @Override
    public List<AchillesReport> getReports(AchillesFile file) {

        return achillesReportRepository.findAllByOrderBySortOrderAsc().stream().filter(report ->
                report.getMatchers().stream().anyMatch(m -> m.match(file))
        ).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AchillesReport> getReports(DS dataSource) {

        Objects.requireNonNull(dataSource);
        return achillesReportRepository.findAllByOrderBySortOrderAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AchillesFile> getAchillesFile(Long characterizationId, String filePath) {

        return achillesFileRepository.findByCharacterizationAndFilePath(characterizationId, filePath);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getLatestCharacterizationId(DS dataSource) throws NotExistException {

        return getLatestCharacterization(dataSource).orElseThrow(() -> new NotExistException("", Characterization.class)).getId();
    }
}
