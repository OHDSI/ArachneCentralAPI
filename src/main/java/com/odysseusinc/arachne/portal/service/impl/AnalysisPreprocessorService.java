/*
 *
 * Copyright 2018 Observational Health Data Sciences and Informatics
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Company: Odysseus Data Services, Inc.
 * Product Owner/Architecture: Gregory Klebanov
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva, Mikhail Mironov
 * Created: August 03, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.impl;

import com.odysseusinc.arachne.commons.service.preprocessor.AbstractPreprocessorService;
import com.odysseusinc.arachne.commons.service.preprocessor.PreprocessorRegistry;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.AnalysisFile;
import com.odysseusinc.arachne.portal.model.ArachneFile;
import com.odysseusinc.arachne.portal.util.AnalysisHelper;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnalysisPreprocessorService extends AbstractPreprocessorService<Analysis> {

    private final AnalysisHelper analysisHelper;

    @Autowired
    public AnalysisPreprocessorService(AnalysisHelper analysisHelper,
                                       PreprocessorRegistry<Analysis> preprocessorRegistry) {

        super(preprocessorRegistry);
        this.analysisHelper = analysisHelper;
    }

    public void preprocessFile(Analysis analysis, AnalysisFile file) {

        Path analysisFolder = analysisHelper.getAnalysisFolder(analysis);
        File target = analysisFolder.resolve(file.getRealName()).toFile();
        getPreprocessorRegistry().getPreprocessor(file.getContentType())
                .preprocess(analysis, target);
    }

    @Override
    protected List<File> getFiles(Analysis analysis) {

        Path analysisFolder = analysisHelper.getAnalysisFolder(analysis);
        return analysis
                .getFiles()
                .stream()
                .map(analysisFile -> analysisFolder.resolve(analysisFile.getRealName()).toFile())
                .collect(Collectors.toList());
    }

    @Override
    protected Optional<String> getContentType(Analysis analysis, File file) {

        return analysis
                .getFiles()
                .stream()
                .filter(analysisFile -> analysisFile.getRealName().equals(file.getName()))
                .findFirst().map(ArachneFile::getContentType);
    }
}
