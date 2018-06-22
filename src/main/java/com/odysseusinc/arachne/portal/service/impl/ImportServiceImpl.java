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
 * Created: August 25, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.impl;

import static com.odysseusinc.arachne.commons.utils.TemplateUtils.loadTemplate;

import com.github.jknack.handlebars.Template;
import com.odysseusinc.arachne.portal.service.ImportService;
import com.odysseusinc.arachne.portal.util.ImportedFile;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImportServiceImpl implements ImportService {

    private static final String ESTIMATION_RUNNER_NAME = "run_ple_analysis.R";
    private static final String ESTIMATION_RUNNER_PATH = "preprocessor/estimation/" + ESTIMATION_RUNNER_NAME;

    private final GenericConversionService conversionService;

    @Autowired
    public ImportServiceImpl(GenericConversionService conversionService) {

        this.conversionService = conversionService;
    }

    @Override
    public List<MultipartFile> processEstimation(List<ImportedFile> importedFiles) throws IOException {

        final List<MultipartFile> mpfList = importedFiles.stream()
                .map(importedFile -> conversionService.convert(handleFile(importedFile), MockMultipartFile.class))
                .collect(Collectors.toList());

        mpfList.addAll(getAdditionalFiles());

        return mpfList;
    }

    protected ImportedFile handleFile(ImportedFile file) {

        return file;
    }

    protected List<MultipartFile> getAdditionalFiles() throws IOException {

        Resource runner = new ClassPathResource(ESTIMATION_RUNNER_PATH);
        MockMultipartFile estimationRunner = new MockMultipartFile(
                ESTIMATION_RUNNER_NAME,
                ESTIMATION_RUNNER_NAME,
                null,
                runner.getInputStream()
        );

        return Collections.singletonList(estimationRunner);
    }
}
