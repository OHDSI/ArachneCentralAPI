/*
 *
 * Copyright 2018 Odysseus Data Services, inc.
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
 * Created: September 14, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.controller.submission;

import com.google.common.io.Files;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisExecutionStatusDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult;
import com.odysseusinc.arachne.portal.api.v1.controller.BaseController;
import com.odysseusinc.arachne.portal.api.v1.dto.ApproveDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.BaseSubmissionAndAnalysisTypeDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.BaseSubmissionDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.CreateSubmissionsDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.FileDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.ResultFileDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.SubmissionDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.SubmissionFileDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.SubmissionStatusHistoryElementDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UploadFileDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.converters.FileDtoContentHandler;
import com.odysseusinc.arachne.portal.exception.NoExecutableFileException;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.ResultFile;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.model.SubmissionFile;
import com.odysseusinc.arachne.portal.model.SubmissionStatus;
import com.odysseusinc.arachne.portal.model.SubmissionStatusHistoryElement;
import com.odysseusinc.arachne.portal.model.search.ResultFileSearch;
import com.odysseusinc.arachne.portal.service.ToPdfConverter;
import com.odysseusinc.arachne.portal.service.analysis.BaseAnalysisService;
import com.odysseusinc.arachne.portal.service.submission.BaseSubmissionService;
import com.odysseusinc.arachne.portal.service.submission.SubmissionInsightService;
import com.odysseusinc.arachne.portal.util.AnalysisHelper;
import com.odysseusinc.arachne.portal.util.ContentStorageHelper;
import com.odysseusinc.arachne.portal.util.HttpUtils;
import com.odysseusinc.arachne.portal.util.UserUtils;
import com.odysseusinc.arachne.portal.util.ZipUtil;
import com.odysseusinc.arachne.storage.model.ArachneFileMeta;
import com.odysseusinc.arachne.storage.service.ContentStorageService;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;

import static com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult.ErrorCode.NO_ERROR;
import static org.springframework.util.StringUtils.getFilename;

public abstract class BaseSubmissionController<T extends Submission, A extends Analysis, DTO extends SubmissionDTO>
        extends BaseController {

    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseSubmissionController.class);
    protected final BaseAnalysisService<A> analysisService;
    protected final BaseSubmissionService<T, A> submissionService;
    protected final SubmissionInsightService submissionInsightService;
    protected final ToPdfConverter toPdfConverter;
    private final ContentStorageService contentStorageService;
    private final ContentStorageHelper contentStorageHelper;

    public BaseSubmissionController(BaseAnalysisService<A> analysisService,
                                    BaseSubmissionService<T, A> submissionService,
                                    ToPdfConverter toPdfConverter,
                                    SubmissionInsightService submissionInsightService,
                                    ContentStorageService contentStorageService,
                                    ContentStorageHelper contentStorageHelper) {

        this.analysisService = analysisService;
        this.submissionService = submissionService;
        this.toPdfConverter = toPdfConverter;
        this.submissionInsightService = submissionInsightService;
        this.contentStorageHelper = contentStorageHelper;
        this.contentStorageService = contentStorageService;
    }

    @ApiOperation("Create and send submission.")
    @PostMapping("/api/v1/analysis-management/{analysisId}/submissions")
    public JsonResult<List<DTO>> createSubmission(
            Authentication principal,
            @RequestBody @Validated CreateSubmissionsDTO createSubmissionsDTO,
            @PathVariable("analysisId") Long analysisId)
            throws PermissionDeniedException, NotExistException, IOException, NoExecutableFileException, ValidationException {

        final JsonResult<List<DTO>> result;
        if (principal == null) {
            throw new PermissionDeniedException();
        }
        IUser user = getCurrentUser(principal);
        if (user == null) {
            throw new PermissionDeniedException();
        }
        Analysis analysis = analysisService.getById(analysisId);

        final List<Submission> submissions = AnalysisHelper.createSubmission(submissionService,
                createSubmissionsDTO.getDataSources(), user, analysis);

        final List<DTO> submissionDTOs = submissions.stream()
                .map(s -> conversionService.convert(s, getSubmissionDTOClass()))
                .collect(Collectors.toList());

        result = new JsonResult<>(NO_ERROR);
        result.setResult(submissionDTOs);
        return result;
    }

    @ApiOperation("Get submission.")
    @GetMapping("/api/v1/analysis-management/submissions/{submissionId}")
    public BaseSubmissionAndAnalysisTypeDTO getSubmission(@PathVariable("submissionId") Long submissionId) throws NotExistException {

        T submission = submissionService.getSubmissionById(submissionId);
        BaseSubmissionDTO dto = conversionService.convert(submission, BaseSubmissionDTO.class);
        return new BaseSubmissionAndAnalysisTypeDTO(dto, submission.getSubmissionGroup().getAnalysisType());
    }

    @ApiOperation("Update submission")
    @PutMapping("/api/v1/analysis-management/submissions/{submissionId}")
    public DTO update(
            @PathVariable("submissionId") Long id, @RequestBody @Valid DTO submissionDTO) {

        submissionDTO.setId(id);
        final T submission = conversionService.convert(submissionDTO, getSubmissionClass());
        final T updatedSubmission = submissionService.updateSubmission(submission);
        return conversionService.convert(updatedSubmission, getSubmissionDTOClass());
    }

    @ApiOperation("Approve submission for execute")
    @PostMapping("/api/v1/analysis-management/submissions/{submissionId}/approve")
    public JsonResult<DTO> approveSubmission(
            Principal principal,
            @PathVariable("submissionId") Long id,
            @RequestBody @Valid ApproveDTO approveDTO) throws
            PermissionDeniedException, NotExistException, IOException {

        Boolean isApproved = approveDTO.getIsApproved();
        IUser user = getUser(principal);
        T updatedSubmission = submissionService.approveSubmission(id, isApproved, approveDTO.getComment(), user);
        DTO updatedSubmissionDTO = conversionService.convert(updatedSubmission, getSubmissionDTOClass());
        return new JsonResult<>(NO_ERROR, updatedSubmissionDTO);
    }

    @ApiOperation("Approve submission results for show to owner")
    @PostMapping("/api/v1/analysis-management/submissions/{submissionId}/approveresult")
    public JsonResult<DTO> approveSubmissionResult(
            Authentication principal,
            @PathVariable("submissionId") Long submissionId,
            @RequestBody @Valid ApproveDTO approveDTO) throws PermissionDeniedException, NotExistException {

        //ToDo remove after front will be changed
        approveDTO.setIsSuccess(true);
        IUser user = getCurrentUser(principal);
        Submission updatedSubmission = submissionService.approveSubmissionResult(submissionId, approveDTO, user);

        DTO submissionDTO = conversionService.convert(updatedSubmission, getSubmissionDTOClass());
        return new JsonResult<>(NO_ERROR, submissionDTO);
    }

    @ApiOperation("Manual upload submission result files")
    @PostMapping("/api/v1/analysis-management/submissions/result/manualupload")

    public JsonResult<Boolean> uploadSubmissionResults(
            Principal principal,
            @RequestParam("submissionId") Long id,
            @RequestParam(value = "archive", defaultValue = "false") boolean archive,
            @Valid UploadFileDTO uploadFileDTO
    ) throws IOException {

        LOGGER.info("uploading result files for submission with id='{}'", id);
        if (uploadFileDTO.getFile() == null) {
            return new JsonResult<>(JsonResult.ErrorCode.VALIDATION_ERROR, false);
        }
        MultipartFile uploadedFile = uploadFileDTO.getFile();
        String fileName = ObjectUtils.firstNonNull(uploadFileDTO.getLabel(), uploadedFile.getOriginalFilename());

        Path tempDirectory = Files.createTempDir().toPath();

        try {
            File localFile = tempDirectory.resolve(fileName).toFile();
            localFile.createNewFile();
            uploadedFile.transferTo(localFile);

            if (archive) {
                submissionService.uploadCompressedResultsByDataOwner(id, localFile);
            } else {
                submissionService.uploadResultFileByDataOwner(id, localFile);
            }
            submissionService.updateSubmissionExtendedInfo(id);
        } finally {
            FileUtils.deleteDirectory(tempDirectory.toFile());
        }
        return new JsonResult<>(JsonResult.ErrorCode.NO_ERROR, true);
    }

    @ApiOperation("Delete manually uploaded submission result file")
    @DeleteMapping("/api/v1/analysis-management/submissions/{submissionId}/result/{fileUuid}")
    public JsonResult<Boolean> deleteSubmissionResultsByUuid(
            @PathVariable("submissionId") Long id,
            @PathVariable("fileUuid") String fileUuid
    ) {

        LOGGER.info("deleting result file for submission with id={} having uuid={}", id, fileUuid);
        JsonResult.ErrorCode errorCode;
        Boolean hasResult;
        try {

            ResultFile resultFile = submissionService.getResultFileByPath(
                    contentStorageService.getFileByIdentifier(fileUuid).getPath());

            hasResult = submissionService.deleteSubmissionResultFile(id, resultFile);
            errorCode = JsonResult.ErrorCode.NO_ERROR;
        } catch (NotExistException e) {
            LOGGER.warn("Submission was not found, id: {}", id);
            errorCode = JsonResult.ErrorCode.VALIDATION_ERROR;
            hasResult = false;
        } catch (ValidationException e) {
            LOGGER.warn("Result file was not deleted", e);
            errorCode = JsonResult.ErrorCode.VALIDATION_ERROR;
            hasResult = false;
        }
        JsonResult<Boolean> result = new JsonResult<>(errorCode);
        result.setResult(hasResult);
        return result;
    }

    @ApiOperation("Delete submission insight")
    @DeleteMapping("/api/v1/analysis-management/submissions/{submissionId}/insight")
    public JsonResult deleteSubmissionInsight(@PathVariable("submissionId") Long submissionId) throws NotExistException {

        submissionInsightService.deleteSubmissionInsight(submissionId);
        return new JsonResult<>(NO_ERROR);
    }

    @ApiOperation("Download all result files of the submission.")
    @GetMapping("/api/v1/analysis-management/submissions/{submissionId}/results/all")
    public void downloadAllSubmissionResultFiles(
            Authentication principal,
            @PathVariable("submissionId") Long submissionId,
            HttpServletResponse response) throws PermissionDeniedException, NotExistException, IOException {

        final Submission submission = submissionService.getSubmissionById(submissionId);
        final String analysisTypeCode = submission.getSubmissionGroup().getAnalysis().getType().getCode();
        final String archiveName = String.format("%s_submission_result_%s_%s.zip", analysisTypeCode, submissionId,
                System.currentTimeMillis());

        String contentType = "application/zip, application/octet-stream";
        response.setContentType(contentType);
        response.setHeader("Content-type", contentType);
        response.setHeader("Content-Disposition",
                "attachment; filename=" + archiveName);

        IUser user = getCurrentUser(principal);
        submissionService
                .getSubmissionResultAllFiles(user, submission.getSubmissionGroup().getAnalysis().getId(),
                        submissionId, archiveName, response.getOutputStream());
        response.flushBuffer();
    }

    @ApiOperation("Download query file of the submission group by submission.")
    @GetMapping("/api/v1/analysis-management/submissions/{submissionId}/files/{fileId}/download")
    public void downloadSubmissionGroupFileBySubmission(
            @PathVariable("submissionId") Long submissionId,
            @PathVariable("fileId") Long fileId,
            HttpServletResponse response) throws PermissionDeniedException, NotExistException, IOException {

        Submission submission = submissionService.getSubmissionById(submissionId);
        downloadSubmissionGroupFile(submission.getSubmissionGroup().getId(), fileId, response);
    }

    @ApiOperation("Download query file of the submission group.")
    @GetMapping("/api/v1/analysis-management/submission-groups/{submissionGroupId}/files/{fileId}/download")
    public void downloadSubmissionGroupFile(
            @PathVariable("submissionGroupId") Long submissionGroupId,
            @PathVariable("fileId") Long fileId,
            HttpServletResponse response) throws PermissionDeniedException, NotExistException, IOException {

        SubmissionFile analysisFile = submissionService.getSubmissionFile(submissionGroupId, fileId);
        HttpUtils.putFileContentToResponse(
                response,
                analysisFile.getContentType(),
                getFilename(analysisFile.getRealName()),
                analysisService.getSubmissionFile(analysisFile));
    }

    @ApiOperation("Download submission files")
    @GetMapping("/api/v1/analysis-management/submissions/{submissionId}/files")
    public void getSubmissionFileChunk(
            @PathVariable("submissionId") Long id,
            @RequestParam("updatePassword") String updatePassword,
            @RequestParam("fileName") String fileName,
            HttpServletResponse response
    ) throws IOException {

        try {
            Path file = submissionService.getSubmissionArchiveChunk(id, updatePassword, fileName);
            HttpUtils.putFileContentToResponse(response, MediaType.APPLICATION_OCTET_STREAM_VALUE, fileName, file);
        } catch (FileNotFoundException e) {
            LOGGER.warn("Submission file was not found, id: {}, fileName: {}", id, fileName);
            response.setStatus(HttpStatus.NOT_FOUND.value());
        } catch (IOException ex) {
            LOGGER.info("Error writing file to output stream. Filename was '{}'", fileName, ex);
        }
    }

    @ApiOperation("Get single entry from zip file of the submission.")
    @GetMapping("/api/v1/analysis-management/submissions/{submissionId}/results/zip-entry")
    public JsonResult<ResultFileDTO> getResultZipEntry(
            Authentication principal,
            @PathVariable("submissionId") Long submissionId,
            @RequestParam(value = "path") String path,
            @RequestParam(value = "entry-name") String entryName
    ) throws PermissionDeniedException {

        IUser user = getCurrentUser(principal);
        if (StringUtils.isNoneBlank(entryName)) {
            ResultFileSearch resultFileSearch = new ResultFileSearch();
            String filePath = FilenameUtils.getPath(path);
            String fileName = FilenameUtils.getName(path);
            resultFileSearch.setRealName(fileName);
            resultFileSearch.setPath(filePath);
            List<? extends ArachneFileMeta> resultFileList = submissionService.getResultFiles(user, submissionId, resultFileSearch);
            if (!resultFileList.isEmpty()) {
                ArachneFileMeta zipMetaFile = resultFileList.get(0);
                try (ZipInputStream zin = new ZipInputStream(contentStorageService.getContentByFilepath(zipMetaFile.getPath()))) {
                    byte[] content = ZipUtil.extractZipEntry(zin, entryName);
                    if (ArrayUtils.isEmpty(content)) {
                        LOGGER.info("Cannot find zip entry in the submissionId: {}, path: {}, entry", submissionId, path, entryName);
                    }
                    ResultFileDTO resultFileDTO = new ResultFileDTO(conversionService.convert(zipMetaFile, FileDTO.class));
                    String jsonBody = IOUtils.toString(content, StandardCharsets.UTF_8.name());
                    resultFileDTO.setContent(jsonBody);
                    resultFileDTO.setName(entryName);
                    resultFileDTO.setSubmissionId(submissionId);
                    resultFileDTO.setRelativePath(path);
                    return new JsonResult<>(NO_ERROR, resultFileDTO);
                } catch (IOException e) {
                    LOGGER.error("Cannot read zipFile: {}", zipMetaFile.getPath());
                }
            }
        }
        return new JsonResult<>(NO_ERROR);
    }

    @ApiOperation("Get result files of the submission.")
    @GetMapping("/api/v1/analysis-management/submissions/{submissionId}/results")
    public List<ResultFileDTO> getResultFiles(
            Authentication principal,
            @PathVariable("submissionId") Long submissionId,
            @RequestParam(value = "path", required = false, defaultValue = "") String path,
            @RequestParam(value = "real-name", required = false) String realName
    ) throws PermissionDeniedException, IOException {

        IUser user = getCurrentUser(principal);

        ResultFileSearch resultFileSearch = new ResultFileSearch();
        resultFileSearch.setPath(path);
        resultFileSearch.setRealName(realName);

        List<? extends ArachneFileMeta> resultFileList = submissionService.getResultFiles(user, submissionId, resultFileSearch);
        String resultFilesPath = contentStorageHelper.getResultFilesDir(Submission.class, submissionId, null);

        return resultFileList.stream()
                .map(rf -> {
                    ResultFileDTO rfDto = conversionService.convert(rf, ResultFileDTO.class);
                    rfDto.setSubmissionId(submissionId);
                    rfDto.setRelativePath(contentStorageHelper.getRelativePath(resultFilesPath, rfDto.getPath()));
                    return rfDto;
                })
                .collect(Collectors.toList());
    }

    @ApiOperation("Get status history of the submission")
    @GetMapping("/api/v1/analysis-management/submissions/{submissionId}/status-history")
    public JsonResult<List<SubmissionStatusHistoryElementDTO>> getStatusHistory(
            @PathVariable("submissionId") Long submissionId) throws NotExistException {

        Submission submission = submissionService.getSubmissionById(submissionId);
        List<SubmissionStatusHistoryElement> submissionStatusHistory =
                submissionService.getSubmissionStatusHistory(submission.getSubmissionGroup().getAnalysis().getId(), submissionId);
        List<SubmissionStatusHistoryElementDTO> convert = new LinkedList<>();
        for (SubmissionStatusHistoryElement submissionStatusHistoryElement : submissionStatusHistory) {
            convert.add(conversionService.convert(submissionStatusHistoryElement, SubmissionStatusHistoryElementDTO.class));
        }

        JsonResult<List<SubmissionStatusHistoryElementDTO>> result = new JsonResult<>(NO_ERROR);
        result.setResult(convert);
        return result;
    }

    @ApiOperation("Get query file of the submission by submission.")
    @GetMapping("/api/v1/analysis-management/submissions/{submissionId}/files/{fileId}")
    public JsonResult<SubmissionFileDTO> getSubmissionGroupFileInfoBySubmission(
            @PathVariable("submissionId") Long submissionId,
            @PathVariable("fileId") Long fileId)
            throws NotExistException, IOException {

        Submission submission = submissionService.getSubmissionById(submissionId);
        return getSubmissionGroupFileInfo(submission.getSubmissionGroup().getId(), fileId, Boolean.TRUE);
    }

    @ApiOperation("Get query file of the submission group.")
    @GetMapping("/api/v1/analysis-management/submission-groups/{submissionGroupId}/files/{fileId}")
    public JsonResult<SubmissionFileDTO> getSubmissionGroupFileInfo(
            @PathVariable("submissionGroupId") Long submissionGroupId,
            @PathVariable("fileId") Long fileId,
            @RequestParam(defaultValue = "true") Boolean withContent)
            throws NotExistException, IOException {

        final SubmissionFile submissionFile = submissionService.getSubmissionFile(submissionGroupId, fileId);
        SubmissionFileDTO fileDto = conversionService.convert(submissionFile, SubmissionFileDTO.class);
        if (withContent) {
            fileDto = (SubmissionFileDTO) FileDtoContentHandler
                    .getInstance(fileDto, analysisService.getPath(submissionFile).toFile())
                    .withPdfConverter(toPdfConverter::convert)
                    .handle();
        }
        return new JsonResult<>(NO_ERROR, fileDto);
    }

    @ApiOperation("Update analysis execution status.")
    @PostMapping("/api/v1/analysis-management/submissions/{submissionId}/status/{password}")
    public void setStatus(@PathVariable("submissionId") Long id,
                          @PathVariable("password") String password,
                          @RequestBody CommonAnalysisExecutionStatusDTO status) throws NotExistException {

        final String stdoutDiff = status.getStdout();
        LOGGER.debug("stdout for submission with i='{}' recieved\n{}", id, stdoutDiff);
        List<SubmissionStatus> submissionStatuses = new ArrayList<SubmissionStatus>() {
            {
                add(SubmissionStatus.STARTING);
                add(SubmissionStatus.IN_PROGRESS);
                add(SubmissionStatus.QUEUE_PROCESSING);
            }
        };
        T submission = submissionService.getSubmissionByIdAndUpdatePasswordAndStatus(
                id, password, submissionStatuses);
        final String stdout = submission.getStdout();
        submission.setStdout(stdout == null ? stdoutDiff : stdout + stdoutDiff);
        submission.setStdoutDate(status.getStdoutDate());
        submission.setUpdated(new Date());
        if (submission.getStatus().equals(SubmissionStatus.STARTING) || submission.getStatus().equals(SubmissionStatus.QUEUE_PROCESSING)) {
            submissionService.moveSubmissionToNewStatus(submission, SubmissionStatus.IN_PROGRESS, null, null);
        } else {
            submissionService.saveSubmission(submission);
        }
    }

    @ApiOperation("Get query files of the submission group.")
    @GetMapping("/api/v1/analysis-management/submission-groups/{submissionGroupId}/files")
    public List<SubmissionFileDTO> getSubmissionGroupFiles(
            @PathVariable("submissionGroupId") Long submissionGroupId)
            throws PermissionDeniedException, NotExistException, IOException {

        final List<SubmissionFile> submissionFile = submissionService.getSubmissionFiles(submissionGroupId);
        return submissionFile.stream()
                .map(sf -> conversionService.convert(sf, SubmissionFileDTO.class))
                .collect(Collectors.toList());
    }

    private ArachneFileMeta getResultFile(Authentication principal, Long submissionId, String fileUuid)
            throws NotExistException, PermissionDeniedException {

        Submission submission = submissionService.getSubmissionById(submissionId);
        IUser user = getCurrentUser(principal);
        return submissionService.getResultFileAndCheckPermission(user, submission, submission.getSubmissionGroup().getAnalysis().getId(), fileUuid);
    }

    @ApiOperation("Get result file of the submission.")
    @GetMapping("/api/v1/analysis-management/submissions/{submissionId}/results/{fileUuid}")
    public JsonResult<ResultFileDTO> getResultFileInfo(
            Authentication principal,
            @PathVariable("submissionId") Long submissionId,
            @RequestParam(defaultValue = "true") Boolean withContent,
            @PathVariable("fileUuid") String fileUuid) throws PermissionDeniedException, NotExistException, IOException {

        ArachneFileMeta file = getResultFile(principal, submissionId, fileUuid);
        String resultFilesPath = contentStorageHelper.getResultFilesDir(Submission.class, submissionId, null);

        ResultFileDTO resultFileDTO = new ResultFileDTO(conversionService.convert(file, FileDTO.class));
        resultFileDTO.setRelativePath(contentStorageHelper.getRelativePath(resultFilesPath, resultFileDTO.getPath()));

        if (withContent) {
            byte[] content = IOUtils.toByteArray(contentStorageService.getContentByFilepath(file.getPath()));
            resultFileDTO = (ResultFileDTO) FileDtoContentHandler
                    .getInstance(resultFileDTO, content)
                    .withPdfConverter(toPdfConverter::convert)
                    .handle();
        }

        return new JsonResult<>(NO_ERROR, resultFileDTO);
    }

    @ApiOperation("Download result file of the submission.")
    @GetMapping("/api/v1/analysis-management/submissions/{submissionId}/results/{fileUuid}/download")
    public void downloadResultFile(
            Authentication principal,
            @PathVariable("submissionId") Long submissionId,
            @PathVariable("fileUuid") String fileUuid,
            HttpServletResponse response) throws PermissionDeniedException, NotExistException, IOException {

        ArachneFileMeta file = getResultFile(principal, submissionId, fileUuid);
        HttpUtils.putFileContentToResponse(
                response,
                file.getContentType(),
                file.getName(),
                contentStorageService.getContentByFilepath(file.getPath()));
    }

    @ApiOperation("Download all files of the submission group.")
    @GetMapping("/api/v1/analysis-management/submission-groups/{submissionGroupId}/files/all")
    public void downloadAllSubmissionGroupFiles(
            @PathVariable("submissionGroupId") Long submissionGroupId,
            HttpServletResponse response) throws PermissionDeniedException, NotExistException, IOException {

        String archiveName = "submission_" + submissionGroupId + "_"
                + System.currentTimeMillis()
                + ".zip";
        String contentType = "application/zip, application/octet-stream";
        response.setContentType(contentType);
        response.setHeader("Content-type", contentType);
        response.setHeader("Content-Disposition",
                "attachment; filename=" + archiveName);
        submissionService.getSubmissionAllFiles(submissionGroupId, archiveName, response.getOutputStream());
        response.flushBuffer();
    }

    protected abstract Class<DTO> getSubmissionDTOClass();

    protected abstract Class<T> getSubmissionClass();

    private IUser getCurrentUser(Authentication authentication) {
        return userService.getById(UserUtils.getCurrentUser(authentication).getId());
    }
}
