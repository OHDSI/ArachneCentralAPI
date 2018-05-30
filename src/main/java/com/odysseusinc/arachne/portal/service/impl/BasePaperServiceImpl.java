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
 * Created: September 17, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.impl;

import static java.util.Collections.singletonList;

import com.odysseusinc.arachne.commons.utils.CommonFileUtils;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.model.AbstractPaperFile;
import com.odysseusinc.arachne.portal.model.AbstractUserStudyListItem;
import com.odysseusinc.arachne.portal.model.AntivirusFile;
import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.Paper;
import com.odysseusinc.arachne.portal.model.PaperFavourite;
import com.odysseusinc.arachne.portal.model.PaperFileType;
import com.odysseusinc.arachne.portal.model.PaperPaperFile;
import com.odysseusinc.arachne.portal.model.PaperProtocolFile;
import com.odysseusinc.arachne.portal.model.PublishState;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.model.search.PaperSearch;
import com.odysseusinc.arachne.portal.model.search.PaperSpecification;
import com.odysseusinc.arachne.portal.model.search.StudySearch;
import com.odysseusinc.arachne.portal.model.solr.SolrCollection;
import com.odysseusinc.arachne.portal.model.statemachine.study.StudyState;
import com.odysseusinc.arachne.portal.model.statemachine.study.StudyStateActions;
import com.odysseusinc.arachne.portal.repository.PaperFavouritesRepository;
import com.odysseusinc.arachne.portal.repository.PaperPaperFileRepository;
import com.odysseusinc.arachne.portal.repository.PaperProtocolFileRepository;
import com.odysseusinc.arachne.portal.repository.PaperRepository;
import com.odysseusinc.arachne.portal.service.BasePaperService;
import com.odysseusinc.arachne.portal.service.BaseStudyService;
import com.odysseusinc.arachne.portal.service.BaseUserService;
import com.odysseusinc.arachne.portal.service.StudyFileService;
import com.odysseusinc.arachne.portal.service.impl.antivirus.events.AntivirusJob;
import com.odysseusinc.arachne.portal.service.impl.antivirus.events.AntivirusJobEvent;
import com.odysseusinc.arachne.portal.service.impl.antivirus.events.AntivirusJobFileType;
import com.odysseusinc.arachne.portal.service.impl.antivirus.events.AntivirusJobResponseEventBase;
import com.odysseusinc.arachne.portal.service.impl.antivirus.events.AntivirusJobPaperPaperFileResponseEvent;
import com.odysseusinc.arachne.portal.service.impl.antivirus.events.AntivirusJobPaperProtocolFileResponseEvent;
import com.odysseusinc.arachne.portal.service.impl.antivirus.events.AntivirusJobResponse;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public abstract class BasePaperServiceImpl<
        P extends Paper,
        PS extends PaperSearch,
        S extends Study,
        DS extends IDataSource,
        SS extends StudySearch,
        SU extends AbstractUserStudyListItem> implements BasePaperService<P, PS, S, DS, SS, SU> {
    private static final Logger log = LoggerFactory.getLogger(BasePaperServiceImpl.class);

    @Autowired
    private PaperRepository<P> paperRepository;
    @Autowired
    private PaperPaperFileRepository paperPaperFileRepository;
    @Autowired
    private PaperProtocolFileRepository paperProtocolFileRepository;
    @Autowired
    private PaperFavouritesRepository paperFavouritesRepository;
    @Autowired
    private BaseStudyService<S, DS, SS, SU> studyService;
    @Autowired
    private BaseUserService userService;
    @Autowired
    private StudyFileService fileService;
    @Autowired
    @Qualifier("restTemplate")
    private RestTemplate restTemplate;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private BaseSolrServiceImpl solrService;


    @Transactional(rollbackFor = Exception.class)
    @Override
    @PreAuthorize("hasPermission(#studyId, 'Study', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).EDIT_STUDY)")
    public P create(IUser owner, Long studyId) {

        final Study study = studyService.getById(studyId);
        final P paper = createPaper();
        paper.setStudy(study);
        paper.setPublishState(PublishState.DRAFT);

        beforePaperSave(paper);
        P save = paperRepository.save(paper);
        afterPaperSave(paper);
        return save;
    }

    protected void afterPaperSave(P newPaper) {
        
        solrService.indexBySolr(newPaper);
    }

    protected void beforePaperSave(P newPaper) {

    }

    public abstract P createPaper();

    @Override
    public Page<P> getPapersAccordingToCurrentUser(PS paperSearch, IUser user) {

        final PaperSpecification<P> paperSpecification = new PaperSpecification<>(paperSearch, user);
        return paperRepository.findAll(paperSpecification, new PageRequest(paperSearch.getPage(), paperSearch.getPageSize()));
    }


    @Override
    public P get(Long id) {

        return getPaperByIdOrThrow(id);
    }

    private P getPaperByIdOrThrow(Long id) {

        return Optional.ofNullable(paperRepository.findOne(id))
                .orElseThrow(() -> new NotExistException(Paper.class));
    }

    @Override
    public Optional<P> getByStudyId(Long studyId) {

        return paperRepository.findByStudyId(studyId);
    }

    @PreAuthorize("hasPermission(#paper.id, 'Paper', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).EDIT_PAPER)")
    @PostAuthorize("@ArachnePermissionEvaluator.processPermissions(principal, returnObject )")
    @Override
    public P update(P paper) {

        final P exists = getPaperByIdOrThrow(paper.getId());

        final PublishState publishState = paper.getPublishState();
        if (publishState != null && validatePublishStateTransition(publishState, exists)) {
            exists.setPublishState(publishState);
            exists.setPublishedDate(publishState == PublishState.PUBLISHED ? new Date() : null);
        }

        beforePaperUpdate(exists, paper);
        P save = paperRepository.save(exists);
        afterPaperUpdate(exists, paper);

        return save;
    }

    protected void afterPaperUpdate(P fromDb, P updated) {

        solrService.indexBySolr(fromDb);
    }

    protected void beforePaperUpdate(P exists, P updated) {

    }

    @PreAuthorize("hasPermission(#id, 'Paper', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).EDIT_PAPER)")
    @Transactional
    @Override
    public void delete(Long id) throws FileNotFoundException {

        final Paper paper = Optional.of(paperRepository.getOne(id))
                .orElseThrow(() -> new NotExistException(Paper.class));

        Stream.concat(paper.getPapers().stream(), paper.getProtocols().stream()).forEach(
                file -> {
                    try {
                        deleteFile(id, file.getUuid(), file.getType());
                    } catch (FileNotFoundException ex) {
                        log.error("Paper file with uuid={} is not found", file.getUuid());
                    }
                }
        );
        if (paperRepository.deleteById(id) == 0) {
            throw new NotExistException(Paper.class);
        }
    }

    @PreAuthorize("hasPermission(#paperId, 'Paper', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).LIMITED_EDIT_PAPER)")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String saveFile(Long paperId, MultipartFile file, PaperFileType fileType, String label, IUser user) throws IOException {

        if (file == null) {
            throw new IllegalArgumentException("File must not be null");
        }
        final Paper paper = get(paperId);

        final String realName = file.getOriginalFilename();
        final String contentType = CommonFileUtils.getContentType(realName, file);
        AbstractPaperFile paperFile = savePaperFile(fileType, label, user, paper, contentType, realName, null);
        fileService.saveFile(file, paperFile);

        AntivirusJobFileType antivirusJobFileType;
        switch (fileType) {
            case PAPER:
                antivirusJobFileType = AntivirusJobFileType.PAPER_PAPER_FILE;
                break;
            case PROTOCOL:
                antivirusJobFileType = AntivirusJobFileType.PAPER_PROTOCOL_FILE;
                break;
            default:
                throw new IllegalArgumentException();
        }
        eventPublisher.publishEvent(new AntivirusJobEvent(this, new AntivirusJob(paperFile.getId(), paperFile.getRealName(), fileService.getFileInputStream(paperFile), antivirusJobFileType)));
        return paperFile.getUuid();
    }

    @PreAuthorize("hasPermission(#paperId, 'Paper', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).LIMITED_EDIT_PAPER)")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String saveFile(Long paperId, String link, PaperFileType type, String label, IUser user) throws MalformedURLException {

        if (StringUtils.isEmpty(link)) {
            throw new IllegalArgumentException();
        }
        final Paper paper = get(paperId);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.APPLICATION_OCTET_STREAM));
        HttpEntity<String> entity = new HttpEntity<>(headers);
        URL url = new URL(link);
        String fileName = FilenameUtils.getName(url.getPath());
        ResponseEntity<byte[]> response = restTemplate.exchange(link, HttpMethod.GET, entity, byte[].class);
        if (response.getStatusCode() == HttpStatus.OK) {
            String contentType = response.getHeaders().getContentType().toString();
            final AbstractPaperFile abstractPaperFile = savePaperFile(type, label, user, paper, CommonFileUtils.TYPE_LINK, fileName, link);
            return abstractPaperFile.getUuid();
        } else {
            throw new IllegalArgumentException();
        }
    }

    @PreAuthorize("hasPermission(#paperId, 'Paper', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).ACCESS_PAPER)")
    @Override
    public AbstractPaperFile getFile(Long paperId, String fileUuid, PaperFileType fileType) throws
            FileNotFoundException {

        return getAbstractPaperFile(paperId, fileUuid, fileType);
    }

    @PreAuthorize("hasPermission(#paperId, 'Paper', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).EDIT_PAPER)")
    @Override
    public void updateFile(Long paperId, String uuid, MultipartFile multipartFile,
                           PaperFileType fileType, IUser user) throws IOException {

        final AbstractPaperFile paperFile = getAbstractPaperFile(paperId, uuid, fileType);
        fileService.updateFile(multipartFile, paperFile);

    }

    @PreAuthorize("hasPermission(#paperId, 'Paper', "
            + "T(com.odysseusinc.arachne.portal.security.ArachnePermission).LIMITED_EDIT_PAPER)")
    //@Transactional not usable for this method.
    @Override
    public void deleteFile(Long paperId, String fileUuid, PaperFileType fileType) throws FileNotFoundException {

        AbstractPaperFile paperFile;
        switch (fileType) {
            case PAPER: {
                paperFile = paperPaperFileRepository.findByPaperIdAndUuid(paperId, fileUuid)
                        .orElseThrow(() -> new NotExistException(AbstractPaperFile.class));
                paperPaperFileRepository.delete(paperFile.getId());
                break;
            }
            case PROTOCOL: {
                paperFile = paperProtocolFileRepository.findByPaperIdAndUuid(paperId, fileUuid)
                        .orElseThrow(() -> new NotExistException(AbstractPaperFile.class));
                paperProtocolFileRepository.delete(paperFile.getId());
                break;
            }
            default: {
                throw new IllegalArgumentException("Illegal filetype: " + fileType);
            }
        }
        fileService.delete(paperFile);
    }

    @Transactional
    @Override
    public void setFavourite(Long userId, Long id, boolean isFavourite) {

        final Paper paper = get(id);
        if (isFavourite) {
            paperFavouritesRepository.save(new PaperFavourite(userId, id));
        } else {
            paperFavouritesRepository.deleteByUserIdAndPaperId(userId, id);
        }
    }

    private AbstractPaperFile getAbstractPaperFile(Long paperId, String uuid, PaperFileType fileType) {

        Optional<? extends AbstractPaperFile> optionalPaperFile;
        switch (fileType) {
            case PAPER: {
                optionalPaperFile = paperPaperFileRepository.findByPaperIdAndUuid(paperId, uuid);
                break;
            }
            case PROTOCOL: {
                optionalPaperFile = paperProtocolFileRepository.findByPaperIdAndUuid(paperId, uuid);
                break;
            }
            default: {
                throw new IllegalArgumentException("Illegal filetype: " + fileType);
            }
        }
        return optionalPaperFile
                .orElseThrow(() -> new NotExistException(AbstractPaperFile.class));
    }

    private AbstractPaperFile savePaperFile(PaperFileType fileType,
                                            String label,
                                            IUser user,
                                            Paper paper,
                                            String contentType,
                                            String realName,
                                            String link
    ) {

        AbstractPaperFile paperFile;

        switch (fileType) {
            case PAPER: {
                paperFile = new PaperPaperFile();
                enrichPaperFile(user, label, paper, paperFile, contentType, realName, link);
                paperFile = paperPaperFileRepository.save((PaperPaperFile) paperFile);
                break;
            }
            case PROTOCOL: {
                paperFile = new PaperProtocolFile();
                enrichPaperFile(user, label, paper, paperFile, contentType, realName, link);
                paperFile = paperProtocolFileRepository.save((PaperProtocolFile) paperFile);
                break;
            }
            default: {
                throw new IllegalArgumentException("Illegal filetype: " + fileType);
            }
        }
        return paperFile;
    }

    private void enrichPaperFile(IUser user, String label, Paper paper, AbstractPaperFile paperFile,
                                 String contentType, String realName, String link) {

        paperFile.setPaper(paper);
        paperFile.setAuthor(user);
        paperFile.setContentType(contentType);
        paperFile.setLabel(label);
        paperFile.setLink(link);
        final Date created = new Date();
        paperFile.setCreated(created);
        paperFile.setUpdated(created);
        paperFile.setRealName(realName);
        paperFile.setUuid(UUID.randomUUID().toString());
    }

    @Override
    public void uploadPaperFile(
            Principal principal,
            MultipartFile multipartFile,
            String label,
            String link,
            PaperFileType type,
            Long id
    ) throws PermissionDeniedException, IOException, ValidationException {

        final IUser user = userService.getUser(principal);
        if (multipartFile != null) {
            this.saveFile(id, multipartFile, type, label, user);
        } else if (!org.apache.commons.lang3.StringUtils.isEmpty(link)) {
            this.saveFile(id, link, type, label, user);
        } else {
            throw new ValidationException("File or Link must not be null");
        }
    }

    @Override
    public List<P> findByStudyIds(List<Long> studyIds) {

        return paperRepository.findByStudyIdIn(studyIds);
    }

    @Override
    public void fullDelete(List<P> papers) {

        for (P paper : papers) {

            List<PaperPaperFile> paperPaperFiles = paper.getPapers();
            paperPaperFileRepository.delete(paperPaperFiles);
            fileService.delete(paperPaperFiles);

            List<PaperProtocolFile> protocolFiles = paper.getProtocols();
            paperProtocolFileRepository.delete(protocolFiles);
            fileService.delete(protocolFiles);
        }

        paperRepository.delete(papers);
    }
    
    @Override
    public void indexAllBySolr() throws IOException, NotExistException, SolrServerException, NoSuchFieldException, IllegalAccessException {
        
        solrService.deleteAll(SolrCollection.PAPERS);
        final Map<Long, Study> map = studyService.findWithPapersInAnyTenant()
                .stream()
                .collect(Collectors.toMap(Study::getId, Function.identity()));
        final List<P> papers = paperRepository.findAll();
        for (final P paper : papers) {
            paper.setStudy(map.get(paper.getStudy().getId()));
        }
        solrService.indexBySolr(papers);
    }
    
    @Override
    public void indexBySolr(final P paper) {
        
        solrService.indexBySolr(paper);
    }

    @EventListener
    @Transactional
    @Override
    public void processAntivirusResponse(AntivirusJobPaperPaperFileResponseEvent event) {

        update(event, paperPaperFileRepository);
    }

    @EventListener
    @Transactional
    @Override
    public void processAntivirusResponse(AntivirusJobPaperProtocolFileResponseEvent event) {

        update(event, paperProtocolFileRepository);
    }

    private void update(AntivirusJobResponseEventBase event, JpaRepository repository) {

        final AntivirusJobResponse antivirusJobResponse = event.getAntivirusJobResponse();
        final AntivirusFile file = (AntivirusFile) repository.findOne(antivirusJobResponse.getFileId());
        if (file != null) {
            file.setAntivirusStatus(antivirusJobResponse.getStatus());
            file.setAntivirusDescription(antivirusJobResponse.getDescription());
            repository.save(file);
        }
    }

    private boolean validatePublishStateTransition(PublishState state, P exists) {

        return !state.equals(PublishState.PUBLISHED)
                || Arrays.asList(StudyState.valueOf(exists.getStudy().getState().getName().toUpperCase())
                .getActions()).contains(StudyStateActions.PUBLISH_PAPER);
    }

}
