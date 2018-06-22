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

package com.odysseusinc.arachne.portal.service;

import com.odysseusinc.arachne.portal.model.AbstractStudyFile;
import com.odysseusinc.arachne.portal.model.Study;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface StudyFileService {

    Path getPathToFile(AbstractStudyFile studyFile);

    void saveFile(MultipartFile file, AbstractStudyFile studyFile) throws IOException;

    InputStream getFileInputStream(AbstractStudyFile studyFile) throws FileNotFoundException;

    void updateFile(MultipartFile file, AbstractStudyFile studyFile) throws IOException;

    void delete(AbstractStudyFile studyFile) throws FileNotFoundException;

    void delete(List<? extends AbstractStudyFile> files);

    Path getPath(Study study);

    void archiveFiles(OutputStream os, Path filePath, List<? extends AbstractStudyFile> files) throws IOException;

    Path getStudyFilePath(AbstractStudyFile studyFile);
}
