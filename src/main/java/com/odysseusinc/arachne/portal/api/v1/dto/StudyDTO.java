/*
 *
 * Copyright 2017 Observational Health Data Sciences and Informatics
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

import com.odysseusinc.arachne.portal.api.v1.dto.dictionary.StudyStatusDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.dictionary.StudyTypeDTO;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class StudyDTO {

    public Long id;

    private String title;

    private StudyTypeDTO type;

    private String description;

    private String role;

    private List<ParticipantDTO> participants = new ArrayList<>();

    private List<DataSourceDTO> dataSources = new LinkedList<>();

    private List<BaseAnalysisDTO> analyses = new LinkedList<>();

    private Date created;

    private StudyStatusDTO status;

    private Date updated;

    private Date startDate;

    private Date endDate;

    private List<StudyFileDTO> files = new LinkedList<>();

    private PermissionsDTO permissions;

    private Boolean favourite;

    private Long paperId;

    private Boolean privacy;


    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public List<ParticipantDTO> getParticipants() {

        return participants;
    }

    public void setParticipants(List<ParticipantDTO> participants) {

        this.participants = participants;
    }

    public List<DataSourceDTO> getDataSources() {

        return dataSources;
    }

    public void setDataSources(List<DataSourceDTO> dataSources) {

        this.dataSources = dataSources;
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


    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public String getTitle() {

        return title;
    }

    public void setTitle(String title) {

        this.title = title;
    }

    public StudyStatusDTO getStatus() {

        return status;
    }

    public void setStatus(StudyStatusDTO status) {

        this.status = status;
    }

    public StudyTypeDTO getType() {

        return type;
    }

    public void setType(StudyTypeDTO type) {

        this.type = type;
    }

    public String getRole() {

        return role;
    }

    public void setRole(String role) {

        this.role = role;
    }

    public List<BaseAnalysisDTO> getAnalyses() {

        return analyses;
    }

    public void setAnalyses(List<BaseAnalysisDTO> analyses) {

        this.analyses = analyses;
    }

    public List<StudyFileDTO> getFiles() {

        return files;
    }

    public void setFiles(List<StudyFileDTO> files) {

        this.files = files;
    }

    public PermissionsDTO getPermissions() {

        return permissions;
    }

    public void setPermissions(PermissionsDTO permissions) {

        this.permissions = permissions;
    }

    public Boolean getFavourite() {

        return favourite;
    }

    public void setFavourite(Boolean favourite) {

        this.favourite = favourite;
    }

    public Long getPaperId() {

        return paperId;
    }

    public void setPaperId(Long paperId) {

        this.paperId = paperId;
    }

    public Boolean getPrivacy() {

        return privacy;
    }

    public void setPrivacy(Boolean privacy) {

        this.privacy = privacy;
    }
}
