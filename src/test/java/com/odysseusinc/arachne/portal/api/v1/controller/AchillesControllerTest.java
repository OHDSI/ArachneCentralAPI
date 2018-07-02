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
 * Created: May 25, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.odysseusinc.arachne.portal.model.achilles.AchillesFile;
import com.odysseusinc.arachne.portal.repository.AchillesFileRepository;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@DatabaseTearDown(value = "/data/empty.xml", type = DatabaseOperation.DELETE_ALL)
public class AchillesControllerTest extends BaseControllerTest {
    private static final String API_DATASOURCE = "/api/v1/achilles/datasource/";

    private static final String API_REPORTS = "/api/v1/achilles/datasource/%s/reports";
    private static final String API_FILES = "/api/v1/achilles/datasource/%s/files/%s";
    private static final int REPORTS_COUNT = 13;
    private static final String DASHBOARD_JSON = "{\"SUMMARY\":{\"ATTRIBUTE_NAME\":[\"Source name\","
            + "\"Number of persons\"],\"ATTRIBUTE_VALUE\":[\"sample1\",\"116352\"]},"
            + "\"GENDER_DATA\":{\"CONCEPT_ID\":[8532,8507],\"CONCEPT_NAME\":[\"FEMALE\","
            + "\"MALE\"],\"COUNT_VALUE\":[64347,52005]},\"AGE_AT_FIRST_OBSERVATION_HISTOGRAM\":{\"MIN\":0,"
            + "\"MAX\":100,\"INTERVAL_SIZE\":1,\"INTERVALS\":100,\"DATA\":{\"INTERVAL_INDEX\":[],"
            + "\"COUNT_VALUE\":[],\"PERCENT_VALUE\":[]}},\"CUMULATIVE_DURATION\":"
            + "{\"SERIES_NAME\":[],\"X_LENGTH_OF_OBSERVATION\":[],\"Y_PERCENT_PERSONS\":[]},"
            + "\"OBSERVED_BY_MONTH\":{\"MONTH_YEAR\":[],\"COUNT_VALUE\":[],\"PERCENT_VALUE\":[]}}";
    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private AchillesFileRepository achillesFileRepository;
    private final List<String> FILE_NAMES = Lists.newArrayList(
            "achillesheel.json",
            "condition_treemap.json",
            "conditionera_treemap.json",
            "dashboard.json",
            "datadensity.json",
            "death.json",
            "drug_treemap.json",
            "drugera_treemap.json",
            "measurement_treemap.json",
            "observation_treemap.json",
            "observationperiod.json",
            "person.json",
            "procedure_treemap.json",
            "visit_treemap.json");

    private static final String RESTRICTED_DS = "2";
    private static final String PUBLIC_DS = "1";
    private static final String PRIVATE_DS = "3";

    @Test
    @WithUserDetails(value = "admin@odysseusinc.com")
    @DatabaseSetup({
            "/data/achilles/datanode.xml",
            "/data/achilles/users.xml",
            "/data/achilles/studies.xml",
            "/data/achilles/datasources.xml",
            "/data/achilles/reports.xml",
            "/data/achilles/characterizations.xml",
            "/data/achilles/permissions.xml",
    })
    public void getLatestCharacterization() throws Exception {

        final int NUMBER_OF_FILES = FILE_NAMES.size();
        //datasource should return all achilles files
        mvc.perform(get(API_DATASOURCE + PUBLIC_DS)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.result.files", hasSize(NUMBER_OF_FILES)));

    }

    @Test
    @WithUserDetails(value = "admin@odysseusinc.com")
    @DatabaseSetup({
            "/data/achilles/datanode.xml",
            "/data/achilles/users.xml",
            "/data/achilles/studies.xml",
            "/data/achilles/datasources.xml",
            "/data/achilles/reports.xml",
            "/data/achilles/characterizations.xml",
            "/data/achilles/permissions.xml",
    })
    public void listReports() throws Exception {

        mvc.perform(get(String.format(API_REPORTS, PUBLIC_DS))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.result", hasSize(REPORTS_COUNT)));

    }

    @WithUserDetails(value = "admin@odysseusinc.com")
    @DatabaseSetup(value = {
            "/data/achilles/datanode.xml",
            "/data/achilles/users.xml",
            "/data/achilles/studies.xml",
            "/data/achilles/datasources.xml",
            "/data/achilles/reports.xml",
            "/data/achilles/characterizations.xml",
            "/data/achilles/permissions.xml",
            "/data/achilles/study-participants.xml"
    })
    public void getFile() throws Exception {

        Bootstrap bootstrap = new Bootstrap();
        AchillesFile observationFile = achillesFileRepository.findOne(11L);
        assertThat(observationFile, is(notNullValue()));
        observationFile.setData(bootstrap.observationJson());
        AchillesFile dashboardFile = achillesFileRepository.findOne(4L);
        assertThat(dashboardFile, is(notNullValue()));
        dashboardFile.setData(bootstrap.dashboardJson());
        achillesFileRepository.save(Arrays.asList(observationFile, dashboardFile));

        mvc.perform(get(String.format(API_FILES, PUBLIC_DS, "dashboard.json"))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.result.SUMMARY.ATTRIBUTE_NAME[0]",
                        is("Source name")));

        mvc.perform(get(String.format(API_FILES, PUBLIC_DS, "observations/observation_3051227.json"))
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.result.OBSERVATIONS_BY_TYPE.CONCEPT_NAME",
                        is("Lab observation numeric result")));

    }

    class Bootstrap {

        JsonObject dashboardJson() {

            JsonParser parser = new JsonParser();
            return parser.parse(DASHBOARD_JSON).getAsJsonObject();
        }

        JsonObject observationJson() throws IOException {

            JsonParser parser = new JsonParser();
            Resource resource = wac.getResource("classpath:/observation_3051227.json");
            try (final JsonReader reader = new JsonReader(new InputStreamReader(resource.getInputStream()))) {
                return parser.parse(reader).getAsJsonObject();
            }
        }

    }

}