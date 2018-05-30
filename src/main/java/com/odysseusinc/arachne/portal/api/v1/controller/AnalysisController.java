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
 * Created: January 13, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.controller;

import static com.odysseusinc.arachne.commons.utils.CommonFileUtils.convertToUnixPath;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
import com.odysseusinc.arachne.commons.utils.CommonFileUtils;
import com.odysseusinc.arachne.portal.PortalStarter;
import com.odysseusinc.arachne.portal.api.v1.dto.AnalysisCreateDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.AnalysisDTO;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.service.BaseDataNodeService;
import com.odysseusinc.arachne.portal.service.BaseDataSourceService;
import com.odysseusinc.arachne.portal.service.DataReferenceService;
import com.odysseusinc.arachne.portal.service.ImportService;
import com.odysseusinc.arachne.portal.service.ToPdfConverter;
import com.odysseusinc.arachne.portal.service.submission.SubmissionInsightService;
import com.odysseusinc.arachne.portal.service.analysis.BaseAnalysisService;
import com.odysseusinc.arachne.portal.service.submission.BaseSubmissionService;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@SuppressWarnings("unused")
@RestController
public class AnalysisController extends BaseAnalysisController<Analysis, AnalysisDTO, DataNode, AnalysisCreateDTO> {

    public static final String RUN_PLP_ANALYSIS_FILE_NAME = "run_plp_analysis.r";
    public static final String RUN_IR_ANALYSIS_FILE_NAME = "run_ir_analysis.r";
    private static final String RUN_CC_REPORTS_FILE_NAME = "run_cc_reports.R";
    private static final String CC_SQLS_DIR = "sql/cc";
    private static final String CIRCE_JAR = "circe-1.2.2-SNAPSHOT.jar";
    private static final String CIRCE_JAR_RES = "circe-1.2.2-SNAPSHOT.jar.res";
    private static final String COMMONS_IO_JAR = "commons-io-2.6.jar";
    private static final String COMMONS_IO_JAR_RES = "commons-io-2.6.jar.res";
    private static final String COMMONS_LANG_JAR = "commons-lang3-3.7.jar";
    private static final String COMMONS_LANG_JAR_RES = "commons-lang3-3.7.jar.res";
    private static final String JACKSON_JAR = "jackson-annotations-2.9.2.jar";
    private static final String JACKSON_JAR_RES = "jackson-annotations-2.9.2.jar.res";
    private static final String JARS_IR_PATH = "jars/ir/";
    public static final String IR_RESOURCES_PATH = "r/ir/";

    static {
        ANALISYS_MIMETYPE_MAP.put(CommonAnalysisType.COHORT, CommonFileUtils.TYPE_COHORT_SQL);
        ANALISYS_MIMETYPE_MAP.put(CommonAnalysisType.ESTIMATION, CommonFileUtils.TYPE_ESTIMATION);
    }

    public AnalysisController(
            BaseDataSourceService dataSourceService,
            BaseDataNodeService baseDataNodeService,
            BaseAnalysisService analysisService,
            DataReferenceService dataReferenceService,
            GenericConversionService conversionService,
            SimpMessagingTemplate wsTemplate,
            JmsTemplate jmsTemplate,
            ImportService importService,
            BaseSubmissionService submissionService,
            ToPdfConverter toPdfConverter,
            SubmissionInsightService submissionInsightService) {

        super(analysisService,
                submissionService,
                dataReferenceService,
                jmsTemplate,
                conversionService,
                baseDataNodeService,
                dataSourceService,
                importService,
                wsTemplate,
                toPdfConverter,
                submissionInsightService);
    }

    @Override
    protected Class<Analysis> getAnalysisClass() {

        return Analysis.class;
    }

    @Override
    protected Class<AnalysisDTO> getAnalysisDTOClass() {

        return AnalysisDTO.class;
    }

    @Override
    protected void attachPredictionFiles(List<MultipartFile> files) throws IOException {

        files.add(new MockMultipartFile(RUN_PLP_ANALYSIS_FILE_NAME, RUN_PLP_ANALYSIS_FILE_NAME, null,
                readResource("r/" + RUN_PLP_ANALYSIS_FILE_NAME)));
    }

    protected void attachIncidenceRatesFiles(List<MultipartFile> files) throws IOException {

        files.add(new MockMultipartFile(RUN_IR_ANALYSIS_FILE_NAME, RUN_IR_ANALYSIS_FILE_NAME, null,
                readResource("r/" + RUN_IR_ANALYSIS_FILE_NAME)));
        files.add(new MockMultipartFile(CIRCE_JAR, CIRCE_JAR, null,
                readResource(JARS_IR_PATH + CIRCE_JAR_RES)));
        files.add(new MockMultipartFile(COMMONS_IO_JAR, COMMONS_IO_JAR, null,
                readResource(JARS_IR_PATH + COMMONS_IO_JAR_RES)));
        files.add(new MockMultipartFile(COMMONS_LANG_JAR, COMMONS_LANG_JAR, null,
                readResource(JARS_IR_PATH + COMMONS_LANG_JAR_RES)));
        files.add(new MockMultipartFile(JACKSON_JAR, JACKSON_JAR, null,
                readResource(JARS_IR_PATH + JACKSON_JAR_RES)));
        files.add(new MockMultipartFile("additionalCriteria.sql", "additionalCriteria.sql", null,
                readResource(IR_RESOURCES_PATH  + "additionalCriteria.sql")));
        files.add(new MockMultipartFile("analysis_summary.sql", "analysis_summary.sql", null,
                readResource(IR_RESOURCES_PATH  + "analysis_summary.sql")));
        files.add(new MockMultipartFile("delete_strata.sql", "delete_strata.sql", null,
                readResource(IR_RESOURCES_PATH  + "delete_strata.sql")));
        files.add(new MockMultipartFile("groupQuery.sql", "groupQuery.sql", null,
                readResource(IR_RESOURCES_PATH  + "groupQuery.sql")));
        files.add(new MockMultipartFile("ir_analysis_query_builder.r", "ir_analysis_query_builder.r", null,
                readResource(IR_RESOURCES_PATH  + "ir_analysis_query_builder.r")));
        files.add(new MockMultipartFile("ir_dist.sql", "ir_dist.sql", null,
                readResource(IR_RESOURCES_PATH  + "ir_dist.sql")));
        files.add(new MockMultipartFile("performAnalysis.sql", "performAnalysis.sql", null,
                readResource(IR_RESOURCES_PATH  + "performAnalysis.sql")));
        files.add(new MockMultipartFile("strata.sql", "strata.sql", null,
                readResource(IR_RESOURCES_PATH  + "strata.sql")));
        files.add(new MockMultipartFile("strata_rules.sql", "strata_rules.sql", null,
                readResource(IR_RESOURCES_PATH  + "strata_rules.sql")));
        files.add(new MockMultipartFile("strata_stats.sql", "strata_stats.sql", null,
                readResource(IR_RESOURCES_PATH + "strata_stats.sql")));
    }


    @Override
    protected void attachCohortCharacterizationFiles(List<MultipartFile> files) throws IOException, URISyntaxException {

        files.add(new MockMultipartFile(RUN_CC_REPORTS_FILE_NAME, RUN_CC_REPORTS_FILE_NAME, null,
                readResource("r/" + RUN_CC_REPORTS_FILE_NAME)));

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(
                PortalStarter.class.getClassLoader());

        List<MultipartFile> multipartFiles = Arrays.stream(resolver.getResources(
                "classpath:/" + CC_SQLS_DIR + "/**"))
                .filter(r -> ((ClassPathResource) r).getPath().endsWith(".sql"))
                .map(this::convertToMultipartFile)
                .collect(Collectors.toList());

        files.addAll(multipartFiles);
    }

    private MultipartFile convertToMultipartFile(Resource resource) {

        try {
            String rootPath = ((ClassPathResource) resource).getPath();
            String name = convertToUnixPath(rootPath.substring(rootPath.indexOf(CC_SQLS_DIR) + CC_SQLS_DIR.length() + 1));
            return new MockMultipartFile(name, name, null, readResource(CC_SQLS_DIR + "/" + name));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}


