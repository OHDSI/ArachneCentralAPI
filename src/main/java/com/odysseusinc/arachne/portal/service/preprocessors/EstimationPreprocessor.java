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

package com.odysseusinc.arachne.portal.service.preprocessors;

import com.github.jknack.handlebars.Template;
import com.odysseusinc.arachne.commons.service.preprocessor.Preprocessor;
import com.odysseusinc.arachne.portal.exception.AlreadyExistException;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.AnalysisFile;
import com.odysseusinc.arachne.portal.service.analysis.BaseAnalysisService;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

// NOTE:
// Left here as both as an example of preprocessor and for case of new change of requirements
//@PreprocessorComponent(contentType = CommonFileUtils.TYPE_ESTIMATION)
public class EstimationPreprocessor implements Preprocessor<Analysis> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EstimationPreprocessor.class);
    private static final String ANALYSIS_BUNDLE_FILENAME = "PopulationLevelEffectEstimationAnalysis-v1_0_0.tar.gz";
    private static final String ESTIMATION_ANALYSIS_SOURCE = "preprocessor/estimation/" + ANALYSIS_BUNDLE_FILENAME;
    private static final String ANALYSIS_RUNNER_FILENAME = "estimation.r";

    private final ApplicationContext applicationContext;
    private BaseAnalysisService<Analysis> analysisService;
    private Template template;
    private final Object monitor = new Object();

    @Autowired
    public EstimationPreprocessor(ApplicationContext applicationContext) {

        this.applicationContext = applicationContext;
    }

    @Override
    public void preprocess(Analysis analysis, File file) {

        LOGGER.info("Running Estimation preprocessor");
        init();
        attachEstimationAnalysisCode(analysis);
        attachRunner(analysis, file);
    }

    private void init() {

        if (analysisService == null) {
            synchronized (monitor) {
                if (analysisService == null) {
                    analysisService = applicationContext.getBean(BaseAnalysisService.class);
                }
                if (template == null) {
                    template = BeanFactoryAnnotationUtils.qualifiedBeanOfType(
                            applicationContext.getAutowireCapableBeanFactory(),
                            Template.class,
                            "estimationRunnerTemplate"
                    );
                }
            }
        }
    }

    private void attachEstimationAnalysisCode(Analysis analysis) {

        Resource resource = new ClassPathResource(ESTIMATION_ANALYSIS_SOURCE);
        try (final InputStream in = resource.getInputStream()) {
            final MultipartFile analysisFile = new MockMultipartFile(
                    ANALYSIS_BUNDLE_FILENAME,
                    ANALYSIS_BUNDLE_FILENAME,
                    null,
                    in
            );
            analysisService.saveFile(analysisFile,
                    analysis.getAuthor(),
                    analysis,
                    analysisFile.getName(),
                    false,
                    null);
        } catch (IOException e) {
            LOGGER.error("Failed to add file", e);
            throw new UncheckedIOException(e);
        } catch (AlreadyExistException e) {
            LOGGER.error("Failed to save file", e);
        }
    }

    private Map<String, Object> prepareParameters(Analysis analysis, File estimationFile) {

        Map<String, Object> parameters = new HashMap<>();
        String estimationNameWithoutExt = FilenameUtils.removeExtension(estimationFile.getName());

        parameters.put("analysisFile", estimationFile.getName());
        parameters.put("targetCohort", estimationNameWithoutExt + "_target.sql");
        parameters.put("comparatorCohort", estimationNameWithoutExt + "_comparator.sql");
        parameters.put("outcomeCohort", estimationNameWithoutExt + "_outcome.sql");

        return parameters;
    }

    private void attachRunner(Analysis analysis, File file){

        try {
            String runnerContent = template.apply(prepareParameters(analysis, file));
            final MultipartFile analysisFile = new MockMultipartFile(
                    ANALYSIS_RUNNER_FILENAME,
                    ANALYSIS_RUNNER_FILENAME,
                    "text/x-r-source",
                    runnerContent.getBytes()
            );
            AnalysisFile createdFile = analysisService.saveFile(analysisFile,
                    analysis.getAuthor(),
                    analysis,
                    ANALYSIS_RUNNER_FILENAME,
                    true,
                    null);
            String fileUuid = createdFile.getUuid();
            // Set via service function to unselect all other files
            analysisService.setIsExecutable(fileUuid);
        } catch (IOException e) {
            LOGGER.error("Failed to generate estimation R execution", e);
            throw new UncheckedIOException(e);
        } catch (AlreadyExistException e) {
            LOGGER.error("Failed to save file", e);
        }
    }
}
