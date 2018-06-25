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
 * Created: May 31, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.controller;

import static com.github.springtestdbunit.assertion.DatabaseAssertionMode.NON_STRICT;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseSetups;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.annotation.ExpectedDatabases;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
import com.odysseusinc.arachne.portal.api.v1.dto.AnalysisCreateDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.AnalysisUpdateDTO;
import java.io.FileInputStream;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

@RunWith(SpringRunner.class)
@DatabaseSetup("/data/users.xml")
@ExpectedDatabase(value = "/data/users.xml", assertionMode = NON_STRICT)
@DatabaseTearDown(value = "/data/empty.xml", type = DatabaseOperation.DELETE_ALL)
public class AnalysisControllerTests extends BaseControllerTest {

    private static final Long STUDY_ID = 1L;
    private static final Long DATA_SOURCE_ID = 1L;
    private static final Long ANALYSIS_ID = 1L;
    private static final String ANALYSIS_TYPE_ID = CommonAnalysisType.CUSTOM.name();
    private static final String ANALYSIS_TITLE = "analysisTitle";
    private static final String ANALYSIS_DESCRIPTION = "analysisDescription";

    private static final String UPDATED_ANALYSIS_TITLE = "updatedAnalysisTitle";
    private static final String UPDATED_ANALYSIS_DESCRIPTION_VALUE = "updatedAnalysisDescription";
    private final JSONObject ANALYSIS_JSON_OBJECT = new JSONObject()
            .put("title", ANALYSIS_TITLE);

    private final JSONObject UPDATED_ANALYSIS_JSON_OBJECT = new JSONObject()
            .put("id", ANALYSIS_ID)
            .put("title", UPDATED_ANALYSIS_TITLE)
            .put("type", new JSONObject().put("id", ANALYSIS_TYPE_ID));

    private final JSONObject UPDATED_ANALYSIS_DESCR_JSON_OBJECT = new JSONObject()
            .put("id", ANALYSIS_ID)
            .put("description", UPDATED_ANALYSIS_DESCRIPTION_VALUE)
            .put("type", new JSONObject().put("id", ANALYSIS_TYPE_ID));

    private final JSONObject UPDATED_ANALYSIS_TYPE_JSON_OBJECT = new JSONObject()
            .put("id", ANALYSIS_ID)
            .put("title", ANALYSIS_TITLE)
            .put("type", new JSONObject().put("id", "COHORT_CHARACTERIZATION"));

    private final JSONObject SUBMISSION_JSON_OBJECT = new JSONObject()
            .put("dataSource", new JSONObject().put("id", DATA_SOURCE_ID));

    public AnalysisControllerTests() throws JSONException {
        super();
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetups({
            @DatabaseSetup("/data/study-with-contributor.xml"),
            @DatabaseSetup("/data/analysis/empty-analysis.xml")
    })
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/study-with-contributor.xml", assertionMode = NON_STRICT),
            @ExpectedDatabase(value = "/data/analysis/analysis.xml", assertionMode = NON_STRICT),
    })
    public void testCreateAnalysis() throws Exception {

        AnalysisCreateDTO analysisDTO = new AnalysisCreateDTO();
        analysisDTO.setTitle(ANALYSIS_TITLE);
        analysisDTO.setTypeId(ANALYSIS_TYPE_ID);
        analysisDTO.setStudyId(STUDY_ID);

        MvcResult mvcResult = mvc.perform(
                post("/api/v1/analysis-management/analyses")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(analysisDTO)))
                .andExpect(OK_STATUS)
                .andExpect(NO_ERROR_CODE)
                .andExpect(jsonPath("$.result.id").isNotEmpty())
                .andReturn();

        JSONObject result = getResultJSONObject(mvcResult);
        JSONAssert.assertEquals(ANALYSIS_JSON_OBJECT, result, false);
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetups({
            @DatabaseSetup("/data/study-with-contributor.xml"),
            @DatabaseSetup("/data/analysis/analysis-before-updating.xml")
    })
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/study-with-contributor.xml", assertionMode = NON_STRICT),
            @ExpectedDatabase(value = "/data/analysis/analysis-after-title-updating.xml", assertionMode = NON_STRICT),
    })
    public void testUpdateAnalysisTitle() throws Exception {

        AnalysisUpdateDTO analysisDTO = new AnalysisUpdateDTO();
        analysisDTO.setTitle(UPDATED_ANALYSIS_TITLE);
        analysisDTO.setTypeId(ANALYSIS_TYPE_ID);

        testUpdate(analysisDTO, UPDATED_ANALYSIS_JSON_OBJECT, UPDATED_ANALYSIS_TITLE);
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetups({
            @DatabaseSetup("/data/study-with-contributor.xml"),
            @DatabaseSetup("/data/analysis/analysis-before-updating.xml")
    })
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/study-with-contributor.xml", assertionMode = NON_STRICT),
            @ExpectedDatabase(value = "/data/analysis/analysis-after-description-updating.xml", assertionMode = NON_STRICT),
    })
    public void testUpdateAnalysisDescription() throws Exception {

        AnalysisUpdateDTO analysisDTO = new AnalysisUpdateDTO();
        analysisDTO.setDescription(UPDATED_ANALYSIS_DESCRIPTION_VALUE);
        analysisDTO.setTypeId(ANALYSIS_TYPE_ID);

        testUpdate(analysisDTO, UPDATED_ANALYSIS_DESCR_JSON_OBJECT, null);
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetups({
            @DatabaseSetup("/data/study-with-contributor.xml"),
            @DatabaseSetup("/data/analysis/analysis-before-updating.xml")
    })
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/study-with-contributor.xml", assertionMode = NON_STRICT),
            @ExpectedDatabase(value = "/data/analysis/analysis-after-type-updating.xml", assertionMode = NON_STRICT),
    })
    public void testUpdateAnalysisType() throws Exception {

        AnalysisUpdateDTO analysisDTO = new AnalysisUpdateDTO();
        analysisDTO.setTypeId(CommonAnalysisType.COHORT_CHARACTERIZATION.name());

        testUpdate(analysisDTO, UPDATED_ANALYSIS_TYPE_JSON_OBJECT, "Cohort Characterization");
    }

    private void testUpdate(AnalysisUpdateDTO analysisDTO, JSONObject expected, String newValue) throws Exception {

        MvcResult mvcResult = mvc.perform(
                put("/api/v1/analysis-management/analyses/" + ANALYSIS_ID)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(analysisDTO)))
                .andExpect(OK_STATUS)
                .andExpect(NO_ERROR_CODE)
                .andReturn();
        JSONObject result = getResultJSONObject(mvcResult);
        JSONAssert.assertEquals(expected, result, false);
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetups({
            @DatabaseSetup("/data/study-with-contributor.xml"),
            @DatabaseSetup("/data/analysis/analysis-after-title-updating.xml")
    })
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/study-with-contributor.xml", assertionMode = NON_STRICT),
            @ExpectedDatabase(value = "/data/analysis/analysis-after-title-updating.xml", assertionMode = NON_STRICT)
    })
    public void testGetAnalysis() throws Exception {

        MvcResult mvcResult = mvc.perform(
                get("/api/v1/analysis-management/analyses/" + ANALYSIS_ID))
                .andExpect(OK_STATUS)
                .andExpect(NO_ERROR_CODE)
                .andReturn();

        JSONAssert.assertEquals(UPDATED_ANALYSIS_JSON_OBJECT, getResultJSONObject(mvcResult), false);
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetups({
            @DatabaseSetup("/data/study-with-contributor.xml"),
            @DatabaseSetup("/data/analysis/analysis-list.xml")
    })
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/study-with-contributor.xml", assertionMode = NON_STRICT),
            @ExpectedDatabase(value = "/data/analysis/analysis-list.xml", assertionMode = NON_STRICT)
    })
    public void testGetAnalysisList() throws Exception {

        mvc.perform(
                get("/api/v1/analysis-management/analyses?study-id=" + STUDY_ID))
                .andExpect(OK_STATUS)
                .andExpect(NO_ERROR_CODE)
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result", hasSize(3)));
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetups({
            @DatabaseSetup("/data/study-with-contributor.xml"),
            @DatabaseSetup("/data/analysis/analysis-list.xml")
    })
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/study-with-contributor.xml", assertionMode = NON_STRICT),
            @ExpectedDatabase(value = "/data/analysis/analysis-after-deleting.xml", assertionMode = NON_STRICT),
    })
    public void testDeleteAnalysis() throws Exception {

        mvc.perform(
                delete("/api/v1/analysis-management/analyses/" + ANALYSIS_ID))
                .andExpect(NO_ERROR_CODE);
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetups({
            @DatabaseSetup("/data/study-with-contributor.xml"),
            @DatabaseSetup("/data/analysis/analysis-list.xml")
    })
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/study-with-contributor.xml", assertionMode = NON_STRICT),
            @ExpectedDatabase(value = "/data/analysis/code-file.xml", assertionMode = NON_STRICT),
    })
    public void testUploadCodeFile() throws Exception {

        FileInputStream fileInputStream = new FileInputStream(this.getClass().getResource("/test.jpg").getPath());
        MockMultipartFile multipartFile = new MockMultipartFile("files", "test.jpg", "image/jpeg", fileInputStream);
        this.mvc.perform(
                fileUpload("/api/v1/analysis-management/analyses/{analysisId}/upload", ANALYSIS_ID)
                        .file(multipartFile)
                        .contentType(MULTIPART_FORM_DATA))
                .andExpect(NO_ERROR_CODE)
                .andExpect(OK_STATUS);
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetups({
            @DatabaseSetup("/data/study-with-contributor.xml"),
            @DatabaseSetup("/data/analysis/analysis-list.xml"),
            @DatabaseSetup("/data/analysis/code-file-before-deleting.xml")
    })
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/study-with-contributor.xml", assertionMode = NON_STRICT),
            @ExpectedDatabase(value = "/data/analysis/code-file-after-deleting.xml", assertionMode = NON_STRICT),
    })
    public void testDeleteCodeFile() throws Exception {

        String uuid = "68b75ac9-ab29-49a6-8edb-95142456f5fc";
        mvc.perform(
                delete("/api/v1/analysis-management/analyses/{analysisId}/code-files/{fileUuid}", ANALYSIS_ID, uuid))
                .andExpect(NO_ERROR_CODE);
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetups({
            @DatabaseSetup("/data/study-with-contributor.xml"),
            @DatabaseSetup("/data/analysis/analysis-list.xml"),
            @DatabaseSetup("/data/analysis/code-file-before-deleting.xml")
    })
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/study-with-contributor.xml", assertionMode = NON_STRICT),
            @ExpectedDatabase(value = "/data/analysis/code-file-after-updating.xml", assertionMode = NON_STRICT),
    })
    public void testUpdateCodeFile() throws Exception {

        String uuid = "68b75ac9-ab29-49a6-8edb-95142456f5fc";

        String path = this.getClass().getResource("/test.jpg").getPath();
        FileInputStream fileInputStream = new FileInputStream(path);
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test_updated.jpg", "image/jpeg", fileInputStream);

        MockMultipartHttpServletRequestBuilder builder = fileUpload(
                "/api/v1/analysis-management/analyses/{analysisId}/files/{fileUuid}", ANALYSIS_ID, uuid);
        builder.with(request -> {
            request.setMethod("PUT");
            return request;
        });

        mvc.perform(builder
                .file(multipartFile)
                .param("label", "labelUploadedFile")
                .param("file", path)
                .contentType(MULTIPART_FORM_DATA))
                .andExpect(NO_ERROR_CODE)
                .andExpect(TRUE_RESULT);
    }

}
