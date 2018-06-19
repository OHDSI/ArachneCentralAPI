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
 * Created: May 11, 2017
 *
 */

package com.odysseusinc.arachne.portal.util;

import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.model.SubmissionFile;
import com.odysseusinc.arachne.portal.service.AnalysisPaths;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LegacyAnalysisHelper {

    private final AnalysisHelper analysisHelper;

    @Autowired
    public LegacyAnalysisHelper(AnalysisHelper analysisHelper) {

        this.analysisHelper = analysisHelper;
    }

    public Path getOldSubmissionFolder(Submission submission) {

        return analysisHelper.getAnalysisFolder(submission.getAnalysis()).resolve(submission.getId().toString());
    }

    public Path getOldSubmissionResultFolder(Submission submission) {

        return getOldSubmissionFolder(submission).resolve(AnalysisPaths.RESULT_DIR);
    }

    private Path getOldResultFolder(Submission source) {

        return getOldSubmissionFolder(source).resolve(AnalysisPaths.RESULT_DIR);
    }

    public Optional<Path> getOldSubmissionFile(SubmissionFile file) throws IOException {

        Path analysisFolder = analysisHelper.getAnalysisFolder(file.getSubmissionGroup().getAnalysis());
        FileFinder finder = new FileFinder(file.getUuid());
        Files.walkFileTree(analysisFolder, finder);
        List<Path> result = finder.getResult();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.iterator().next());
    }

}
