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
 * Created: February 07, 2017
 *
 */

package com.odysseusinc.arachne.portal.util;

import com.odysseusinc.arachne.portal.exception.ConverterRuntimeException;
import com.odysseusinc.arachne.portal.exception.IORuntimeException;
import com.odysseusinc.arachne.portal.exception.NoExecutableFileException;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.model.SubmissionFile;
import com.odysseusinc.arachne.portal.model.SubmissionGroup;
import com.odysseusinc.arachne.portal.service.AnalysisPaths;
import com.odysseusinc.arachne.portal.service.submission.BaseSubmissionService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AnalysisHelper implements AnalysisPaths {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalysisHelper.class);
    private static final String SUBMISSION_GROUP_PREFIX = "sg_";

    @Value("${files.store.path}")
    private String fileStorePath;
    @Value("${analisis.file.maxsize}")
    private Long maximumSize;

    public static List<Submission> createSubmission(BaseSubmissionService submissionService,
                                                    List<Long> datasourceIds, IUser user,
                                                    Analysis analysis)
            throws IOException, NotExistException, NoExecutableFileException, ValidationException {

        final List<Submission> submissions = new LinkedList<>();

        //removes datasourceId duplicates
        Set<Long> datasourceIdSet = new HashSet<>(datasourceIds);

        SubmissionGroup submissionGroup = submissionService.createSubmissionGroup(user, analysis);

        for (Long datasourceId : datasourceIdSet) {
            submissions.add(submissionService.createSubmission(user, analysis, datasourceId, submissionGroup));
        }

        return submissions;
    }

    public String getStoreFilesPath() {

        return fileStorePath + File.separator + CONTENT_DIR;
    }

    public Path getSplittedFolder(SubmissionGroup source) {

        Path splitedFolder = getSubmissionGroupFolder(source).resolve(SPLITED_DIR);
        File file = splitedFolder.toFile();
        if (!file.exists()) {
            if (!file.mkdirs()) {
                throw new ConverterRuntimeException("Cann't create folder \"" + SPLITED_DIR + "\" ");
            }
        }
        return splitedFolder;
    }

    public Path getSubmissionFile(SubmissionFile file) {

        return getSubmissionGroupFolder(file.getSubmissionGroup())
                .resolve(file.getUuid());
    }

    public Path getArchiveFolder(SubmissionGroup source) {

        Path archiveFolderPath = getSubmissionGroupFolder(source).resolve(
                ARCHIVE_DIR);
        File archiveFolder = archiveFolderPath.toFile();
        if (!archiveFolder.exists()) {
            if (!archiveFolder.mkdirs()) {
                throw new ConverterRuntimeException("can not create folder archive");
            }
        }
        return archiveFolderPath;
    }

    public Path getResultArchPath(Submission submission) {

        Path path = getSubmissionFolder(submission).resolve(RESULTS_ARCH_DIR);
        if (Files.notExists(path)) {
            if (!path.toFile().mkdirs()) {
                throw new IORuntimeException("can not create folder");
            }
        }
        return path;
    }


    public Path getSubmissionFolder(Submission submission) {

        return getSubmissionGroupFolder(submission.getSubmissionGroup()).resolve(
                submission.getId().toString());
    }

    public Path getSubmissionResultFolder(Submission submission) {

        return getSubmissionFolder(submission).resolve(RESULT_DIR);
    }

    public Path getSubmissionGroupFolder(SubmissionGroup submissionGroup) {

        final Analysis analysis = submissionGroup.getAnalysis();
        return Paths.get(getStoreFilesPath(),
                analysis.getStudy().getId().toString(),
                analysis.getId().toString(),
                SUBMISSION_GROUP_PREFIX + submissionGroup.getId().toString());
    }

    public void compressAndSplit(ArrayList<File> files, File zipArchive) {

        try {
            ZipFile zipFile = new ZipFile(zipArchive.getAbsoluteFile());
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
            zipFile.createZipFile(files, parameters, true, maximumSize);
        } catch (ZipException ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new ConverterRuntimeException(ex.getMessage());
        }
    }

    public File getZipArchiveFile(Submission source, File splitedFolder) {

        return Paths.get(
                splitedFolder.getPath(),
                source.getId() + ".zip"
        ).toFile();
    }

    public Path getAnalysisFolder(Analysis analysis) {

        String studyFilesPath = getStoreFilesPath();
        Path filesStoreDirPath = Paths.get(studyFilesPath);
        File fileStoreDir = filesStoreDirPath.toFile();
        if (!fileStoreDir.exists()) {
            if (!fileStoreDir.mkdirs()) {
                throw new IORuntimeException("cann't create directory:" + filesStoreDirPath);
            }
        }
        Path studyStoreDirPath = Paths.get(studyFilesPath, analysis.getStudy().getId().toString());
        File studyStoreDir = studyStoreDirPath.toFile();
        if (!studyStoreDir.exists()) {
            if (!studyStoreDir.mkdirs()) {
                throw new IORuntimeException("cann't create directory:" + studyStoreDirPath);
            }
        }
        Path storeDirPath = studyStoreDirPath.resolve(analysis.getId().toString());
        File storeDir = storeDirPath.toFile();
        if (!storeDir.exists()) {
            if (!storeDir.mkdirs()) {
                throw new IORuntimeException("cann't create directory:" + storeDirPath);
            }
        }
        return storeDirPath;
    }

}
