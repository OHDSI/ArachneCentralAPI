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
import static com.odysseusinc.arachne.portal.api.v1.controller.BaseControllerTest.ADMIN_EMAIL;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.odysseusinc.arachne.portal.api.v1.dto.dictionary.CreateStudyTypeDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.dictionary.StudyTypeDTO;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;

@RunWith(SpringRunner.class)
@WithUserDetails(value = ADMIN_EMAIL)
@DatabaseSetup("/data/users-without-external-dependency.xml")
@ExpectedDatabase(value = "/data/users-without-external-dependency.xml", assertionMode = NON_STRICT)
@DatabaseTearDown(value = "/data/empty.xml", type = DatabaseOperation.DELETE_ALL)
public class StudyTypeControllerTests extends BaseControllerTest {

    private static final Long ID = 1L;
    private static final String NAME = "studyTypeName";
    private static final String UPDATED_NAME = "updatedName";
    private final JSONObject STUDY_TYPE_JSON_OBJECT = new JSONObject()
            .put("name", NAME);

    private final JSONObject UPDATED_STUDY_TYPE_JSON_OBJECT = new JSONObject()
            .put("id", ID)
            .put("name", UPDATED_NAME);

    public StudyTypeControllerTests() throws JSONException {

    }

    @Test
    @DatabaseSetup("/data/study/type/empty-study-type.xml")
    @ExpectedDatabase(value = "/data/study/type/added-study-type.xml", assertionMode = NON_STRICT)
    public void testCreateStudyType() throws Exception {

        CreateStudyTypeDTO dto = new CreateStudyTypeDTO();
        dto.setName(NAME);

        MvcResult mvcResult = mvc.perform(
                post("/api/v1/admin/study-types")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(dto)))

                .andExpect(jsonPath("$.result.id").isNotEmpty())
                .andExpect(OK_STATUS)
                .andReturn();

        JSONAssert.assertEquals(STUDY_TYPE_JSON_OBJECT, getResultJSONObject(mvcResult), false);
    }

    @Test
    @DatabaseSetup("/data/study/type/study-type-before-updating.xml")
    @ExpectedDatabase(value = "/data/study/type/study-type-after-updating.xml", assertionMode = NON_STRICT)
    public void testUpdateStudyType() throws Exception {

        StudyTypeDTO dto = new StudyTypeDTO(ID);
        dto.setName(UPDATED_NAME);

        MvcResult mvcResult = mvc.perform(
                put("/api/v1/admin/study-types/" + ID)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(dto)))
                .andExpect(jsonPath("$.result.id").isNotEmpty())
                .andExpect(OK_STATUS)
                .andReturn();

        JSONAssert.assertEquals(UPDATED_STUDY_TYPE_JSON_OBJECT, getResultJSONObject(mvcResult), false);
    }

    @Test
    @DatabaseSetup("/data/study/type/study-type-after-updating.xml")
    @ExpectedDatabase(value = "/data/study/type/study-type-after-updating.xml", assertionMode = NON_STRICT)
    public void testGetStudyType() throws Exception {

        MvcResult mvcResult = mvc.perform(
                get("/api/v1/study-management/study-types/" + ID))
                .andExpect(OK_STATUS)
                .andReturn();
        JSONAssert.assertEquals(UPDATED_STUDY_TYPE_JSON_OBJECT, getResultJSONObject(mvcResult), false);
    }

    @Test
    @DatabaseSetup("/data/study/type/study-types.xml")
    @ExpectedDatabase(value = "/data/study/type/study-types.xml", assertionMode = NON_STRICT)
    public void testGetStudyTypes() throws Exception {

        mvc.perform(
                get("/api/v1/study-management/study-types?page=0&pagesize=10"))
                .andExpect(OK_STATUS)
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result", hasSize(3)));
    }

    @Test
    @DatabaseSetup("/data/study/type/study-types.xml")
    @ExpectedDatabase(value = "/data/study/type/study-types-after-deleting.xml", assertionMode = NON_STRICT)
    public void testDeleteStudyType() throws Exception {

        this.mvc.perform(
                delete("/api/v1/admin/study-types/" + ID));
    }

}
