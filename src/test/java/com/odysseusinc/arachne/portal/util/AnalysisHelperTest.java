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
 * Created: April 20, 2017
 *
 */

package com.odysseusinc.arachne.portal.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.mockito.Mockito.when;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.NotUniqueException;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.AnalysisFile;
import com.odysseusinc.arachne.portal.model.DataSource;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.StudyType;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.model.SubmissionGroup;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.repository.AnalysisFileRepository;
import com.odysseusinc.arachne.portal.repository.AnalysisRepository;
import com.odysseusinc.arachne.portal.repository.StudyRepository;
import com.odysseusinc.arachne.portal.repository.StudyTypeRepository;
import com.odysseusinc.arachne.portal.repository.SubmissionFileRepository;
import com.odysseusinc.arachne.portal.security.ArachnePermission;
import com.odysseusinc.arachne.portal.service.BaseUserService;
import com.odysseusinc.arachne.portal.service.DataSourceService;
import com.odysseusinc.arachne.portal.service.StudyService;
import com.odysseusinc.arachne.portal.service.analysis.BaseAnalysisService;
import com.odysseusinc.arachne.portal.service.submission.SubmissionService;
import edu.emory.mathcs.backport.java.util.Collections;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.util.Assert;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application.properties")
@ActiveProfiles("test")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class})
@DbUnitConfiguration(databaseConnection = {"primaryDataSource"})
@DatabaseSetup("../rest/oldAdminUser.xml")
public class AnalysisHelperTest {
    @Autowired
    private BaseAnalysisService<Analysis> analysisService;
    @Autowired
    private AnalysisRepository analysisRepository;
    @Autowired
    private StudyRepository studyRepository;
    @Autowired
    private StudyTypeRepository studyTypeRepository;
    @MockBean
    private DataSourceService dataSourceService;
    @Autowired
    private BaseUserService userService;
    @Autowired
    private StudyService studyService;
    @Autowired
    private AnalysisFileRepository analysisFileRepository;
    @Autowired
    private SubmissionFileRepository submissionFileRepository;
    @Autowired
    private AnalysisHelper analysisHelper;
    @Autowired
    private SubmissionService submissionService;

    @Test
    @WithUserDetails("admin@odysseusinc.com")
    public void createSubmission() throws Exception {

        DataSource dataSource = prepareDataSource();
        when(dataSourceService.getByIdUnsecured(1L)).thenReturn(dataSource);

        IUser user = prepareUser();
        Study study = prepareStudy(user);
        Analysis analysis = prepareAnalysis(user, study);
        List<Submission> submissions = AnalysisHelper.createSubmission(submissionService,
                Collections.<Long>singletonList(1L), user, analysis);
        try {
            assertThat(submissions, is(not(empty())));
            assertThat(submissions, contains(
                    hasProperty("status", notNullValue())
            ));
            assertThat(submissions, contains(
                    hasProperty("submissionGroup", notNullValue())
            ));
        } finally {
            cleanup(submissions, analysis, study);
        }
    }

    private DataSource prepareDataSource() {

        DataSource dataSource = new DataSource();
        dataSource.setId(1L);
        dataSource.setName("test");
        return dataSource;
    }

    private IUser prepareUser() {

        return userService.getByEmail("admin@odysseusinc.com");
    }

    private Analysis prepareAnalysis(IUser author, Study study) throws Exception {

        List<Analysis> exists = analysisRepository.findByTitleAndStudyId("AnalysisHelperTest#test", study.getId());
        if (!exists.isEmpty()) {
            analysisRepository.delete(exists);
        }
        Analysis analysis = new Analysis();
        analysis.setTitle("AnalysisHelperTest#test");
        analysis.setFiles(new ArrayList<>());
        analysis.setAuthor(author);
        analysis.setStudy(study);
        analysis.setType(CommonAnalysisType.COHORT_CHARACTERIZATION);
        Set<ArachnePermission> permissions = new HashSet<>();
        permissions.add(ArachnePermission.CREATE_SUBMISSION);
        permissions.add(ArachnePermission.CREATE_ANALYSIS);
        analysis.setPermissions(permissions);
        analysis = analysisService.create(analysis);

        AnalysisFile file = new AnalysisFile();
        file.setUuid(UUID.randomUUID().toString());
        file.setAnalysis(analysis);
        file.setContentType("text/plain");
        file.setLabel("");
        file.setAuthor(author);
        file.setUpdatedBy(author);
        file.setExecutable(Boolean.TRUE);
        file.setRealName("test.sql");
        Date created = new Date();
        file.setCreated(created);
        file.setUpdated(created);
        file.setVersion(1);
        file = analysisFileRepository.save(file);
        analysis.getFiles().add(file);

        Path dir = Paths.get(analysisHelper.getStoreFilesPath(), study.getId().toString(), analysis.getId().toString());
        Files.createDirectories(dir);
        Path path = dir.resolve(file.getUuid());
        Files.write(path, "test".getBytes());

        return analysis;
    }

    private Study prepareStudy(IUser owner) throws NotUniqueException, NotExistException {

        List<Study> exists = studyRepository.findByTitle("AnalysisHelperTest#test");
        if (!exists.isEmpty()) {
            analysisFileRepository.deleteAll();
            submissionFileRepository.deleteAll();
            studyRepository.delete(exists);
        }
        Study study = new Study();
        study.setTitle("AnalysisHelperTest#test");
        StudyType studyType = studyTypeRepository.findAll().iterator().next();
        Assert.notNull(studyType);
        study.setType(studyType);

        return studyService.create(owner, study);
    }

    private void cleanup(List<Submission> submissions, Analysis analysis, Study study) {

        SubmissionGroup group = submissions.iterator().next().getSubmissionGroup();
        group.getFiles().forEach(file -> FileUtils.deleteQuietly(analysisHelper
                .getSubmissionFile(file).toFile()));
        analysis.getFiles().forEach(analysisFile -> FileUtils.deleteQuietly(
                analysisHelper.getAnalysisFolder(analysis).resolve(analysisFile.getUuid()).toFile()));
        analysisFileRepository.deleteAll();
        submissionFileRepository.deleteAll();
        studyRepository.delete(study);
    }

}