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
 * Created: June 13, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.controller;

import static com.github.springtestdbunit.assertion.DatabaseAssertionMode.NON_STRICT;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseSetups;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.annotation.ExpectedDatabases;
import com.odysseusinc.arachne.portal.api.v1.dto.ApproveDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.CreateSubmissionsDTO;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.util.AnalysisHelper;
import com.odysseusinc.arachne.portal.util.ContentStorageHelper;
import com.odysseusinc.arachne.storage.model.ArachneFileMeta;
import com.odysseusinc.arachne.storage.service.ContentStorageService;
import com.odysseusinc.arachne.storage.util.TypifiedJcrTemplate;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.jcr.Node;
import org.apache.commons.io.FileUtils;
import org.assertj.core.util.Lists;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@RunWith(SpringRunner.class)
@DatabaseSetups({
        @DatabaseSetup("/data/users.xml"),
        @DatabaseSetup("/data/study-with-contributor.xml"),
        @DatabaseSetup("/data/datanode-with-datasources.xml"),
        @DatabaseSetup("/data/analysis/submission/study-datasource-analysis-file.xml")
})
@ExpectedDatabases({
        @ExpectedDatabase(value = "/data/users.xml", assertionMode = NON_STRICT),
        @ExpectedDatabase(value = "/data/study-with-contributor.xml", assertionMode = NON_STRICT),
        @ExpectedDatabase(value = "/data/datanode-with-datasources.xml", assertionMode = NON_STRICT),
        @ExpectedDatabase(value = "/data/analysis/submission/study-datasource-analysis-file.xml", assertionMode = NON_STRICT)
})
@DatabaseTearDown(value = "/data/empty.xml", type = DatabaseOperation.DELETE_ALL)
public class AnalysisSubmissionControllerTests extends BaseControllerTest {

    private static final long STUDY_ID = 1L;
    private static final long ANALYSIS_ID = 3L;
    private static final long SUBMISSION_GROUP_ID = 1L;
    private static final long SUBMISSION_ID = 1L;
    private static final long PUBLIC_MANUAL_DATA_SOURCE_ID = 1L;
    private static final long PUBLIC_SUPERVISED_DATA_SOURCE_ID = 2L;
    private static final long NOT_STUDY_DATASOURCE_ID = 3L;
    private static final long DATA_NODE_ONWER_ID = 2L;

    private static final String STUDY_LEAD = "admin@odysseusinc.com";
    private static final String DATA_NODE_ONWER = "user1@odysseusinc.com";
    private static final String DATA_NODE_USER = "user2@odysseusinc.com";

    @Autowired
    private AnalysisHelper analysisHelper;

    @Autowired
    private ContentStorageService contentStorageService;

    @Autowired
    private ContentStorageHelper contentStorageHelper;

    @Autowired
    private TypifiedJcrTemplate jcrTemplate;

    @Value("${files.store.path}")
    private String filesStore;

    @After
    public void cleanUp() {

        FileUtils.deleteQuietly(new File(filesStore));

        jcrTemplate.exec(session -> {

            String path = getResultFilePath(1L);

            try {
                Node fileNode = session.getNode(path);
                fileNode.remove();
                session.save();
            } catch (Exception ex) {}
            return null;
        });
    }

    @Test
    @WithUserDetails(STUDY_LEAD)
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/analysis/submission/result/submission-datasource1.xml", assertionMode = NON_STRICT),
    })
    public void testCreateSubmissionManualDS() throws Exception {

        createSubmission(STUDY_ID, ANALYSIS_ID, PUBLIC_MANUAL_DATA_SOURCE_ID);
    }

    @Test
    @WithUserDetails(DATA_NODE_ONWER)
    @ExpectedDatabase(value = "/data/analysis/submission/result/submission-datasource3.xml", assertionMode = NON_STRICT)
    public void testCreateSubmissionWithNotConnectedDS() throws Exception {

        sendRequest(STUDY_ID, ANALYSIS_ID, NOT_STUDY_DATASOURCE_ID)
                .andExpect(PERMISSION_DENIED_CODE);
    }

    @Test
    @WithUserDetails(DATA_NODE_ONWER)
    @DatabaseSetups({
            @DatabaseSetup("/data/analysis/submission/submission-pending.xml")
    })
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/analysis/submission/result/submission-datasource2-approved.xml", assertionMode = NON_STRICT),

    })
    public void testApproveSubmissionByOwner() throws Exception {

        ApproveDTO approveDTO = new ApproveDTO(SUBMISSION_ID, true, null, "string");
        prepareAnalysisFile(STUDY_ID, ANALYSIS_ID);
        prepareSubmissionGroupFile(STUDY_ID, ANALYSIS_ID, SUBMISSION_GROUP_ID);

        mvc.perform(
                post("/api/v1/analysis-management/submissions/{submissionId}/approve", 1)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(approveDTO))
        ).andExpect(NO_ERROR_CODE);
    }

    @Test
    @WithUserDetails(DATA_NODE_USER)
    @DatabaseSetups({
            @DatabaseSetup("/data/analysis/submission/submission-pending.xml")
    })
    @ExpectedDatabase(value = "/data/analysis/submission/submission-pending.xml", assertionMode = NON_STRICT)
    public void testApproveSubmissionByNonOwner() throws Exception {

        ApproveDTO approveDTO = new ApproveDTO(SUBMISSION_ID, true, null, null);
        prepareAnalysisFile(STUDY_ID, ANALYSIS_ID);
        prepareSubmissionGroupFile(STUDY_ID, ANALYSIS_ID, SUBMISSION_GROUP_ID);

        mvc.perform(
                post("/api/v1/analysis-management/submissions/{submissionId}/approve", 1)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(approveDTO))
        ).andExpect(PERMISSION_DENIED_CODE);
    }

    @Test
    @WithUserDetails(DATA_NODE_ONWER)
    @DatabaseSetups({
            @DatabaseSetup("/data/analysis/submission/submission-pending.xml")
    })
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/analysis/submission/result/submission-datasource2-declined.xml", assertionMode = NON_STRICT),

    })
    public void testDeclineSubmissionByOwner() throws Exception {

        ApproveDTO approveDTO = new ApproveDTO(SUBMISSION_ID, false, null, "comment");
        prepareAnalysisFile(STUDY_ID, ANALYSIS_ID);
        prepareSubmissionGroupFile(STUDY_ID, ANALYSIS_ID, SUBMISSION_GROUP_ID);

        mvc.perform(
                post("/api/v1/analysis-management/submissions/{submissionId}/approve", 1)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(approveDTO))

        ).andExpect(NO_ERROR_CODE);
    }

    @Test
    @WithUserDetails(DATA_NODE_USER)
    @DatabaseSetups({
            @DatabaseSetup("/data/analysis/submission/submission-pending.xml")
    })
    @ExpectedDatabase(value = "/data/analysis/submission/submission-pending.xml", assertionMode = NON_STRICT)
    public void testDeclineSubmissionByNonOwner() throws Exception {

        ApproveDTO approveDTO = new ApproveDTO(SUBMISSION_ID, false, null, "comment");
        prepareAnalysisFile(STUDY_ID, ANALYSIS_ID);
        prepareSubmissionGroupFile(STUDY_ID, ANALYSIS_ID, SUBMISSION_GROUP_ID);

        mvc.perform(
                post("/api/v1/analysis-management/submissions/{submissionId}/approve", 1)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(approveDTO))
        ).andExpect(PERMISSION_DENIED_CODE);
    }

    @Test
    @WithUserDetails(DATA_NODE_ONWER)
    @DatabaseSetups({
            @DatabaseSetup("/data/analysis/submission/submission-executed.xml")
    })
    @ExpectedDatabase(value = "/data/analysis/submission/result/submission-datasource2-result-approved.xml", assertionMode = NON_STRICT)
    public void testApproveSubmissionResultByOwner() throws Exception {

        ApproveDTO approveDTO = new ApproveDTO(SUBMISSION_ID, true, null, null);
        prepareAnalysisFile(STUDY_ID, ANALYSIS_ID);
        prepareSubmissionGroupFile(STUDY_ID, ANALYSIS_ID, SUBMISSION_GROUP_ID);

        mvc.perform(
                post("/api/v1/analysis-management/submissions/{submissionId}/approveresult", 1)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(approveDTO))
        ).andExpect(NO_ERROR_CODE);
    }

    @Test
    @WithUserDetails(STUDY_LEAD)
    @DatabaseSetups({
            @DatabaseSetup("/data/analysis/submission/submission-executed.xml")
    })
    @ExpectedDatabase(value = "/data/analysis/submission/submission-executed.xml", assertionMode = NON_STRICT)
    public void testApproveSubmissionResultByNonOwner() throws Exception {

        ApproveDTO approveDTO = new ApproveDTO(SUBMISSION_ID, true, null, null);
        prepareAnalysisFile(STUDY_ID, ANALYSIS_ID);
        prepareSubmissionGroupFile(STUDY_ID, ANALYSIS_ID, SUBMISSION_GROUP_ID);

        mvc.perform(
                post("/api/v1/analysis-management/submissions/{submissionId}/approveresult", 1)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(approveDTO))
        ).andExpect(PERMISSION_DENIED_CODE);
    }

    @Test
    @WithUserDetails(DATA_NODE_ONWER)
    @DatabaseSetups({
            @DatabaseSetup("/data/analysis/submission/submission-executed.xml")
    })
    @ExpectedDatabase(value = "/data/analysis/submission/result/submission-datasource2-result-declined.xml", assertionMode = NON_STRICT)
    public void testDeclineSubmissionResultByOwner() throws Exception {

        ApproveDTO approveDTO = new ApproveDTO(SUBMISSION_ID, false, null, "comment");
        prepareAnalysisFile(STUDY_ID, ANALYSIS_ID);
        prepareSubmissionGroupFile(STUDY_ID, ANALYSIS_ID, SUBMISSION_GROUP_ID);

        mvc.perform(
                post("/api/v1/analysis-management/submissions/{submissionId}/approveresult", 1)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(approveDTO))
        ).andExpect(NO_ERROR_CODE);
    }

    @Test
    @WithUserDetails(DATA_NODE_USER)
    @DatabaseSetups({
            @DatabaseSetup("/data/analysis/submission/submission-executed.xml")
    })
    @ExpectedDatabase(value = "/data/analysis/submission/submission-executed.xml", assertionMode = NON_STRICT)
    public void testDeclineSubmissionResultByNonOwner() throws Exception {

        ApproveDTO approveDTO = new ApproveDTO(SUBMISSION_ID, false, null, "comment");
        prepareAnalysisFile(STUDY_ID, ANALYSIS_ID);
        prepareSubmissionGroupFile(STUDY_ID, ANALYSIS_ID, SUBMISSION_GROUP_ID);

        mvc.perform(
                post("/api/v1/analysis-management/submissions/{submissionId}/approveresult", 1)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(approveDTO))
        ).andExpect(PERMISSION_DENIED_CODE);
    }

    private void createSubmission(Long studyId, Long analysisId, Long dataSourceId) throws Exception {

        final MvcResult mvcResult = sendRequest(studyId, analysisId, dataSourceId)
                .andExpect(NO_ERROR_CODE)
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result", hasSize(1)))
                .andReturn();
        JSONObject expectedJsonObject = new JSONObject()
                .put("dataSource", new JSONObject().put("id", dataSourceId));
        JSONAssert.assertEquals(expectedJsonObject, getResultJSONArray(mvcResult).getJSONObject(0), false);
    }

    private ResultActions sendRequest(Long studyId, Long analysisId, Long dataSourceId) throws Exception {

        prepareAnalysisFile(studyId, analysisId);
        CreateSubmissionsDTO createSubmissionsDTO = new CreateSubmissionsDTO();
        createSubmissionsDTO.setDataSources(Lists.newArrayList(dataSourceId));
        return mvc.perform(
                post("/api/v1/analysis-management/" + analysisId + "/submissions")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(createSubmissionsDTO))
        );
    }

    private void prepareAnalysisFile(Long studyId, Long analysisId) throws IOException {

        Path dir = Paths.get(analysisHelper.getStoreFilesPath(), studyId.toString(), analysisId.toString());
        Files.createDirectories(dir);
        Path path = dir.resolve("078f135d-07f0-4c6a-afd2-b705a1c8d948");
        Files.write(path, "SELECT * FROM death LIMIT 10".getBytes());
    }

    private void prepareSubmissionGroupFile(Long studyId, Long analysisId, Long submissionGroupId) throws IOException {

        Path dir = Paths.get(analysisHelper.getStoreFilesPath(), studyId.toString(), analysisId.toString(), "sg_" + submissionGroupId);
        Files.createDirectories(dir);
        Path path = dir.resolve("06cd6d72-c6f5-4d9f-87a7-79025e79623f");
        Files.write(path, "SELECT * FROM death LIMIT 10".getBytes());
    }

    /*private void prepareResultFile(Long studyId, Long analysisId, Long submissionGroupId, Long submissionId) throws IOException {

        Path dir = Paths.get(analysisHelper.getStoreFilesPath(), studyId.toString(), analysisId.toString(), "sg_" + submissionGroupId, submissionId.toString(), AnalysisPaths.RESULT_DIR);
        Files.createDirectories(dir);
        Path path = dir.resolve("9934135d-07f0-4c6a-afd2-b705a1c8d948");
        Files.write(path, "SELECT * FROM death LIMIT 10".getBytes());
    }*/

    private String getResultFilePath(Long submissionId) {

        Submission submission = new Submission();
        submission.setId(submissionId);

        return contentStorageHelper.getResultFilesDir(submission, "test.sql");
    }

    private ArachneFileMeta prepareResultFile(Long submissionId) throws IOException {

        String filepath = getResultFilePath(submissionId);

        File tempFile = File.createTempFile("result-file", "tests");
        tempFile.deleteOnExit();

        Files.write(tempFile.toPath(), "SELECT * FROM death LIMIT 10".getBytes());

        return contentStorageService.saveFile(tempFile, filepath, 2L);
    }

    @Test
    @WithUserDetails(DATA_NODE_ONWER)
    @DatabaseSetups({
            @DatabaseSetup("/data/analysis/submission/submission-in-progress.xml")
    })
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/analysis/submission/result/submission-in-progress-with-added-file.xml", assertionMode = NON_STRICT),

    })
    public void testManualUploadingSubmissionResult() throws Exception {

        FileInputStream fileInputStream = new FileInputStream(this.getClass().getResource("/test.jpg").getPath());
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.sql", "image/jpeg", fileInputStream);

        mvc.perform(
                fileUpload("/api/v1/analysis-management/submissions/result/manualupload")
                        .file(multipartFile)
                        .contentType(MULTIPART_FORM_DATA)
                        .param("submissionId", "1")
                        .param("label", "test.sql")

        ).andExpect(NO_ERROR_CODE);
    }

//    @Test
//    @WithUserDetails(DATA_NODE_ONWER)
//    @DatabaseSetups({
//            @DatabaseSetup("/data/analysis/submission/submission-published-with-added-files.xml"),
//    })
//    @ExpectedDatabases({
//            @ExpectedDatabase(value = "/data/analysis/submission/submission-published-with-added-files.xml", assertionMode = NON_STRICT),
//
//    })
//    public void testGetUploadingSubmissionResult() throws Exception {
//
//        prepareResultFile(1L);
//    }

    @Test
    @WithUserDetails(DATA_NODE_ONWER)
    @DatabaseSetups({
            @DatabaseSetup("/data/analysis/submission/submission-in-progress.xml"),
            @DatabaseSetup("/data/analysis/submission/result-file.xml")

    })
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/analysis/submission/submission-in-progress.xml", assertionMode = NON_STRICT),
            @ExpectedDatabase(value = "/data/analysis/submission/result/empty-result-file.xml", assertionMode = NON_STRICT),

    })
    public void testDeleteSubmissionResult() throws Exception {

        Long submissionId = 1L;
        ArachneFileMeta fileMeta = prepareResultFile(1L);
        String fileUuid = fileMeta.getUuid();

        mvc.perform(
                delete("/api/v1/analysis-management/submissions/{submissionId}/result/{fileUuid}",
                        submissionId, fileUuid)
        ).andExpect(NO_ERROR_CODE);

        if (checkFileExists(getResultFilePath(submissionId))) {
            throw new Exception("JCR file was not deleted");
        }
    }

    private boolean checkFileExists(String path) {

        return jcrTemplate.exec(session -> {

            try {
                session.getNode(path);
            } catch (Exception ex) {
                return false;
            }
            return true;
        });
    }
}
