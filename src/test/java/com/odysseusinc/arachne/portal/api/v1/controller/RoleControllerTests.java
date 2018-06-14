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
import static com.github.springtestdbunit.assertion.DatabaseAssertionMode.NON_STRICT_UNORDERED;
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
import com.odysseusinc.arachne.portal.api.v1.dto.dictionary.RoleDTO;
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
public class RoleControllerTests extends BaseControllerTest {

    private static final Long ROLE_ID = 1L;
    private static final String ROLE_NAME = "USER_ROLE";
    private static final String ROLE_DESCRIPTION = "User role";

    private static final String UPDATED_ROLE_NAME = "SUPER_ADMIN";

    private final JSONObject ROLE_JSON_OBJECT = new JSONObject()
            .put("name", ROLE_NAME)
            .put("description", ROLE_DESCRIPTION);

    private final JSONObject UPDATED_ROLE_JSON_OBJECT = new JSONObject()
            .put("id", ROLE_ID)
            .put("description", ROLE_DESCRIPTION)
            .put("name", UPDATED_ROLE_NAME);

    public RoleControllerTests() throws JSONException {

    }

    @Test
    @DatabaseSetup(value = "/data/role/empty-role.xml")
    @ExpectedDatabase(table = "roles", value = "/data/role/added-role.xml", assertionMode = NON_STRICT)
    public void testRole() throws Exception {

        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setName(ROLE_NAME);
        roleDTO.setDescription(ROLE_DESCRIPTION);

        MvcResult mvcResult = mvc.perform(
                post("/api/v1/admin/roles/")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(roleDTO)))

                .andExpect(jsonPath("$.result.id").isNotEmpty())
                .andExpect(OK_STATUS)
                .andExpect(NO_ERROR_CODE)
                .andReturn();

        JSONAssert.assertEquals(ROLE_JSON_OBJECT, getResultJSONObject(mvcResult), false);
    }

    @Test
    @DatabaseSetup(value = "/data/role/role-before-updating.xml")
    @ExpectedDatabase(table = "roles", value = "/data/role/roles.xml", assertionMode = NON_STRICT_UNORDERED)
    public void testUpdateRole() throws Exception {

        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(ROLE_ID);
        roleDTO.setName(UPDATED_ROLE_NAME);
        roleDTO.setDescription(ROLE_DESCRIPTION);

        MvcResult mvcResult = mvc.perform(
                put("/api/v1/admin/roles/" + ROLE_ID)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(roleDTO)))
                .andExpect(jsonPath("$.result.id").isNotEmpty())
                .andExpect(OK_STATUS)
                .andExpect(NO_ERROR_CODE)
                .andReturn();

        JSONAssert.assertEquals(UPDATED_ROLE_JSON_OBJECT, getResultJSONObject(mvcResult), false);
    }

    @Test
    @DatabaseSetup(value = "/data/role/roles.xml")
    @ExpectedDatabase(table = "roles", value = "/data/role/roles.xml", assertionMode = NON_STRICT)
    public void testGetRole() throws Exception {

        MvcResult mvcResult = mvc.perform(
                get("/api/v1/admin/roles/" + ROLE_ID))
                .andExpect(OK_STATUS)
                .andExpect(NO_ERROR_CODE)
                .andReturn();
        JSONAssert.assertEquals(UPDATED_ROLE_JSON_OBJECT, getResultJSONObject(mvcResult), false);
    }

    @Test
    @DatabaseSetup(value = "/data/role/roles.xml")
    @ExpectedDatabase(table = "roles", value = "/data/role/roles.xml", assertionMode = NON_STRICT)
    public void testGetRoles() throws Exception {

        mvc.perform(
                get("/api/v1/admin/roles?page=0&pagesize=10"))
                .andExpect(OK_STATUS)
                .andExpect(NO_ERROR_CODE)
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result", hasSize(3)));
    }

    @Test
    @DatabaseSetup(value = "/data/role/roles.xml")
    @ExpectedDatabase(table = "roles", value = "/data/role/roles-after-deleting.xml", assertionMode = NON_STRICT)
    public void testDeleteRole() throws Exception {

        this.mvc.perform(
                delete("/api/v1/admin/roles/" + ROLE_ID))
                .andExpect(NO_ERROR_CODE)
                .andExpect(TRUE_RESULT);
    }

}
