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
 * Created: May 26, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.mail;

import com.odysseusinc.arachne.portal.model.IUser;

public class RegistrationMailMessage extends ArachneMailMessage implements UserRegistrantMailMessage {

    private String subject = "${app-title} registration";
    private String template = "mail/registration";

    public RegistrationMailMessage(IUser user, String portalUrl, String registrationCode) {

        super(user);
        parameters.put("userFirstName", user.getFirstname());
        parameters.put("portalUrl", portalUrl);
        parameters.put("registrationCode", registrationCode);
        parameters.put("callbackUrl", portalUrl + "/auth/login");
    }

    @Override
    protected String getSubject() {

        return subject;
    }

    public void setSubject(String subject) {

        this.subject = subject;
    }

    @Override
    protected String getTemplate() {

        return template;
    }

    public void setTemplate(String template) {

        this.template = template;
    }

    public void setCallbackUrl(String callbackUrl) {

        parameters.put("callbackUrl", callbackUrl);
    }
}
