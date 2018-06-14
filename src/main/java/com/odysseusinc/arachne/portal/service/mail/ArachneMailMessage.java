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

import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.User;
import java.util.HashMap;
import java.util.Map;

public abstract class ArachneMailMessage {

    private String fromPersonal = "${app-title}";
    protected Map<String, Object> parameters = new HashMap<>();
    protected IUser user;

    protected ArachneMailMessage(IUser user) {

        this.user = user;
        parameters.put("userFirstName", user.getFirstname());
    }

    public final Map<String, Object> getParameters() {

        return parameters;
    }

    public IUser getUser() {

        return user;
    }

    String getFromPersonal() {

        return fromPersonal;
    }

    public void setFromPersonal(String fromPersonal) {

        this.fromPersonal = fromPersonal;
    }

    protected abstract String getSubject();

    /**
     * @return Email template path (relatively to "classpath:templates/") OR template content
     */
    protected abstract String getTemplate();
}
