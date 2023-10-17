package com.odysseusinc.arachne.portal.service.analysis;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnalysisMetadata {
    public static String NAME = "analysisMetadata.json";
    private String analysisName;
    private String analysisType;
    private String runtimeEnvironmentId;
    private String entryPoint;
    private String studyName;
}
