package com.odysseusinc.arachne.portal.service.analysis.impl;

import com.odysseusinc.arachne.portal.model.Analysis;
import org.springframework.security.access.AccessDeniedException;

public class AnalysisUtils {

    public static void throwAccessDeniedExceptionIfLocked(Analysis analysis) {

        if (analysis.getLocked()) {
            final String ANALYSIS_LOCKE_EXCEPTION = "Analysis with id='%s' is locked, file access forbidden";
            final String message = String.format(ANALYSIS_LOCKE_EXCEPTION, analysis.getId());
            throw new AccessDeniedException(message);
        }
    }
}
