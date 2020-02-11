package com.odysseusinc.arachne.portal.service.analysis.heracles;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface HeraclesAnalysisService {

    List<MultipartFile> createAnalysesFiles(HeraclesAnalysisKind analysisSpec);
}
