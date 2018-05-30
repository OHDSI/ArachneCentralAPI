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

import com.odysseusinc.arachne.portal.model.DataSource;
import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.model.PaperStudyDataSource;
import com.odysseusinc.arachne.portal.model.StudyDataSourceLink;
import org.springframework.beans.factory.annotation.Autowired;
import com.odysseusinc.arachne.portal.api.v1.dto.converters.BaseConversionServiceAwareConverter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;

@Component
public class StudyDataSourceLinkToPaperStudyDataSourceConverter
        extends BaseConversionServiceAwareConverter<StudyDataSourceLink, PaperStudyDataSource> {

    @Override
    public PaperStudyDataSource convert(StudyDataSourceLink studyDataSourceLink) {

        final PaperStudyDataSource studyDataSource = new PaperStudyDataSource();

        final IDataSource dataSource = studyDataSourceLink.getDataSource();
        studyDataSource.setDataSource(dataSource);

        return studyDataSource;
    }
}
