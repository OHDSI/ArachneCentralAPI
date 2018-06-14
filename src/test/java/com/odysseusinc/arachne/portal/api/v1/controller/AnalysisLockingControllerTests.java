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
 * Created: June 15, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.controller;

import static com.github.springtestdbunit.assertion.DatabaseAssertionMode.NON_STRICT;
import static com.github.springtestdbunit.assertion.DatabaseAssertionMode.NON_STRICT_UNORDERED;
import static java.lang.Boolean.TRUE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseSetups;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.annotation.ExpectedDatabases;
import com.odysseusinc.arachne.portal.api.v1.dto.AnalysisLockDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.AnalysisUnlockRequestDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.InvitationActionDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.InvitationType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DatabaseTearDown(value = "/data/empty.xml", type = DatabaseOperation.DELETE_ALL)
public class AnalysisLockingControllerTests extends BaseControllerTest {

    private static final Long STUDY_ID = 1L;
    private static final Long ANALYSIS_ID = 1L;

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetups({
            @DatabaseSetup("/data/users.xml"),
            @DatabaseSetup("/data/study-with-contributor.xml"),
            @DatabaseSetup("/data/analysis/analysis-list.xml")
    })
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/users.xml", assertionMode = NON_STRICT),
            @ExpectedDatabase(value = "/data/study-with-contributor.xml", assertionMode = NON_STRICT),
            @ExpectedDatabase(value = "/data/analysis/analysis-after-locking.xml", assertionMode = NON_STRICT),
    })
    public void testLockAnalysis() throws Exception {

        AnalysisLockDTO lockFileDTO = new AnalysisLockDTO();
        lockFileDTO.setLocked(TRUE);

        mvc.perform(
                post("/api/v1/analysis-management/analyses/{analysisId}/lock", ANALYSIS_ID)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(lockFileDTO)))
                .andExpect(NO_ERROR_CODE);
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetups({
            @DatabaseSetup("/data/study/study-participant-with-contributor-and-leader-before.xml"),
            @DatabaseSetup("/data/analysis/analysis-after-locking.xml")
    })
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/study/study-participant-with-contributor-and-leader-before.xml", assertionMode = NON_STRICT),
            @ExpectedDatabase(value = "/data/analysis/analysis-after-locking.xml", assertionMode = NON_STRICT),
            @ExpectedDatabase(value = "/data/analysis/analysis-unlock-requests.xml", assertionMode = NON_STRICT),
    })
    public void testUnlockRequest() throws Exception {

        AnalysisUnlockRequestDTO requestDTO = new AnalysisUnlockRequestDTO();
        requestDTO.setDescription("please unlock");

        mvc.perform(
                post("/api/v1/analysis-management/analyses/{analysisId}/unlock-request", ANALYSIS_ID)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(requestDTO)))
                .andExpect(NO_ERROR_CODE);
    }

    @Test
    @WithUserDetails(value = "user@mail.com")
    @DatabaseSetups({
            @DatabaseSetup("/data/study/study-participant-with-contributor-and-leader-before.xml"),
            @DatabaseSetup("/data/analysis/analysis-after-locking.xml"),
            @DatabaseSetup("/data/analysis/analysis-unlock-requests-before-processing.xml")
    })
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/study/study-participant-with-contributor-and-leader-before.xml", assertionMode = NON_STRICT),
            @ExpectedDatabase(value = "/data/analysis/analysis-list.xml", assertionMode = NON_STRICT),
            @ExpectedDatabase(value = "/data/analysis/analysis-unlock-requests-after-processing.xml", assertionMode = NON_STRICT),
    })
    public void testProcessUnlockRequest() throws Exception {

        InvitationActionDTO actionDTO = new InvitationActionDTO();
        actionDTO.setAccepted(true);
        actionDTO.setId(30L);
        actionDTO.setType(InvitationType.UNLOCK_ANALYSIS);

        mvc.perform(
                post("/api/v1/user-management/users/invitations")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(actionDTO)))
                .andExpect(NO_ERROR_CODE);
    }

}
