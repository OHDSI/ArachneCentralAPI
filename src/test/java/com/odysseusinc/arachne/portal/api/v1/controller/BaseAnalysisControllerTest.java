package com.odysseusinc.arachne.portal.api.v1.controller;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.Study;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BaseAnalysisControllerTest {

    @Test
    void formatStudyNameWithStudy() {
        Analysis analysis = analysis(CommonAnalysisType.COHORT_PATHWAY, "roman-cohorts");
        Study study = new Study();
        study.setTitle("Duck Diseases");
        analysis.setStudy(study);
        Assertions.assertEquals("txp-Duck Diseases-roman_cohorts-code.zip", BaseAnalysisController.formatStudyName(analysis));
    }

    @Test
    void formatStudyNameNoStudy() {
        Analysis analysis = analysis(CommonAnalysisType.INCIDENCE, "Incidental Incidents");
        Assertions.assertEquals("ir-Incidental Incidents-code.zip", BaseAnalysisController.formatStudyName(analysis));
    }

    private Analysis analysis(CommonAnalysisType type, String title) {
        Analysis analysis = new Analysis();
        analysis.setType(type);
        analysis.setTitle(title);
        return analysis;
    }
}
