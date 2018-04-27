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
 * Created: September 11, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters.study;

import com.odysseusinc.arachne.portal.api.v1.dto.BaseAnalysisDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.DataSourceDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.ParticipantDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.PermissionsDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.StudyDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.StudyFileDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.converters.BaseConversionServiceAwareConverter;
import com.odysseusinc.arachne.portal.api.v1.dto.dictionary.StudyStatusDTO;
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
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

public abstract class BaseStudyToStudyDTOConverter<S extends Study, DTO extends StudyDTO> extends BaseConversionServiceAwareConverter<S, DTO> {


    private final BaseStudyService studyService;
    protected final AnalysisService analysisService;

    @Autowired
    public BaseStudyToStudyDTOConverter(BaseStudyService studyService, AnalysisService analysisService) {

        this.studyService = studyService;
        this.analysisService = analysisService;
    }

    @Override
    public DTO convert(final S source) {

        final DTO studyDTO = createResultObject();
        studyDTO.setStatus(conversionService.convert(source.getStatus(), StudyStatusDTO.class));
        studyDTO.setTitle(source.getTitle());
        studyDTO.setType(conversionService.convert(source.getType(), StudyTypeDTO.class));
        studyDTO.setEndDate(source.getEndDate());
        studyDTO.setStartDate(source.getStartDate());
        studyDTO.setDescription(source.getDescription());
        
        final Tenant studyTenant = source.getTenant();

        for (final UserStudyExtended studyUserLink : source.getParticipants()) {
            
            final ParticipantDTO participantDTO = conversionService.convert(studyUserLink, ParticipantDTO.class);
            
            if (!studyUserLink.getUser().getTenants().contains(studyTenant)) {
                participantDTO.setCanBeRecreated(Boolean.FALSE);
            }
            
            studyDTO.getParticipants().add(participantDTO);
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

            studyDTO.getDataSources().add(dataSourceDTO);
        }

        List<Analysis> analyses = getAnalyses(source);
        for (final Analysis analysis : analyses) {
            studyDTO.getAnalyses().add(conversionService.convert(analysis, BaseAnalysisDTO.class));
        }

        List<StudyFile> files = studyService.getFilesByStudyId(
                source.getId(),
                EntityUtils.fromAttributePaths("author")
        );
        for (final StudyFile studyFile : files) {
            studyDTO.getFiles().add(conversionService.convert(studyFile, StudyFileDTO.class));
        }

        studyDTO.setCreated(source.getCreated());
        studyDTO.setUpdated(source.getUpdated());
        studyDTO.setId(source.getId());
        studyDTO.setPermissions(conversionService.convert(source, PermissionsDTO.class));

        studyDTO.setPaperId(source.getPaper() == null ? null : source.getPaper().getId());
        studyDTO.setPrivacy(source.getPrivacy());

        proceedAdditionalFields(studyDTO, source);

        return studyDTO;
    }

    protected List<Analysis> getAnalyses(S source) {

        return analysisService.getByStudyId(
                source.getId(),
                EntityUtils.fromAttributePaths("author")
        );
    }
}
