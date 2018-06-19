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
 * Created: June 02, 2017
 *
 */

//package com.odysseusinc.arachne.portal.rest;
//
//import com.odysseusinc.arachne.commons.study.datasource.*;
//import com.odysseusinc.arachne.commons.user.CommonArachneUserDTO;
//import com.odysseusinc.arachne.commons.user.CommonArachneUserStatusDTO;
//import com.odysseusinc.arachne.commons.user.CommonArachneUserTypeDTO;
//import com.odysseusinc.arachne.commons.util.JsonResult;
//import com.odysseusinc.arachne.portal.config.WebApplicationStarter;
//import com.odysseusinc.arachne.portal.model.User;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.FixMethodOrder;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.junit.runners.MethodSorters;
//import org.springframework.boot.test.SpringApplicationConfiguration;
//import org.springframework.boot.test.WebIntegrationTest;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.Date;
//
//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(WebApplicationStarter.class)
//@WebIntegrationTest("server.port:9002")
//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//@TestPropertySource(locations = "classpath:application.properties")
//public class DataSourceTests extends BaseTest {
//
//    @Before
//    public void setUp() throws Exception {
//
//        this.base = "http://localhost:" + port + "/api/";
//        template = new RestTemplate();
//
//        createAdminUser();
//        createOwningUser();
//
//        login();
//
//    }
//
//
//    private void createAdminUser() throws Exception {
//
//        User admin = userService.getByUsername("admin@odysseusinc.com");
//
//        if (admin != null && (System.currentTimeMillis() - admin.getCreated().getTime() > 10000)) {
//            userService.remove(admin.getId());
//            admin = null;
//        }
//
//        if (admin == null) {
//            User user = new User();
//            user.setEmail("admin@odysseusinc.com");
//            user.setCreated(new Date());
//            user.setUpdated(new Date());
//            user.setEnabled(true);
//            user.setPassword(passwordEncoder.encode("password"));
//            user.setLastPasswordReset(new Date());
//            user.setProfessionalType(getProffesionalType());
//            userService.create(user);
//        }
//
//    }
//
//
//    private void createOwningUser() throws Exception {
//
//        User owner = userService.getByUsername("owner@example.com");
//
//        if (owner != null && (System.currentTimeMillis() - owner.getCreated().getTime() > 10000)) {
//            userService.remove(owner.getId());
//            owner = null;
//        }
//
//        if (owner == null) {
//            User user = new User();
//            user.setEmail("owner@example.com");
//            user.setCreated(new Date());
//            user.setUpdated(new Date());
//            user.setEnabled(true);
//            user.setPassword(passwordEncoder.encode("password"));
//            user.setLastPasswordReset(new Date());
//            user.setProfessionalType(getProffesionalType());
//            userService.create(user);
//        }
//
//    }
//
//
//
//    @Test
//    public void testDataSourceRegistration() {
//
//        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
//        headers.add("Content-Type", "application/json");
//        headers.add(tokenHeader, authToken);
//        CommonDataSourceDTO commonDataSourceDTO = getDataSourceRegistrationDTO();
//        HttpEntity<CommonDataSourceDTO> httpEntity = new HttpEntity<>(commonDataSourceDTO, headers);
//
//        RestTemplate template = new RestTemplate();
//        ResponseEntity<JsonResult> responseEntity = template.exchange(base + "v1/data-source", HttpMethod.POST, httpEntity, JsonResult.class);
//
//        JsonResult result = responseEntity.getBody();
//        Assert.assertEquals(JsonResult.ErrorCode.NO_ERROR.getCode(), result.errorCode);
//
//    }
//
//
//    private CommonDataSourceDTO getDataSourceRegistrationDTO() {
//
//        CommonDataSourceDTO commonDataSourceDTO = new CommonDataSourceDTO();
//
//        commonDataSourceDTO.setName("Clinic 1");
//
//        commonDataSourceDTO.setDescription("EMR data from clinic 1.");
//
//        CommonDBMSDTO commonDBMSDTO = new CommonDBMSDTO();
//        commonDBMSDTO.setName("POSTGRESQL");
//        commonDataSourceDTO.setDbms(commonDBMSDTO);
//
//        commonDataSourceDTO.setCdmVersion(CommonCDMVersionDTO.valueOf("V5_1"));
//
//        commonDataSourceDTO.setPatientsNumber(1_000_000);
//
//        commonDataSourceDTO.setType(CommonDataSourceTypeDTO.valueOf("EMR"));
//
//        commonDataSourceDTO.setAccessType(CommonDataSourceAccessTypeDTO.valueOf("PUBLIC"));
//
//        commonDataSourceDTO.setLicenseType(CommonLicenseTypeDTO.valueOf("PER_STUDY"));
//
//        commonDataSourceDTO.setCountry("US");
//
//        commonDataSourceDTO.setOwner(getDataSourceRegistrationUserDTO());
//
//        return commonDataSourceDTO;
//
//    }
//
//
//    private CommonArachneUserDTO getDataSourceRegistrationUserDTO() {
//        CommonArachneUserDTO commonArachneUserDTO = new CommonArachneUserDTO();
//        commonArachneUserDTO.setGeneralId("2");
//        commonArachneUserDTO.setType(CommonArachneUserTypeDTO.PORTAL);
//        commonArachneUserDTO.setFirstname("John");
//        commonArachneUserDTO.setLastname("Smith");
//        commonArachneUserDTO.setLogin("jsmith");
//        commonArachneUserDTO.setStatus(CommonArachneUserStatusDTO.APPROVED);
//        return commonArachneUserDTO;
//    }
//
//}
