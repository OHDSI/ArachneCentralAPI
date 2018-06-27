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
import static com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult.ErrorCode.ALREADY_EXIST;
import static com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult.ErrorCode.VALIDATION_ERROR;
import static com.odysseusinc.arachne.portal.api.v1.controller.UserControllerTests.USER_2_UUID;
import static com.odysseusinc.arachne.portal.api.v1.dto.InvitationType.COLLABORATOR;
import static com.odysseusinc.arachne.portal.model.ParticipantRole.CONTRIBUTOR;
import static com.odysseusinc.arachne.portal.model.ParticipantRole.DATA_SET_OWNER;
import static com.odysseusinc.arachne.portal.model.ParticipantRole.LEAD_INVESTIGATOR;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Calendar.MILLISECOND;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseSetups;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.annotation.ExpectedDatabases;
import com.odysseusinc.arachne.commons.utils.UserIdUtils;
import com.odysseusinc.arachne.portal.api.v1.dto.AddStudyParticipantDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.CreateStudyDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.InvitationActionDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.StudyDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UpdateParticipantDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.dictionary.StudyStatusDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.dictionary.StudyTypeDTO;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;

@RunWith(SpringRunner.class)
@DatabaseTearDown(value = "/data/empty.xml", type = DatabaseOperation.DELETE_ALL)
public class StudyControllerTests extends BaseControllerTest {

    @Value("${files.store.path}")
    private String fileStorePath;

    private static final String UPDATED_DESCRIPTION = "updatedDescription";

    private static final String STUDY_TITLE = "Test Study";
    private static final Long STUDY_TYPE_ID = 4L;
    private static final Long STUDY_ID = 1L;
    private static final Long STUDY_STATUS_ID = 1L;

    private static final String UPDATED_STUDY_TITLE = "Study Title New";

    private final JSONObject STUDY_JSON_OBJECT = new JSONObject()
            .put("title", STUDY_TITLE)
            .put("type", new JSONObject().put("id", STUDY_TYPE_ID));

    private final JSONObject UPDATED_STUDY_JSON_OBJECT = new JSONObject()
            .put("title", STUDY_TITLE)
            .put("description", UPDATED_DESCRIPTION)
            .put("startDate", DATE.getTime())
            .put("endDate", END_DATE.getTime())
            .put("type", new JSONObject().put("id", STUDY_TYPE_ID));

    private final JSONObject UPDATED_STUDY_TITLE_JSON_OBJECT = new JSONObject()
            .put("title", UPDATED_STUDY_TITLE)
            .put("description", "description")
            .put("startDate", DATE.getTime())
            .put("endDate", END_DATE.getTime())
            .put("type", new JSONObject().put("id", STUDY_TYPE_ID));

    private final JSONObject UPDATED_STUDY_TYPE_JSON_OBJECT = new JSONObject()
            .put("title", STUDY_TITLE)
            .put("description", "description")
            .put("startDate", DATE.getTime())
            .put("endDate", END_DATE.getTime())
            .put("type", new JSONObject().put("id", 1L));

    private final JSONObject UPDATED_STUDY_STATUS_JSON_OBJECT = new JSONObject()
            .put("title", STUDY_TITLE)
            .put("description", "description")
            .put("startDate", DATE.getTime())
            .put("endDate", END_DATE.getTime())
            .put("type", new JSONObject().put("id", STUDY_TYPE_ID))
            .put("status", new JSONObject().put("id", 2L));

    private final JSONObject UPDATED_STUDY_START_DATE_JSON_OBJECT = new JSONObject()
            .put("title", STUDY_TITLE)
            .put("description", "description")
            .put("startDate", UPDATED_DATE.getTime())
            .put("endDate", END_DATE.getTime())
            .put("type", new JSONObject().put("id", STUDY_TYPE_ID))
            .put("status", new JSONObject().put("id", 1L));

    private final JSONObject UPDATED_STUDY_END_DATE_JSON_OBJECT = new JSONObject()
            .put("title", STUDY_TITLE)
            .put("description", "description")
            .put("startDate", DATE.getTime())
            .put("endDate", UPDATED_DATE.getTime())
            .put("type", new JSONObject().put("id", STUDY_TYPE_ID))
            .put("status", new JSONObject().put("id", 1L));

    private static Date DATE;
    private static Date END_DATE;
    private static Date UPDATED_DATE;

    static {
        Calendar c = Calendar.getInstance();
        c.set(2017, Calendar.JANUARY, 1, 0, 0, 0);
        c.clear(MILLISECOND);
        DATE = c.getTime();

        c.set(2018, Calendar.JANUARY, 1, 0, 0, 0);
        c.clear(MILLISECOND);
        END_DATE = c.getTime();
        // todo: fix
        UPDATED_DATE = new GregorianCalendar(2017, 5, 1).getTime();
    }

    public StudyControllerTests() throws JSONException {

    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetup("/data/user/admin-user.xml")
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/study/study.xml", assertionMode = NON_STRICT),
    })
    public void testCreateStudy() throws Exception {

        CreateStudyDTO studyDTO = new CreateStudyDTO();
        studyDTO.setTypeId(STUDY_TYPE_ID);
        studyDTO.setTitle(STUDY_TITLE);

        MvcResult mvcResult = mvc.perform(
                post("/api/v1/study-management/studies/")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(studyDTO)))
                .andExpect(jsonPath("$.result.id").isNotEmpty())
                .andExpect(NO_ERROR_CODE)
                .andExpect(OK_STATUS)
                .andReturn();

        JSONObject result = getResultJSONObject(mvcResult);
        JSONAssert.assertEquals(STUDY_JSON_OBJECT, result, false);
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetup("/data/study/study-before-updating.xml")
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/study/study-after-updating-description.xml", assertionMode = NON_STRICT),
    })
    public void testUpdateStudyDescription() throws Exception {

        StudyDTO updatedStudyDTO = new StudyDTO();
        updatedStudyDTO.setId(STUDY_ID);
        updatedStudyDTO.setDescription(UPDATED_DESCRIPTION);

        StudyStatusDTO status = new StudyStatusDTO(STUDY_STATUS_ID, "Initiate");
        updatedStudyDTO.setStatus(status);

        testUpdate(updatedStudyDTO, UPDATED_STUDY_JSON_OBJECT, null);
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetup("/data/study/study-before-updating.xml")
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/study/study-after-updating-type.xml", assertionMode = NON_STRICT_UNORDERED),
    })
    public void testUpdateStudyType() throws Exception {

        StudyDTO updatedStudyDTO = new StudyDTO();
        updatedStudyDTO.setId(STUDY_ID);
        updatedStudyDTO.setDescription("description");

        StudyTypeDTO type = new StudyTypeDTO(1L);
        type.setName("type1");
        updatedStudyDTO.setType(type);

        StudyStatusDTO status = new StudyStatusDTO(STUDY_STATUS_ID, "Initiate");
        updatedStudyDTO.setStatus(status);

        testUpdate(updatedStudyDTO, UPDATED_STUDY_TYPE_JSON_OBJECT, "type1");
    }


    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetup("/data/study/study-before-updating.xml")
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/study/study-after-updating-status.xml", assertionMode = NON_STRICT_UNORDERED),
    })
    public void testUpdateStudyStatus() throws Exception {

        StudyDTO updatedStudyDTO = new StudyDTO();
        updatedStudyDTO.setId(STUDY_ID);
        updatedStudyDTO.setDescription("description");

        StudyTypeDTO type = new StudyTypeDTO(STUDY_TYPE_ID);
        type.setName("type1");
        updatedStudyDTO.setType(type);

        StudyStatusDTO status = new StudyStatusDTO(2L, "Active");
        updatedStudyDTO.setStatus(status);

        testUpdate(updatedStudyDTO, UPDATED_STUDY_STATUS_JSON_OBJECT, "Active");
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetup("/data/study/study-before-updating.xml")
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/study/study-after-updating-title.xml", assertionMode = NON_STRICT),
    })
    public void testUpdateStudyTitle() throws Exception {

        StudyDTO updatedStudyDTO = new StudyDTO();
        updatedStudyDTO.setId(STUDY_ID);
        updatedStudyDTO.setTitle("Study Title New");
        updatedStudyDTO.setDescription("description");

        StudyTypeDTO type = new StudyTypeDTO(STUDY_TYPE_ID);
        type.setName("type");
        updatedStudyDTO.setType(type);

        StudyStatusDTO status = new StudyStatusDTO(STUDY_STATUS_ID, "Initiate");
        updatedStudyDTO.setStatus(status);

        testUpdate(updatedStudyDTO, UPDATED_STUDY_TITLE_JSON_OBJECT, UPDATED_STUDY_TITLE);
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetup("/data/study/study-before-updating.xml")
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/study/study-after-updating-start-date.xml", assertionMode = NON_STRICT),
    })
    public void testUpdateStudyStartDate() throws Exception {

        StudyDTO updatedStudyDTO = new StudyDTO();
        updatedStudyDTO.setId(STUDY_ID);
        updatedStudyDTO.setDescription("description");
        updatedStudyDTO.setStartDate(UPDATED_DATE);

        StudyStatusDTO status = new StudyStatusDTO(STUDY_STATUS_ID, "Initiate");
        updatedStudyDTO.setStatus(status);

        testUpdate(updatedStudyDTO, UPDATED_STUDY_START_DATE_JSON_OBJECT, UPDATED_DATE);
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetup("/data/study/study-before-updating.xml")
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/study/study-after-updating-end-date.xml", assertionMode = NON_STRICT),
    })
    public void testUpdateStudyEndDate() throws Exception {

        StudyDTO updatedStudyDTO = new StudyDTO();
        updatedStudyDTO.setId(STUDY_ID);
        updatedStudyDTO.setDescription("description");
        updatedStudyDTO.setEndDate(UPDATED_DATE);

        StudyStatusDTO status = new StudyStatusDTO(STUDY_STATUS_ID, "Initiate");
        updatedStudyDTO.setStatus(status);

        testUpdate(updatedStudyDTO, UPDATED_STUDY_END_DATE_JSON_OBJECT, UPDATED_DATE);
    }

    private void testUpdate(StudyDTO updatedStudyDTO, JSONObject expected, Object newValue) throws Exception {

        MvcResult mvcResult = mvc.perform(
                put("/api/v1/study-management/studies/" + STUDY_ID)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(updatedStudyDTO)))
                .andExpect(NO_ERROR_CODE)
                .andExpect(jsonPath("$.result.id").isNotEmpty())
                .andReturn();

        JSONObject result = getResultJSONObject(mvcResult);
        JSONAssert.assertEquals(expected, result, false);
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetup("/data/study/study-after-updating-description.xml")
    @ExpectedDatabase(value = "/data/study/study-after-updating-description.xml", assertionMode = NON_STRICT)
    public void testGetStudy() throws Exception {

        MvcResult mvcResult = mvc.perform(
                get("/api/v1/study-management/studies/" + STUDY_ID)
        )
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andReturn();

        JSONAssert.assertEquals(UPDATED_STUDY_JSON_OBJECT, getResponse(mvcResult), false);
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetup("/data/study/study-after-updating-description.xml")
    @ExpectedDatabase(value = "/data/study/study-after-updating-description.xml", assertionMode = NON_STRICT)
    public void testGetStudyList() throws Exception {

        mvc.perform(
                get("/api/v1/study-management/studies/"))
                .andExpect(NO_ERROR_CODE)
                .andExpect(jsonPath("$.result.numberOfElements").isNotEmpty());
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetup("/data/study/study-after-updating-description.xml")
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/study/study-after-updating-description.xml", assertionMode = NON_STRICT),
            @ExpectedDatabase(value = "/data/study/studies-files.xml", assertionMode = NON_STRICT),
    })
    public void testUploadFile() throws Exception {

        String path = this.getClass().getResource("/test.jpg").getPath();
        FileInputStream fileInputStream = new FileInputStream(path);
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", fileInputStream);

        mvc.perform(
                fileUpload("/api/v1/study-management/studies/" + STUDY_ID + "/upload")
                        .file(multipartFile)
                        .param("label", "labelUploadedFile")
                        .param("file", path)
                        .contentType(MULTIPART_FORM_DATA))
                .andExpect(NO_ERROR_CODE);
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetups({
            @DatabaseSetup("/data/study/study-after-updating-description.xml"),
            @DatabaseSetup("/data/study/studies-files-before-deleting.xml")
    })
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/study/studies-files-after-file-deleting.xml", assertionMode = NON_STRICT),
    })
    public void testDeleteFile() throws Exception {

        String uuid = "07c463d8-075a-4a33-b027-c9f6b623f722";
        prepareStudyFile(STUDY_ID, uuid);

        mvc.perform(
                delete("/api/v1/study-management/studies/{study_id}/files/{fileUuid}", STUDY_ID, uuid))
                .andExpect(TRUE_RESULT)
                .andExpect(NO_ERROR_CODE);

        // todo check file system
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetups({
            @DatabaseSetup("/data/study/study-after-updating-description.xml")
    })
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/study/study-participant-after-add.xml", assertionMode = NON_STRICT),
    })
    public void testAddParticipant() throws Exception {

        AddStudyParticipantDTO participantDTO = new AddStudyParticipantDTO();
        participantDTO.setUserId(USER_2_UUID);
        participantDTO.setRole(CONTRIBUTOR);

        mvc.perform(
                post("/api/v1/study-management/studies/" + STUDY_ID + "/participants")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(participantDTO)))
                .andExpect(NO_ERROR_CODE)
                .andReturn();
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetups({
            @DatabaseSetup("/data/study/study-participant-before-changing-role.xml")
    })
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/study/study-participant-with-2-leaders.xml", assertionMode = NON_STRICT),
    })
    public void testUpdateContributorRoleToLeadInvestigator() throws Exception {

        UpdateParticipantDTO participantDTO = new UpdateParticipantDTO();
        participantDTO.setRole(LEAD_INVESTIGATOR.name());

        mvc.perform(
                put("/api/v1/study-management/studies/" + STUDY_ID + "/participants/" + UserIdUtils.idToUuid(2L))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(participantDTO)))
                .andExpect(NO_ERROR_CODE)
                .andReturn();
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetups({
            @DatabaseSetup("/data/study/study-participant-before-changing-role.xml")
    })
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/study/study-participant-before-changing-role.xml", assertionMode = NON_STRICT),
    })
    public void testUpdateContributorRoleToDatasetOwner() throws Exception {


        UpdateParticipantDTO participantDTO = new UpdateParticipantDTO();
        participantDTO.setRole(DATA_SET_OWNER.name());

        mvc.perform(
                put("/api/v1/study-management/studies/" + STUDY_ID + "/participants/" + 2L)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(participantDTO)))
                .andExpect(jsonPath("$.errorCode").value(VALIDATION_ERROR.getCode()));
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetups({
            @DatabaseSetup("/data/study/study-participant-before-changing-role.xml")
    })
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/study/study-participant-before-changing-role.xml", assertionMode = NON_STRICT),
    })
    public void testUpdateContributorRoleForLastLeadInvestigator() throws Exception {

        UpdateParticipantDTO participantDTO = new UpdateParticipantDTO();
        participantDTO.setRole(CONTRIBUTOR.name());

        mvc.perform(
                put("/api/v1/study-management/studies/" + STUDY_ID + "/participants/" + UserIdUtils.idToUuid(ADMIN_ID))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(participantDTO)))
                .andExpect(jsonPath("$.errorCode").value(VALIDATION_ERROR.getCode()));
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetups({
            @DatabaseSetup("/data/study/study-participant-with-2-leaders-before-updating.xml")
    })
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/study/study-participant-with-contributor-and-leader.xml", assertionMode = NON_STRICT),
    })
    public void testUpdateContributorRoleToContributor() throws Exception {

        UpdateParticipantDTO participantDTO = new UpdateParticipantDTO();
        participantDTO.setRole(CONTRIBUTOR.name());

        mvc.perform(
                put("/api/v1/study-management/studies/" + STUDY_ID + "/participants/" + UserIdUtils.idToUuid(ADMIN_ID))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(participantDTO)))
                .andExpect(NO_ERROR_CODE)
                .andReturn();
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetups({
            @DatabaseSetup("/data/study/study-participant.xml")
    })
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/study/study-participant.xml", assertionMode = NON_STRICT),
    })
    public void testAddExistedParticipant() throws Exception {

        AddStudyParticipantDTO participantDTO = new AddStudyParticipantDTO();
        participantDTO.setUserId(USER_2_UUID);
        participantDTO.setRole(CONTRIBUTOR);

        mvc.perform(
                post("/api/v1/study-management/studies/" + STUDY_ID + "/participants")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(participantDTO)))
                .andExpect(jsonPath("$.errorCode").value(ALREADY_EXIST.getCode()));
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetup("/data/study/study-participant-before-acceptance.xml")
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/study/study-participant-after-acceptance.xml", assertionMode = NON_STRICT),
    })
    public void testAcceptInvitation() throws Exception {

        testInvitation(TRUE);
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetup("/data/study/study-participant-before-acceptance.xml")
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/study/study-participant-after-declining.xml", assertionMode = NON_STRICT),
    })
    public void testDeclineInvitation() throws Exception {

        testInvitation(FALSE);
    }

    public void testInvitation(Boolean accepted) throws Exception {

        InvitationActionDTO invitationActionDTO = new InvitationActionDTO();
        invitationActionDTO.setType(COLLABORATOR);
        invitationActionDTO.setAccepted(accepted);
        invitationActionDTO.setId(2L);

        mvc.perform(
                post("/api/v1/user-management/users/invitations/")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(invitationActionDTO)))
                .andExpect(NO_ERROR_CODE)
                .andExpect(jsonPath("$.result").isNotEmpty());
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetups({
            @DatabaseSetup(value = "/data/study/study-participant-before-deleting.xml", type = DatabaseOperation.INSERT),
    })
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/study/study-participant-after-soft-deleting.xml", assertionMode = NON_STRICT),
    })
    public void testSoftDeleteParticipant() throws Exception {

        deleteParticipant();
    }

//    @Test ARACHNE-1332
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetup(value = "/data/study/study-participant-before-deleting.xml", type = DatabaseOperation.INSERT)
    @ExpectedDatabases({
            @ExpectedDatabase(value = "/data/study/study-participant-after-hard-deleting.xml", assertionMode = NON_STRICT),
    })
    public void testHardParticipant() throws Exception {

        deleteParticipant();
    }

    private void deleteParticipant() throws Exception {

        Long participantId = 2L;
        mvc.perform(
                delete("/api/v1/study-management/studies/" + STUDY_ID + "/participants/" + UserIdUtils.idToUuid(participantId)))
                .andExpect(NO_ERROR_CODE);
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetup("/data/study/study-participant-after-soft-deleting.xml")
    @ExpectedDatabase(value = "/data/study/study-after-deleting.xml", assertionMode = NON_STRICT)
    public void testDeleteStudy() throws Exception {

        mvc.perform(
                delete("/api/v1/study-management/studies/" + STUDY_ID))
                .andExpect(NO_ERROR_CODE);
    }

    private void prepareStudyFile(Long studyId, String filename) throws IOException {

        Path dir = Paths.get(fileStorePath, "content", String.valueOf(studyId));
        Files.createDirectories(dir);
        Path path = dir.resolve(filename);
        Files.write(path, "SELECT * FROM death LIMIT 10".getBytes());
    }

}
