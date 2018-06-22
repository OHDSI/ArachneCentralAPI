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

package com.odysseusinc.arachne.portal.service;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.odysseusinc.arachne.portal.exception.AlreadyExistException;
import com.odysseusinc.arachne.portal.exception.FieldException;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.NotUniqueException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.model.AbstractUserStudyListItem;
import com.odysseusinc.arachne.portal.model.IDataSource;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.ParticipantRole;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.StudyDataSourceLink;
import com.odysseusinc.arachne.portal.model.StudyFile;
import com.odysseusinc.arachne.portal.model.SuggestSearchRegion;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.model.UserStudy;
import com.odysseusinc.arachne.portal.model.search.StudySearch;
import com.odysseusinc.arachne.portal.component.SelfReferencingBean;
import com.odysseusinc.arachne.portal.service.impl.antivirus.events.AntivirusJobStudyFileResponseEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface BaseStudyService<
        T extends Study,
        DS extends IDataSource,
        SS extends StudySearch,
        SU extends AbstractUserStudyListItem> extends SelfReferencingBean {

    T create(IUser owner, T study) throws NotUniqueException, NotExistException;

    T createWorkspace(Long ownerId, T workspace);

    T createWorkspace(Long ownerId);

    void delete(Long id) throws NotExistException;

    T getById(Long id) throws NotExistException;

    T update(T study)
            throws NotExistException, NotUniqueException, ValidationException;

    void setFavourite(Long userId, Long studyId, Boolean isFavourite) throws NotExistException;

    Page<AbstractUserStudyListItem> findStudies(SS studySearch);

    SU getStudy(final IUser user, final Long studyId);

    List<IUser> findLeads(T study);

    UserStudy addParticipant(IUser createdBy, Long studyId, Long participantId,
                             ParticipantRole role) throws NotExistException, AlreadyExistException;

    UserStudy updateParticipantRole(Long studyId, Long userId, ParticipantRole role)
            throws NotExistException, AlreadyExistException, ValidationException;

    void removeParticipant(Long id, Long participantId)
            throws NotExistException, PermissionDeniedException, ValidationException;

    String saveFile(MultipartFile multipartFile, Long studyId, String label, IUser user)
            throws IOException;

    String saveFile(String link, Long studyId, String label, IUser user) throws IOException;

    StudyFile getStudyFile(Long studyId, String fileName);

    StudyFile getStudyFileUnsecured(Long studyId, String fileName);

    Boolean getDeleteStudyFile(Long studyId, String uuid) throws FileNotFoundException;

    List<StudyDataSourceLink> listApprovedDataSources(Long studyId);

    StudyDataSourceLink addDataSource(IUser createdBy, Long id, Long dataSourceId)
            throws NotExistException, AlreadyExistException;

    DS addVirtualDataSource(IUser createdBy, Long studyId, String dataSourceName, List<String> dataOwnerIdList)
            throws NotExistException, AlreadyExistException, NoSuchFieldException,
            IOException, ValidationException, FieldException, IllegalAccessException,
            SolrServerException;

    DS getStudyDataSource(IUser user, Long studyId, Long dataSourceId);

    DS updateVirtualDataSource(IUser user, Long studyId, Long dataSourceId, String name, List<String> dataOwnerIds)
            throws IllegalAccessException, IOException, NoSuchFieldException, SolrServerException, ValidationException;

    void removeDataSource(Long id, Long dataSourceId) throws NotExistException;

    void removeDataSourceUnsecured(Long studyId, Long dataSourceId);

    void processDataSourceInvitation(IUser user, Long id, Boolean accepted,
                                     String comment);

    Iterable<T> suggestStudy(String query, IUser owner, Long id, SuggestSearchRegion region);

    void getAllStudyFilesExceptLinks(Long studyId, String archiveName, OutputStream os) throws IOException;

    StudyDataSourceLink getByIdAndStatusPendingAndToken(Long studyDataSourceId, String token) throws NotExistException;

    List<User> getApprovedUsers(DS dataSource);

    List<Long> getStudyIdsOfDataSource(Long dataSourceId);

    boolean fullDelete(List<T> studies);

    List<T> getByIds(List<Long> studyIds);

    List<StudyDataSourceLink> getLinksByStudyId(Long id, EntityGraph dataSource);

    List<StudyFile> getFilesByStudyId(Long id, EntityGraph author);

    void processAntivirusResponse(AntivirusJobStudyFileResponseEvent event);

    void indexAllBySolr()
            throws IOException,
            NotExistException,
            SolrServerException,
            NoSuchFieldException,
            IllegalAccessException;

    List<T> findWithPapersInAnyTenant();

    List<T> findWithAnalysesInAnyTenant();

    List<T> findByIdsInAnyTenant(Set<Long> studyIds);
    
    T findByIdInAnyTenant(Long studyId);

    T findWorkspaceForUser(IUser user, Long userId) throws NotExistException;

    T findOrCreateWorkspaceForUser(IUser user, Long userId);
}
