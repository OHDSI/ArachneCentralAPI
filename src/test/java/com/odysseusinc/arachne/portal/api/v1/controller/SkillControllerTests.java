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
import com.odysseusinc.arachne.portal.api.v1.dto.dictionary.SkillDTO;
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
@DatabaseSetup("/data/users.xml")
@ExpectedDatabase(value = "/data/users.xml", assertionMode = NON_STRICT)
@DatabaseTearDown(value = "/data/empty.xml", type = DatabaseOperation.DELETE_ALL)
public class SkillControllerTests extends BaseControllerTest {

    private static final Long SKILL_ID = 1L;
    private static final String SKILL_NAME = "Levitation";

    private static final String UPDATED_SKILL_NAME = "Reading";

    private final JSONObject SKILL_JSON_OBJECT = new JSONObject()
            .put("name", SKILL_NAME);

    private final JSONObject UPDATED_SKILL_JSON_OBJECT = new JSONObject()
            .put("id", SKILL_ID)
            .put("name", UPDATED_SKILL_NAME);

    public SkillControllerTests() throws JSONException {

    }

    @Test
    @DatabaseSetup("/data/skill/empty-skill.xml")
    @ExpectedDatabase(value = "/data/skill/added-skill.xml", assertionMode = NON_STRICT)
    public void testCreateSkill() throws Exception {

        SkillDTO skillDTO = new SkillDTO();
        skillDTO.setName(SKILL_NAME);

        MvcResult mvcResult = mvc.perform(
                post("/api/v1/user-management/skills/")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(skillDTO)))

                .andExpect(jsonPath("$.result.id").isNotEmpty())
                .andExpect(OK_STATUS)
                .andExpect(NO_ERROR_CODE)
                .andReturn();

        JSONAssert.assertEquals(SKILL_JSON_OBJECT, getResultJSONObject(mvcResult), false);
    }

    @Test
    @DatabaseSetup("/data/skill/skill-before-updating.xml")
    @ExpectedDatabase(value = "/data/skill/skill-after-updating.xml", assertionMode = NON_STRICT)
    public void testUpdateSkill() throws Exception {

        SkillDTO skillDTO = new SkillDTO();
        skillDTO.setId(SKILL_ID);
        skillDTO.setName(UPDATED_SKILL_NAME);

        MvcResult mvcResult = mvc.perform(
                put("/api/v1/admin/skills/" + SKILL_ID)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(skillDTO)))
                .andExpect(jsonPath("$.result.id").isNotEmpty())
                .andExpect(OK_STATUS)
                .andExpect(NO_ERROR_CODE)
                .andReturn();

        JSONAssert.assertEquals(UPDATED_SKILL_JSON_OBJECT, getResultJSONObject(mvcResult), false);
    }

    @Test
    @DatabaseSetup("/data/skill/skill-after-updating.xml")
    @ExpectedDatabase(value = "/data/skill/skill-after-updating.xml", assertionMode = NON_STRICT)
    public void testGetSkill() throws Exception {

        MvcResult mvcResult = mvc.perform(
                get("/api/v1/user-management/skills/" + SKILL_ID))
                .andExpect(OK_STATUS)
                .andExpect(NO_ERROR_CODE)
                .andReturn();
        JSONAssert.assertEquals(UPDATED_SKILL_JSON_OBJECT, getResultJSONObject(mvcResult), false);
    }

    @Test
    @DatabaseSetup("/data/skill/skills.xml")
    @ExpectedDatabase(value = "/data/skill/skills.xml", assertionMode = NON_STRICT)
    public void testGetSkills() throws Exception {

        mvc.perform(
                get("/api/v1/user-management/skills?page=0&pagesize=10"))
                .andExpect(OK_STATUS)
                .andExpect(NO_ERROR_CODE)
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result", hasSize(3)));
    }

    @Test
    @DatabaseSetup("/data/skill/skills.xml")
    @ExpectedDatabase(value = "/data/skill/skills.xml", assertionMode = NON_STRICT)
    public void testSuggestSkills() throws Exception {

        mvc.perform(
                get("/api/v1/user-management/skills/search?query=re&limit=10"))
                .andExpect(OK_STATUS)
                .andExpect(NO_ERROR_CODE)
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result", hasSize(2)));
    }


    @Test
    @DatabaseSetup("/data/skill/skills.xml")
    @ExpectedDatabase(value = "/data/skill/skills-after-deleting.xml", assertionMode = NON_STRICT)
    public void testDeleteSkill() throws Exception {

        this.mvc.perform(
                delete("/api/v1/admin/skills/" + SKILL_ID))
                .andExpect(NO_ERROR_CODE)
                .andExpect(TRUE_RESULT);
    }

}
