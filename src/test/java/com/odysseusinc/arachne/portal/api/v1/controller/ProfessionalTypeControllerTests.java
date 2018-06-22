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
import com.odysseusinc.arachne.commons.api.v1.dto.CommonProfessionalTypeDTO;
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
@ExpectedDatabase(table = "users", value = "/data/users-without-external-dependency.xml", assertionMode = NON_STRICT)
@DatabaseTearDown(value = "/data/empty.xml", type = DatabaseOperation.DELETE_ALL)
public class ProfessionalTypeControllerTests extends BaseControllerTest {

    private static final Long PROFESSIONAL_TYPE_ID = 2L;
    private static final String PROFESSIONAL_TYPE_NAME = "Finance";
    private static final String UPDATED_PROFESSIONAL_TYPE_NAME = "IT Professional";

    private final JSONObject PROFESSIONAL_TYPE_JSON_OBJECT = new JSONObject()
            .put("name", PROFESSIONAL_TYPE_NAME);

    private final JSONObject UPDATED_PROFESSIONAL_TYPE_JSON_OBJECT = new JSONObject()
            .put("id", PROFESSIONAL_TYPE_ID)
            .put("name", UPDATED_PROFESSIONAL_TYPE_NAME);

    public ProfessionalTypeControllerTests() throws JSONException {

    }

    @Test
    @DatabaseSetup(value = "/data/professionaltype/empty-professional-type.xml")
    @ExpectedDatabase(table = "users_data", value = "/data/users-without-external-dependency.xml", assertionMode = NON_STRICT)
    public void testCreateProfessionalType() throws Exception {

        CommonProfessionalTypeDTO professionalTypeDTO = new CommonProfessionalTypeDTO();
        professionalTypeDTO.setName(PROFESSIONAL_TYPE_NAME);

        MvcResult mvcResult = mvc.perform(
                post("/api/v1/admin/professional-types/")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(professionalTypeDTO)))
                .andExpect(jsonPath("$.result.id").isNotEmpty())
                .andExpect(OK_STATUS)
                .andExpect(NO_ERROR_CODE)
                .andReturn();

        JSONAssert.assertEquals(PROFESSIONAL_TYPE_JSON_OBJECT, getResultJSONObject(mvcResult), false);
    }

    @Test
    @DatabaseSetup(value = "/data/professionaltype/professional-type-before-updating.xml")
    @ExpectedDatabase(table = "professional_types", value = "/data/professionaltype/professional-type-after-updating.xml", assertionMode = NON_STRICT)
    public void testUpdateProfessionalType() throws Exception {

        CommonProfessionalTypeDTO professionalTypeDTO = new CommonProfessionalTypeDTO();
        professionalTypeDTO.setName(UPDATED_PROFESSIONAL_TYPE_NAME);
        professionalTypeDTO.setId(PROFESSIONAL_TYPE_ID);

        MvcResult mvcResult = mvc.perform(
                put("/api/v1/admin/professional-types/" + PROFESSIONAL_TYPE_ID)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(professionalTypeDTO)))
                .andExpect(jsonPath("$.result.id").isNotEmpty())
                .andExpect(OK_STATUS)
                .andExpect(NO_ERROR_CODE)
                .andReturn();

        JSONAssert.assertEquals(UPDATED_PROFESSIONAL_TYPE_JSON_OBJECT, getResultJSONObject(mvcResult), false);
    }

    @Test
    @DatabaseSetup("/data/professionaltype/professional-type-after-updating.xml")
    @ExpectedDatabase(table = "professional_types", value = "/data/professionaltype/professional-type-after-updating.xml", assertionMode = NON_STRICT)
    public void testGetProfessionalType() throws Exception {

        MvcResult mvcResult = mvc.perform(
                get("/api/v1/user-management/professional-types/" + PROFESSIONAL_TYPE_ID))
                .andExpect(OK_STATUS)
                .andExpect(NO_ERROR_CODE)
                .andReturn();
        JSONAssert.assertEquals(UPDATED_PROFESSIONAL_TYPE_JSON_OBJECT, getResultJSONObject(mvcResult), false);
    }

    @Test
    @DatabaseSetup("/data/professionaltype/professional-type-list.xml")
    @ExpectedDatabase(table = "professional_types", value = "/data/professionaltype/professional-type-list.xml", assertionMode = NON_STRICT)
    public void testGetProfessionalTypeList() throws Exception {

        mvc.perform(
                get("/api/v1/user-management/professional-types/"))
                .andExpect(OK_STATUS)
                .andExpect(NO_ERROR_CODE)
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result", hasSize(3)));
    }

    @Test
    @DatabaseSetup("/data/professionaltype/professional-type-list.xml")
    @ExpectedDatabase(table = "professional_types", value = "/data/professionaltype/professional-types-after-deleting.xml", assertionMode = NON_STRICT)
    public void testDeleteProfessionalType() throws Exception {

        mvc.perform(
                delete("/api/v1/admin/professional-types/" + PROFESSIONAL_TYPE_ID))
                .andExpect(NO_ERROR_CODE)
                .andReturn();
    }

}
