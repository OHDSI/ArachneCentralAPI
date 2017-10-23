/*
 *
 * Copyright 2017 Observational Health Data Sciences and Informatics
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

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.StandardOpenOption.READ;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.odysseusinc.arachne.execution_engine_common.util.CommonFileUtils;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.model.DataSource;
import com.odysseusinc.arachne.portal.model.achilles.AchillesFile;
import com.odysseusinc.arachne.portal.model.achilles.AchillesReport;
import com.odysseusinc.arachne.portal.model.achilles.Characterization;
import com.odysseusinc.arachne.portal.repository.AchillesFileRepository;
import com.odysseusinc.arachne.portal.repository.AchillesReportRepository;
import com.odysseusinc.arachne.portal.repository.CharacterizationRepository;
import com.odysseusinc.arachne.portal.service.AchillesService;
import com.odysseusinc.arachne.portal.service.StudyService;
import com.odysseusinc.arachne.portal.util.ZipUtil;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.method.P;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

public abstract class BaseAchillesService<DS extends DataSource> implements AchillesService<DS> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AchillesService.class);
    protected final CharacterizationRepository characterizationRepository;
    protected final AchillesFileRepository achillesFileRepository;
    protected final AchillesReportRepository achillesReportRepository;
    protected final StudyService studyService;

    public BaseAchillesService(AchillesFileRepository achillesFileRepository,
                               CharacterizationRepository characterizationRepository,
                               StudyService studyService,
                               AchillesReportRepository achillesReportRepository) {

        this.achillesFileRepository = achillesFileRepository;
        this.characterizationRepository = characterizationRepository;
        this.studyService = studyService;
        this.achillesReportRepository = achillesReportRepository;
    }

    @Override
    public Characterization createCharacterization(DS dataSource,
                                                   MultipartFile data) throws IOException {

        Characterization characterization = new Characterization();
        characterization.setDataSource(dataSource);
        Timestamp now = new Timestamp(new Date().getTime());
        characterization.setDate(now);
        Path tempDir = Files.createTempDirectory("achilles_");
        try {
            unzipData(data, tempDir);
            List<AchillesFile> files = new LinkedList<>();
            JsonParser parser = new JsonParser();
            Files.walkFileTree(tempDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                    AchillesFile achillesFile = new AchillesFile();
                    achillesFile.setFilePath(tempDir.relativize(file).toString());
                    try (JsonReader reader = new JsonReader(new InputStreamReader(Files.newInputStream(file, READ)))) {
                        JsonObject jsonObject = parser.parse(reader).getAsJsonObject();
                        achillesFile.setData(jsonObject);
                    }
                    files.add(achillesFile);
                    return CONTINUE;
                }
            });
            final Characterization result = characterizationRepository.save(characterization);
            files.forEach(file -> file.setCharacterization(result));
            achillesFileRepository.save(files);
            return characterizationRepository.findOne(result.getId());
        } finally {
            FileUtils.deleteQuietly(tempDir.toFile());
        }
    }

    protected void unzipData(MultipartFile file, Path destination) throws IOException {

        if (Files.notExists(destination)) {
            Files.createDirectories(destination);
        }
        Objects.requireNonNull(file);
        Path archive = Files.createTempFile("achilles", ".zip");
        FileCopyUtils.copy(file.getInputStream(), new FileOutputStream(archive.toFile()));
        try {
            CommonFileUtils.unzipFiles(archive.toFile(), destination.toFile());
        } catch (ZipException e) {
            throw new java.util.zip.ZipException(e.getMessage());
        }
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
