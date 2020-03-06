package com.odysseusinc.arachne.portal.service.analysis.heracles.parts;

import com.odysseusinc.arachne.portal.service.analysis.heracles.HeraclesAnalysisKind;

import java.util.List;

public interface HeraclesRenderer {

    List<String> getParameters();

    String getPartName();

    String render(HeraclesAnalysisKind analysisSpec);
}
