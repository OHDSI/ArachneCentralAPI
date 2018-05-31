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
 * Created: April 11, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.mail;

import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.UserStudy;

public class InvitationCollaboratorMailSender extends InvitationArachneMailMessage {

    public InvitationCollaboratorMailSender(String portalUrl, IUser user, UserStudy userStudy) {

        super(portalUrl, user, userStudy.getToken(), userStudy.getCreatedBy());

        parameters.put("studyUrl", portalUrl + "/study-manager/studies/"
                + String.valueOf(userStudy.getStudy().getId()));
        parameters.put("userFirstName", userStudy.getUser().getFirstname());
        parameters.put("studyTitle", userStudy.getStudy().getTitle());

        parameters.put("userStudyId", userStudy.getId());
    }

    @Override
    public String getSubject() {

        return "${app-title} invitation to study";
    }

    @Override
    public String getTemplate() {

        return "mail/invitation_collaborator";
    }
}
