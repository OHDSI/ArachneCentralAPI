package com.odysseusinc.arachne.portal.service.analysis.impl;

import static com.odysseusinc.arachne.commons.types.DBMSType.MS_SQL_SERVER;
import static com.odysseusinc.arachne.commons.types.DBMSType.ORACLE;
import static com.odysseusinc.arachne.commons.types.DBMSType.PDW;
import static com.odysseusinc.arachne.commons.types.DBMSType.POSTGRESQL;
import static com.odysseusinc.arachne.commons.types.DBMSType.REDSHIFT;
import static com.odysseusinc.arachne.commons.utils.CommonFileUtils.ANALYSIS_INFO_FILE_DESCRIPTION;
import static com.odysseusinc.arachne.commons.utils.CommonFileUtils.OHDSI_JSON_EXT;
import static com.odysseusinc.arachne.commons.utils.CommonFileUtils.OHDSI_SQL_EXT;
import static com.odysseusinc.arachne.portal.service.analysis.impl.AnalysisUtils.throwAccessDeniedExceptionIfLocked;
import static feign.Util.isNotBlank;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import com.google.common.collect.ImmutableMap;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
import com.odysseusinc.arachne.commons.types.DBMSType;
import com.odysseusinc.arachne.commons.utils.AnalysisArchiveUtils;
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
import com.odysseusinc.arachne.portal.service.analysis.AnalysisService;
import com.odysseusinc.arachne.portal.service.impl.AnalysisPreprocessorService;
import com.odysseusinc.arachne.portal.service.impl.antivirus.events.AntivirusJob;
import com.odysseusinc.arachne.portal.service.impl.antivirus.events.AntivirusJobEvent;
import com.odysseusinc.arachne.portal.service.impl.antivirus.events.AntivirusJobFileType;
import com.odysseusinc.arachne.portal.util.AnalysisHelper;
import com.odysseusinc.arachne.portal.util.ZipUtil;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
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
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Transactional
@Service
public class AnalysisFilesSavingServiceImpl<A extends Analysis> implements AnalysisFilesSavingService<A> {

    private static final Logger log = LoggerFactory.getLogger(AnalysisFilesSavingServiceImpl.class);

    private final AnalysisFileRepository analysisFileRepository;
    private final AnalysisHelper analysisHelper;
    private final AnalysisPreprocessorService preprocessorService;
    private final AnalysisService analysisService;
    private final ApplicationEventPublisher eventPublisher;
    private final RestTemplate restTemplate;

    public AnalysisFilesSavingServiceImpl(AnalysisFileRepository analysisFileRepository, AnalysisHelper analysisHelper, AnalysisPreprocessorService preprocessorService, @Lazy AnalysisService analysisService, ApplicationEventPublisher eventPublisher, RestTemplate restTemplate) {

        this.analysisFileRepository = analysisFileRepository;
        this.analysisHelper = analysisHelper;
        this.preprocessorService = preprocessorService;
        this.analysisService = analysisService;
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
                if (StringUtils.hasText(f.getLink())) {
                    savedFiles.add(saveFileByLink(f.getLink(), user, analysis, f.getLabel(), f.getExecutable()));
                } else if (f.getFile() != null) {
                    savedFiles.add(saveFile(f.getFile(), user, analysis, f.getLabel(), f.getExecutable(), null));
                } else {
                    errorFileMessages.add("Invalid file: \"" + f.getLabel() + "\"");
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
    public List<AnalysisFile> saveFiles(List<MultipartFile> multipartFiles, IUser user, A analysis, CommonAnalysisType analysisType,
                                        DataReference dataReference) throws IOException {

        List<MultipartFile> filteredFiles = multipartFiles.stream()
                .filter(file -> !(CommonAnalysisType.COHORT.equals(analysisType) && file.getName().endsWith(OHDSI_JSON_EXT)))
                .filter(file -> !file.getName().startsWith(ANALYSIS_INFO_FILE_DESCRIPTION))
                .collect(Collectors.toList());

        List<AnalysisFile> savedFiles = new ArrayList<>();
        List<String> errorFileMessages = new ArrayList<>();
        for (MultipartFile f : filteredFiles) {
            try {
                final boolean isExecutable = analysisService.detectExecutable(analysisType, f);
                savedFiles.add(saveFile(f, user, analysis, f.getName(), isExecutable, dataReference));
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

            AnalysisFile analysisFile = new AnalysisFile();
            analysisFile.setDataReference(dataReference);
            analysisFile.setUuid(fileNameLowerCase);
            analysisFile.setAnalysis(analysis);
            analysisFile.setContentType(CommonFileUtils.getContentType(originalFilename, targetPath.toString()));
            analysisFile.setLabel(label);
            analysisFile.setAuthor(user);
            analysisFile.setUpdatedBy(user);
            analysisFile.setExecutable(false);
            analysisFile.setRealName(originalFilename);
            Date created = new Date();
            analysisFile.setCreated(created);
            analysisFile.setUpdated(created);
            analysisFile.setVersion(1);

            AnalysisFile saved = analysisFileRepository.save(analysisFile);
            analysis.getFiles().add(saved);

            if (Boolean.TRUE.equals(isExecutable)) {
                analysisService.setIsExecutable(saved.getUuid());
            }

            preprocessorService.preprocessFile(analysis, analysisFile);
            eventPublisher.publishEvent(new AntivirusJobEvent(this, new AntivirusJob(saved.getId(), saved.getName(), new FileInputStream(targetPath.toString()), AntivirusJobFileType.ANALYSIS_FILE)));
            return saved;

        } catch (IOException | RuntimeException ex) {
            String message = "error save file to disk, filename=" + fileNameLowerCase + " ex=" + ex.toString();
            log.error(message, ex);
            throw new ArachneSystemRuntimeException(message);
        }
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

                Files.copy(new ByteArrayInputStream(response.getBody()),
                        targetPath, REPLACE_EXISTING);
                AnalysisFile analysisFile = new AnalysisFile();
                analysisFile.setUuid(fileNameLowerCase);
                analysisFile.setAnalysis(analysis);
                analysisFile.setContentType(contentType);
                analysisFile.setLabel(label);
                analysisFile.setAuthor(user);
                analysisFile.setExecutable(Boolean.TRUE.equals(isExecutable));
                analysisFile.setRealName(originalFileName);
                analysisFile.setEntryPoint(originalFileName);

                Date created = new Date();
                analysisFile.setCreated(created);
                analysisFile.setUpdated(created);
                analysisFile.setVersion(1);
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
    public void saveCohortAnalysisArchive(A analysis, DataReference dataReference, IUser user, List<MultipartFile> files) {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(out)) {
            MultipartFile genericSqlFile = files.stream()
                    .filter(file -> file.getName().endsWith(OHDSI_SQL_EXT))
                    .findAny()
                    .orElseThrow(() -> new ArachneSystemRuntimeException(String.format("There is no sql file for %s analysis.", analysis.getId())));
            generateFilesForEachDialectAndAddToZip(zos, genericSqlFile);

            Collection<MultipartFile> allFilesWithoutSqlFile = files.stream()
                    .filter(file -> ObjectUtils.notEqual(file, genericSqlFile))
                    .collect(Collectors.toList());
            ZipUtil.addZipEntries(zos, allFilesWithoutSqlFile);

            String analysisName = getAnalysisName(genericSqlFile);
            String fileName = AnalysisArchiveUtils.getArchiveFileName(CommonAnalysisType.COHORT, analysisName);
            MultipartFile sqlArchive = new MockMultipartFile(fileName, fileName, com.google.common.net.MediaType.ZIP.toString(),
                    out.toByteArray());
            saveFile(sqlArchive, user, analysis, fileName, false, dataReference);

        } catch (Exception e) {
            log.error("Failed to save zip file for {} analysis", e);
            throw new ArachneSystemRuntimeException(e);
        }
    }

    @Override
    public void updateAnalysisFromMetaFiles(A analysis, List<MultipartFile> entityFiles) throws IOException {

        final MultipartFile descriptionFile = entityFiles.stream()
                .filter(file -> ANALYSIS_INFO_FILE_DESCRIPTION.equals(file.getName()))
                .findFirst().orElse(null);

        if (descriptionFile != null) {
            String description = IOUtils.toString(descriptionFile.getInputStream(), StandardCharsets.UTF_8);
            if(isNotBlank(description)) {
                analysis.setDescription(description);
            }
        }
    }

    private void generateFilesForEachDialectAndAddToZip(ZipOutputStream zos, MultipartFile file) throws IOException {
        String statement = IOUtils.toString(file.getInputStream(), StandardCharsets.UTF_8);
        String renderedSql = SqlRender.renderSql(statement, null, null);
        String baseName = FilenameUtils.getBaseName(file.getOriginalFilename());
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        List<DBMSType> dbTypes = Arrays.asList(POSTGRESQL, ORACLE, MS_SQL_SERVER, REDSHIFT, PDW);
        for (DBMSType dialect : dbTypes) {
            String sql = SqlTranslate.translateSql(renderedSql, dialect.getOhdsiDB());
            String fileName = String.format("%s.%s.%s", baseName , dialect.getLabel().replace(" ", "-") , extension);
            ZipUtil.addZipEntry(zos, fileName, new ByteArrayInputStream(sql.getBytes(StandardCharsets.UTF_8)));
        }
    }

    private String getAnalysisName(MultipartFile file) {

        String baseName = FilenameUtils.getBaseName(file.getOriginalFilename());
        return baseName.replaceAll("\\.ohdsi", "");
    }

    private void ensureLabelIsUnique(Long analysisId, String label) throws AlreadyExistException {

        if (!analysisFileRepository.findAllByAnalysisIdAndLabel(analysisId, label).isEmpty()) {
            throw new AlreadyExistException("File with such name " + label + " already exists");
        }
    }

}
