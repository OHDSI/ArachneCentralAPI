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
 * Created: July 17, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.impl;

import static java.util.Collections.singletonList;

import com.odysseusinc.arachne.commons.utils.CommonFileUtils;
import com.odysseusinc.arachne.portal.exception.IORuntimeException;
import com.odysseusinc.arachne.portal.model.AbstractPaperFile;
import com.odysseusinc.arachne.portal.model.AbstractStudyFile;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.StudyFile;
import com.odysseusinc.arachne.portal.service.ToPdfConverter;
import com.odysseusinc.arachne.portal.service.StudyFileService;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.odysseusinc.arachne.portal.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StudyFileServiceImpl implements StudyFileService {

    Logger LOGGER = LoggerFactory.getLogger(StudyFileServiceImpl.class);

    @Value("${files.store.path}")
    private String fileStorePath;

    private final RestTemplate restTemplate;

    @Autowired
    public StudyFileServiceImpl(@Qualifier("restTemplate") RestTemplate restTemplate) {

        this.restTemplate = restTemplate;
    }

    @Override
    public InputStream getFileInputStream(AbstractStudyFile studyFile) throws FileNotFoundException {

        Objects.requireNonNull(studyFile, "File must not be null");
        final File file = getPathToFile(studyFile).toFile();
        if (!file.exists()) {
            if (!StringUtils.isEmpty(studyFile.getLink())) {
                InputStream response = getInputStream(studyFile);
                if (response != null) {
                    return response;
                }
            }
            throw new FileNotFoundException();
        }
        return new FileInputStream(file);
    }

    private InputStream getInputStream(AbstractStudyFile studyFile) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.APPLICATION_OCTET_STREAM));
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> response = restTemplate.exchange(
                studyFile.getLink(),
                HttpMethod.GET, entity, byte[].class);
        if (response.getStatusCode() == HttpStatus.OK) {
            return new ByteArrayInputStream(response.getBody());
        }
        return null;
    }

    @Override
    public Path getPathToFile(AbstractStudyFile studyFile) {

        final File contentDirectory = getContentDirectory(studyFile);
        return contentDirectory.toPath().resolve(studyFile.getUuid());
    }

    @Override
    public void saveFile(MultipartFile multipartFile, AbstractStudyFile studyFile) throws IOException {

        saveFile(multipartFile, studyFile, false);
    }

    @Override
    public void updateFile(MultipartFile multipartFile, AbstractStudyFile studyFile) throws IOException {

        saveFile(multipartFile, studyFile, true);
    }

    @Override
    public void delete(AbstractStudyFile studyFile) throws FileNotFoundException {

        final File file = getPathToFile(studyFile).toFile();

        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        if (!file.delete()) {
            throw new IORuntimeException("Can't delete file:" + file);
        }
    }

    @Override
    public void delete(List<? extends AbstractStudyFile> files) {

        for (AbstractStudyFile file : files) {
            try {
                delete(file);
            } catch (FileNotFoundException e) {
                LOGGER.debug("File with uuid : {} cannot be found on file system, may be it's already deleted", file.getUuid());
            }
        }
    }

    @Override
    public Path getPath(Study study) {

        Path path = Paths.get(fileStorePath, "content", study.getId().toString());
        createDirs(path);
        return path;
    }

    @Override
    public void archiveFiles(OutputStream os, Path filePath, List<? extends AbstractStudyFile> files) throws IOException {

        ZipOutputStream zos = new ZipOutputStream(os);
        for (AbstractStudyFile studyFile : files) {
            ZipEntry entry = new ZipEntry(studyFile.getRealName());
            Path file = filePath.resolve(studyFile.getUuid());
            entry.setSize(file.toFile().length());
            zos.putNextEntry(entry);
            zos.write(Files.readAllBytes(file));
            zos.closeEntry();
        }
        zos.flush();
        zos.close();
    }

    public Path getStudyFilePath(AbstractStudyFile studyFile) {

        final File contentDirectory = getContentDirectory(studyFile);
        return contentDirectory.toPath().resolve(studyFile.getUuid());
    }

    private void createDirs(Path path) {

        final File file = path.toFile();

        if (!file.exists()) {
            if (!file.mkdirs()) {
                throw new IORuntimeException("Can't create folder:" + file);
            }
        }
    }

    private void saveFile(MultipartFile multipartFile, AbstractStudyFile studyFile, boolean rewriteExisting) throws IOException {

        final Path file = getStudyFilePath(studyFile);
        final InputStream inputStream = multipartFile.getInputStream();
        CopyOption[] copyOptions = {};
        if (rewriteExisting) {
            copyOptions = new CopyOption[]{StandardCopyOption.REPLACE_EXISTING};
        }
        Files.copy(inputStream, file, copyOptions);
        inputStream.close();
    }

    private File getContentDirectory(AbstractStudyFile studyFile) {

        File dir;
        if (studyFile instanceof StudyFile) {
            dir = getPath(((StudyFile) studyFile).getStudy()).toFile();
        } else if (studyFile instanceof AbstractPaperFile) {
            dir = Paths.get(fileStorePath, "papers", ((AbstractPaperFile) studyFile).getPaper().getId().toString()).toFile();
        } else {
            throw new IllegalArgumentException("file type is not recognized");
        }
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IORuntimeException();
            }
        }
        return dir;
    }
}
