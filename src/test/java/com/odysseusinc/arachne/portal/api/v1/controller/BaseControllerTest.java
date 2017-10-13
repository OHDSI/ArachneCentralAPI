/**
 *
 * Copyright 2017 Observational Health Data Sciences and Informatics
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

import static com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult.ErrorCode.NO_ERROR;
import static com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult.ErrorCode.PERMISSION_DENIED;
import static java.lang.Boolean.TRUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.odysseusinc.arachne.portal.PortalStarter;
import java.io.UnsupportedEncodingException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

@SuppressWarnings(value = "unchecked")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application.properties")
@SpringBootTest(classes = PortalStarter.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class,
        WithSecurityContextTestExecutionListener.class})
@DbUnitConfiguration(databaseConnection = {"primaryDataSource"})
public class BaseControllerTest {

    static final CharacterEncodingFilter CHARACTER_ENCODING_FILTER = new CharacterEncodingFilter();

    static {
        CHARACTER_ENCODING_FILTER.setEncoding("UTF-8");
        CHARACTER_ENCODING_FILTER.setForceEncoding(true);
    }

    static final String ADMIN_EMAIL = "admin@odysseusinc.com";
    static final Long ADMIN_ID = 1L;

    static final String FIRST_NAME = "firstName";
    static final String LAST_NAME = "lastName";

    @Autowired
    WebApplicationContext wac;
    @Autowired
    ObjectMapper objectMapper;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    MockMvc mvc;
    final static ResultMatcher NO_ERROR_CODE = jsonPath("$.errorCode").value(NO_ERROR.getCode());
    final static ResultMatcher PERMISSION_DENIED_CODE = jsonPath("$.errorCode").value(PERMISSION_DENIED.getCode());
    final static ResultMatcher OK_STATUS = status().isOk();
    final static ResultMatcher TRUE_RESULT = jsonPath("$.result").value(TRUE);

    static JSONObject ADMIN_DTO;

    static {
        try {
            ADMIN_DTO = new JSONObject().put("id", ADMIN_ID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Before
    public void before() throws Exception {

        mvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(SecurityMockMvcConfigurers.springSecurity())

                .addFilter(CHARACTER_ENCODING_FILTER)
                .build();
    }

    JSONObject getResponse(MvcResult mvcResult) throws UnsupportedEncodingException, JSONException {

        return new JSONObject(mvcResult.getResponse().getContentAsString());
    }

    JSONArray getResponeJSONArray(MvcResult mvcResult) throws UnsupportedEncodingException, JSONException {

        return new JSONArray(mvcResult.getResponse().getContentAsString());
    }

    JSONObject getResultJSONObject(MvcResult mvcResult) throws UnsupportedEncodingException, JSONException {

        JSONObject result = getResponse(mvcResult);
        return (JSONObject) result.get("result");
    }

    JSONArray getResultJSONArray(MvcResult mvcResult) throws UnsupportedEncodingException, JSONException {

        JSONObject result = getResponse(mvcResult);
        return (JSONArray) result.get("result");
    }

    JSONObject getGeneral(MvcResult mvcResult) throws UnsupportedEncodingException, JSONException {

        return (JSONObject) getResultJSONObject(mvcResult).get("general");
    }


}
