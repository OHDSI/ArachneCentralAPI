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
import static java.lang.Boolean.FALSE;
import static java.util.Calendar.MILLISECOND;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonProfessionalTypeDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonUserRegistrationDTO;
import com.odysseusinc.arachne.commons.utils.UserIdUtils;
import com.odysseusinc.arachne.portal.api.v1.dto.UserLinkDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UserProfileGeneralDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UserPublicationDTO;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;

@RunWith(SpringRunner.class)
@DatabaseTearDown(value = "/data/empty.xml", type = DatabaseOperation.DELETE_ALL)
public class UserControllerTests extends BaseControllerTest {

    private static final String UPDATED_FIRST_NAME = "updatedFirstName";
    private static final String UPDATED_LAST_NAME = "updatedLastName";
    private static final String UPDATED_MIDDLE_NAME = "updatedMiddleName";
    private static final String UPDATED_AFFILIATION = "updatedAffiliation";
    private static final String UPDATED_PERSONAL_SUMMARY = "updatedPersonalSummary";

    static final String ADMIN_FIRST_NAME = "adminFirstName";
    static final String ADMIN_MIDDLE_NAME = "adminMiddleName";
    static final String ADMIN_LAST_NAME = "adminLastName";
    static final Long ADMIN_ID = 1L;

    private static final String EMAIL = "mail@mail.com";
    private static final String MIDDLE_NAME = "middleName";
    private static final String PASSWORD = "XJHCRr7BJv87";
    private static final String BAD_PASSWORD = "password";

    private static final Long PROFESSIONAL_TYPE_ID = 1L;
    private static final Long USER_ID = 2L;

    private static final String USER_LINK_DESCRIPTION = "userLinkDescription";
    private static final String USER_LINK_TITLE = "userLinkTitle";
    private static final String USER_LINK_URL = "userLinkUrl";

    private static final String PUBLISHER = "userPublicationPublisher";
    public static final String USER_2_UUID = UserIdUtils.idToUuid(USER_ID);
    private static final String ORGANIZATION = "Odysseus Inc";

    private final JSONObject ADMIN_JSON_OBJECT = new JSONObject()
            .put("firstname", ADMIN_FIRST_NAME)
            .put("middlename", ADMIN_MIDDLE_NAME)
            .put("lastname", ADMIN_LAST_NAME);

    private final JSONObject USER_JSON_OBJECT = new JSONObject()
            .put("firstname", FIRST_NAME)
            .put("middlename", MIDDLE_NAME)
            .put("lastname", LAST_NAME);

    private final JSONObject USER_LINK = new JSONObject()
            .put("description", USER_LINK_DESCRIPTION)
            .put("title", USER_LINK_TITLE)
            .put("url", USER_LINK_URL);

    private final JSONObject PUBLICATION = new JSONObject()
            .put("description", USER_LINK_DESCRIPTION)
            .put("title", USER_LINK_TITLE)
            .put("url", USER_LINK_URL)
            .put("publisher", PUBLISHER)
            .put("date", DATE.getTime());

    private final JSONObject UPDATED_USER_JSON_OBJECT = new JSONObject()
            .put("firstname", UPDATED_FIRST_NAME)
            .put("middlename", UPDATED_MIDDLE_NAME)
            .put("lastname", UPDATED_LAST_NAME)
            .put("affiliation", UPDATED_AFFILIATION)
            .put("personalSummary", UPDATED_PERSONAL_SUMMARY);

    private static Date DATE;

    static {
        Calendar c = Calendar.getInstance();
        c.set(2017, 0, 1, 0, 0, 0);
        c.clear(MILLISECOND);
        DATE = c.getTime();
    }

    public UserControllerTests() throws JSONException {

    }

    @Test
    @DatabaseSetup("/data/user/admin-user.xml")
    @ExpectedDatabase(value = "/data/user/registered-user-and-admin.xml", assertionMode = DatabaseAssertionMode.NON_STRICT)
    public void testCreateUser() throws Exception {

        CommonUserRegistrationDTO inputDTO = getCommonUserRegistrationDTO(PASSWORD);

        MvcResult mvcResult = mvc.perform(
                post("/api/v1/auth/registration")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(inputDTO))
                        .with(anonymous()))
                .andExpect(OK_STATUS)
                .andReturn();
    }

    @Test
    @DatabaseSetup("/data/user/admin-user.xml")
    @ExpectedDatabase(value = "/data/user/admin-user.xml", assertionMode = DatabaseAssertionMode.NON_STRICT)
    public void testCreateUserWithBadPassword() throws Exception {

        CommonUserRegistrationDTO inputDTO = getCommonUserRegistrationDTO(BAD_PASSWORD);

        MvcResult mvcResult = mvc.perform(
                post("/api/v1/auth/registration")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(inputDTO))
                        .with(anonymous()))
                .andExpect(VALIDATION_ERROR_CODE)
                .andExpect(OK_STATUS)
                .andReturn();
    }

    private CommonUserRegistrationDTO getCommonUserRegistrationDTO(String password) {

        CommonUserRegistrationDTO inputDTO = new CommonUserRegistrationDTO();
        inputDTO.setEmail(EMAIL);
        inputDTO.setFirstname(FIRST_NAME);
        inputDTO.setLastname(LAST_NAME);
        inputDTO.setMiddlename(MIDDLE_NAME);
        inputDTO.setPassword(password);
        inputDTO.setProfessionalTypeId(PROFESSIONAL_TYPE_ID);
        inputDTO.setOrganization(ORGANIZATION);
        return inputDTO;
    }

    private static final String ACTIVATION_CODE = "activationCode";

    @Test
    @DatabaseSetup("/data/user/not-activated-user.xml")
    @ExpectedDatabase(value = "/data/user/activated-user.xml", assertionMode = NON_STRICT)
    public void testActivateUser() throws Exception {

        mvc.perform(
                get("/api/v1/user-management/activation/" + ACTIVATION_CODE)
                        .with(anonymous()))

                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/auth/login?message=email-confirmed"))
                .andReturn();
    }

    @Test
    @DatabaseSetup("/data/user/activated-user.xml")
    @ExpectedDatabase(value = "/data/user/activated-user.xml", assertionMode = NON_STRICT)
    public void testEnableUserWithException() throws Exception {

        mvc.perform(
                post("/api/v1/admin/users/" + USER_ID + "/enable/true")
                        .with(anonymous()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DatabaseSetup("/data/user/not-activated-user.xml")
    @ExpectedDatabase(value = "/data/user/enabled-user.xml", assertionMode = NON_STRICT)
    public void testEnableUser() throws Exception {

        mvc.perform(
                post("/api/v1/admin/users/" + UserIdUtils.idToUuid(USER_ID) + "/enable/true")
                        .with(user(ADMIN_EMAIL).roles("ADMIN")))
                .andExpect(NO_ERROR_CODE)
                .andExpect(OK_STATUS)
                .andExpect(TRUE_RESULT)
                .andReturn();
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetup("/data/user/admin-user.xml")
    @ExpectedDatabase(value = "/data/user/admin-user.xml", assertionMode = NON_STRICT)
    public void testGetUserInfo() throws Exception {

        MvcResult mvcResult = mvc.perform(
                get("/api/v1/auth/me"))
                .andExpect(NO_ERROR_CODE)
                .andExpect(OK_STATUS)
                .andReturn();

        JSONAssert.assertEquals(ADMIN_JSON_OBJECT, getResultJSONObject(mvcResult), false);
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetup("/data/user/admin-user.xml")
    @ExpectedDatabase(value = "/data/user/updated-admin-user.xml", assertionMode = NON_STRICT)
    public void testUpdate() throws Exception {

        UserProfileGeneralDTO dto = getUserProfileGeneralDTO();

        MvcResult mvcResult = mvc.perform(
                post("/api/v1/user-management/users/profile")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(dto)))
                .andExpect(NO_ERROR_CODE)
                .andExpect(OK_STATUS)
                .andReturn();

        JSONObject general = getGeneral(mvcResult);
        JSONAssert.assertEquals(UPDATED_USER_JSON_OBJECT, general, false);
        Assert.assertEquals(PROFESSIONAL_TYPE_ID.intValue(), general.getJSONObject("professionalType").get("id"));
    }

    private UserProfileGeneralDTO getUserProfileGeneralDTO() {

        UserProfileGeneralDTO inputDTO = new UserProfileGeneralDTO();
        inputDTO.setFirstname(UPDATED_FIRST_NAME);
        inputDTO.setLastname(UPDATED_LAST_NAME);
        inputDTO.setMiddlename(UPDATED_MIDDLE_NAME);

        inputDTO.setAffiliation(UPDATED_AFFILIATION);
        inputDTO.setPersonalSummary(UPDATED_PERSONAL_SUMMARY);

        CommonProfessionalTypeDTO professionalTypeDTO = new CommonProfessionalTypeDTO();
        professionalTypeDTO.setId(PROFESSIONAL_TYPE_ID);
        inputDTO.setProfessionalType(professionalTypeDTO);

        return inputDTO;
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetup("/data/user/enabled-user.xml")
    @ExpectedDatabase(value = "/data/user/enabled-user.xml", assertionMode = NON_STRICT)
    public void testGetProfile() throws Exception {

        MvcResult mvcResult = mvc.perform(
                get("/api/v1/user-management/users/" + USER_2_UUID + "/profile"))
                .andExpect(NO_ERROR_CODE)
                .andExpect(OK_STATUS)
                .andReturn();

        JSONAssert.assertEquals(USER_JSON_OBJECT, getGeneral(mvcResult), false);
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetup(value = "/data/user/users-for-suggestion.xml")
    @ExpectedDatabase(value = "/data/user/users-for-suggestion.xml", assertionMode = NON_STRICT)
    public void testSuggest() throws Exception {

        final String query = "fir";
        final Long studyId = 1L;

        MvcResult mvcResult = mvc.perform(
                get("/api/v1/user-management/users/suggest?target=STUDY&query=" + query + "&id=" + studyId))
                .andExpect(OK_STATUS)
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3)))
                .andReturn();

        JSONArray suggested = new JSONArray(mvcResult.getResponse().getContentAsString());

        List<Object> list = new ArrayList<>();
        for (int i = 0; i < suggested.length(); i++) {
            list.add(suggested.getJSONObject(i).get("id"));
        }
        // suggested user ids
        Assert.assertTrue(list.contains(UserIdUtils.idToUuid(3L)));
        Assert.assertTrue(list.contains(UserIdUtils.idToUuid(6L)));
        Assert.assertTrue(list.contains(UserIdUtils.idToUuid(7L)));
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetup("/data/user/admin-user-and-skill.xml")
    @ExpectedDatabase(value = "/data/user/admin-user-with-skill.xml", assertionMode = NON_STRICT)
    public void testAddSkillToUser() throws Exception {

        final Integer skillId = 1;

        MvcResult mvcResult = mvc.perform(
                post("/api/v1/user-management/users/skills/" + skillId))
                .andExpect(NO_ERROR_CODE)
                .andExpect(OK_STATUS)
                .andExpect(jsonPath("$.result.skills").isNotEmpty())
                .andExpect(jsonPath("$.result.skills[0].id").value(1))
                .andReturn();

        JSONAssert.assertEquals(ADMIN_JSON_OBJECT, getGeneral(mvcResult), false);
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetup("/data/user/admin-user-with-skill.xml")
    @ExpectedDatabase(value = "/data/user/admin-user-and-skill.xml", assertionMode = NON_STRICT)
    public void testRemoveSkillFromUser() throws Exception {

        final Integer skillId = 1;

        MvcResult mvcResult = mvc.perform(
                delete("/api/v1/user-management/users/skills/" + skillId))
                .andExpect(NO_ERROR_CODE)
                .andExpect(OK_STATUS)
                .andExpect(jsonPath("$.result.skills").isEmpty())
                .andReturn();

        JSONAssert.assertEquals(ADMIN_JSON_OBJECT, getGeneral(mvcResult), false);
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetup("/data/user/admin-user.xml")
    @ExpectedDatabase(value = "/data/user/admin-user-with-link.xml", assertionMode = NON_STRICT)
    public void testLinkToUser() throws Exception {

        UserLinkDTO link = getUserLinkDTO();

        MvcResult mvcResult = mvc.perform(
                post("/api/v1/user-management/users/links")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(link)))
                .andExpect(NO_ERROR_CODE)
                .andExpect(OK_STATUS)

                .andExpect(jsonPath("$.result.links").isNotEmpty())
                .andExpect(jsonPath("$.result.links").isArray())
                .andExpect(jsonPath("$.result.links", hasSize(1)))
                .andReturn();


        JSONAssert.assertEquals(ADMIN_JSON_OBJECT, getGeneral(mvcResult), FALSE);

        JSONArray links = (JSONArray) getResultJSONObject(mvcResult).get("links");
        JSONAssert.assertEquals(USER_LINK, (JSONObject) links.get(0), FALSE);
    }

    private UserLinkDTO getUserLinkDTO() {

        UserLinkDTO link = new UserLinkDTO();
        link.setDescription(USER_LINK_DESCRIPTION);
        link.setTitle(USER_LINK_TITLE);
        link.setUrl(USER_LINK_URL);
        return link;
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetup("/data/user/admin-user-with-link-for-deleting.xml")
    @ExpectedDatabase(value = "/data/user/admin-user.xml", assertionMode = NON_STRICT)
    public void testRemoveLinkFromUser() throws Exception {

        final Integer linkId = 1;

        MvcResult mvcResult = mvc.perform(
                delete("/api/v1/user-management/users/links/" + linkId))
                .andExpect(NO_ERROR_CODE)
                .andExpect(OK_STATUS)

                .andExpect(jsonPath("$.result.skills").isEmpty())
                .andReturn();
        JSONAssert.assertEquals(ADMIN_JSON_OBJECT, getGeneral(mvcResult), FALSE);
    }


    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetup("/data/user/admin-user.xml")
    @ExpectedDatabase(value = "/data/user/admin-user-with-publication.xml", assertionMode = NON_STRICT)
    public void testAddPublicationToUser() throws Exception {

        UserPublicationDTO inputDTO = new UserPublicationDTO();
        inputDTO.setDescription(USER_LINK_DESCRIPTION);
        inputDTO.setTitle(USER_LINK_TITLE);
        inputDTO.setUrl(USER_LINK_URL);
        inputDTO.setDate(DATE);
        inputDTO.setPublisher(PUBLISHER);

        MvcResult mvcResult = mvc.perform(
                post("/api/v1/user-management/users/publications")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(NO_ERROR_CODE)
                .andExpect(OK_STATUS)

                .andExpect(jsonPath("$.result.publications").isNotEmpty())
                .andExpect(jsonPath("$.result.publications", hasSize(1)))

                .andReturn();

        JSONAssert.assertEquals(ADMIN_JSON_OBJECT, getGeneral(mvcResult), FALSE);

        JSONArray publications = (JSONArray) getResultJSONObject(mvcResult).get("publications");
        JSONAssert.assertEquals(PUBLICATION, (JSONObject) publications.get(0), FALSE);

    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetup("/data/user/admin-user-with-publication-for-deleting.xml")
    @ExpectedDatabase(value = "/data/user/admin-user.xml", assertionMode = NON_STRICT)
    public void testDeletePublicationFromUser() throws Exception {

        Integer publicationId = 1;

        MvcResult mvcResult = mvc.perform(
                delete("/api/v1/user-management/users/publications/" + publicationId))
                .andExpect(jsonPath("$.result.publications").isEmpty())
                .andReturn();

        JSONAssert.assertEquals(ADMIN_JSON_OBJECT, getGeneral(mvcResult), false);
    }

    @Test
    @WithUserDetails(value = ADMIN_EMAIL)
    @DatabaseSetup("/data/user/admin-user.xml")
    @ExpectedDatabase(value = "/data/user/admin-user.xml", assertionMode = NON_STRICT)
    public void testUploadAvatar() throws Exception {

        FileInputStream fileInputStream = new FileInputStream(this.getClass().getResource("/test.jpg").getPath());
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", fileInputStream);

        this.mvc.perform(
                fileUpload("/api/v1/user-management/users/avatar")
                        .file(multipartFile)
                        .contentType(MULTIPART_FORM_DATA))
                .andExpect(TRUE_RESULT)
                .andExpect(NO_ERROR_CODE)
                .andExpect(OK_STATUS);
    }

}
