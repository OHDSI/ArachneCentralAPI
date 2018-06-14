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
 * Created: September 14, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.controller;

import static com.github.springtestdbunit.assertion.DatabaseAssertionMode.NON_STRICT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseSetups;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.odysseusinc.arachne.portal.api.v1.dto.CreatePaperDTO;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;

@RunWith(SpringRunner.class)
@DatabaseSetups({
        @DatabaseSetup("/data/users.xml"),
        @DatabaseSetup("/data/study-with-contributor.xml")
})
@DatabaseTearDown(value = "/data/empty.xml", type = DatabaseOperation.DELETE_ALL)
public class BasePaperControllerTest extends BaseControllerTest {

    private final JSONObject PRIVATE_PAPER = new JSONObject()
            .put("study", new JSONObject()
                    .put("title", "Title")
                    .put("description", "Description")
            );

    public BasePaperControllerTest() throws JSONException {

    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @ExpectedDatabase(value = "/data/paper/papers-after-creation.xml", assertionMode = NON_STRICT)
    public void testCreatePaper() throws Exception {

        final CreatePaperDTO createPaperDTO = new CreatePaperDTO();
        createPaperDTO.setStudyId(1L);
        MvcResult mvcResult = mvc.perform(
                post("/api/v1/papers")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(createPaperDTO)))
                .andExpect(OK_STATUS)
                .andReturn();
    }
}
