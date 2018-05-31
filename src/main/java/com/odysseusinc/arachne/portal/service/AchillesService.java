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
 * Created: May 19, 2017
 *
 */

package com.odysseusinc.arachne.portal.service;

import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.model.achilles.AchillesFile;
import com.odysseusinc.arachne.portal.model.achilles.AchillesReport;
import com.odysseusinc.arachne.portal.model.achilles.Characterization;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.web.multipart.MultipartFile;


public interface AchillesService<DS extends IDataSource> {

    void createCharacterization(DS dataSource, MultipartFile data) throws IOException;

    List<Characterization> getCharacterizations(DS dataSource);

    Optional<Characterization> getCharacterization(DS dataSource, Long characterizationId);

    Optional<Characterization> getLatestCharacterization(DS dataSource);

    List<AchillesReport> getReports(AchillesFile file);

    List<AchillesReport> getReports(DS dataSource);

    Optional<AchillesFile> getAchillesFile(Long characterizationId, String filePath);

    Long getLatestCharacterizationId(DS dataSource) throws NotExistException;
}
