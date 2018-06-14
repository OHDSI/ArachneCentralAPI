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
 * Created: November 26, 2016
 *
 */

package com.odysseusinc.arachne.portal.repository;

import com.odysseusinc.arachne.portal.model.AnalysisFile;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisFileRepository extends JpaRepository<AnalysisFile, Long> {

    AnalysisFile findByUuid(String uuid);

    List<AnalysisFile> findAllByAnalysisIdAndRealName(Long analysisId, String name);

    List<AnalysisFile> findAllByAnalysisIdAndDataReferenceId(Long analysisId, Long dataReferenceId);

    List<AnalysisFile> findByIdIn(List<Long> ids);

    List<AnalysisFile> findAllByAnalysisIdAndLabel(Long analysisId, String label);
}
