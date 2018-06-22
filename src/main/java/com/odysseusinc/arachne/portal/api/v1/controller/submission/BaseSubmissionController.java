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
 * Created: September 14, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.controller.submission;

import static com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult.ErrorCode.NO_ERROR;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

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
import com.odysseusinc.arachne.storage.model.ArachneFileMeta;
import com.odysseusinc.arachne.storage.service.ContentStorageService;
import io.swagger.annotations.ApiOperation;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

public abstract class BaseSubmissionController<T extends Submission, A extends Analysis, DTO extends SubmissionDTO>
        extends BaseController {

    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseSubmissionController.class);
    protected final BaseAnalysisService<A> analysisService;
    protected final BaseSubmissionService<T, A> submissionService;
    protected final SubmissionInsightService submissionInsightService;
    protected final ToPdfConverter toPdfConverter;
    private ContentStorageService contentStorageService;
    private ContentStorageHelper contentStorageHelper;

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
    @RequestMapping(value = "/api/v1/analysis-management/{analysisId}/submissions", method = POST)
    public JsonResult<List<DTO>> createSubmission(
            Principal principal,
            @RequestBody @Validated CreateSubmissionsDTO createSubmissionsDTO,
            @PathVariable("analysisId") Long analysisId)
            throws PermissionDeniedException, NotExistException, IOException, NoExecutableFileException, ValidationException {

        final JsonResult<List<DTO>> result;
        if (principal == null) {
            throw new PermissionDeniedException();
        }
        IUser user = userService.getByEmail(principal.getName());
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
    @RequestMapping(value = "/api/v1/analysis-management/submissions/{submissionId}", method = GET)
    public BaseSubmissionAndAnalysisTypeDTO getSubmission(@PathVariable("submissionId") Long submissionId) throws NotExistException {

        T submission = submissionService.getSubmissionById(submissionId);
        BaseSubmissionDTO dto = conversionService.convert(submission, BaseSubmissionDTO.class);
        return new BaseSubmissionAndAnalysisTypeDTO(dto, submission.getSubmissionGroup().getAnalysisType());
    }

    @ApiOperation("Update submission")
    @RequestMapping(value = "/api/v1/analysis-management/submissions/{submissionId}", method = PUT)
    public DTO update(
            @PathVariable("submissionId") Long id, @RequestBody @Valid DTO submissionDTO) {

        submissionDTO.setId(id);
        final T submission = conversionService.convert(submissionDTO, getSubmissionClass());
        final T updatedSubmission = submissionService.updateSubmission(submission);
        return conversionService.convert(updatedSubmission, getSubmissionDTOClass());
    }

    @ApiOperation("Approve submission for execute")
    @RequestMapping(value = "/api/v1/analysis-management/submissions/{submissionId}/approve", method = POST)
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
    @RequestMapping(value = "/api/v1/analysis-management/submissions/{submissionId}/approveresult", method = POST)
    public JsonResult<DTO> approveSubmissionResult(
            Principal principal,
            @PathVariable("submissionId") Long submissionId,
            @RequestBody @Valid ApproveDTO approveDTO) throws PermissionDeniedException, NotExistException {

        //ToDo remove after front will be changed
        approveDTO.setIsSuccess(true);

        Submission updatedSubmission = submissionService.approveSubmissionResult(submissionId, approveDTO, userService
                .getByEmail(principal.getName()));

        DTO submissionDTO = conversionService.convert(updatedSubmission, getSubmissionDTOClass());
        return new JsonResult<>(NO_ERROR, submissionDTO);
    }

    @ApiOperation("Manual upload submission result files")
    @RequestMapping(value = "/api/v1/analysis-management/submissions/result/manualupload",
            method = POST)

    public JsonResult<Boolean> uploadSubmissionResults(
            Principal principal,
            @RequestParam("submissionId") Long id,
            @Valid UploadFileDTO uploadFileDTO
    ) throws IOException, NotExistException, ValidationException {

        LOGGER.info("uploading result files for submission with id='{}'", id);

        JsonResult.ErrorCode errorCode;
        Boolean hasResult;
        if (uploadFileDTO.getFile() == null) {
            errorCode = JsonResult.ErrorCode.VALIDATION_ERROR;
            hasResult = false;
        } else {
            submissionService.uploadResultsByDataOwner(id, uploadFileDTO.getLabel(), uploadFileDTO.getFile());
            errorCode = JsonResult.ErrorCode.NO_ERROR;
            hasResult = true;
        }
        JsonResult<Boolean> result = new JsonResult<>(errorCode);
        result.setResult(hasResult);
        return result;
    }

    @ApiOperation("Delete manually uploaded submission result file")
    @RequestMapping(value = "/api/v1/analysis-management/submissions/{submissionId}/result/{fileUuid}",
            method = DELETE)
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
    @RequestMapping(value = "/api/v1/analysis-management/submissions/{submissionId}/insight", method = DELETE)
    public JsonResult deleteSubmissionInsight(@PathVariable("submissionId") Long submissionId) throws NotExistException {

        submissionInsightService.deleteSubmissionInsight(submissionId);
        return new JsonResult<>(NO_ERROR);
    }

    @ApiOperation("Download all result files of the submission.")
    @RequestMapping(value = "/api/v1/analysis-management/submissions/{submissionId}/results/all", method = GET)
    public void downloadAllSubmissionResultFiles(
            Principal principal,
            @PathVariable("submissionId") Long submissionId,
            HttpServletResponse response) throws PermissionDeniedException, NotExistException, IOException {

        String archiveName = "submission_result_" + submissionId + "_"
                + Long.toString(System.currentTimeMillis())
                + ".zip";
        String contentType = "application/zip, application/octet-stream";
        response.setContentType(contentType);
        response.setHeader("Content-type", contentType);
        response.setHeader("Content-Disposition",
                "attachment; filename=" + archiveName);

        Submission submission = submissionService.getSubmissionById(submissionId);
        IUser user = userService.getByEmail(principal.getName());
        submissionService
                .getSubmissionResultAllFiles(user, submission.getSubmissionGroup().getAnalysis().getId(),
                        submissionId, archiveName, response.getOutputStream());
        response.flushBuffer();
    }

    @ApiOperation("Download query file of the submission group by submission.")
    @RequestMapping(value = "/api/v1/analysis-management/submissions/{submissionId}/files/{fileId}/download",
            method = GET)
    public void downloadSubmissionGroupFileBySubmission(
            @PathVariable("submissionId") Long submissionId,
            @PathVariable("fileId") Long fileId,
            HttpServletResponse response) throws PermissionDeniedException, NotExistException, IOException {

        Submission submission = submissionService.getSubmissionById(submissionId);
        downloadSubmissionGroupFile(submission.getSubmissionGroup().getId(), fileId, response);
    }

    @ApiOperation("Download query file of the submission group.")
    @RequestMapping(value = "/api/v1/analysis-management/submission-groups/{submissionGroupId}/files/{fileId}/download",
            method = GET)
    public void downloadSubmissionGroupFile(
            @PathVariable("submissionGroupId") Long submissionGroupId,
            @PathVariable("fileId") Long fileId,
            HttpServletResponse response) throws PermissionDeniedException, NotExistException, IOException {

        SubmissionFile analysisFile = submissionService.getSubmissionFile(submissionGroupId, fileId);
        HttpUtils.putFileContentToResponse(
                response,
                analysisFile.getContentType(),
                StringUtils.getFilename(analysisFile.getRealName()),
                analysisService.getSubmissionFile(analysisFile));
    }

    @ApiOperation("Download submission files")
    @RequestMapping(value = "/api/v1/analysis-management/submissions/{submissionId}/files", method = GET)
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

    @ApiOperation("Get result files of the submission.")
    @RequestMapping(value = "/api/v1/analysis-management/submissions/{submissionId}/results", method = GET)
    public List<ResultFileDTO> getResultFiles(
            Principal principal,
            @PathVariable("submissionId") Long submissionId,
            @RequestParam(value = "path", required = false, defaultValue = "") String path,
            @RequestParam(value = "real-name", required = false) String realName
    ) throws PermissionDeniedException, IOException {

        IUser user = userService.getByEmail(principal.getName());

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
    @RequestMapping(value = "/api/v1/analysis-management/submissions/{submissionId}/status-history", method = GET)
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
    @RequestMapping(value = "/api/v1/analysis-management/submissions/{submissionId}/files/{fileId}", method = GET)
    public JsonResult<SubmissionFileDTO> getSubmissionGroupFileInfoBySubmission(
            @PathVariable("submissionId") Long submissionId,
            @PathVariable("fileId") Long fileId)
            throws NotExistException, IOException {

        Submission submission = submissionService.getSubmissionById(submissionId);
        return getSubmissionGroupFileInfo(submission.getSubmissionGroup().getId(), fileId, Boolean.TRUE);
    }

    @ApiOperation("Get query file of the submission group.")
    @RequestMapping(value = "/api/v1/analysis-management/submission-groups/{submissionGroupId}/files/{fileId}",
            method = GET)
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
    @RequestMapping(value = "/api/v1/analysis-management/submissions/{submissionId}/status/{password}", method = POST)
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
    @RequestMapping(value = "/api/v1/analysis-management/submission-groups/{submissionGroupId}/files",
            method = GET)
    public List<SubmissionFileDTO> getSubmissionGroupFiles(
            @PathVariable("submissionGroupId") Long submissionGroupId)
            throws PermissionDeniedException, NotExistException, IOException {

        final List<SubmissionFile> submissionFile = submissionService.getSubmissionFiles(submissionGroupId);
        return submissionFile.stream()
                .map(sf -> conversionService.convert(sf, SubmissionFileDTO.class))
                .collect(Collectors.toList());
    }

    private ArachneFileMeta getResultFile(Principal principal, Long submissionId, String fileUuid)
            throws NotExistException, PermissionDeniedException {

        Submission submission = submissionService.getSubmissionById(submissionId);
        IUser user = userService.getByEmail(principal.getName());
        return submissionService.getResultFileAndCheckPermission(user, submission, submission.getSubmissionGroup().getAnalysis().getId(), fileUuid);
    }

    @ApiOperation("Get result file of the submission.")
    @RequestMapping(value = "/api/v1/analysis-management/submissions/{submissionId}/results/{fileUuid}", method = GET)
    public JsonResult<ResultFileDTO> getResultFileInfo(
            Principal principal,
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
    @RequestMapping(value = "/api/v1/analysis-management/submissions/{submissionId}/results/{fileUuid}/download",
            method = GET)
    public void downloadResultFile(
            Principal principal,
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
    @RequestMapping(value = "/api/v1/analysis-management/submission-groups/{submissionGroupId}/files/all", method = GET)
    public void downloadAllSubmissionGroupFiles(
            @PathVariable("submissionGroupId") Long submissionGroupId,
            HttpServletResponse response) throws PermissionDeniedException, NotExistException, IOException {

        String archiveName = "submission_" + submissionGroupId + "_"
                + Long.toString(System.currentTimeMillis())
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
}
