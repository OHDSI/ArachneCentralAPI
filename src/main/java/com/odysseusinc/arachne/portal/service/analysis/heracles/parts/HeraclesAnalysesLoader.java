package com.odysseusinc.arachne.portal.service.analysis.heracles.parts;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public interface HeraclesAnalysesLoader {

    Map<Integer, HeraclesAnalysis> readHeraclesAnalyses();

    Map<Integer, Set<HeraclesAnalysisParameter>> readAnalysesParams(Map<Integer, HeraclesAnalysis> heraclesAnalysisMap);

    class HeraclesAnalysis {
        private final Integer id;
        private final String name;
        private final String filename;
        private final boolean hasResults;
        private final boolean hasDistResults;

        public HeraclesAnalysis(Integer id, String name, String filename, boolean hasResults, boolean hasDistResults) {

            this.id = id;
            this.name = name;
            this.filename = filename;
            this.hasResults = hasResults;
            this.hasDistResults = hasDistResults;
        }

        public Integer getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getFilename() {
            return filename;
        }

        public boolean hasResults() {
            return hasResults;
        }

        public boolean hasDistResults() {
            return hasDistResults;
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) return true;
            if (!(o instanceof HeraclesAnalysis)) return false;
            HeraclesAnalysis analysis = (HeraclesAnalysis) o;
            return Objects.equals(id, analysis.id) &&
                    Objects.equals(name, analysis.name);
        }

        @Override
        public int hashCode() {

            return Objects.hash(id, name);
        }
    }

    class HeraclesAnalysisParameter {

        private final Integer analysisId;
        private final String paramName;
        private final String value;

        public HeraclesAnalysisParameter(Integer analysisId, String paramName, String value) {

            this.analysisId = analysisId;
            this.paramName = paramName;
            this.value = value;
        }

        public Integer getAnalysisId() {

            return analysisId;
        }

        public String getParamName() {

            return paramName;
        }

        public String getValue() {

            return value;
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) return true;
            if (!(o instanceof HeraclesAnalysisParameter)) return false;
            HeraclesAnalysisParameter that = (HeraclesAnalysisParameter) o;
            return Objects.equals(analysisId, that.analysisId) &&
                    Objects.equals(paramName, that.paramName) &&
                    Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {

            return Objects.hash(analysisId, paramName, value);
        }
    }
}
