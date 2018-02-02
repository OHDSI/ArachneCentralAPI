package com.odysseusinc.arachne.portal.api.v1.dto;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;

public class BaseSubmissionAndAnalysisTypeDTO extends BaseSubmissionDTO {

 private CommonAnalysisType analysisType;

    public BaseSubmissionAndAnalysisTypeDTO(BaseSubmissionDTO submissionDTO, CommonAnalysisType analysisType) {

      super(submissionDTO);
      this.analysisType = analysisType;
    }

    public CommonAnalysisType getAnalysisType() {
        return analysisType;
    }

    public void setAnalysisType(CommonAnalysisType analysisType) {
        this.analysisType = analysisType;
    }
}
