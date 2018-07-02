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
 * Created: March 13, 2018
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.portal.model.Paper;
import com.odysseusinc.arachne.portal.model.Study;

public class PaperSolrExtractors {

    public static class TitleExtractor implements SolrFieldExtractor<Paper> {

        @Override
        public String extract(final Paper paper) {

            final Study study = paper.getStudy();
            return new StudySolrExtractors.TitleExtractor().extract(study);
        }
    }
    
    public static class StudyIdExtractor implements SolrFieldExtractor<Paper> {

        @Override
        public Object extract(final Paper paper) {

            final Study study = paper.getStudy();

            return study.getId();
        }
    }
}
