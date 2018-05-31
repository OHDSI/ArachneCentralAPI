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
 * Created: February 11, 2017
 *
 */

package com.odysseusinc.arachne.portal.security;

import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.AnalysisFile;
import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.model.Organization;
import com.odysseusinc.arachne.portal.model.Paper;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.model.SubmissionInsight;
import com.odysseusinc.arachne.portal.model.User;

public enum ArachnePermission {
    EDIT_STUDY(Study.class),
    ACCESS_STUDY(Study.class),
    INVITE_CONTRIBUTOR(Study.class),
    INVITE_DATANODE(Study.class),
    UNLINK_DATASOURCE(Study.class),
    UPLOAD_FILES(Study.class),
    CREATE_ANALYSIS(Study.class),
    DELETE_ANALYSIS(Analysis.class),
    EDIT_ANALYSIS(Analysis.class),
    DELETE_ANALYSIS_FILES(Analysis.class, AnalysisFile.class),
    UPLOAD_ANALYSIS_FILES(Analysis.class),
    LOCK_ANALYSIS_FILE(Analysis.class),
    CREATE_SUBMISSION(Analysis.class),
    APPROVE_SUBMISSION(Submission.class),
    EDIT_DATANODE(DataNode.class),
    IMPORT_FROM_DATANODE(DataNode.class),
    ACCESS_DATASOURCE(IDataSource.class),
    CREATE_DATASOURCE(DataNode.class),
    EDIT_DATASOURCE(IDataSource.class),
    DELETE_DATASOURCE(IDataSource.class),
    SENDING_UNLOCK_ANALYSIS_REQUEST(Analysis.class),
    EDIT_PAPER(Paper.class),
    EDIT_INSIGHT(SubmissionInsight.class),
    ACCESS_PAPER(Paper.class),
    LIMITED_EDIT_PAPER(Paper.class),
    ACCESS_USER(User.class),
    UPLOAD_ACHILLES_REPORTS(IDataSource.class),
    ACCESS_ACHILLES_REPORT_PERMISSION(IDataSource.class),
    EDIT_ACHILLES_REPORT_PERMISSION(IDataSource.class),
    UPDATE_SUBMISSION(Submission.class),
    ACCESS_ORGANIZATION(Organization.class),
    CREATE_ORGANIZATION(Organization.class),
    UPDATE_ORGANIZATION(Organization.class),
    DELETE_ORGANIZATION(Organization.class);

    private Class<?>[] applicableClass;

    ArachnePermission(Class<?>... clazz) {

        applicableClass = clazz;
    }

    public Class<?>[] getApplicableClass() {

        return applicableClass;
    }
}
