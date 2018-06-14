/*
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
 * Created: October 10, 2017
 *
 */

package com.odysseusinc.arachne.portal.component;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseSetups;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.odysseusinc.arachne.portal.SingleContextTest;
import com.odysseusinc.arachne.portal.api.v1.controller.BaseControllerTest;
import com.odysseusinc.arachne.portal.model.DataSource;
import com.odysseusinc.arachne.portal.model.Paper;
import com.odysseusinc.arachne.portal.security.ArachnePermission;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@DatabaseTearDown(value = "/data/empty.xml", type = DatabaseOperation.DELETE_ALL)
@TestExecutionListeners({TransactionalTestExecutionListener.class})
@Transactional
public class ArachnePermissionEvaluatorTest extends SingleContextTest {

    @Autowired
    private ArachnePermissionEvaluator<Paper, DataSource> permissionEvaluator;

    @Test
    @WithUserDetails(value = "admin@odysseusinc.com")
    @DatabaseSetup({"/data/study/study-before-updating.xml"})
    public void leadStudyPermissions() {

        AccessHelper study = new AccessHelper("Study");
        study.hasAccess("%s should have access to study", ArachnePermission.ACCESS_STUDY);
        study.hasAccess("%s should have permission to invite datanode to study", ArachnePermission.INVITE_DATANODE);
        study.hasAccess("%s should have permission to invite contributor to study", ArachnePermission.INVITE_CONTRIBUTOR);
        study.hasAccess("%s should have permission to edit study", ArachnePermission.EDIT_STUDY);
        study.hasAccess("%s should have permission to create analysis", ArachnePermission.CREATE_ANALYSIS);
        study.hasAccess("%s should have permission to delete analysis", ArachnePermission.DELETE_ANALYSIS);
        study.hasAccess("%s should have permission to create submission", ArachnePermission.CREATE_SUBMISSION);
        study.hasAccess("%s should have permission to upload files", ArachnePermission.UPLOAD_FILES);
        study.hasAccess("%s should have permission to upload analysis files", ArachnePermission.UPLOAD_ANALYSIS_FILES);
        study.hasAccess("%s should have permission to delete analysis files", ArachnePermission.DELETE_ANALYSIS_FILES);
        study.hasAccess("%s should have permission to lock analysis files", ArachnePermission.LOCK_ANALYSIS_FILE);
        study.hasAccess("%s should have permission to unlink datasource", ArachnePermission.UNLINK_DATASOURCE);
        study.hasAccess("%s should have permission to edit paper", ArachnePermission.EDIT_PAPER);
        study.hasAccess("%s should have permission to access paper", ArachnePermission.ACCESS_PAPER);
    }

    @Test
    @WithUserDetails(value = "user@mail.com")
    @DatabaseSetup({"/data/study/study-before-updating.xml"})
    public void userStudyPermissions() {

        AccessHelper study = new AccessHelper("Study");
        study.hasAccess("%s should have access to study", ArachnePermission.ACCESS_STUDY);
        study.hasNoAccess("%s should not have permission to invite datanode to study", ArachnePermission.INVITE_DATANODE);
        study.hasNoAccess("%s should not have permission to invite contributor to study", ArachnePermission.INVITE_CONTRIBUTOR);
        study.hasNoAccess("%s should not have permission to edit study", ArachnePermission.EDIT_STUDY);
        study.hasNoAccess("%s should not have permission to create analysis", ArachnePermission.CREATE_ANALYSIS);
        study.hasNoAccess("%s should not have permission to delete analysis", ArachnePermission.DELETE_ANALYSIS);
        study.hasNoAccess("%s should not have permission to create submission", ArachnePermission.CREATE_SUBMISSION);
        study.hasNoAccess("%s should not have permission to upload files", ArachnePermission.UPLOAD_FILES);
        study.hasNoAccess("%s should not have permission to upload analysis files", ArachnePermission.UPLOAD_ANALYSIS_FILES);
        study.hasNoAccess("%s should not have permission to delete analysis files", ArachnePermission.DELETE_ANALYSIS_FILES);
        study.hasNoAccess("%s should not have permission to lock analysis files", ArachnePermission.LOCK_ANALYSIS_FILE);
        study.hasNoAccess("%s should not have permission to unlink datasource", ArachnePermission.UNLINK_DATASOURCE);
        study.hasNoAccess("%s should not have permission to edit paper", ArachnePermission.EDIT_PAPER);
        study.hasNoAccess("%s should not have permission to access paper", ArachnePermission.ACCESS_PAPER);
    }

    @Test
    @WithUserDetails(value = "admin@odysseusinc.com")
    @DatabaseSetups({
            @DatabaseSetup("/data/users.xml"),
            @DatabaseSetup("/data/study-with-contributor.xml"),
            @DatabaseSetup("/data/analysis/analysis-before-updating.xml")
    })
    public void leadAnalysisPermissions() {

        AccessHelper analysis = new AccessHelper("Analysis");
        analysis.hasAccess("%s should have access to study", ArachnePermission.ACCESS_STUDY);
        analysis.hasAccess("%s should have permission to create analysis", ArachnePermission.CREATE_ANALYSIS);
        analysis.hasAccess("%s should have permission to create analysis", ArachnePermission.DELETE_ANALYSIS);
        analysis.hasAccess("%s should have permission to upload analysis file", ArachnePermission.UPLOAD_ANALYSIS_FILES);
        analysis.hasAccess("%s should have permission to delete analysis file", ArachnePermission.DELETE_ANALYSIS_FILES);
        analysis.hasAccess("%s should have permission to lock analysis file", ArachnePermission.LOCK_ANALYSIS_FILE);
    }

    @Test
    @WithUserDetails(value = "user1@odysseusinc.com")
    @DatabaseSetups({
            @DatabaseSetup("/data/users.xml"),
            @DatabaseSetup("/data/study-with-contributor.xml"),
            @DatabaseSetup("/data/analysis/analysis-before-updating.xml"),
    })
    public void userAnalysisPermissions() {

        AccessHelper analysis = new AccessHelper("Analysis");
        analysis.hasAccess("%s should have access to study", ArachnePermission.ACCESS_STUDY);
        analysis.hasNoAccess("%s should not have permission to create analysis", ArachnePermission.CREATE_ANALYSIS);
        analysis.hasNoAccess("%s should not have permission to create analysis", ArachnePermission.DELETE_ANALYSIS);
        analysis.hasNoAccess("%s should not have permission to upload analysis file", ArachnePermission.UPLOAD_ANALYSIS_FILES);
        analysis.hasNoAccess("%s should not have permission to delete analysis file", ArachnePermission.DELETE_ANALYSIS_FILES);
        analysis.hasNoAccess("%s should not have permission to lock analysis file", ArachnePermission.LOCK_ANALYSIS_FILE);
    }

    @Test
    @WithUserDetails(value = "admin@odysseusinc.com")
    @DatabaseSetups({
            @DatabaseSetup("/data/users.xml"),
            @DatabaseSetup("/data/study-with-contributor.xml"),
            @DatabaseSetup("/data/analysis/analysis-list.xml"),
            @DatabaseSetup("/data/datanode-with-datasources.xml"),
            @DatabaseSetup("/data/analysis/submission/submission-executed.xml"),
    })
    public void leadSubmissionPermissions(){

        AccessHelper submission = new AccessHelper("Submission");
        submission.hasAccess("%s should have access to study", ArachnePermission.ACCESS_STUDY);
        submission.hasAccess("%s should have permission to create submission", ArachnePermission.CREATE_SUBMISSION);
        //TODO check for dataset owner
//        submission.hasAccess("%s should have permission to approve submission", ArachnePermission.APPROVE_SUBMISSION);
    }

    @Test
    @WithUserDetails("user1@odysseusinc.com")
    @DatabaseSetups({
            @DatabaseSetup("/data/users.xml"),
            @DatabaseSetup("/data/datanode-with-datasources.xml"),
    })
    public void leadDataSourcePermissions() {
        AccessHelper dataSource = new AccessHelper("DataSource");
        dataSource.hasAccess("%s should have access to datasource", ArachnePermission.ACCESS_DATASOURCE);
        dataSource.hasAccess("%s should have access to create datasource", ArachnePermission.CREATE_DATASOURCE);
        dataSource.hasAccess("%s should have access to create datasource", ArachnePermission.DELETE_DATASOURCE);
    }

    @Test
    @WithUserDetails("user2@odysseusinc.com")
    @DatabaseSetups({
            @DatabaseSetup("/data/users.xml"),
            @DatabaseSetup("/data/datanode-with-datasources.xml"),
    })
    public void userDataSourcePermissions(){

        AccessHelper dataSource = new AccessHelper("DataSource");
        dataSource.hasAccess("%s should have access to datasource", ArachnePermission.ACCESS_DATASOURCE);
        dataSource.hasNoAccess("%s should not have access to create datasource", ArachnePermission.CREATE_DATASOURCE);
        dataSource.hasNoAccess("%s should not have access to create datasource", ArachnePermission.DELETE_DATASOURCE);
    }

    @Test
    @WithUserDetails("admin@odysseusinc.com")
    @DatabaseSetups({
            @DatabaseSetup("/data/users.xml"),
            @DatabaseSetup("/data/study-with-contributor.xml"),
            @DatabaseSetup("/data/analysis/analysis-list.xml")
    })
    public void leadCanDeleteAnalysisFile() {

        AccessHelper analysis = new AccessHelper("Analysis");
        analysis.hasAccess("%s should be able to delete analysis files" , ArachnePermission.DELETE_ANALYSIS_FILES);
    }

    @Test
    @WithUserDetails("user1@odysseusinc.com")
    @DatabaseSetups({
            @DatabaseSetup("/data/users.xml"),
            @DatabaseSetup("/data/study-with-contributor.xml"),
            @DatabaseSetup("/data/analysis/analysis-created-by-user1.xml")
    })
    public void analysisCreatorCanDeleteAnalysisFile() {

        AccessHelper analysis = new AccessHelper("Analysis");
        analysis.hasAccess("%s should be able to delete analysis files" , ArachnePermission.DELETE_ANALYSIS_FILES);
    }

    @Test
    @WithUserDetails("user2@odysseusinc.com")
    @DatabaseSetups({
            @DatabaseSetup("/data/users.xml"),
            @DatabaseSetup("/data/study-with-contributor.xml"),
            @DatabaseSetup("/data/analysis/analysis-created-by-user1.xml")
    })
    public void contributorCantDeleteAnalysisFile() {

        AccessHelper analysis = new AccessHelper("Analysis");
        analysis.hasNoAccess("%s should not be able to delete analysis files" , ArachnePermission.DELETE_ANALYSIS_FILES);
    }

    @Test
    @WithUserDetails("user2@odysseusinc.com")
    @DatabaseSetups({
            @DatabaseSetup("/data/users.xml"),
            @DatabaseSetup("/data/study-with-pending-contributor.xml"),
            @DatabaseSetup("/data/analysis/analysis-created-by-user1.xml")
    })
    public void pendingInvestigatorCantDeleteAnalysisFile() {

        AccessHelper analysis = new AccessHelper("Analysis");
        analysis.hasNoAccess("%s should not be able to delete analysis files" , ArachnePermission.DELETE_ANALYSIS_FILES);
    }

    private void checkPermission(String desc, ArachnePermission permission, String domain, boolean expectedResult) {

        checkPermission(desc, permission, 1L, domain, expectedResult);
    }

    private void checkPermission(String desc, ArachnePermission permission, Long targetId,
                                 String domain,
                                 boolean expectedResult) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean result = permissionEvaluator.hasPermission(authentication, targetId,
                domain, permission);
        assertThat(String.format(desc, authentication.getName()),
                result, is(expectedResult));
    }

    class AccessHelper {
        private String domain;

        public AccessHelper(String domain) {

            this.domain = domain;
        }

        private void hasAccess(String desc, ArachnePermission permission) {

            checkPermission(desc, permission, domain, true);
        }

        private void hasNoAccess(String desc, ArachnePermission permission) {

            checkPermission(desc, permission, domain, false);
        }
    }
}
