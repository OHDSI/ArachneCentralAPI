/*
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
 * Authors: Anton Gackovka
 * Created: February 19, 2018
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters;

import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.model.UserStudyExtended;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StudySolrExtractors {
    public static class ParticipantsExtractor implements Function<Object, Object> {

        @Override
        public Object apply(final Object o) {

            final Study study = tryConvert(o, "Participant list");
            return study.getParticipants().stream()
                    .filter(link -> link.getStatus().isPendingOrApproved())
                    .map(UserStudyExtended::getUser)
                    .map(User::getId)
                    .map(String::valueOf)
                    .collect(Collectors.toList());
        }
    }

    public static class PrivacyExtractor implements Function<Object, Object> {

        @Override
        public String apply(final Object o) {

            final Study study = tryConvert(o, "Privacy");
            return String.valueOf(!study.getPrivacy());
        }
    }

    public static class TitleExtractor implements Function<Object, Object> {

        @Override
        public String apply(final Object o) {

            final Study study = tryConvert(o, "Title");
            return study.getTitle();
        }
    }

    private static Study tryConvert(final Object o, final String s) {

        if (!(o instanceof Study)) {
            throw new IllegalArgumentException(s + " can be extracted only from Study object.");
        }
        return (Study) o;
    }
}
