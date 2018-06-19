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
 * Created: February 19, 2018
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.model.UserStudyExtended;
import com.odysseusinc.arachne.portal.model.security.Tenant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StudySolrExtractors {
    public static class ParticipantsExtractor implements SolrFieldExtractor<Study> {

        @Override
        public Object extract(final Study study) {

            return study.getParticipants().stream()
                    .filter(link -> link.getStatus().isPendingOrApproved())
                    .map(UserStudyExtended::getUser)
                    .map(IUser::getId)
                    .map(String::valueOf)
                    .collect(Collectors.toSet());
        }
    }

    public static class PrivacyExtractor implements SolrFieldExtractor<Study> {

        @Override
        public String extract(final Study study) {

            return String.valueOf(!study.getPrivacy());
        }
    }

    public static class TitleExtractor implements SolrFieldExtractor<Study> {

        @Override
        public String extract(final Study study) {

            return study.getTitle();
        }
    }

    public static class TenantsExtractor implements SolrFieldExtractor<Study> {

        @Override
        public List<String> extract(final Study study) {

            return Collections.singletonList(String.valueOf(study.getTenant().getId()));
        }
    }
}
