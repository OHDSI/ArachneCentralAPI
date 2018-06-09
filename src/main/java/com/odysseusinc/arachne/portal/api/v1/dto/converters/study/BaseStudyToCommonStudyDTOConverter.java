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
 * Created: June 7, 2018
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters.study;

import com.odysseusinc.arachne.portal.api.v1.dto.BaseAnalysisDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.CommonStudyDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.DataSourceDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.PermissionsDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.StudyFileDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.converters.BaseConversionServiceAwareConverter;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.StudyDataSourceLink;
import com.odysseusinc.arachne.portal.model.StudyFile;
import com.odysseusinc.arachne.portal.model.security.Tenant;
import com.odysseusinc.arachne.portal.service.BaseStudyService;
import com.odysseusinc.arachne.portal.service.analysis.AnalysisService;
import com.odysseusinc.arachne.portal.util.ArachneConverterUtils;
import com.odysseusinc.arachne.portal.util.EntityUtils;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseStudyToCommonStudyDTOConverter<S extends Study, DTO extends CommonStudyDTO> extends BaseConversionServiceAwareConverter<S, DTO> {
    protected final BaseStudyService studyService;
    protected final AnalysisService analysisService;
    protected final ArachneConverterUtils converterUtils;

    @Autowired
    public BaseStudyToCommonStudyDTOConverter(BaseStudyService studyService, AnalysisService analysisService, ArachneConverterUtils converterUtils) {

        this.studyService = studyService;
        this.analysisService = analysisService;
        this.converterUtils = converterUtils;
    }

    @Override
    public DTO convert(S source) {

        final DTO commonDTO = createResultObject();
        commonDTO.setTitle(source.getTitle());
        final Tenant studyTenant = source.getTenant();
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
            commonDTO.getDataSources().add(dataSourceDTO);
        }
        commonDTO.setAnalyses(converterUtils.convertList(getAnalyses(source), BaseAnalysisDTO.class));
        List<StudyFile> files = studyService.getFilesByStudyId(
                source.getId(),
                EntityUtils.fromAttributePaths("author")
        );
        commonDTO.setFiles(converterUtils.convertList(files, StudyFileDTO.class));
        commonDTO.setId(source.getId());
        commonDTO.setKind(source.getKind());
        commonDTO.setPermissions(conversionService.convert(source, PermissionsDTO.class));
        return commonDTO;
    }

    protected List<Analysis> getAnalyses(S source) {

        return analysisService.getByStudyId(
                source.getId(),
                EntityUtils.fromAttributePaths("author")
        );
    }
}
