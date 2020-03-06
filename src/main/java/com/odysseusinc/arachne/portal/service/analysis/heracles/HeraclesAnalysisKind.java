package com.odysseusinc.arachne.portal.service.analysis.heracles;

import edu.emory.mathcs.backport.java.util.Arrays;
import edu.emory.mathcs.backport.java.util.Collections;

import java.util.List;

import static com.odysseusinc.arachne.portal.service.analysis.heracles.HeraclesConstants.HERACLES_FULL_ANALYSES_SET;
import static com.odysseusinc.arachne.portal.service.analysis.heracles.HeraclesConstants.INCLUDE_DRUG_TYPE_UTILIZATION_DEFAULT;
import static com.odysseusinc.arachne.portal.service.analysis.heracles.HeraclesConstants.INCLUDE_VISIT_TYPE_UTILIZATION_DEFAULT;

/**
 * {@HeraclesAnalysisKind} Enum group heracles parameters similar from Atlas preset: FULL and defines common interface which can be extended with other known presets like Quick and Const and Utils
 */
public enum HeraclesAnalysisKind {

    FULL(HERACLES_FULL_ANALYSES_SET,
            "''", 5, true, false, INCLUDE_DRUG_TYPE_UTILIZATION_DEFAULT, INCLUDE_VISIT_TYPE_UTILIZATION_DEFAULT, false, false);

    private final Boolean refreshStats;
    private final boolean rollupUtilizationDrug;
    private final boolean rollupUtilizationVisit;
    private final Boolean runAhillesHeel;
    private final List<Integer> analysesIds;
    private final int[] conditionConceptIds = new int[]{};
    private final int[] drugConceptIds = new int[]{};
    private final int[] includeDrugTypeUtilization;
    private final int[] includeVisitTypeUtilization;
    private final int[] measurementConceptIds = new int[]{};
    private final int[] observationConceptIds = new int[]{};
    private final int[] procedureConceptIds = new int[]{};
    private final Integer smallCellCount;
    private final String periods;

    HeraclesAnalysisKind(List<Integer> analysesIds, String periods, Integer smallCellCount, Boolean refreshStats, Boolean runAhillesHeel, int[] includeDrugTypeUtilization, int[] includeVisitTypeUtilization, boolean rollupUtilizationDrug, boolean rollupUtilizationVisit) {

        this.analysesIds = analysesIds;
        this.periods = periods;
        this.smallCellCount = smallCellCount;
        this.refreshStats = refreshStats;
        this.runAhillesHeel = runAhillesHeel;
        this.includeDrugTypeUtilization = includeDrugTypeUtilization;
        this.includeVisitTypeUtilization = includeVisitTypeUtilization;
        this.rollupUtilizationDrug = rollupUtilizationDrug;
        this.rollupUtilizationVisit = rollupUtilizationVisit;
    }

    public Boolean getRefreshStats() {
        return refreshStats;
    }

    public boolean isRollupUtilizationDrug() {
        return rollupUtilizationDrug;
    }

    public boolean isRollupUtilizationVisit() {
        return rollupUtilizationVisit;
    }

    public Boolean getRunAhillesHeel() {
        return runAhillesHeel;
    }

    public List<Integer> getAnalysesIds() {
        return Collections.unmodifiableList(analysesIds);
    }

    public int[] getConditionConceptIds() {
        return conditionConceptIds;
    }

    public int[] getDrugConceptIds() {
        return drugConceptIds;
    }

    public int[] getIncludeDrugTypeUtilization() {
        return includeDrugTypeUtilization;
    }

    public int[] getIncludeVisitTypeUtilization() {
        return includeVisitTypeUtilization;
    }

    public int[] getMeasurementConceptIds() {
        return measurementConceptIds;
    }

    public int[] getObservationConceptIds() {
        return observationConceptIds;
    }

    public int[] getProcedureConceptIds() {
        return procedureConceptIds;
    }

    public Integer getSmallCellCount() {
        return smallCellCount;
    }

    public String getPeriods() {
        return periods;
    }
}


