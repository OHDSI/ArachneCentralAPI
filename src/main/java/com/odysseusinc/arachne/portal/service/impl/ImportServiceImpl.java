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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImportServiceImpl implements ImportService {

    private static final String ESTIMATION_PACKRAT_NAME = "PopulationLevelEffectEstimationAnalysis-v1_0_0.tar.gz";
    private static final String ESTIMATION_PACKRAT_PATH = "preprocessor/estimation/" + ESTIMATION_PACKRAT_NAME;

    private static final String ESTIMATION_PACKRAT_WRAPPER_NAME = "packratRun.r";
    private static final String ESTIMATION_PACKRAT_WRAPPER_PATH = "preprocessor/estimation/" + ESTIMATION_PACKRAT_WRAPPER_NAME;

    private final GenericConversionService conversionService;

    private final Map<String, Object> estimationTemplateParams = new HashMap<>();

    @Autowired
    public ImportServiceImpl(GenericConversionService conversionService) {

        this.conversionService = conversionService;
        estimationTemplateParams.put("packratPath", "./" + ESTIMATION_PACKRAT_NAME);
    }

    @Override
    public List<MultipartFile> processEstimation(List<ImportedFile> importedFiles) throws IOException {

        final List<MultipartFile> mpfList = importedFiles.stream()
                .map(importedFile -> {
                    ImportedFile file = importedFile;
                    if (FilenameUtils.getExtension(importedFile.getOriginalFilename()).toLowerCase().equals("r")) {
                        try {
                            file = fillEstimationCodePlaceholders(importedFile);
                        } catch (IOException ex) {
                            throw new UncheckedIOException(ex);
                        }
                    }
                    return conversionService.convert(file, MockMultipartFile.class);
                })
                .collect(Collectors.toList());

        Resource packratRunResource = new ClassPathResource(ESTIMATION_PACKRAT_WRAPPER_PATH);
        mpfList.add(
                new MockMultipartFile(
                        ESTIMATION_PACKRAT_WRAPPER_NAME,
                        ESTIMATION_PACKRAT_WRAPPER_NAME,
                        null,
                        packratRunResource.getInputStream()
                )
        );

        Resource packratBundleResource = new ClassPathResource(ESTIMATION_PACKRAT_PATH);
        mpfList.add(
                new MockMultipartFile(
                        ESTIMATION_PACKRAT_NAME,
                        ESTIMATION_PACKRAT_NAME,
                        null,
                        packratBundleResource.getInputStream()
                )
        );

        return mpfList;
    }

    private ImportedFile fillEstimationCodePlaceholders(ImportedFile importedFile) throws IOException {

        Template template = loadTemplate(importedFile);
        String processedContent = template.apply(estimationTemplateParams);
        return new ImportedFile(importedFile.getFilename(), processedContent.getBytes());
    }
}
