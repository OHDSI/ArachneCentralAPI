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
 * Created: May 19, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.impl;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.StandardOpenOption.READ;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.model.DataSource;
import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.model.achilles.AchillesFile;
import com.odysseusinc.arachne.portal.model.achilles.Characterization;
import com.odysseusinc.arachne.portal.repository.AchillesFileRepository;
import com.odysseusinc.arachne.portal.repository.CharacterizationRepository;
import com.odysseusinc.arachne.portal.service.AchillesImportService;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.transaction.Transactional;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class AchillesImportServiceImpl implements AchillesImportService {

    private static final String IMPORT_ACHILLES_RESULT_LOG =
            "{} import Achilles result for Data Source with id='{}', name='{}', Data Node with id='{}' name='{}'";

    private static final Logger LOGGER = LoggerFactory.getLogger(AchillesImportService.class);
    private static final ThreadLocal<List<AchillesFile>> batch = new ThreadLocal<>();

    protected final EntityManager entityManager;
    protected final CharacterizationRepository characterizationRepository;
    protected final AchillesFileRepository achillesFileRepository;


    @Autowired
    public AchillesImportServiceImpl(EntityManager entityManager,
                                     CharacterizationRepository characterizationRepository,
                                     AchillesFileRepository achillesFileRepository) {

        this.entityManager = entityManager;
        this.characterizationRepository = characterizationRepository;
        this.achillesFileRepository = achillesFileRepository;
    }

    @Override
    @Async(value = "importAchillesReportsExecutor")
    @Transactional
    public void importData(IDataSource dataSource, File archivedData) throws IOException {

        Characterization characterization = new Characterization();
        characterization.setDataSource(dataSource);
        final Long dataSourceId = dataSource.getId();
        final String dataSourceName = dataSource.getName();
        final DataNode dataNode = dataSource.getDataNode();
        final Long dataNodeId = dataNode.getId();
        final String dataNodeName = dataNode.getName();
        LOGGER.info(IMPORT_ACHILLES_RESULT_LOG, "Started", dataSourceId, dataSourceName, dataNodeId, dataNodeName);
        Timestamp now = new Timestamp(new Date().getTime());
        characterization.setDate(now);
        Path tempDir = Files.createTempDirectory("achilles_");
        entityManager.setFlushMode(FlushModeType.COMMIT);
        batch.set(new ArrayList<>());
        try {
            unzipData(archivedData, tempDir);
            List<AchillesFile> files = new LinkedList<>();
            JsonParser parser = new JsonParser();
            final Characterization result = characterizationRepository.save(characterization);
            Files.walkFileTree(tempDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                    AchillesFile achillesFile = new AchillesFile();
                    achillesFile.setFilePath(tempDir.relativize(file).toString());
                    try (JsonReader reader = new JsonReader(new InputStreamReader(Files.newInputStream(file, READ)))) {
                        JsonObject jsonObject = parser.parse(reader).getAsJsonObject();
                        achillesFile.setData(jsonObject);
                    }
                    achillesFile.setCharacterization(result);
                    saveAsBatch(achillesFile);
                    return CONTINUE;
                }
            });
            flush();
            LOGGER.info(IMPORT_ACHILLES_RESULT_LOG, "Finished", dataSourceId, dataSourceName, dataNodeId, dataNodeName);
        } finally {
            FileUtils.deleteQuietly(tempDir.toFile());
        }
    }

    private void unzipData(File archivedFile, Path destination) throws IOException {

        /*
        Objects.requireNonNull(archivedFile);
        if (Files.notExists(destination)) {
            Files.createDirectories(destination);
        }
        try {
            CommonFileUtils.unzipFiles(archivedFile, destination.toFile());
            FileUtils.deleteQuietly(archivedFile);
        } catch (ZipException e) {
            throw new java.util.zip.ZipException(e.getMessage());
        }
        */
        // Alternate unzip implementation. Code is below unzips achilles loosing jsons
        java.util.zip.ZipFile zipFile = new ZipFile(archivedFile);
        try {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File entryDestination = new File(destination.toString(), entry.getName());
                if (entry.isDirectory()) {
                    entryDestination.mkdirs();
                } else {
                    entryDestination.getParentFile().mkdirs();
                    InputStream in = zipFile.getInputStream(entry);
                    OutputStream out = new FileOutputStream(entryDestination);
                    IOUtils.copy(in, out);
                    IOUtils.closeQuietly(in);
                    out.close();
                }
            }
        } finally {
            zipFile.close();
        }
    }

    private void saveAsBatch(AchillesFile achillesFile) {

        List<AchillesFile> achillesFiles = batch.get();
        if (achillesFile != null) {
            achillesFiles.add(achillesFile);
        }
        if (achillesFiles.size() == 1000) {
            flush();
        }
    }

    private void flush() {

        final List<AchillesFile> achillesFiles = batch.get();
        if (!CollectionUtils.isEmpty(achillesFiles)) {
            achillesFileRepository.save(achillesFiles);
            batch.set(new ArrayList<>());
        }
        entityManager.flush();
        entityManager.clear();
    }
}
