package com.odysseusinc.arachne.portal.component;

import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.AnalysisFile;
import com.odysseusinc.arachne.portal.model.ParticipantRole;
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
                .map(UserStudyExtended::getRole)
                .collect(Collectors.toSet())
                .contains(ParticipantRole.LEAD_INVESTIGATOR);
    }

    public static <T> Predicate<T> hasRole(ArachneUser user, String role) {
        return entity -> user.getAuthorities().contains(new SimpleGrantedAuthority(role));
    }
}
