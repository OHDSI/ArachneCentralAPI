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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.model.achilles.AchillesFile;
import com.odysseusinc.arachne.portal.model.achilles.Characterization;
import com.odysseusinc.arachne.portal.repository.AchillesFileRepository;
import com.odysseusinc.arachne.portal.repository.CharacterizationRepository;
import com.odysseusinc.arachne.portal.service.AchillesImportService;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.transaction.Transactional;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AchillesImportServiceImpl implements AchillesImportService {

    private static final String IMPORT_ACHILLES_RESULT_LOG =
            "{} import Achilles result for Data Source with id='{}', name='{}', Data Node with id='{}' name='{}'";

    private static final Logger LOGGER = LoggerFactory.getLogger(AchillesImportService.class);

    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    private int batchSize = 1000;

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
        try {
            final Characterization result = characterizationRepository.save(characterization);
            JsonParser parser = new JsonParser();
            List<AchillesFile> files = new ArrayList<>(batchSize);
            ZipFile zipFile = new ZipFile(archivedData);
            for(ZipEntry entry :  Collections.list(zipFile.entries())){
                String name = entry.getName();
                if (!entry.isDirectory()) {
                    try(final InputStream in = zipFile.getInputStream(entry)) {
                        try(JsonReader reader = new JsonReader(new InputStreamReader(in))){
                            JsonObject jsonObject = parser.parse(reader).getAsJsonObject();
                            AchillesFile achillesFile = new AchillesFile();
                            achillesFile.setData(jsonObject);
                            achillesFile.setFilePath(name);
                            achillesFile.setCharacterization(result);
                            files.add(achillesFile);
                            if (files.size() == batchSize) {
                                flush(files);
                                files = new ArrayList<>(batchSize);
                            }
                        }
                    }
                }
            }
            flush(files);
            LOGGER.info(IMPORT_ACHILLES_RESULT_LOG, "Finished", dataSourceId, dataSourceName, dataNodeId, dataNodeName);
        } finally {
            FileUtils.deleteQuietly(tempDir.toFile());
        }
    }

    private void flush(List<AchillesFile> achillesFiles) {

        for(AchillesFile file : achillesFiles) {
            if (file.getId() == null) {
                entityManager.persist(file);
            } else {
                entityManager.merge(file);
            }
        }
        entityManager.flush();
        entityManager.clear();
    }
}
