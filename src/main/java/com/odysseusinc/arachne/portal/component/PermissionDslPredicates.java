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
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva, Mikhail Mironov
 * Created: October 11, 2017
 *
 */

package com.odysseusinc.arachne.portal.component;

import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.AnalysisFile;
import com.odysseusinc.arachne.portal.model.ParticipantRole;
import com.odysseusinc.arachne.portal.model.ParticipantStatus;
import com.odysseusinc.arachne.portal.model.UserStudyExtended;
import com.odysseusinc.arachne.portal.model.security.ArachneUser;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class PermissionDslPredicates {

    public static <T> Predicate<T> instanceOf(Class<T> targetClass) {
        return targetClass::isInstance;
    }

    public static <T extends Analysis> Predicate<T> analysisAuthorIs(ArachneUser user) {
        return analysis -> Objects.nonNull(analysis.getAuthor()) && Objects.equals(analysis.getAuthor().getId(), user.getId());
    }

    public static <T extends AnalysisFile> Predicate<T> analysisFileAuthorIs(ArachneUser user) {
        return analysisFile -> Objects.nonNull(analysisFile.getAnalysis().getAuthor())
                && Objects.equals(analysisFile.getAnalysis().getAuthor().getId(), user.getId());
    }

    public static <T extends AnalysisFile> Predicate<T> userIsLeadInvestigator(ArachneUser user) {
        return analysisFile -> Objects.nonNull(analysisFile.getAnalysis())
                && analysisFile.getAnalysis().getStudy().getParticipants().stream()
                .filter(userLink -> Objects.equals(user.getId(), userLink.getUser().getId()))
                .filter(userLink -> ParticipantStatus.APPROVED.equals(userLink.getStatus()))
                .findFirst()
                .map(UserStudyExtended::getRole)
                .map(ParticipantRole.LEAD_INVESTIGATOR::equals)
                .orElse(Boolean.FALSE);
    }

    public static <T> Predicate<T> hasRole(ArachneUser user, String role) {
        return entity -> user.getAuthorities().contains(new SimpleGrantedAuthority(role));
    }
}
