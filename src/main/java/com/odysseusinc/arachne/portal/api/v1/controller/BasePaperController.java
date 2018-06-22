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

package com.odysseusinc.arachne.portal.api.v1.controller;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import com.odysseusinc.arachne.commons.utils.CommonFileUtils;
import com.odysseusinc.arachne.portal.api.v1.dto.BooleanDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.CreatePaperDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.FileDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.PaperDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.PaperFileDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.ShortPaperDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UpdatePaperDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.UploadFileDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.converters.FileDtoContentHandler;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.NotUniqueException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.model.AbstractPaperFile;
import com.odysseusinc.arachne.portal.model.AbstractUserStudyListItem;
import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.Paper;
import com.odysseusinc.arachne.portal.model.PaperFileType;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.model.search.PaperSearch;
import com.odysseusinc.arachne.portal.model.search.StudySearch;
import com.odysseusinc.arachne.portal.service.BasePaperService;
import com.odysseusinc.arachne.portal.service.ToPdfConverter;
import com.odysseusinc.arachne.portal.service.StudyFileService;
import com.odysseusinc.arachne.portal.util.FileUtils;
import io.swagger.annotations.ApiOperation;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Validated
public abstract class BasePaperController
        <P extends Paper,
        PS extends PaperSearch,
        U_P_DTO extends UpdatePaperDTO,
        S_P_DTO extends ShortPaperDTO,
        DTO extends PaperDTO,
        C_P_DTO extends CreatePaperDTO,
        S extends Study,
        DS extends IDataSource,
        SS extends StudySearch,
        SU extends AbstractUserStudyListItem> extends BaseController {

    protected BasePaperService<P, PS, S, DS, SS, SU> paperService;
    protected GenericConversionService conversionService;
    protected StudyFileService fileService;

    @Autowired
    public BasePaperController(BasePaperService<P, PS, S, DS, SS, SU> paperService, GenericConversionService conversionService, StudyFileService fileService) {

        this.paperService = paperService;
        this.conversionService = conversionService;
        this.fileService = fileService;
    }

    @Autowired
    private ToPdfConverter toPdfConverter;

    @ApiOperation("Get Papers")
    @RequestMapping(method = GET)
    public Page<S_P_DTO> getPapers(
            Principal principal,
            PS paperSearch
    ) throws PermissionDeniedException, IOException {

        handleInputParams(paperSearch);
        final IUser user = getUser(principal);
        final Page<P> paperPage = paperService.getPapersAccordingToCurrentUser(paperSearch, user);
        Page<S_P_DTO> result = paperPage.map(paper -> {
            final S_P_DTO dto = convertPaperToShortPaperDTO(paper);
            dto.setFavourite(paper.getFollowers().contains(user));
            return dto;
        });
        return result;
    }

    protected abstract S_P_DTO convertPaperToShortPaperDTO(P paper);

    private void handleInputParams(PS paperSearch) {
        // find a better way of doing such things

        paperSearch.setPage(paperSearch.getPage() - 1);
    }

    @ApiOperation("Get Paper")
    @RequestMapping(value = "/{id}", method = GET)
    public DTO getPaper(
            @PathVariable("id") Long id
    ) {

        final P paper = paperService.get(id);
        return convertPaperToPaperDTO(paper);
    }

    protected abstract DTO convertPaperToPaperDTO(P paper);

    @ApiOperation("Create Paper")
    @RequestMapping(method = POST)
    public DTO createPaper(@RequestBody C_P_DTO createPaperDTO, Principal principal)
            throws PermissionDeniedException {

        final IUser user = getUser(principal);
        final P paper = paperService.create(user, createPaperDTO.getStudyId());
        return convertPaperToPaperDTO(paper);
    }

    @ApiOperation("Update Paper")
    @RequestMapping(path = "/{id}", method = PUT)
    public DTO updatePaper(@PathVariable Long id, @RequestBody U_P_DTO updatePaperDTO)
            throws PermissionDeniedException {

        final P paper = convertUpdatePaperDtoToPaper(updatePaperDTO);
        paper.setId(id);
        return convertPaperToPaperDTO(paperService.update(paper));
    }

    protected abstract P convertUpdatePaperDtoToPaper(U_P_DTO updatePaperDTO);

    @ApiOperation("Set Paper favourite")
    @RequestMapping(value = "{id}/favourite", method = PUT)
    public void setFavourite(
            @PathVariable("id") Long id,
            @RequestBody BooleanDTO isFavourite,
            Principal principal)
            throws PermissionDeniedException, NotUniqueException, ValidationException {

        final IUser user = getUser(principal);
        paperService.setFavourite(user.getId(), id, isFavourite.isValue());
    }

    @ApiOperation("Delete Study Paper")
    @RequestMapping(value = "/{id}", method = DELETE)
    public void deletePaper(@PathVariable("id") Long id) throws FileNotFoundException {

        paperService.delete(id);
    }

    @ApiOperation("Upload file to the Paper")
    @RequestMapping(value = "/{id}/files", method = POST)
    public void uploadFile(
            Principal principal,
            @Valid UploadFileDTO uploadFileDTO,
            @RequestParam("type") PaperFileType type,
            @PathVariable("id") @NotNull Long id
    ) throws PermissionDeniedException, IOException, ValidationException {

        paperService.uploadPaperFile(principal, uploadFileDTO.getFile(), uploadFileDTO.getLabel(), uploadFileDTO.getLink(), type, id);
    }

    @ApiOperation("Get file of the Paper")
    @RequestMapping(value = "/{id}/files/{fileUuid}", method = GET)
    public FileDTO getFile(
            @PathVariable("id") Long id,
            @PathVariable("fileUuid") String uuid,
            @RequestParam("type") PaperFileType type,
            @RequestParam(defaultValue = "true") Boolean withContent) throws PermissionDeniedException, IOException {

        final AbstractPaperFile paperFile = paperService.getFile(id, uuid, type);
        FileDTO fileDto = conversionService.convert(paperFile, PaperFileDTO.class);

        if (withContent) {
            fileDto = FileDtoContentHandler
                    .getInstance(fileDto, fileService.getPathToFile(paperFile).toFile())
                    .withPdfConverter(toPdfConverter::convert)
                    .handle();
        }


        return fileDto;
    }

    @ApiOperation("Download file of the Paper")
    @RequestMapping(value = "/{id}/files/{fileUuid}/download", method = GET)
    public void downloadFile(
            @PathVariable("id") Long id,
            @PathVariable("fileUuid") String uuid,
            @RequestParam("type") PaperFileType type,
            HttpServletResponse response) throws PermissionDeniedException, IOException {

        final AbstractPaperFile paperFile = paperService.getFile(id, uuid, type);
        final InputStream inputStream = fileService.getFileInputStream(paperFile);

        response.setContentType(paperFile.getContentType());
        response.setHeader("Content-Disposition", "attachment; filename=" + paperFile.getRealName());
        response.setHeader("Content-type", paperFile.getContentType());
        IOUtils.copy(inputStream, response.getOutputStream());
        response.flushBuffer();
    }

    @ApiOperation(value = "Update one of the paper files", hidden = true)
    @RequestMapping(value = "/{id}/files/{fileUuid}", method = PUT)
    public void updateFile(@RequestParam MultipartFile file,
                           @RequestParam("type") PaperFileType type,
                           @PathVariable("id") Long id,
                           @PathVariable("fileUuid") String uuid,
                           Principal principal
    ) throws PermissionDeniedException, IOException {

        final IUser user = getUser(principal);
        paperService.updateFile(id, uuid, file, type, user);
    }

    @ApiOperation("Delete file of the Paper")
    @RequestMapping(value = "/{id}/files/{fileUuid}", method = DELETE)
    public void deleteFile(
            @PathVariable("id") Long id,
            @PathVariable("fileUuid") String uuid,
            @RequestParam("type") PaperFileType type
    ) throws PermissionDeniedException, NotExistException, FileNotFoundException {

        paperService.deleteFile(id, uuid, type);
    }
}
