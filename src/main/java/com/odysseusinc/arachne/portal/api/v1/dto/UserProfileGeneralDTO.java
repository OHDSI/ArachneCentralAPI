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
 * Created: January 16, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonProfessionalTypeDTO;

public class UserProfileGeneralDTO extends DTO {

    private String affiliation;
    private String firstname;
    private String middlename;
    private String lastname;
    private String personalSummary;

    private CommonProfessionalTypeDTO professionalType;

    private String phone;
    private String mobile;
    private String address1;
    private String address2;
    private String city;
    private Long stateProvinceId;
    private String zipCode;
    private CountryDTO country;
    private String contactEmail;

    public String getAffiliation() {

        return affiliation;
    }

    public void setAffiliation(String affiliation) {

        this.affiliation = affiliation;
    }

    public String getFirstname() {

        return firstname;
    }

    public void setFirstname(String firstname) {

        this.firstname = firstname;
    }

    public String getMiddlename() {

        return middlename;
    }

    public void setMiddlename(String middlename) {

        this.middlename = middlename;
    }

    public String getLastname() {

        return lastname;
    }

    public void setLastname(String lastname) {

        this.lastname = lastname;
    }

    public CommonProfessionalTypeDTO getProfessionalType() {

        return professionalType;
    }

    public void setProfessionalType(CommonProfessionalTypeDTO professionalType) {

        this.professionalType = professionalType;
    }

    public String getPersonalSummary() {

        return personalSummary;
    }

    public void setPersonalSummary(String personalSummary) {

        this.personalSummary = personalSummary;
    }

    public String getPhone() {

        return phone;
    }

    public void setPhone(String phone) {

        this.phone = phone;
    }

    public String getMobile() {

        return mobile;
    }

    public void setMobile(String mobile) {

        this.mobile = mobile;
    }

    public String getAddress1() {

        return address1;
    }

    public void setAddress1(String address1) {

        this.address1 = address1;
    }

    public String getAddress2() {

        return address2;
    }

    public void setAddress2(String address2) {

        this.address2 = address2;
    }

    public String getCity() {

        return city;
    }

    public void setCity(String city) {

        this.city = city;
    }

    public Long getStateProvinceId() {

        return stateProvinceId;
    }

    public void setStateProvinceId(Long stateProvinceId) {

        this.stateProvinceId = stateProvinceId;
    }

    public String getZipCode() {

        return zipCode;
    }

    public void setZipCode(String zipCode) {

        this.zipCode = zipCode;
    }

    public CountryDTO getCountry() {

        return country;
    }

    public void setCountry(CountryDTO country) {

        this.country = country;
    }

    public String getContactEmail() {

        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {

        this.contactEmail = contactEmail;
    }
}
