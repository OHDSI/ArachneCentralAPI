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
 * Authors: Sergey Maletsky
 * Created: May 23, 2018
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonUserRegistrationDTO;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import java.util.List;

public class BulkUsersRegistrationDTO {
    private boolean emailConfirmationRequired;
    @NotEmpty
    private List<Long> tenantIds;
    @NotEmpty
    private String password;
    @Valid
    private List<CommonUserRegistrationDTO> users;

    public boolean getEmailConfirmationRequired() {

        return emailConfirmationRequired;
    }

    public void setEmailConfirmationRequired(boolean emailConfirmationRequired) {

        this.emailConfirmationRequired = emailConfirmationRequired;
    }

    public List<Long> getTenantIds() {

        return tenantIds;
    }

    public void setTenantIds(List<Long> tenantIds) {

        this.tenantIds = tenantIds;
    }

    public String getPassword() {

        return password;
    }

    public void setPassword(String password) {

        this.password = password;
    }

    public List<CommonUserRegistrationDTO> getUsers() {

        return users;
    }

    public void setUsers(List<CommonUserRegistrationDTO> users) {

        this.users = users;
    }
}
