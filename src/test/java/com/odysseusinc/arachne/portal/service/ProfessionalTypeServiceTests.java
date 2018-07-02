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

package com.odysseusinc.arachne.portal.service;

import static com.github.springtestdbunit.assertion.DatabaseAssertionMode.NON_STRICT;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.odysseusinc.arachne.portal.SingleContextTest;
import com.odysseusinc.arachne.portal.api.v1.controller.BaseControllerTest;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

@RunWith(SpringRunner.class)
@DatabaseTearDown(value = "/data/empty.xml", type = DatabaseOperation.DELETE_ALL)
public class ProfessionalTypeServiceTests extends SingleContextTest {

    private static final Long PROFESSIONAL_TYPE_ID = 1L;

    @Autowired
    private ProfessionalTypeService professionalTypeService;

    @Test
    @DatabaseSetup("/data/professionaltype/professional-types-for-invalid-deleting.xml")
    @ExpectedDatabase(value = "/data/professionaltype/professional-types-for-invalid-deleting.xml", assertionMode = NON_STRICT)
    public void testInvalidDeletingProfessionalType() throws Exception {

        try {
            professionalTypeService.delete(PROFESSIONAL_TYPE_ID);
            Assert.assertTrue(false);
        } catch (Exception ex) {
            Assert.assertTrue(ex.getCause() instanceof ConstraintViolationException);
        }
    }
}
