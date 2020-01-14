package com.odysseusinc.arachne.portal.service.analysis;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
import com.odysseusinc.arachne.portal.api.v1.dto.UploadFileDTO;
import com.odysseusinc.arachne.portal.exception.AlreadyExistException;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.AnalysisFile;
import com.odysseusinc.arachne.portal.model.DataReference;
import com.odysseusinc.arachne.portal.model.IUser;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AnalysisFilesSavingService<A extends Analysis>  {

    List<AnalysisFile> saveFiles(List<UploadFileDTO> files, IUser user, A analysis) throws IOException;

    List<AnalysisFile> saveFiles(List<MultipartFile> multipartFiles, IUser user, A analysis, CommonAnalysisType analysisType,
                                 DataReference dataReference) throws IOException;

    void saveCOHORTAnalysisArchive(A analysis, DataReference dataReference, IUser user, List<MultipartFile> files) throws IOException;

    AnalysisFile saveFile(MultipartFile multipartFile, IUser user, A analysis, String label,
                          Boolean isExecutable, DataReference dataReference) throws IOException, AlreadyExistException;

    AnalysisFile saveFileByLink(String link, IUser user, A analysis, String label, Boolean isExecutable)
            throws IOException, AlreadyExistException;

    void updateAnalysisFromMetaFiles(A analysis, List<MultipartFile> entityFiles) throws IOException;
}
