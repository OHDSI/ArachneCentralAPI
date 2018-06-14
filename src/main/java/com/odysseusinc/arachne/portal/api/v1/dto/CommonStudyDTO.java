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
 * Authors: Anastasiia Klochkova
 * Created: June 8, 2018
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto;

import com.odysseusinc.arachne.portal.model.StudyKind;
import java.util.LinkedList;
import java.util.List;

public class CommonStudyDTO {
    public Long id;

    private String title;

    private List<DataSourceDTO> dataSources = new LinkedList<>();

    private List<BaseAnalysisDTO> analyses = new LinkedList<>();

    private List<StudyFileDTO> files = new LinkedList<>();

    private StudyKind kind;

    private PermissionsDTO permissions;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public List<DataSourceDTO> getDataSources() {

        return dataSources;
    }

    public void setDataSources(List<DataSourceDTO> dataSources) {

        this.dataSources = dataSources;
    }

    public String getTitle() {

        return title;
    }

    public void setTitle(String title) {

        this.title = title;
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

    public StudyKind getKind() {

        return kind;
    }

    public void setKind(StudyKind kind) {

        this.kind = kind;
    }

    public PermissionsDTO getPermissions() {

        return permissions;
    }

    public void setPermissions(final PermissionsDTO permissions) {

        this.permissions = permissions;
    }
}
