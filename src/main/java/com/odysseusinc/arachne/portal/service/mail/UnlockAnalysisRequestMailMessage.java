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
 * Created: May 17, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.mail;

import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.AnalysisUnlockRequest;
import com.odysseusinc.arachne.portal.model.IUser;

public class UnlockAnalysisRequestMailMessage extends InvitationArachneMailMessage {

    public UnlockAnalysisRequestMailMessage(String portalUrl, IUser user, AnalysisUnlockRequest request) {

        super(portalUrl, user, request.getToken(), request.getAuthor());
        final Analysis analysis = request.getAnalysis();
        parameters.put("analysisUrl", portalUrl + "/analysis-execution/analyses/" + analysis.getId());
        parameters.put("analysisTitle", analysis.getTitle());
        parameters.put("requestId", request.getId());
        parameters.put("userUuid", user.getUuid());
    }

    @Override
    public String getSubject() {

        return "${app-title} unlock analysis request";
    }

    @Override
    public String getTemplate() {

        return "mail/unlock_analysis_request";
    }
}
