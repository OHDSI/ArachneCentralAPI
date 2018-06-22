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
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseSetups;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.annotation.ExpectedDatabases;
import com.odysseusinc.arachne.commons.utils.UserIdUtils;
import com.odysseusinc.arachne.portal.api.v1.dto.CreateVirtualDataSourceDTO;
import java.util.Arrays;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DatabaseSetups(value = {
        @DatabaseSetup("/data/users.xml"),
        @DatabaseSetup("/data/study-with-contributor.xml"),
})
@ExpectedDatabases({
        @ExpectedDatabase(value = "/data/users.xml", assertionMode = NON_STRICT),
        @ExpectedDatabase(value = "/data/study-with-contributor.xml", assertionMode = NON_STRICT)
})
@DatabaseTearDown(value = "/data/empty.xml", type = DatabaseOperation.DELETE_ALL)
public class StudyDataSourceControllerTests extends BaseControllerTest {

    private static final String STUDY_LEAD = "admin@odysseusinc.com";
    private static final long STUDY_ID = 1L;
    private static final long PUBLIC_UNSUPERVISED_DATA_SOURCE_ID = 1L;
    private static final long PRIVATE_UNSUPERVISED_DATASOURCE_ID = 3L;

    public StudyDataSourceControllerTests() throws JSONException {

    }

    @Test
    @WithUserDetails(STUDY_LEAD)
    @DatabaseSetup("/data/datanode-with-datasources.xml")
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/datanode-with-datasources.xml", assertionMode = NON_STRICT),
            @ExpectedDatabase(value = "/data/study/datasource/study-data-source1-1.xml", assertionMode = NON_STRICT),
    })
    public void testAddDataSource() throws Exception {

        mvc.perform(
                post("/api/v1/study-management/studies/{studyId}/data-sources/{dataSourceId}",
                        STUDY_ID, PUBLIC_UNSUPERVISED_DATA_SOURCE_ID)
        ).andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(STUDY_LEAD)
    @ExpectedDatabase(value = "/data/study/datasource/study-data-source1-virtual.xml", assertionMode = NON_STRICT)
    public void testAddVirtualDataSource() throws Exception {

        final CreateVirtualDataSourceDTO createVirtualDataSourceDTO = new CreateVirtualDataSourceDTO();
        createVirtualDataSourceDTO.setName("virtual");
        createVirtualDataSourceDTO.setDataOwnersIds(Arrays.asList(UserIdUtils.idToUuid(1l)));
        mvc.perform(
                post("/api/v1/study-management/studies/{studyId}/data-sources",
                        STUDY_ID)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(createVirtualDataSourceDTO))
        ).andExpect(NO_ERROR_CODE);
    }

    @Test
    @WithUserDetails(STUDY_LEAD)
    @DatabaseSetups({
            @DatabaseSetup("/data/datanode-with-datasources.xml"),
            @DatabaseSetup("/data/study/datasource/study-data-source-before-deleting.xml")
    })
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/datanode-with-datasources.xml", assertionMode = NON_STRICT),
            @ExpectedDatabase(value = "/data/study/datasource/study-data-source-after-deleting.xml", assertionMode = NON_STRICT),
    })
    public void testPublicDataSource() throws Exception {

        mvc.perform(
                delete("/api/v1/study-management/studies/{studyId}/data-sources/{dataSourceId}",
                        STUDY_ID, PUBLIC_UNSUPERVISED_DATA_SOURCE_ID)
        ).andExpect(NO_ERROR_CODE);
    }
}
