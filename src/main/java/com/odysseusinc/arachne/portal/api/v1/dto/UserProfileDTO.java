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

import com.odysseusinc.arachne.portal.api.v1.dto.dictionary.SkillDTO;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by AKrutov on 19.10.2016.
 */
public class UserProfileDTO extends DTO {
    private String id;

    private UserProfileGeneralDTO general;

    private Set<SkillDTO> skills;
    private List<UserPublicationDTO> publications;
    private List<UserLinkDTO> links;
    private Boolean enabled;
    private Date created;
    private Date updated;
    private Boolean isEditable;


    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public UserProfileGeneralDTO getGeneral() {

        return general;
    }

    public void setGeneral(UserProfileGeneralDTO general) {

        this.general = general;
    }

    public Set<SkillDTO> getSkills() {

        return skills;
    }

    public void setSkills(Set<SkillDTO> skills) {

        this.skills = skills;
    }

    public List<UserPublicationDTO> getPublications() {

        return publications;
    }

    public void setPublications(List<UserPublicationDTO> publications) {

        this.publications = publications;
    }

    public List<UserLinkDTO> getLinks() {

        return links;
    }

    public void setLinks(List<UserLinkDTO> links) {

        this.links = links;
    }

    public Boolean getEnabled() {

        return enabled;
    }

    public void setEnabled(Boolean enabled) {

        this.enabled = enabled;
    }

    public Date getCreated() {

        return created;
    }

    public void setCreated(Date created) {

        this.created = created;
    }

    public Date getUpdated() {

        return updated;
    }

    public void setUpdated(Date updated) {

        this.updated = updated;
    }

    public Boolean getIsEditable() {

        return isEditable;
    }

    public void setIsEditable(Boolean editable) {

        isEditable = editable;
    }
}
