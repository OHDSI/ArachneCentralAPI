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
 * Created: November 30, 2016
 *
 */

package com.odysseusinc.arachne.portal.model;

import static com.odysseusinc.arachne.portal.security.ArachnePermission.ACCESS_DATASOURCE;
import static com.odysseusinc.arachne.portal.security.ArachnePermission.ACCESS_PAPER;
import static com.odysseusinc.arachne.portal.security.ArachnePermission.ACCESS_STUDY;
import static com.odysseusinc.arachne.portal.security.ArachnePermission.APPROVE_SUBMISSION;
import static com.odysseusinc.arachne.portal.security.ArachnePermission.CREATE_ANALYSIS;
import static com.odysseusinc.arachne.portal.security.ArachnePermission.CREATE_DATASOURCE;
import static com.odysseusinc.arachne.portal.security.ArachnePermission.CREATE_SUBMISSION;
import static com.odysseusinc.arachne.portal.security.ArachnePermission.DELETE_ANALYSIS;
import static com.odysseusinc.arachne.portal.security.ArachnePermission.DELETE_ANALYSIS_FILES;
import static com.odysseusinc.arachne.portal.security.ArachnePermission.DELETE_DATASOURCE;
import static com.odysseusinc.arachne.portal.security.ArachnePermission.EDIT_DATANODE;
import static com.odysseusinc.arachne.portal.security.ArachnePermission.EDIT_PAPER;
import static com.odysseusinc.arachne.portal.security.ArachnePermission.EDIT_STUDY;
import static com.odysseusinc.arachne.portal.security.ArachnePermission.INVITE_CONTRIBUTOR;
import static com.odysseusinc.arachne.portal.security.ArachnePermission.INVITE_DATANODE;
import static com.odysseusinc.arachne.portal.security.ArachnePermission.LIMITED_EDIT_PAPER;
import static com.odysseusinc.arachne.portal.security.ArachnePermission.LOCK_ANALYSIS_FILE;
import static com.odysseusinc.arachne.portal.security.ArachnePermission.SENDING_UNLOCK_ANALYSIS_REQUEST;
import static com.odysseusinc.arachne.portal.security.ArachnePermission.UNLINK_DATASOURCE;
import static com.odysseusinc.arachne.portal.security.ArachnePermission.UPLOAD_ANALYSIS_FILES;
import static com.odysseusinc.arachne.portal.security.ArachnePermission.UPLOAD_FILES;

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
            ACCESS_PAPER
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
            ACCESS_PAPER
    }),
    DATA_SET_OWNER("Data Set Owner", new ArachnePermission[]{
            CREATE_ANALYSIS,
            UPLOAD_FILES,
            DELETE_ANALYSIS,
            ACCESS_STUDY,
            CREATE_SUBMISSION,
            UPLOAD_ANALYSIS_FILES,
            APPROVE_SUBMISSION,
            EDIT_DATANODE,
            CREATE_DATASOURCE,
            DELETE_DATASOURCE,
            ACCESS_DATASOURCE,
            UNLINK_DATASOURCE,
            ACCESS_PAPER
    }),
    DATA_SET_USER("Data Set User", new ArachnePermission[]{
            ACCESS_DATASOURCE
    }),
    PAPER_READER("Paper reader", new ArachnePermission[]{
            ACCESS_PAPER
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
