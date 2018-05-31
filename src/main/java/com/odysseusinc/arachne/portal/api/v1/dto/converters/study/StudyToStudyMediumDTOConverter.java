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
 * Created: September 08, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters.study;

import static com.odysseusinc.arachne.portal.model.ParticipantRole.CONTRIBUTOR;
import static com.odysseusinc.arachne.portal.model.ParticipantRole.LEAD_INVESTIGATOR;

import com.odysseusinc.arachne.portal.api.v1.dto.DataSourceDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.ParticipantDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.ParticipantExtendedDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.StudyMediumDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.dictionary.StudyStatusDTO;
import com.odysseusinc.arachne.portal.model.ParticipantRole;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.StudyDataSourceLink;
import com.odysseusinc.arachne.portal.model.UserStudyExtended;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import com.odysseusinc.arachne.portal.api.v1.dto.converters.BaseConversionServiceAwareConverter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class StudyToStudyMediumDTOConverter extends BaseConversionServiceAwareConverter<Study, StudyMediumDTO> {

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {

        Map<Object, Boolean> seen = new HashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    @Override
    public StudyMediumDTO convert(Study source) {

        final StudyMediumDTO studyDTO = new StudyMediumDTO();

        studyDTO.setId(source.getId());
        studyDTO.setTitle(source.getTitle());
        studyDTO.setDescription(source.getDescription());
        studyDTO.setCreated(source.getCreated());
        studyDTO.setStartDate(source.getStartDate());
        studyDTO.setEndDate(source.getEndDate());
        studyDTO.setStatus(conversionService.convert(source.getStatus(), StudyStatusDTO.class));

        final Map<ParticipantRole, List<UserStudyExtended>> studyParticipants = source.getParticipants()
                .stream()
                .collect(Collectors.groupingBy(link -> link.getRole() == ParticipantRole.LEAD_INVESTIGATOR
                        ? LEAD_INVESTIGATOR : CONTRIBUTOR));

        final List<UserStudyExtended> studyLeads = studyParticipants.get(LEAD_INVESTIGATOR);
        if (!CollectionUtils.isEmpty(studyLeads)) {
            studyDTO.setStudyLeads(studyLeads.stream()
                    .filter(distinctByKey(p -> p.getUser().getId()))
                    .map(studyParticipant -> conversionService.convert(studyParticipant, ParticipantDTO.class))
                    .collect(Collectors.toList())
            );
        }

        final List<UserStudyExtended> studyContributors = studyParticipants.get(CONTRIBUTOR);
        if (!CollectionUtils.isEmpty(studyContributors)) {
            studyDTO.setStudyParticipants(studyContributors.stream()
                    .filter(distinctByKey(p -> p.getUser().getId()))
                    .map(studyParticipant -> conversionService.convert(studyParticipant, ParticipantExtendedDTO.class))
                    .collect(Collectors.toList())
            );
        }

        final List<StudyDataSourceLink> studyDataSources = source.getDataSources();

        studyDTO.setStudyDataSources(studyDataSources.stream()
                .map(studyDataSource -> conversionService.convert(studyDataSource.getDataSource(), DataSourceDTO.class))
                .collect(Collectors.toList())
        );

        return studyDTO;
    }
}
