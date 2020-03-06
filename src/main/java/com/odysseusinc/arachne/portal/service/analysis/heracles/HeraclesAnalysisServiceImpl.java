package com.odysseusinc.arachne.portal.service.analysis.heracles;

import com.odysseusinc.arachne.portal.service.analysis.heracles.parts.HeraclesRenderer;
import org.apache.commons.io.FilenameUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static com.odysseusinc.arachne.portal.util.ResourcesUtils.loadResource;

@Service
public class HeraclesAnalysisServiceImpl implements HeraclesAnalysisService {

    private static final String CREATE_HERACLES_TABLES = "/sql/heracles/createHeraclesTables.sql";
    private static final String HERACLES_NEW_QUERIES = "/r/run_heracles_analysis.r";
    private static final String RESULTS_QUERIES = "/org/ohdsi/cohortresults_pack.zip";
    private final List<HeraclesRenderer> renderers;

    public HeraclesAnalysisServiceImpl(List<HeraclesRenderer> renderers) {

        this.renderers = renderers;
    }


    @Override
    public List<MultipartFile> createAnalysesFiles(HeraclesAnalysisKind analysisSpec) {

        List<MultipartFile> files = new ArrayList<>();
        for (HeraclesRenderer renderer : this.renderers) {
            final String scriptBody = renderer.render(analysisSpec);
            final String fileName = renderer.getPartName();
            final MockMultipartFile file = new MockMultipartFile(fileName, fileName, null, scriptBody.getBytes());
            files.add(file);
        }
        attachResourceFile(files, RESULTS_QUERIES, "application/octet-stream");
        attachResourceFile(files, HERACLES_NEW_QUERIES, null);
        attachResourceFile(files, CREATE_HERACLES_TABLES, null);
        return files;
    }

    private void attachResourceFile(List<MultipartFile> files, String resourcePath, String contentType) {

        final String resultsName = FilenameUtils.getName(resourcePath);
        final MockMultipartFile cohortResults = new MockMultipartFile(resultsName, resultsName, contentType, loadResource(resourcePath));
        files.add(cohortResults);
    }

}
