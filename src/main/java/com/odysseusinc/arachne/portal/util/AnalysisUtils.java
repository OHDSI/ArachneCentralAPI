/**
 *
 * Copyright 2017 Observational Health Data Sciences and Informatics
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
 * Created: September 26, 2017
 *
 */

package com.odysseusinc.arachne.portal.util;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import com.odysseusinc.arachne.commons.utils.CommonFileUtils;
import com.odysseusinc.arachne.portal.model.ResultFile;
import com.odysseusinc.arachne.portal.model.Submission;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.UUID;

public class AnalysisUtils {

    private AnalysisUtils() {

    }

    public static ResultFile createResultFile(
            Path toDirectory,
            Path fromDirectory,
            String name,
            Submission submission
    ) throws IOException {

        String uuid = UUID.randomUUID().toString();
        Path sourceFilePath = fromDirectory.resolve(name);
        Path targetFilePath = toDirectory.resolve(uuid);
        Files.copy(sourceFilePath, targetFilePath, REPLACE_EXISTING);
        ResultFile resultFile = new ResultFile();
        resultFile.setCreated(new Date());
        resultFile.setUpdated(new Date());
        resultFile.setRealName(name);
        resultFile.setUuid(uuid);
        resultFile.setSubmission(submission);
        resultFile.setContentType(CommonFileUtils.getContentType(name, targetFilePath.toAbsolutePath().toString()));
        return resultFile;
    }
}
