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
 * Authors: Anastasiia Klochkova
 * Created: June 5, 2018
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters.study;

import com.odysseusinc.arachne.portal.api.v1.dto.BaseAnalysisDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.DataSourceDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.ParticipantDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.StudyFileDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.WorkspaceDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.converters.BaseConversionServiceAwareConverter;
import com.odysseusinc.arachne.portal.api.v1.dto.dictionary.StudyTypeDTO;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.StudyDataSourceLink;
import com.odysseusinc.arachne.portal.model.StudyFile;
import com.odysseusinc.arachne.portal.model.UserStudyExtended;
import com.odysseusinc.arachne.portal.model.security.Tenant;
import com.odysseusinc.arachne.portal.service.BaseStudyService;
import com.odysseusinc.arachne.portal.service.analysis.AnalysisService;
import com.odysseusinc.arachne.portal.util.EntityUtils;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseStudyToWorkspaceDTOConverter<S extends Study, DTO extends WorkspaceDTO> extends BaseConversionServiceAwareConverter<S, DTO> {
    private final BaseStudyService studyService;
    protected final AnalysisService analysisService;

    @Autowired
    public BaseStudyToWorkspaceDTOConverter(BaseStudyService studyService, AnalysisService analysisService) {

        this.studyService = studyService;
        this.analysisService = analysisService;
    }

    @Override
    public DTO convert(S source) {
        final DTO workspaceDTO = createResultObject();
        workspaceDTO.setTitle(source.getTitle());
        workspaceDTO.setType(conversionService.convert(source.getType(), StudyTypeDTO.class));
        workspaceDTO.setDescription(source.getDescription());

        final Tenant studyTenant = source.getTenant();
        for (final UserStudyExtended studyUserLink : source.getParticipants()) {
            final ParticipantDTO participantDTO = conversionService.convert(studyUserLink, ParticipantDTO.class);
            if (!studyUserLink.getUser().getTenants().contains(studyTenant)) {
                participantDTO.setCanBeRecreated(Boolean.FALSE);
            }
            workspaceDTO.getParticipants().add(participantDTO);
        }

        final List<StudyDataSourceLink> foundLinks = studyService.getLinksByStudyId(
                source.getId(),
                EntityUtils.fromAttributePaths(
                        "dataSource.dataNode.dataNodeUsers.user"
                )
        );
        for (final StudyDataSourceLink studyDataSourceLink : foundLinks) {
            final DataSourceDTO dataSourceDTO = conversionService.convert(studyDataSourceLink,
                    DataSourceDTO.class);
            if (!studyDataSourceLink.getDataSource().getTenants().contains(studyTenant)) {
                dataSourceDTO.setCanBeRecreated(Boolean.FALSE);
            }
            workspaceDTO.getDataSources().add(dataSourceDTO);
        }
        List<Analysis> analyses = getAnalyses(source);
        for (final Analysis analysis : analyses) {
            workspaceDTO.getAnalyses().add(conversionService.convert(analysis, BaseAnalysisDTO.class));
        }
        List<StudyFile> files = studyService.getFilesByStudyId(
                source.getId(),
                EntityUtils.fromAttributePaths("author")
        );
        for (final StudyFile studyFile : files) {
            workspaceDTO.getFiles().add(conversionService.convert(studyFile, StudyFileDTO.class));
        }
        workspaceDTO.setId(source.getId());
        workspaceDTO.setPrivacy(source.getPrivacy());
        return workspaceDTO;
    }
    protected List<Analysis> getAnalyses(S source) {

        return analysisService.getByStudyId(
                source.getId(),
                EntityUtils.fromAttributePaths("author")
        );
    }
}
