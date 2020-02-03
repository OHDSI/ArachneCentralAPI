package com.odysseusinc.arachne.portal.service.analysis.impl;

import com.google.common.collect.ImmutableMap;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
import com.odysseusinc.arachne.commons.types.DBMSType;
import com.odysseusinc.arachne.commons.utils.CommonFileUtils;
import com.odysseusinc.arachne.portal.api.v1.dto.UploadFileDTO;
import com.odysseusinc.arachne.portal.exception.AlreadyExistException;
import com.odysseusinc.arachne.portal.exception.ArachneSystemRuntimeException;
import com.odysseusinc.arachne.portal.exception.IORuntimeException;
import com.odysseusinc.arachne.portal.exception.ValidationRuntimeException;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.AnalysisFile;
import com.odysseusinc.arachne.portal.model.DataReference;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.repository.AnalysisFileRepository;
import com.odysseusinc.arachne.portal.service.analysis.AnalysisFilesSavingService;
import com.odysseusinc.arachne.portal.service.impl.AnalysisPreprocessorService;
import com.odysseusinc.arachne.portal.service.impl.antivirus.events.AntivirusJob;
import com.odysseusinc.arachne.portal.service.impl.antivirus.events.AntivirusJobEvent;
import com.odysseusinc.arachne.portal.service.impl.antivirus.events.AntivirusJobFileType;
import com.odysseusinc.arachne.portal.util.AnalysisHelper;
import com.odysseusinc.arachne.portal.util.ZipUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

import static com.odysseusinc.arachne.commons.utils.CommonFileUtils.ANALYSIS_INFO_FILE_DESCRIPTION;
import static com.odysseusinc.arachne.commons.utils.CommonFileUtils.OHDSI_JSON_EXT;
import static com.odysseusinc.arachne.commons.utils.CommonFileUtils.OHDSI_SQL_EXT;
import static com.odysseusinc.arachne.portal.service.analysis.impl.AnalysisUtils.throwAccessDeniedExceptionIfLocked;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Transactional
@Service
public class AnalysisFilesSavingServiceImpl<A extends Analysis> implements AnalysisFilesSavingService<A> {

    private static final Logger log = LoggerFactory.getLogger(AnalysisFilesSavingServiceImpl.class);

    private final AnalysisFileRepository analysisFileRepository;
    private final AnalysisHelper analysisHelper;
    private final AnalysisPreprocessorService preprocessorService;
    private final ApplicationEventPublisher eventPublisher;
    private final RestTemplate restTemplate;

    public AnalysisFilesSavingServiceImpl(AnalysisFileRepository analysisFileRepository, AnalysisHelper analysisHelper, AnalysisPreprocessorService preprocessorService, ApplicationEventPublisher eventPublisher, RestTemplate restTemplate) {

        this.analysisFileRepository = analysisFileRepository;
        this.analysisHelper = analysisHelper;
        this.preprocessorService = preprocessorService;
        this.eventPublisher = eventPublisher;
        this.restTemplate = restTemplate;
    }

    @Override
    @PreAuthorize("hasPermission(#analysis, "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).UPLOAD_ANALYSIS_FILES)")
    public List<AnalysisFile> saveFiles(List<UploadFileDTO> files, IUser user, A analysis) throws IOException {

        List<String> errorFileMessages = new ArrayList<>();
        List<AnalysisFile> savedFiles = new ArrayList<>();
        for (UploadFileDTO f : files) {
            try {
                if (StringUtils.isNotBlank(f.getLink())) {
                    savedFiles.add(saveFileByLink(f.getLink(), user, analysis, f.getLabel(), f.getExecutable()));
                } else if (f.getFile() != null) {
                    savedFiles.add(saveFile(f.getFile(), user, analysis, f.getLabel(), f.getExecutable(), null));
                } else {
                    errorFileMessages.add(String.format("Invalid file: \"%s\"", f.getLabel()));
                }
            } catch (AlreadyExistException e) {
                errorFileMessages.add(e.getMessage());
            }
        }
        if (!errorFileMessages.isEmpty()) {
            throw new ValidationRuntimeException("Failed to save files", ImmutableMap.of("file", errorFileMessages));
        }
        return savedFiles;
    }

    @Override
    @PreAuthorize("hasPermission(#analysis, "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).UPLOAD_ANALYSIS_FILES)")
    public List<AnalysisFile> saveFiles(List<MultipartFile> multipartFiles, IUser user, A analysis, DataReference dataReference) {

        return this.saveFiles(multipartFiles, user, analysis, dataReference, (fileName, analysisType) -> false);
    }

    @PreAuthorize("hasPermission(#analysis, "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).UPLOAD_ANALYSIS_FILES)")
    protected List<AnalysisFile> saveFiles(List<MultipartFile> multipartFiles, IUser user, A analysis, DataReference dataReference, BiPredicate<String, CommonAnalysisType> isExecutableProvider) {

        List<MultipartFile> filteredFiles = multipartFiles.stream()
                .filter(file -> !(CommonAnalysisType.COHORT.equals(analysis.getType()) && file.getName().endsWith(OHDSI_JSON_EXT)))
                .filter(file -> !file.getName().startsWith(ANALYSIS_INFO_FILE_DESCRIPTION))
                .collect(Collectors.toList());

        List<AnalysisFile> savedFiles = new ArrayList<>();
        List<String> errorFileMessages = new ArrayList<>();
        for (MultipartFile file : filteredFiles) {
            try {
                final boolean isExecutable = isExecutableProvider.test(file.getOriginalFilename(), analysis.getType());
                savedFiles.add(saveFile(file, user, analysis, file.getName(), isExecutable, dataReference));
            } catch (AlreadyExistException e) {
                errorFileMessages.add(e.getMessage());
            }
        }
        if (!errorFileMessages.isEmpty()) {
            throw new ValidationRuntimeException("Failed to save files", ImmutableMap.of(dataReference.getGuid(), errorFileMessages));
        }
        return savedFiles;
    }

    @Override
    @PreAuthorize("hasPermission(#analysis, "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).UPLOAD_ANALYSIS_FILES)")
    public AnalysisFile saveFile(MultipartFile multipartFile, IUser user, A analysis, String label,
                                 Boolean isExecutable, DataReference dataReference) throws AlreadyExistException {

        ensureLabelIsUnique(analysis.getId(), label);
        String originalFilename = multipartFile.getOriginalFilename();
        String fileNameLowerCase = UUID.randomUUID().toString();
        try {

            Path analysisPath = analysisHelper.getAnalysisPath(analysis);
            Path targetPath = Paths.get(analysisPath.toString(), fileNameLowerCase);
            Files.copy(multipartFile.getInputStream(), targetPath, REPLACE_EXISTING);
            final String contentType = CommonFileUtils.getContentType(originalFilename, targetPath.toString());

            AnalysisFile analysisFile = buildNewAnalysisFileEntry(user, analysis, label, isExecutable, originalFilename, fileNameLowerCase, contentType);
            analysisFile.setDataReference(dataReference);

            AnalysisFile saved = analysisFileRepository.save(analysisFile);
            analysis.getFiles().add(saved);


            preprocessorService.preprocessFile(analysis, analysisFile);
            eventPublisher.publishEvent(new AntivirusJobEvent(this, new AntivirusJob(saved.getId(), saved.getName(), new FileInputStream(targetPath.toString()), AntivirusJobFileType.ANALYSIS_FILE)));
            return saved;

        } catch (IOException | RuntimeException ex) {
            String message = "error save file to disk, filename=" + fileNameLowerCase + " ex=" + ex.toString();
            log.error(message, ex);
            throw new ArachneSystemRuntimeException(message);
        }
    }

    @NotNull
    private AnalysisFile buildNewAnalysisFileEntry(IUser user, A analysis, String label, Boolean isExecutable, String originalFilename, String fileNameLowerCase, String contentType) {

        AnalysisFile analysisFile = new AnalysisFile();
        analysisFile.setUuid(fileNameLowerCase);
        analysisFile.setAnalysis(analysis);
        analysisFile.setContentType(contentType);
        analysisFile.setLabel(label);
        analysisFile.setAuthor(user);
        analysisFile.setUpdatedBy(user);
        analysisFile.setExecutable(Boolean.TRUE.equals(isExecutable));
        analysisFile.setRealName(originalFilename);
        Date now = new Date();
        analysisFile.setCreated(now);
        analysisFile.setUpdated(now);
        analysisFile.setVersion(1);
        return analysisFile;
    }

    @Override
    @PreAuthorize("hasPermission(#analysis, "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).UPLOAD_ANALYSIS_FILES)")
    public AnalysisFile saveFileByLink(String link, IUser user, A analysis, String label, Boolean isExecutable)
            throws IOException, AlreadyExistException {

        ensureLabelIsUnique(analysis.getId(), label);
        throwAccessDeniedExceptionIfLocked(analysis);
        String fileNameLowerCase = UUID.randomUUID().toString();
        try {
            if (link == null) {
                throw new IORuntimeException("wrong url");
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
            HttpEntity<String> entity = new HttpEntity<>(headers);
            URL url = new URL(link);

            String originalFileName = FilenameUtils.getName(url.getPath());

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    link,
                    HttpMethod.GET, entity, byte[].class);

            if (response.getStatusCode() == HttpStatus.OK) {

                final String contentType = response.getHeaders().getContentType().toString();
                Path pathToAnalysis = analysisHelper.getAnalysisPath(analysis);
                Path targetPath = Paths.get(pathToAnalysis.toString(), fileNameLowerCase);
                Files.copy(new ByteArrayInputStream(response.getBody()), targetPath, REPLACE_EXISTING);

                AnalysisFile analysisFile = buildNewAnalysisFileEntry(user, analysis, label, isExecutable, originalFileName, fileNameLowerCase, contentType);
                analysisFile.setEntryPoint(originalFileName);

                return analysisFileRepository.save(analysisFile);
            }
        } catch (IOException | RuntimeException ex) {
            String message = "error save file to disk, filename=" + fileNameLowerCase + " ex=" + ex.toString();
            log.error(message, ex);
            throw new IOException(message);
        }
        return null;
    }

    @PreAuthorize("hasPermission(#analysis, "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).UPLOAD_ANALYSIS_FILES)")
    public void saveCOHORTAnalysisArchive(A analysis, DataReference dataReference, IUser user, List<MultipartFile> files) {

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        StringBuilder generatedFileNameBuilder = new StringBuilder();
        generatedFileNameBuilder.append(CommonAnalysisType.COHORT.getTitle());
        try (final ZipOutputStream zos = new ZipOutputStream(out)) {
            files.stream()
                    .filter(file -> !ANALYSIS_INFO_FILE_DESCRIPTION.equals(file.getName()))
                    .forEach(file -> {
                        try {
                            if (file.getName().endsWith(OHDSI_SQL_EXT)) {
                                String shortBaseName = generateDialectVersions(zos, file);
                                generatedFileNameBuilder.append("_");
                                generatedFileNameBuilder.append(shortBaseName);
                            } else {
                                String fileName = file.getName();
                                ZipUtil.addZipEntry(zos, fileName, file.getInputStream());
                            }
                        } catch (IOException e) {
                            throw new ArachneSystemRuntimeException("Failed to add file to archive: " + file.getName(), e);
                        }
                    });
        } catch (IOException e) {
            throw new ArachneSystemRuntimeException(e);
        }
        String fileName = generatedFileNameBuilder.append(".zip").toString();
        final MultipartFile sqlArchive = new MockMultipartFile(fileName, fileName, "application/zip",
                out.toByteArray());
        try {
            saveFile(sqlArchive, user, analysis, fileName, false, dataReference);
        } catch (AlreadyExistException e) {
            log.error("Failed to save file", e);
        }
    }

    @Override
    public String updateAnalysisFromMetaFiles(A analysis, List<MultipartFile> entityFiles) throws IOException {

        final MultipartFile descriptionFile = entityFiles.stream()
                .filter(file -> ANALYSIS_INFO_FILE_DESCRIPTION.equals(file.getName()))
                .findFirst().orElse(null);

        if (descriptionFile != null) {
            String description = IOUtils.toString(descriptionFile.getInputStream(), StandardCharsets.UTF_8);
            if (isBlank(analysis.getDescription())) {
                analysis.setDescription(description);
                return null;
            }
            if (!StringUtils.equals(description, analysis.getDescription())) {
                return description;
            }
        }
        return null;
    }

    private String generateDialectVersions(ZipOutputStream zos, MultipartFile file) throws IOException {
        String statement = IOUtils.toString(file.getInputStream(), StandardCharsets.UTF_8);
        String renderedSql = SqlRender.renderSql(statement, null, null);
        String baseName = FilenameUtils.getBaseName(file.getOriginalFilename());
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        DBMSType[] dbTypes = new DBMSType[]{DBMSType.POSTGRESQL, DBMSType.ORACLE, DBMSType.MS_SQL_SERVER,
                DBMSType.REDSHIFT, DBMSType.PDW};
        for (final DBMSType dialect : dbTypes) {
            final String sql = SqlTranslate.translateSql(renderedSql, dialect.getOhdsiDB());
            final String fileName = baseName + "."
                    + dialect.getLabel().replace(" ", "-")
                    + "." + extension;
            ZipUtil.addZipEntry(zos, fileName, new ByteArrayInputStream(sql.getBytes(StandardCharsets.UTF_8)));
        }
        return baseName.replaceAll("\\.ohdsi", "");
    }

    private void ensureLabelIsUnique(Long analysisId, String label) throws AlreadyExistException {

        if (!analysisFileRepository.findAllByAnalysisIdAndLabel(analysisId, label).isEmpty()) {
            throw new AlreadyExistException("File with such name " + label + " already exists");
        }
    }

}
