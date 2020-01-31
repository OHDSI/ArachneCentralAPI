package com.odysseusinc.arachne.portal.service.analysis.impl;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ExecutableDetector {

    public boolean isFileExecutable(CommonAnalysisType type, MultipartFile file) {

        return false;
    }
}
