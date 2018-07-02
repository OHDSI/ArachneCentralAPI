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
 * Created: January 13, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto;

import java.util.List;

public class UserInfoDTO extends DTO {
    private String id;
    private String email;
    private String username;
    private String firstname;
    private String lastname;
    private String middlename;
    private Integer notificationsCount;
    private Boolean isAdmin;
    private List<TenantPersonalDTO> tenants;

    public UserInfoDTO() {
    }

    public UserInfoDTO(String id) {
        this.id = id;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {

        return email;
    }

    public void setEmail(String email) {

        this.email = email;
    }

    public String getFirstname() {

        return firstname;
    }

    public void setFirstname(String firstname) {

        this.firstname = firstname;
    }

    public String getLastname() {

        return lastname;
    }

    public void setLastname(String lastname) {

        this.lastname = lastname;
    }

    public String getMiddlename() {

        return middlename;
    }

    public void setMiddlename(String middlename) {

        this.middlename = middlename;
    }

    public Integer getNotificationsCount() {

        return notificationsCount;
    }

    public void setNotificationsCount(Integer notificationsCount) {

        this.notificationsCount = notificationsCount;
    }

    public Boolean getIsAdmin() {

        return isAdmin;
    }

    public void setIsAdmin(Boolean admin) {

        isAdmin = admin;
    }

    public List<TenantPersonalDTO> getTenants() {

        return tenants;
    }

    public void setTenants(List<TenantPersonalDTO> tenants) {

        this.tenants = tenants;
    }
}
