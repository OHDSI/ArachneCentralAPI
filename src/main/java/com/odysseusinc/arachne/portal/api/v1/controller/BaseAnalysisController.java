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
 * Created: September 27, 2017
 */

package com.odysseusinc.arachne.portal.api.v1.controller;

import static com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult.ErrorCode.NO_ERROR;
import static com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult.ErrorCode.PERMISSION_DENIED;
import static com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult.ErrorCode.VALIDATION_ERROR;
import static com.odysseusinc.arachne.portal.util.CommentUtils.getRecentCommentables;
import static com.odysseusinc.arachne.portal.util.HttpUtils.putFileContentToResponse;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import com.google.common.collect.ImmutableMap;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonEntityRequestDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.OptionDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult;
import com.odysseusinc.arachne.commons.service.messaging.ProducerConsumerTemplate;
import com.odysseusinc.arachne.commons.types.DBMSType;
import com.odysseusinc.arachne.commons.utils.CommonFileUtils;
import com.odysseusinc.arachne.portal.api.v1.dto.AnalysisCreateDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.AnalysisDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.AnalysisFileDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.AnalysisLockDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.AnalysisUnlockRequestDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.AnalysisUpdateDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.Commentable;
import com.odysseusinc.arachne.portal.api.v1.dto.DataReferenceDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.FileDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.ShortBaseAnalysisDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.SubmissionGroupDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.SubmissionInsightDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.SubmissionInsightUpdateDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UpdateNotificationDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UploadFileDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UploadFilesDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.converters.FileDtoContentHandler;
import com.odysseusinc.arachne.portal.exception.AlreadyExistException;
import com.odysseusinc.arachne.portal.exception.NotEmptyException;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.NotUniqueException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.exception.ServiceNotAvailableException;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.AnalysisFile;
import com.odysseusinc.arachne.portal.model.AnalysisUnlockRequest;
import com.odysseusinc.arachne.portal.model.AnalysisUnlockRequestStatus;
import com.odysseusinc.arachne.portal.model.CommentTopic;
import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.model.DataReference;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.model.SubmissionInsight;
import com.odysseusinc.arachne.portal.model.search.SubmissionGroupSearch;
import com.odysseusinc.arachne.portal.service.BaseDataNodeService;
import com.odysseusinc.arachne.portal.service.BaseDataSourceService;
import com.odysseusinc.arachne.portal.service.DataReferenceService;
import com.odysseusinc.arachne.portal.service.ImportService;
import com.odysseusinc.arachne.portal.service.ToPdfConverter;
import com.odysseusinc.arachne.portal.service.analysis.BaseAnalysisService;
import com.odysseusinc.arachne.portal.service.messaging.MessagingUtils;
import com.odysseusinc.arachne.portal.service.submission.BaseSubmissionService;
import com.odysseusinc.arachne.portal.service.submission.SubmissionInsightService;
import com.odysseusinc.arachne.portal.util.ImportedFile;
import com.odysseusinc.arachne.portal.util.ZipUtil;
import io.swagger.annotations.ApiOperation;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipOutputStream;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.exception.RuntimeIOException;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

public abstract class BaseAnalysisController<T extends Analysis,
        D extends AnalysisDTO,
        DN extends DataNode,
        A_C_DTO extends AnalysisCreateDTO> extends BaseController<DN, IUser> {
    protected static final Map<CommonAnalysisType, String> ANALISYS_MIMETYPE_MAP = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(AnalysisController.class);
    private static final String ENTITY_IS_NOT_AVAILABLE
            = "'%s' with guid='%s' from DataNode with id='%d' is not available";
    private static final String DEFAULT_EXTENSION = ".txt";
    private static final String DEFAULT_MIMETYPE = "plain/text";
    protected final BaseDataSourceService dataSourceService;
    protected final BaseDataNodeService dataNodeService;
    protected final BaseAnalysisService<T> analysisService;
    protected final DataReferenceService dataReferenceService;
    protected final GenericConversionService conversionService;
    protected final SimpMessagingTemplate wsTemplate;
    protected final JmsTemplate jmsTemplate;
    protected final DestinationResolver destinationResolver;
    protected final ImportService importService;
    protected final BaseSubmissionService<Submission, Analysis> submissionService;
    protected final ToPdfConverter toPdfConverter;
    protected final SubmissionInsightService submissionInsightService;

    @Value("${datanode.messaging.importTimeout}")
    private Long datanodeImportTimeout;

    public BaseAnalysisController(BaseAnalysisService analysisService,
                                  BaseSubmissionService submissionService,
                                  DataReferenceService dataReferenceService,
                                  JmsTemplate jmsTemplate,
                                  GenericConversionService conversionService,
                                  BaseDataNodeService baseDataNodeService,
                                  BaseDataSourceService dataSourceService,
                                  ImportService importService,
                                  SimpMessagingTemplate wsTemplate,
                                  ToPdfConverter toPdfConverter,
                                  SubmissionInsightService submissionInsightService) {

        this.analysisService = analysisService;
        this.submissionService = submissionService;
        this.dataReferenceService = dataReferenceService;
        this.jmsTemplate = jmsTemplate;
        this.conversionService = conversionService;
        this.destinationResolver = jmsTemplate.getDestinationResolver();
        this.dataNodeService = baseDataNodeService;
        this.dataSourceService = dataSourceService;
        this.importService = importService;
        this.wsTemplate = wsTemplate;
        this.submissionInsightService = submissionInsightService;
        this.toPdfConverter = toPdfConverter;
    }

    @ApiOperation("Create analysis.")
    @RequestMapping(value = "/api/v1/analysis-management/analyses", method = POST)
    public JsonResult<D> create(
            Principal principal,
            @RequestBody @Valid A_C_DTO analysisDTO,
            BindingResult bindingResult)
            throws PermissionDeniedException, NotExistException, NotUniqueException {

        JsonResult<D> result;
        IUser user = getUser(principal);

        if (bindingResult.hasErrors()) {
            return setValidationErrors(bindingResult);
        } else {
            T analysis = conversionService.convert(analysisDTO, getAnalysisClass());
            analysis.setAuthor(user);
            analysis = analysisService.create(analysis);
            afterCreate(analysis, analysisDTO);
            result = new JsonResult<>(NO_ERROR);
            result.setResult(conversionService.convert(analysis, getAnalysisDTOClass()));
        }

        return result;
    }

    @ApiOperation("Get short analysis info.")
    @RequestMapping(value = "/api/v1/analysis-management/analyses/{analysisId}/short", method = GET)
    public ShortBaseAnalysisDTO getShortAnalysis(
            @PathVariable("analysisId") Long analysisId)
            throws NotExistException, NotUniqueException {

        T analysis = analysisService.getById(analysisId);
        return conversionService.convert(analysis, ShortBaseAnalysisDTO.class);
    }

    abstract protected Class<T> getAnalysisClass();

    abstract protected Class<D> getAnalysisDTOClass();

    @ApiOperation("Get analysis.")
    @RequestMapping(value = "/api/v1/analysis-management/analyses/{analysisId}", method = GET)
    public JsonResult<D> get(@PathVariable("analysisId") Long id)
            throws NotExistException, PermissionDeniedException {

        JsonResult<D> result;
        T analysis = analysisService.getById(id);
        result = new JsonResult<>(NO_ERROR);
        D analysisDTO = conversionService.convert(analysis, getAnalysisDTOClass());
        result.setResult(analysisDTO);
        return result;
    }

    @ApiOperation("Get submission groups.")
    @RequestMapping(value = "/api/v1/analysis-management/analyses/{analysisId}/submission-groups", method = GET)
    public Page<SubmissionGroupDTO> getSubmissionGroups(
            @PathVariable("analysisId") Long id,
            @ModelAttribute SubmissionGroupSearch submissionGroupSearch
    ) {

        submissionGroupSearch.setAnalysisId(id);
        return submissionService.getSubmissionGroups(submissionGroupSearch).map(sg -> {
            SubmissionGroupDTO sgDTO = conversionService.convert(sg, SubmissionGroupDTO.class);
            sgDTO.getSubmissions().forEach(sd -> {
                Submission s = ((Submission) sd.getConversionSource());
                sd.setAvailableActionList(submissionService.getSubmissionActions(s));
            });
            return sgDTO;
        });
    }

    @ApiOperation("Update analysis.")
    @RequestMapping(value = "/api/v1/analysis-management/analyses/{analysisId}", method = PUT)
    public JsonResult<D> update(
            @PathVariable("analysisId") Long id,
            @RequestBody @Valid AnalysisUpdateDTO analysisDTO,
            BindingResult binding)
            throws NotExistException, NotUniqueException, ValidationException {

        JsonResult<D> result;
        if (binding.hasErrors()) {
            result = new JsonResult<>(VALIDATION_ERROR);
            for (FieldError fieldError : binding.getFieldErrors()) {
                result.getValidatorErrors().put(fieldError.getField(), fieldError.getDefaultMessage());
            }
        } else {
            T analysis = conversionService.convert(analysisDTO, getAnalysisClass());
            analysis.setId(id);
            analysis = analysisService.update(analysis);
            result = new JsonResult<>(NO_ERROR);
            result.setResult(conversionService.convert(analysis, getAnalysisDTOClass()));
        }
        return result;
    }

    @ApiOperation("Delete analysis.")
    @RequestMapping(value = "/api/v1/analysis-management/analyses/{analysisId}", method = DELETE)
    public JsonResult<Boolean> delete(
            @PathVariable("analysisId") Long id)
            throws NotExistException, NotEmptyException {

        JsonResult<Boolean> result;
        if (id == null) {
            result = new JsonResult<>(VALIDATION_ERROR);
            result.getValidatorErrors().put("analysisId", "cannot be null");
        } else {
            analysisService.delete(id);
            result = new JsonResult<>(NO_ERROR);
            result.setResult(Boolean.TRUE);
        }
        return result;
    }

    @ApiOperation("List analyses.")
    @RequestMapping(value = "/api/v1/analysis-management/analyses", method = GET)
    public JsonResult<List<D>> list(
            Principal principal,
            @RequestParam("study-id") Long studyId)
            throws PermissionDeniedException, NotExistException {

        JsonResult<List<D>> result;
        IUser user = userService.getByEmail(principal.getName());
        if (user == null) {
            result = new JsonResult<>(PERMISSION_DENIED);
            return result;
        }
        Iterable<T> analyses = analysisService.list(user, studyId);
        result = new JsonResult<>(NO_ERROR);
        List<D> analysisDTOs = StreamSupport.stream(analyses.spliterator(), false)
                .map(analysis -> conversionService.convert(analysis, getAnalysisDTOClass()))
                .collect(Collectors.toList());
        result.setResult(analysisDTOs);
        return result;
    }

    @ApiOperation("List analysis types")
    @RequestMapping(value = "/api/v1/analysis-management/analyses/types", method = GET)
    public JsonResult<List<OptionDTO>> listTypes()
            throws PermissionDeniedException, NotExistException {

        List<OptionDTO> analysisOptionDTOs = Arrays.stream(CommonAnalysisType.values())
                .map(type -> new OptionDTO(type.name(), type.getTitle()))
                .collect(Collectors.toList());

        return new JsonResult<>(NO_ERROR, analysisOptionDTOs);
    }

    @ApiOperation("Add common entity to analysis")
    @RequestMapping(value = "/api/v1/analysis-management/analyses/{analysisId}/entities", method = POST)
    public JsonResult addCommonEntityToAnalysis(@PathVariable("analysisId") Long analysisId,
                                                @RequestBody @Valid DataReferenceDTO entityReference,
                                                @RequestParam(value = "type", required = false,
                                                        defaultValue = "COHORT") CommonAnalysisType analysisType,
                                                Principal principal)
            throws NotExistException, JMSException, IOException, PermissionDeniedException, URISyntaxException {

        final IUser user = getUser(principal);
        final DataNode dataNode = dataNodeService.getById(entityReference.getDataNodeId());
        final T analysis = analysisService.getById(analysisId);
        final DataReference dataReference = dataReferenceService.addOrUpdate(entityReference.getEntityGuid(), dataNode);
        final List<MultipartFile> entityFiles = getEntityFiles(entityReference, dataNode, analysisType);
        return doAddCommonEntityToAnalysis(analysis, dataReference, user, analysisType, entityFiles);
    }

    protected JsonResult doAddCommonEntityToAnalysis(T analysis, DataReference dataReference, IUser user,
                                                     CommonAnalysisType analysisType,
                                                     List<MultipartFile> files) throws IOException {

        JsonResult result = new JsonResult(NO_ERROR);
        analysisService.saveFiles(files, user, analysis, analysisType, dataReference);
        if (analysisType.equals(CommonAnalysisType.COHORT)) {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            class StringContainer {
                String value = CommonAnalysisType.COHORT.getTitle();
            }
            final StringContainer generatedFileName = new StringContainer();
            try (final ZipOutputStream zos = new ZipOutputStream(out)) {
                files.forEach(file -> {
                    try {
                        if (file.getName().endsWith(CommonFileUtils.OHDSI_SQL_EXT)) {
                            String statement = org.apache.commons.io.IOUtils.toString(file.getInputStream(), "UTF-8");
                            String renderedSql = SqlRender.renderSql(statement, null, null);
                            DBMSType[] dbTypes = new DBMSType[]{DBMSType.POSTGRESQL, DBMSType.ORACLE, DBMSType.MS_SQL_SERVER,
                                    DBMSType.REDSHIFT, DBMSType.PDW};
                            String baseName = FilenameUtils.getBaseName(file.getOriginalFilename());
                            String extension = FilenameUtils.getExtension(file.getOriginalFilename());
                            for (final DBMSType dialect : dbTypes) {
                                final String sql = SqlTranslate.translateSql(renderedSql, DBMSType.MS_SQL_SERVER.getOhdsiDB(),
                                        dialect.getOhdsiDB());
                                final String fileName = baseName + "."
                                        + dialect.getLabel().replaceAll(" ", "-")
                                        + "." + extension;
                                ZipUtil.addZipEntry(zos, fileName, new ByteArrayInputStream(sql.getBytes("UTF-8")));
                            }
                            final String shortBaseName = baseName.replaceAll("\\.ohdsi", "");
                            if (!generatedFileName.value.contains(shortBaseName)) {
                                generatedFileName.value += "_" + shortBaseName;
                            }
                        } else {
                            String fileName = file.getName();
                            ZipUtil.addZipEntry(zos, fileName, file.getInputStream());
                        }
                    } catch (IOException e) {
                        LOGGER.error("Failed to add file to archive", e);
                        throw new RuntimeIOException(e.getMessage(), e);
                    }
                });
            }
            String fileName = generatedFileName.value + ".zip";
            final MultipartFile sqlArchive = new MockMultipartFile(fileName, fileName, "application/zip",
                    out.toByteArray());
            try {
                analysisService.saveFile(sqlArchive, user, analysis, fileName, false, dataReference);
            } catch (AlreadyExistException e) {
                LOGGER.error("Failed to save file", e);
            }
        }
        return result;
    }

    @ApiOperation("update common entity in analysis")
    @RequestMapping(value = "/api/v1/analysis-management/analyses/{analysisId}/entities/{fileUuid}", method = PUT)
    public JsonResult updateCommonEntityInAnalysis(@PathVariable("analysisId") Long analysisId,
                                                   @PathVariable("fileUuid") String fileUuid,
                                                   @RequestParam(value = "type", required = false,
                                                           defaultValue = "COHORT") CommonAnalysisType analysisType,
                                                   Principal principal) throws IOException, JMSException,
            PermissionDeniedException, URISyntaxException {

        final IUser user = getUser(principal);
        final AnalysisFile analysisFile = analysisService.getAnalysisFile(analysisId, fileUuid);
        T analysis = (T) analysisFile.getAnalysis();
        final DataReference dataReference = analysisFile.getDataReference();
        final DataReferenceDTO entityReference = new DataReferenceDTO(
                dataReference.getDataNode().getId(), dataReference.getGuid());

        final List<MultipartFile> entityFiles = getEntityFiles(entityReference, dataReference.getDataNode(), analysisType);

        analysisService.findAnalysisFilesByDataReference(analysis, dataReference).forEach(
                af -> {
                    analysisService.deleteAnalysisFile(analysis, af);
                    analysis.getFiles().remove(af);
                }
        );
        doAddCommonEntityToAnalysis(analysis, dataReference, user, analysisType, entityFiles);
        return new JsonResult(NO_ERROR);
    }

    @ApiOperation("Upload file to attach to analysis.")
    @RequestMapping(value = "/api/v1/analysis-management/analyses/{analysisId}/upload", method = POST)
    public JsonResult<List<AnalysisFileDTO>> uploadFile(Principal principal,
                                                        @Valid UploadFilesDTO uploadFilesLinks,
                                                        @PathVariable("analysisId") Long id)
            throws PermissionDeniedException, NotExistException, IOException {

        IUser user = getUser(principal);
        T analysis = analysisService.getById(id);
        List<UploadFileDTO> files = Stream.concat(uploadFilesLinks.getFiles().stream(), uploadFilesLinks.getLinks().stream()).collect(Collectors.toList());
        return saveFiles(files, user, analysis);
    }

    private JsonResult<List<AnalysisFileDTO>> saveFiles(List<UploadFileDTO> files, IUser user, T analysis) throws IOException {

        List<AnalysisFileDTO> createdFiles = new ArrayList<>();
        JsonResult<List<AnalysisFileDTO>> result = new JsonResult<>(NO_ERROR);
        analysisService.saveFiles(files, user, analysis)
                .forEach(createdFile -> createdFiles.add(conversionService.convert(createdFile, AnalysisFileDTO.class)));
        result.setResult(createdFiles);
        return result;
    }

    @ApiOperation("Replace file in analysis.")
    @RequestMapping(value = "/api/v1/analysis-management/analyses/{analysisId}/files/{fileUuid}", method = PUT)
    public JsonResult<Boolean> updateFile(
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false) Boolean isExecutable,
            @PathVariable("fileUuid") String uuid,
            @PathVariable("analysisId") Long analysisId)
            throws PermissionDeniedException, NotExistException, IOException {

        final JsonResult<Boolean> result;
        analysisService.updateFile(uuid, file, analysisId, isExecutable);
        result = new JsonResult<>(NO_ERROR);
        result.setResult(Boolean.TRUE);
        return result;
    }

    @ApiOperation("Get submission insight")
    @RequestMapping(value = "/api/v1/analysis-management/submissions/{submissionId}/insight", method = GET)
    public JsonResult<SubmissionInsightDTO> getSubmissionInsight(
            @PathVariable("submissionId") Long submissionId,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "order", required = false) Sort.Direction order
    ) throws NotExistException {

        if (size == null) {
            size = Integer.MAX_VALUE;
        }
        if (order == null) {
            order = Sort.Direction.ASC;
        }
        final SubmissionInsight insight = submissionInsightService.getSubmissionInsight(submissionId);
        final SubmissionInsightDTO insightDTO = conversionService.convert(insight, SubmissionInsightDTO.class);
        final Set<CommentTopic> recentTopics = submissionInsightService.getInsightComments(insight, size, new Sort(order, "id"));
        final List<Commentable> recentCommentables = getRecentCommentables(conversionService, recentTopics, insightDTO);
        insightDTO.setRecentCommentEntities(recentCommentables);
        final JsonResult<SubmissionInsightDTO> result = new JsonResult<>(NO_ERROR);
        result.setResult(insightDTO);
        return result;
    }

    @ApiOperation("Create submission insight")
    @RequestMapping(value = "/api/v1/analysis-management/submissions/{submissionId}/insight", method = POST)
    public JsonResult<SubmissionInsightDTO> addSubmissionInsight(
            @PathVariable("submissionId") Long submissionId,
            @RequestBody @Valid SubmissionInsightDTO insightDTO

    ) throws AlreadyExistException, NotExistException {

        final SubmissionInsight insight = conversionService.convert(insightDTO, SubmissionInsight.class);
        final SubmissionInsight savedInsight = submissionInsightService.createSubmissionInsight(submissionId, insight);
        final SubmissionInsightDTO savedInsightDTO = conversionService.convert(savedInsight, SubmissionInsightDTO.class);
        final JsonResult<SubmissionInsightDTO> result = new JsonResult<>(NO_ERROR);
        result.setResult(savedInsightDTO);
        return result;
    }

    @ApiOperation("Update submission insight")
    @RequestMapping(value = "/api/v1/analysis-management/submissions/{submissionId}/insight", method = PUT)
    public JsonResult<SubmissionInsightDTO> updateSubmissionInsight(
            @PathVariable("submissionId") Long submissionId,
            @RequestBody SubmissionInsightUpdateDTO insightDTO
    ) throws NotExistException {

        final SubmissionInsight insight = conversionService.convert(insightDTO, SubmissionInsight.class);
        final SubmissionInsight updatedInsight = submissionInsightService.updateSubmissionInsight(submissionId, insight);
        final SubmissionInsightDTO updatedInsightDTO = conversionService.convert(updatedInsight, SubmissionInsightDTO.class);
        final JsonResult<SubmissionInsightDTO> result = new JsonResult<>(NO_ERROR);
        result.setResult(updatedInsightDTO);
        return result;
    }

    @ApiOperation("Get code file of the submission.")
    @RequestMapping(value = "/api/v1/analysis-management/analyses/{analysisId}/code-files/{fileUuid}/download",
            method = GET)
    public void getFile(
            @PathVariable("analysisId") Long analysisId,
            @PathVariable("fileUuid") String uuid,
            HttpServletResponse response) throws PermissionDeniedException, NotExistException, IOException {

        try {
            AnalysisFile analysisFile = analysisService.getAnalysisFile(analysisId, uuid);
            putFileContentToResponse(
                    response,
                    analysisFile.getContentType(),
                    StringUtils.getFilename(analysisFile.getRealName()),
                    analysisService.getAnalysisFile(analysisFile));
        } catch (IOException ex) {
            LOGGER.info("Error writing file to output stream. Filename was '{}'", uuid, ex);
            throw new IOException("Error writing file to output stream. Filename was " + uuid, ex);
        }
    }

    @ApiOperation("Delete Analysis file")
    @RequestMapping(value = "/api/v1/analysis-management/analyses/{analysisId}/code-files/{fileUuid}", method = DELETE)
    public JsonResult<Boolean> deleteFile(
            @PathVariable("analysisId") Long analysisId,
            @PathVariable("fileUuid") String uuid)
            throws PermissionDeniedException, NotExistException {

        final JsonResult<Boolean> result;
        AnalysisFile analysisFile = analysisService.getAnalysisFile(analysisId, uuid);
        T analysis = (T) analysisFile.getAnalysis();
        Boolean deleteAnalysisFile = analysisService.deleteAnalysisFile(analysis, analysisFile);
        result = new JsonResult<>(NO_ERROR);
        result.setResult(deleteAnalysisFile);

        return result;
    }

    @ApiOperation("Get analysis code file.")
    @RequestMapping(value = "/api/v1/analysis-management/analyses/{analysisId}/code-files/{fileUuid}", method = GET)
    public JsonResult<AnalysisFileDTO> getFileContent(
            @PathVariable("analysisId") Long analysisId,
            @RequestParam(defaultValue = "true") Boolean withContent,
            @PathVariable("fileUuid") String uuid)
            throws PermissionDeniedException, NotExistException, IOException {

        AnalysisFile analysisFile = analysisService.getAnalysisFile(analysisId, uuid);
        AnalysisFileDTO analysisFileDTO = conversionService.convert(analysisFile, AnalysisFileDTO.class);

        if (withContent) {
            analysisFileDTO = (AnalysisFileDTO) FileDtoContentHandler
                    .getInstance(analysisFileDTO, analysisService.getPath(analysisFile).toFile())
                    .withPdfConverter(toPdfConverter::convert)
                    .handle();
        }

        return new JsonResult<>(NO_ERROR, analysisFileDTO);
    }

    @ApiOperation("Lock/unlock analysis files")
    @RequestMapping(value = "/api/v1/analysis-management/analyses/{analysisId}/lock", method = POST)
    public JsonResult<FileDTO> setAnalysisLock(
            @PathVariable("analysisId") Long analysisId,
            @RequestBody AnalysisLockDTO lockFileDTO
    )
            throws NotExistException {

        analysisService.lockAnalysisFiles(analysisId, lockFileDTO.getLocked());
        return new JsonResult<>(NO_ERROR);
    }

    @ApiOperation("Send analysis unlock request")
    @RequestMapping(value = "/api/v1/analysis-management/analyses/{analysisId}/unlock-request", method = POST)
    public JsonResult<FileDTO> sendUnlockRequest(
            Principal principal,
            @PathVariable("analysisId") Long analysisId,
            @RequestBody AnalysisUnlockRequestDTO analysisUnlockRequestDTO
    )
            throws NotExistException, PermissionDeniedException, AlreadyExistException {

        JsonResult result;
        final IUser user = getUser(principal);

        final AnalysisUnlockRequest unlockRequest = new AnalysisUnlockRequest();
        unlockRequest.setUser(user);
        unlockRequest.setStatus(AnalysisUnlockRequestStatus.PENDING);
        unlockRequest.setCreated(new Date());
        unlockRequest.setToken(UUID.randomUUID().toString().replace("-", ""));
        unlockRequest.setDescription(analysisUnlockRequestDTO.getDescription());
        try {
            final AnalysisUnlockRequest analysisUnlockRequest = analysisService.sendAnalysisUnlockRequest(
                    analysisId,
                    unlockRequest
            );
            analysisService.findLeads((T) analysisUnlockRequest.getAnalysis()).forEach(lead ->
                    wsTemplate.convertAndSendToUser(lead.getUsername(),
                            "/topic/invitations",
                            new UpdateNotificationDTO()
                    )
            );
            result = new JsonResult<>(NO_ERROR);
        } catch (AlreadyExistException ex) {
            result = new JsonResult<>(VALIDATION_ERROR);
            result.setErrorMessage("Unlock request for the analysis was already created");
        }
        return result;
    }

    @ApiOperation("Get all files of the submission result.")
    @RequestMapping(value = "/api/v1/analysis-management/analyses/{analysisId}/code-files/all", method = GET)
    public void getAllAnalysisFiles(
            @PathVariable("analysisId") Long analysisId,
            HttpServletResponse response) throws PermissionDeniedException, NotExistException, IOException {

        String archiveName = "analysis_" + analysisId + "_"
                + Long.toString(System.currentTimeMillis())
                + ".zip";
        String contentType = "application/zip, application/octet-stream";
        response.setContentType(contentType);
        response.setHeader("Content-type", contentType);
        response.setHeader("Content-Disposition",
                "attachment; filename=" + archiveName);
        analysisService.getAnalysisAllFiles(analysisId, archiveName, response.getOutputStream());
        response.flushBuffer();
    }

    @ApiOperation("Write content of the code file.")
    @RequestMapping(value = "/api/v1/analysis-management/analyses/{analysisId}/code-files/{fileUuid}",
            method = PUT)
    public JsonResult<Boolean> putFileContent(
            final Principal principal,
            final @RequestBody FileDTO fileDTO,
            final @PathVariable("analysisId") Long analysisId,
            final @PathVariable("fileUuid") String uuid)
            throws PermissionDeniedException, NotExistException, IOException, URISyntaxException {

        final IUser user = getUser(principal);
        final AnalysisFile analysisFile = analysisService.getAnalysisFile(analysisId, uuid);
        analysisService.updateCodeFile(analysisFile, fileDTO, user);
        return new JsonResult<>(NO_ERROR, Boolean.TRUE);
    }

    protected List<MultipartFile> getEntityFiles(DataReferenceDTO entityReference, DataNode dataNode, CommonAnalysisType entityType)
            throws JMSException, IOException, URISyntaxException {

        Long waitForResponse = datanodeImportTimeout;
        Long messageLifeTime = datanodeImportTimeout;
        String baseQueue = MessagingUtils.Entities.getBaseQueue(dataNode);
        CommonEntityRequestDTO request = new CommonEntityRequestDTO(entityReference.getEntityGuid(), entityType);

        ProducerConsumerTemplate exchangeTpl = new ProducerConsumerTemplate(
                destinationResolver,
                request,
                baseQueue,
                waitForResponse,
                messageLifeTime
        );
        final ObjectMessage responseMessage = jmsTemplate.execute(
                exchangeTpl,
                true
        );
        if (responseMessage == null) {
            String message = String.format(ENTITY_IS_NOT_AVAILABLE,
                    entityType.getTitle(),
                    entityReference.getEntityGuid(),
                    entityReference.getDataNodeId());
            throw new ServiceNotAvailableException(message);
        }
        List<MultipartFile> files = new LinkedList<>();
        final List<ImportedFile> importedFiles = (List<ImportedFile>) responseMessage.getObject();
        if (entityType.equals(CommonAnalysisType.ESTIMATION)) {
            files.addAll(importService.processEstimation(importedFiles));
        } else {
            files = importedFiles.stream()
                    .map(file -> conversionService.convert(file, MockMultipartFile.class))
                    .collect(Collectors.toList());
        }
        if (entityType.equals(CommonAnalysisType.PREDICTION)) {
            attachPredictionFiles(files);
        }
        if (entityType.equals(CommonAnalysisType.COHORT_CHARACTERIZATION)) {
            attachCohortCharacterizationFiles(files);
        }
        if (entityType.equals(CommonAnalysisType.INCIDENCE)) {
            attachIncidenceRatesFiles(files);
        }
        return files;
    }

    protected abstract void attachIncidenceRatesFiles(List<MultipartFile> files) throws IOException;

    protected byte[] readResource(final String path) throws IOException {

        Resource resource = new ClassPathResource(path);
        try (InputStream in = resource.getInputStream()) {
            return IOUtils.toByteArray(in);
        }
    }

    protected void afterCreate(T analysis, A_C_DTO analysisDTO) {

    }

    protected abstract void attachPredictionFiles(List<MultipartFile> files) throws IOException;

    protected abstract void attachCohortCharacterizationFiles(List<MultipartFile> files) throws IOException, URISyntaxException;

}
