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
 * Created: January 15, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto;

import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

/**
 * Created by PGrafkin on 30.12.2016.
 */
public class RemindPasswordDTO extends DTO {
    @NotBlank
    @Email
    private String email;

    private String callbackUrl;

    private String registrantToken;

    public String getEmail() {

        return email;
    }

    public void setEmail(String email) {

        this.email = email;
    }

    public String getCallbackUrl() {

        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {

        this.callbackUrl = callbackUrl;
    }

    public String getRegistrantToken() {
        return registrantToken;
    }

    public void setRegistrantToken(String registrantToken) {

        this.registrantToken = registrantToken;
    }
}
