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
 * Created: October 03, 2017
 *
 */

package com.odysseusinc.arachne.portal.service;

import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.CommentTopic;
import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.Organization;
import com.odysseusinc.arachne.portal.model.Paper;
import com.odysseusinc.arachne.portal.model.ParticipantRole;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.model.SubmissionGroup;
import com.odysseusinc.arachne.portal.model.SubmissionInsight;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.model.security.ArachneUser;
import com.odysseusinc.arachne.portal.security.ArachnePermission;
import java.util.List;
import java.util.Set;

public interface BaseArachneSecureService<P extends Paper, DS extends IDataSource> {
    List<ParticipantRole> getRolesByStudy(ArachneUser user, Study study);

    List<ParticipantRole> getRolesByAnalysis(ArachneUser user, Analysis analysis);

    List<ParticipantRole> getRolesBySubmission(ArachneUser user, Submission submission);

    List<ParticipantRole> getRolesByDataSource(ArachneUser user, DS dataSource);

    List<ParticipantRole> getRolesByDataNode(ArachneUser user, DataNode dataNode);

    List<ParticipantRole> getRolesBySubmissionGroup(ArachneUser user, SubmissionGroup submissionGroup);

    List<ParticipantRole> getRolesByPaper(ArachneUser user, P domainObject);

    List<ParticipantRole> getRolesByInsight(ArachneUser user, SubmissionInsight domainObject);

    List<ParticipantRole> getRolesByCommentTopic(ArachneUser user, CommentTopic topic);

    Set<ArachnePermission> getPermissionsForUser(ArachneUser user, IUser targetUser);

    boolean canImportFromDatanode(ArachneUser user, DataNode dataNode);

    boolean wasDataSourceApproved(Analysis analysis, Long dataSourceId);

    List<ParticipantRole> getRolesByOrganization(ArachneUser user, Organization organization);
}
