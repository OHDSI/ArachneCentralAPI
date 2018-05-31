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
 * Created: August 03, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto;

import com.odysseusinc.arachne.portal.api.v1.dto.dictionary.StudyStatusDTO;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StudyMediumDTO extends StudyShortDTO {

    private String description;
    private Date created;
    private Date startDate;
    private Date endDate;
    private List<ParticipantDTO> studyLeads = new ArrayList<>();
    private List<ParticipantExtendedDTO> studyParticipants = new ArrayList<>();
    private List<DataSourceDTO> studyDataSources = new ArrayList<>();
    private StudyStatusDTO status;

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public Date getCreated() {

        return created;
    }

    public void setCreated(Date created) {

        this.created = created;
    }

    public Date getStartDate() {

        return startDate;
    }

    public void setStartDate(Date startDate) {

        this.startDate = startDate;
    }

    public Date getEndDate() {

        return endDate;
    }

    public void setEndDate(Date endDate) {

        this.endDate = endDate;
    }

    public List<ParticipantDTO> getStudyLeads() {

        return studyLeads;
    }

    public void setStudyLeads(List<ParticipantDTO> studyLeads) {

        this.studyLeads = studyLeads;
    }

    public List<ParticipantExtendedDTO> getStudyParticipants() {

        return studyParticipants;
    }

    public void setStudyParticipants(List<ParticipantExtendedDTO> studyParticipants) {

        this.studyParticipants = studyParticipants;
    }

    public List<DataSourceDTO> getStudyDataSources() {

        return studyDataSources;
    }

    public void setStudyDataSources(List<DataSourceDTO> studyDataSources) {

        this.studyDataSources = studyDataSources;
    }

    public StudyStatusDTO getStatus() {

        return status;
    }

    public void setStatus(StudyStatusDTO status) {

        this.status = status;
    }


}
