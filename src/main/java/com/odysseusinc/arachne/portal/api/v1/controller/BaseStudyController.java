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
 * Created: September 08, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.controller;

import static com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult.ErrorCode.NO_ERROR;
import static com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult.ErrorCode.PERMISSION_DENIED;
import static com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult.ErrorCode.SYSTEM_ERROR;
import static com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult.ErrorCode.VALIDATION_ERROR;
import static com.odysseusinc.arachne.portal.util.CommentUtils.getRecentCommentables;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import com.odysseusinc.arachne.commons.api.v1.dto.util.JsonResult;
import com.odysseusinc.arachne.portal.api.v1.dto.AddStudyParticipantDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.BooleanDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.Commentable;
import com.odysseusinc.arachne.portal.api.v1.dto.CreateStudyDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.CreateVirtualDataSourceDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.DataSourceDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.EntityLinkDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.MoveAnalysisDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.StudyDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.StudyListDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.SubmissionInsightDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UpdateNotificationDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UpdateParticipantDTO;
import com.odysseusinc.arachne.portal.exception.AlreadyExistException;
import com.odysseusinc.arachne.portal.exception.FieldException;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.NotUniqueException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.model.AbstractUserStudyListItem;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.CommentTopic;
import com.odysseusinc.arachne.portal.model.DataSource;
import com.odysseusinc.arachne.portal.model.Paper;
import com.odysseusinc.arachne.portal.model.ParticipantRole;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.StudyDataSourceLink;
import com.odysseusinc.arachne.portal.model.StudyFile;
import com.odysseusinc.arachne.portal.model.SubmissionInsight;
import com.odysseusinc.arachne.portal.model.SuggestSearchRegion;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.model.UserStudy;
import com.odysseusinc.arachne.portal.model.search.PaperSearch;
import com.odysseusinc.arachne.portal.model.search.StudySearch;
import com.odysseusinc.arachne.portal.model.statemachine.study.StudyStateMachine;
import com.odysseusinc.arachne.portal.model.statemachine.study.StudyTransition;
import com.odysseusinc.arachne.portal.service.BaseStudyService;
import com.odysseusinc.arachne.portal.service.FileService;
import com.odysseusinc.arachne.portal.service.analysis.BaseAnalysisService;
import io.swagger.annotations.ApiOperation;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

public abstract class BaseStudyController<
        T extends Study,
        DS extends DataSource,
        A extends Analysis,
        SD extends StudyDTO,
        SS extends StudySearch,
        SU extends AbstractUserStudyListItem,
        SL extends StudyListDTO> extends BaseController {

    private static final Logger LOG = LoggerFactory.getLogger(StudyController.class);
    private final FileService fileService;
    private final StudyStateMachine studyStateMachine;
    protected BaseStudyService<T, DS, SS, SU> studyService;
    protected GenericConversionService conversionService;
    private BaseAnalysisService<A> analysisService;
    private SimpMessagingTemplate wsTemplate;

    public BaseStudyController(BaseStudyService<T, DS, SS, SU> studyService,
                               BaseAnalysisService<A> analysisService,
                               GenericConversionService conversionService,
                               SimpMessagingTemplate wsTemplate,
                               FileService fileService,
                               StudyStateMachine studyStateMachine) {

        this.studyService = studyService;
        this.analysisService = analysisService;
        this.conversionService = conversionService;
        this.wsTemplate = wsTemplate;
        this.fileService = fileService;
        this.studyStateMachine = studyStateMachine;
    }

    public abstract T convert(CreateStudyDTO studyDTO);

    public abstract T convert(SD studyDto);

    @ApiOperation("Create study.")
    @RequestMapping(value = "/api/v1/study-management/studies", method = POST)
    public JsonResult<SD> create(
            Principal principal,
            @RequestBody @Valid CreateStudyDTO studyDTO,
            BindingResult binding)
            throws NotExistException, NotUniqueException {

        JsonResult<SD> result;
        User user = userService.getByEmail(principal.getName());
        if (user != null) {
            if (binding.hasErrors()) {
                result = setValidationErrors(binding);
            } else {
                T study = convert(studyDTO);
                study = studyService.create(user, study);
                result = new JsonResult<>(NO_ERROR);
                result.setResult(convert(study));
            }
        } else {
            result = new JsonResult<>(PERMISSION_DENIED);
        }
        return result;
    }

    @ApiOperation("List study statuses.")
    @RequestMapping(value = "/api/v1/study-management/studies/{studyId}/available-state-transitions", method = RequestMethod.GET)
    public List<StudyTransition> getAvailableTransitions(@PathVariable Long studyId) {

        return studyStateMachine.getAvailableStates(studyService.getById(studyId));
    }

    @RequestMapping(value = "/api/v1/study-management/studies/{studyId}", method = GET)
    public JsonResult<SD> get(
            @PathVariable("studyId") Long id,
            Principal principal)
            throws PermissionDeniedException, NotExistException {

        JsonResult<SD> result;
        User user = getUser(principal);
        SU myStudy = studyService.getStudy(user, id);
        result = new JsonResult<>(NO_ERROR);
        SD studyDTO = convert(myStudy);
        result.setResult(studyDTO);
        return result;
    }

    protected abstract SD convert(SU myStudy);

    @RequestMapping(value = "/api/v1/study-management/studies/{studyId}", method = PUT)
    public JsonResult<SD> update(
            @PathVariable("studyId") Long id,
            @RequestBody @Valid SD studyDTO,
            BindingResult binding)
            throws PermissionDeniedException, NotExistException, NotUniqueException, ValidationException {

        JsonResult<SD> result;
        if (binding.hasErrors()) {
            result = setValidationErrors(binding);
        } else {
            T study = convert(studyDTO);
            study.setId(id);
            study = studyService.update(study);
            result = new JsonResult<>(NO_ERROR);
            SD dto = convert(study);
            result.setResult(dto);
        }
        return result;
    }

    protected abstract SD convert(T study);

    @RequestMapping(value = "/api/v1/study-management/studies/{studyId}/favourite", method = PUT)
    public JsonResult updateFavourite(
            @PathVariable("studyId") Long studyId,
            @RequestBody BooleanDTO isFavourite,
            Principal principal)
            throws PermissionDeniedException, NotExistException, NotUniqueException, ValidationException {

        final User user = getUser(principal);
        studyService.setFavourite(user.getId(), studyId, isFavourite.isValue());
        return new JsonResult<>(NO_ERROR);
    }

    @ApiOperation(value = "Delete study.", hidden = true)
    @RequestMapping(value = "/api/v1/study-management/studies/{studyId}", method = DELETE)
    public JsonResult<Boolean> delete(
            @PathVariable("studyId") Long id)
            throws NotExistException {

        JsonResult<Boolean> result;
        if (id == null) {
            result = new JsonResult<>(VALIDATION_ERROR);
            result.getValidatorErrors().put("studyId", "cannot be null");
        } else {
            try {
                studyService.delete(id);
                result = new JsonResult<>(NO_ERROR);
                result.setResult(Boolean.TRUE);
            } catch (NotExistException ex) {
                LOG.error(ex.getMessage(), ex);
                result = new JsonResult<>(VALIDATION_ERROR);
                result.getValidatorErrors().put("studyId", "study with id=" + id + " not found");
                result.setErrorMessage(ex.getMessage());
            } catch (Exception ex) {
                LOG.error(ex.getMessage(), ex);
                result = new JsonResult<>(SYSTEM_ERROR);
                result.setErrorMessage(ex.getMessage());

            }
        }
        return result;
    }

    @ApiOperation("List studies.")
    @RequestMapping(value = "/api/v1/study-management/studies", method = GET)
    public JsonResult<Page<SL>> list(
            Principal principal,
            SS studySearch
    ) throws PermissionDeniedException {

        handleInputSearchParams(studySearch);

        final User user = getUser(principal);

        studySearch.setUserId(user.getId());

        Page<SL> converted =
                studyService.findStudies(studySearch)
                        .map(this::convertListItem);

        return new JsonResult<>(NO_ERROR, converted);
    }

    protected abstract SL convertListItem(AbstractUserStudyListItem abstractUserStudyListItem);

    protected void handleInputSearchParams(SS studyViewSearchParams) {

        if (studyViewSearchParams.getPage() == null) {
            studyViewSearchParams.setPage(1);
        }
        if (studyViewSearchParams.getPagesize() == null) {
            studyViewSearchParams.setPagesize(Integer.MAX_VALUE);
        }
    }


    @ApiOperation("Add participant to the study.")
    @RequestMapping(value = "/api/v1/study-management/studies/{studyId}/participants",
            method = POST)
    public JsonResult<Boolean> addParticipant(
            Principal principal,
            @PathVariable("studyId") Long studyId,
            @RequestBody @Valid AddStudyParticipantDTO addParticipantDTO,
            BindingResult binding
    ) throws PermissionDeniedException, NotExistException, AlreadyExistException {

        JsonResult<Boolean> result;
        if (binding.hasErrors()) {
            return setValidationErrors(binding);
        }
        final User createdBy = getUser(principal);
        UserStudy userStudy = studyService.addParticipant(
                createdBy,
                studyId,
                addParticipantDTO.getUserId(),
                addParticipantDTO.getRole()
        );

        wsTemplate.convertAndSendToUser(
                userStudy.getUser().getUsername(),
                "/topic/invitations",
                new UpdateNotificationDTO()
        );

        return new JsonResult<>(NO_ERROR, Boolean.TRUE);
    }

    @ApiOperation("Update participant.")
    @RequestMapping(value = "/api/v1/study-management/studies/{studyId}/participants/{userId}",
            method = PUT)
    public JsonResult<Boolean> updateParticipantRole(
            @PathVariable("studyId") Long studyId,
            @PathVariable("userId") Long userId,
            @RequestBody @Valid UpdateParticipantDTO participantDTO
    ) throws PermissionDeniedException, NotExistException, AlreadyExistException, ValidationException {

        if (participantDTO.getRole() != null) {
            ParticipantRole newRole = ParticipantRole.valueOf(participantDTO.getRole());
            UserStudy userStudy = studyService.updateParticipantRole(studyId, userId, newRole);
        }

        return new JsonResult<>(NO_ERROR, Boolean.TRUE);
    }

    @ApiOperation("Remove participant from the study.")
    @RequestMapping(value = "/api/v1/study-management/studies/{studyId}/participants/{userId}",
            method = DELETE)
    public JsonResult<Boolean> removeParticipant(
            Principal principal,
            @PathVariable("studyId") Long id,
            @PathVariable("userId") Long userId)
            throws PermissionDeniedException, NotExistException, ValidationException {

        JsonResult<Boolean> result;
        studyService.removeParticipant(id, userId);
        result = new JsonResult<>(NO_ERROR);
        result.setResult(Boolean.TRUE);
        return result;
    }

    @ApiOperation("Change analysis order.")
    @RequestMapping(value = "/api/v1/study-management/move-analysis", method = POST)
    public JsonResult<Boolean> moveAnalysis(@RequestBody MoveAnalysisDTO moveAnalysisDTO) {

        JsonResult<Boolean> result;
        Boolean res = analysisService.moveAnalysis(moveAnalysisDTO.getAnalysisId(), moveAnalysisDTO.getNewIndex());
        result = new JsonResult<>(NO_ERROR);
        result.setResult(res);
        return result;
    }


    @ApiOperation("Upload file to the study files.")
    @RequestMapping(value = "/api/v1/study-management/studies/{studyId}/upload", method = POST)
    public JsonResult<Boolean> uploadFile(
            Principal principal,
            @RequestParam(required = false) MultipartFile[] file,
            @RequestParam String label,
            @RequestParam(required = false) String link,
            @PathVariable("studyId") @NotNull Long id
    ) throws PermissionDeniedException, NotExistException, IOException {

        JsonResult<Boolean> result = null;
        final User user = getUser(principal);
        if (file != null && file.length > 0) {
            for (MultipartFile multipartFile : file) {
                studyService.saveFile(multipartFile, id, label, user);
                result = new JsonResult<>(NO_ERROR);
                result.setResult(Boolean.TRUE);
            }
        } else {
            if (link != null && !link.isEmpty()) {
                studyService.saveFile(link, id, label, user);
                result = new JsonResult<>(NO_ERROR);
                result.setResult(Boolean.TRUE);
            } else {
                result = new JsonResult<>(VALIDATION_ERROR);
                result.setResult(Boolean.FALSE);
            }
        }
        return result;
    }

    @ApiOperation("Get file of the study.")
    @RequestMapping(value = "/api/v1/study-management/studies/{studyId}/files/{fileUuid}", method = GET)
    public void getFile(
            @PathVariable("studyId") Long studyId,
            @PathVariable("fileUuid") String uuid,
            HttpServletResponse response) throws PermissionDeniedException, NotExistException, IOException {

        StudyFile studyFile = studyService.getStudyFile(studyId, uuid);
        final InputStream is = fileService.getFileInputStream(studyFile);
        response.setContentType(studyFile.getContentType());
        response.setHeader("Content-type", studyFile.getContentType());
        response.setHeader("Content-Disposition", "attachment; filename=" + studyFile.getRealName());
        org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
        response.flushBuffer();
    }

    @ApiOperation("Get all files of the study.")
    @RequestMapping(value = "/api/v1/study-management/studies/{studyId}/files/all", method = GET)
    public void getAllFiles(
            @PathVariable("studyId") Long studyId,
            HttpServletResponse response) throws PermissionDeniedException, NotExistException, IOException {

        String archiveName = "study_" + studyId + "_"
                + Long.toString(System.currentTimeMillis())
                + ".zip";
        String contentType = "application/zip, application/octet-stream";
        response.setContentType(contentType);
        response.setHeader("Content-type", contentType);
        response.setHeader("Content-Disposition",
                "attachment; filename=" + archiveName);
        studyService.getStudyAllFiles(studyId, archiveName, response.getOutputStream());
        response.flushBuffer();
    }

    @ApiOperation("Delete file of the study.")
    @RequestMapping(value = "/api/v1/study-management/studies/{studyId}/files/{fileUuid}", method = DELETE)
    public JsonResult<Boolean> deleteFile(
            @PathVariable("studyId") Long studyId,
            @PathVariable("fileUuid") String uuid)
            throws PermissionDeniedException, NotExistException, FileNotFoundException {

        JsonResult<Boolean> result;
        studyService.getDeleteStudyFile(studyId, uuid);
        result = new JsonResult<>(NO_ERROR);
        result.setResult(Boolean.TRUE);
        return result;
    }

    @ApiOperation("List study data sources.")
    @RequestMapping(value = "/api/v1/study-management/studies/{studyId}/data-sources/", method = GET)
    public JsonResult<List<DataSourceDTO>> listDataSources(
            @PathVariable("studyId") Long studyId)
            throws PermissionDeniedException {

        long start = System.currentTimeMillis();
        JsonResult<List<DataSourceDTO>> result;
        List<StudyDataSourceLink> dataSources = studyService.listApprovedDataSources(studyId);
        List<DataSourceDTO> dataSourceDTOs = new ArrayList<>();
        for (StudyDataSourceLink dataSource : dataSources) {
            DataSourceDTO dataSourceDTO = conversionService.convert(dataSource, DataSourceDTO.class);
            dataSourceDTOs.add(dataSourceDTO);
        }
        result = new JsonResult<>(NO_ERROR);
        result.setResult(dataSourceDTOs);

        return result;
    }

    @ApiOperation("Create virtual data source linked to study")
    @RequestMapping(value = "/api/v1/study-management/studies/{studyId}/data-sources", method = POST)
    public JsonResult<DataSourceDTO> createVirtualDataSource(
            Principal principal,
            @PathVariable("studyId") Long studyId,
            @RequestBody @Valid CreateVirtualDataSourceDTO dataSourceDTO
    ) throws PermissionDeniedException, NotExistException, IllegalAccessException, SolrServerException, IOException, ValidationException, FieldException, AlreadyExistException, NoSuchFieldException {

        final User createdBy = getUser(principal);
        final DataSource dataSource = studyService.addVirtualDataSource(
                createdBy,
                studyId,
                dataSourceDTO.getName(),
                dataSourceDTO.getDataOwnersIds()
        );
        final DataSourceDTO registeredDataSourceDTO = conversionService.convert(dataSource, DataSourceDTO.class);
        return new JsonResult<>(NO_ERROR, registeredDataSourceDTO);
    }

    @ApiOperation("Add data source to the study.")
    @RequestMapping(value = "/api/v1/study-management/studies/{studyId}/data-sources/{dataSourceId}", method = POST)
    public EntityLinkDTO addDataSource(
            Principal principal,
            @PathVariable("studyId") Long studyId,
            @PathVariable("dataSourceId") Long dataSourceId)
            throws PermissionDeniedException, NotExistException, AlreadyExistException {

        final User createdBy = getUser(principal);
        StudyDataSourceLink link = studyService.addDataSource(createdBy, studyId, dataSourceId);
        return conversionService.convert(link, EntityLinkDTO.class);
    }

    @ApiOperation("Remove data source from the study.")
    @RequestMapping(value = "/api/v1/study-management/studies/{studyId}/data-sources/{dataSourceId}",
            method = DELETE)
    public JsonResult<Boolean> removeDataSource(
            @PathVariable("studyId") Long id,
            @PathVariable("dataSourceId") Long dataSourceId)
            throws PermissionDeniedException, NotExistException {

        studyService.removeDataSource(id, dataSourceId);
        return new JsonResult<>(NO_ERROR, Boolean.TRUE);
    }

    @ApiOperation("Suggest study.")
    @RequestMapping(value = "/api/v1/study-management/studies/search", method = GET)
    public JsonResult<List<StudyDTO>> suggest(
            Principal principal,
            @RequestParam("id") Long id,
            @RequestParam("query") String query,
            @RequestParam("region") SuggestSearchRegion region) throws PermissionDeniedException {

        JsonResult<List<StudyDTO>> result;
        User owner = getUser(principal);
        Iterable<T> studies = studyService.suggestStudy(query, owner, id, region);
        result = new JsonResult<>(JsonResult.ErrorCode.NO_ERROR);
        List<StudyDTO> studiesDTOs = new LinkedList<>();
        for (Study study : studies) {
            studiesDTOs.add(conversionService.convert(study, StudyDTO.class));
        }
        result.setResult(studiesDTOs);
        return result;
    }

    @ApiOperation("Get recent Insights of Study")
    @RequestMapping(value = "/api/v1/study-management/studies/{studyId}/insights", method = GET)
    public List<SubmissionInsightDTO> getStudyInsights(
            @PathVariable("studyId") Long studyId,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "commentsPerInsight", required = false) Integer commentsPerInsight,
            @RequestParam(value = "order", required = false) Sort.Direction order
    ) {

        if (size == null) {
            size = Integer.MAX_VALUE;
        }
        if (commentsPerInsight == null) {
            commentsPerInsight = Integer.MAX_VALUE;
        }
        if (order == null) {
            order = Sort.Direction.DESC;
        }

        List<SubmissionInsightDTO> submissionInsightDTOS = new ArrayList<>();

        Pageable pageRequest = new PageRequest(0, size, new Sort(order, "created"));
        final Page<SubmissionInsight> page = analysisService.getInsightsByStudyId(studyId, pageRequest);
        final List<SubmissionInsight> insights = page.getContent();
        for (int i = 0; i < insights.size(); i++) {
            final SubmissionInsight insight = insights.get(i);
            final Set<CommentTopic> recentTopics = analysisService.getInsightComments(insight,
                    commentsPerInsight, new Sort(Sort.Direction.DESC, "id"));
            final SubmissionInsightDTO insightDTO = conversionService.convert(insight, SubmissionInsightDTO.class);
            final List<Commentable> recentCommentables = getRecentCommentables(conversionService, recentTopics,
                    insightDTO);
            insightDTO.setRecentCommentEntities(recentCommentables);
            submissionInsightDTOS.add(insightDTO);
        }

        if (LOG.isDebugEnabled()) {
            submissionInsightDTOS.stream().forEach(submissionInsightDTO -> {
                LOG.debug("+" + submissionInsightDTO.getName());
                submissionInsightDTO.getRecentCommentEntities().stream().forEach(commentable -> {
                    LOG.debug("|+" + commentable.getName());
                    commentable.getTopic().getComments().stream().forEach(commentDTO -> {
                        LOG.debug(" |-"
                                + commentDTO.getAuthor().getFirstname() + ":"
                                + commentDTO.getDate() + ":"
                                + commentDTO.getComment());
                    });
                });
            });
        }

        return submissionInsightDTOS;
    }
}
