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
 * Created: June 06, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonAuthenticationRequest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;

@RunWith(SpringRunner.class)
@DatabaseSetup("/data/users.xml")
@ExpectedDatabase(value = "/data/users.xml", assertionMode = DatabaseAssertionMode.NON_STRICT)
@DatabaseTearDown(value = "/data/empty.xml", type = DatabaseOperation.DELETE_ALL)
public class AuthControllerTests extends BaseControllerTest {

    @Value("${arachne.token.header}")
    private String tokenHeader;

    @Test
    public void testLogin() throws Exception {

        Assert.assertNotNull(login());
    }

    private String login() throws Exception {

        CommonAuthenticationRequest authenticationRequest = new CommonAuthenticationRequest();
        authenticationRequest.setUsername(ADMIN_EMAIL);
        authenticationRequest.setPassword("password");

        MvcResult mvcResult = mvc.perform(
                post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(authenticationRequest)))
                .andReturn();

        return getResultJSONObject(mvcResult).getString("token");
    }

    @Test
    public void testRefreshToken() throws Exception {

        String authToken = login();

        MvcResult mvcResult = mvc.perform(
                post("/api/v1/auth/refresh")
                        .header(tokenHeader, authToken)
                        .contentType(APPLICATION_JSON))
                .andExpect(NO_ERROR_CODE)
                .andReturn();

        authToken = getResponse(mvcResult).getString("result");

        mvc.perform(
                get("/api/v1/test")
                        .header(tokenHeader, authToken)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testLogout() throws Exception {

        String authToken = login();

        mvc.perform(
                post("/api/v1/auth/logout")
                        .header(tokenHeader, authToken)
                        .contentType(APPLICATION_JSON))
                .andExpect(NO_ERROR_CODE);

        mvc.perform(
                get("/api/v1/test")
                        .header(tokenHeader, authToken)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}