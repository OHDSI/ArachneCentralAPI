/*
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
 * Authors: Anton Gackovka
 * Created: February 20, 2018
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.model.UserStudyExtended;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AnalysisSolrExtractors {

    public static class StudyIdExtractor implements SolrFieldExtractor<Analysis> {

        @Override
        public Object extract(final Analysis analysis) {

            final Study study = analysis.getStudy();

            return study.getId();
        }
    }

    public static class TitleExtractor implements SolrFieldExtractor<Analysis> {

        @Override
        public Object extract(final Analysis analysis) {

            return analysis.getTitle();
        }
    }
}
