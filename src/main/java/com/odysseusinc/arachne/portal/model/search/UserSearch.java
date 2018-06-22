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
 * Created: October 05, 2017
 *
 */

package com.odysseusinc.arachne.portal.model.search;

public class UserSearch {
    private Boolean enabled;
    private Boolean emailConfirmed;
    private String query;
    private Long[] tenantIds;

    public Boolean getEnabled() {

        return enabled;
    }

    public void setEnabled(Boolean enabled) {

        this.enabled = enabled;
    }

    public Boolean getEmailConfirmed() {

        return emailConfirmed;
    }

    public void setEmailConfirmed(Boolean emailConfirmed) {

        this.emailConfirmed = emailConfirmed;
    }

    public String getQuery() {

        return query;
    }

    public void setQuery(String query) {

        this.query = query;
    }

    public Long[] getTenantIds() {

        return tenantIds;
    }

    public void setTenantIds(Long[] tenantIds) {

        this.tenantIds = tenantIds;
    }
}
