/*
 *
 * Copyright 2018 Odysseus Data Services, inc.
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
 * Created: November 30, 2016
 *
 */

package com.odysseusinc.arachne.portal.model;

import static com.odysseusinc.arachne.portal.security.ArachnePermission.*;

import com.odysseusinc.arachne.portal.security.ArachnePermission;

public enum ParticipantRole {
    LEAD_INVESTIGATOR("Lead Investigator", new ArachnePermission[]{
            INVITE_DATANODE,
            INVITE_CONTRIBUTOR,
            EDIT_STUDY,
            CREATE_ANALYSIS,
            DELETE_ANALYSIS,
            UPLOAD_FILES,
            ACCESS_STUDY,
            CREATE_SUBMISSION,
            UPLOAD_ANALYSIS_FILES,
            DELETE_ANALYSIS_FILES,
            LOCK_ANALYSIS_FILE,
            UNLINK_DATASOURCE,
            EDIT_PAPER,
            LIMITED_EDIT_PAPER,
            ACCESS_PAPER,
            EDIT_ANALYSIS,
            EDIT_INSIGHT,
            UPDATE_SUBMISSION,
            ACCESS_SUBMISSION_RESULTS,
    }),
    CONTRIBUTOR("Contributor", new ArachnePermission[]{
            CREATE_ANALYSIS,
            DELETE_ANALYSIS,
            UPLOAD_FILES,
            ACCESS_STUDY,
            CREATE_SUBMISSION,
            UPLOAD_ANALYSIS_FILES,
            SENDING_UNLOCK_ANALYSIS_REQUEST,
            LIMITED_EDIT_PAPER,
            ACCESS_PAPER,
            EDIT_ANALYSIS,
            EDIT_INSIGHT,
            ACCESS_SUBMISSION_RESULTS
    }),
    // Defines user's ability to import Cohorts / PLEs / PLPs / etc from Data node
    DATA_NODE_IMPORTER("Data Node Importer", new ArachnePermission[] {
            IMPORT_FROM_DATANODE,
    }),
    DATA_SET_OWNER("Data Set Owner", new ArachnePermission[]{
            CREATE_ANALYSIS,
            UPLOAD_FILES,
            DELETE_ANALYSIS,
            ACCESS_STUDY,
            CREATE_SUBMISSION,
            UPLOAD_ANALYSIS_FILES,
            APPROVE_SUBMISSION,
            ACCESS_DATASOURCE,
            UNLINK_DATASOURCE,
            ACCESS_PAPER,
            EDIT_ANALYSIS,
            EDIT_INSIGHT,
            ACCESS_SUBMISSION_RESULTS
    }),
    ANALYSIS_OWNER("Analysis owner", new ArachnePermission[]{
            UPDATE_SUBMISSION
    }),
    STUDY_READER("Study reader", new ArachnePermission[]{
            ACCESS_STUDY,
    }),
    DATANODE_ADMIN("DataNode admin", new ArachnePermission[]{
            EDIT_DATANODE,
            EDIT_DATASOURCE,
            DELETE_DATASOURCE,
            ACCESS_ACHILLES_REPORT_PERMISSION,
            EDIT_ACHILLES_REPORT_PERMISSION,
            CREATE_DATASOURCE,
            ACCESS_DATASOURCE,
            UPLOAD_ACHILLES_REPORTS,
            ACCESS_SUBMISSION_RESULTS
    }),
    STUDY_PENDING_CONTRIBUTOR("Pending contributor", new ArachnePermission[]{
            ACCESS_STUDY,
    }),
    DATA_SET_USER("Data Set User", new ArachnePermission[]{
            ACCESS_DATASOURCE,
            ACCESS_SUBMISSION_RESULTS
    }),
    PAPER_READER("Paper reader", new ArachnePermission[]{
            ACCESS_PAPER
    }),
    ORGANIZATION_ADMIN("Organization admin", new ArachnePermission[] {
            ACCESS_ORGANIZATION,
            CREATE_ORGANIZATION,
            UPDATE_ORGANIZATION,
            DELETE_ORGANIZATION,
    }),
    ORGANIZATION_CREATOR("Organization creator", new ArachnePermission[] {
            ACCESS_ORGANIZATION,
            CREATE_ORGANIZATION
    }),
    ORGANIZATION_READER("Organization user", new ArachnePermission[] {
            ACCESS_ORGANIZATION
    });

    private String title;
    private ArachnePermission[] arachnePermissions;

    ParticipantRole(String role, ArachnePermission[] permissions) {

        arachnePermissions = permissions;
        title = role;
    }

    public ArachnePermission[] getPermissions() {

        return arachnePermissions;
    }

    @Override
    public String toString() {

        return title;
    }
}
