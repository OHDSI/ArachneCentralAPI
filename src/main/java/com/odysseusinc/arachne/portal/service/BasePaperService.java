/*
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
 * Created: September 17, 2017
 *
 */

package com.odysseusinc.arachne.portal.service;

import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.exception.ValidationException;
import com.odysseusinc.arachne.portal.model.AbstractPaperFile;
import com.odysseusinc.arachne.portal.model.Invitationable;
import com.odysseusinc.arachne.portal.model.Paper;
import com.odysseusinc.arachne.portal.model.PaperFileType;
import com.odysseusinc.arachne.portal.model.User;
import com.odysseusinc.arachne.portal.model.search.PaperSearch;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

public interface BasePaperService<T extends Paper, PS extends PaperSearch> {
    Page<T> getPapersAccordingToCurrentUser(PS paperSearch, User user);

    T get(Long id);

    Optional<T> getByStudyId(Long studyId);

    T create(User owner, Long studyId);

    T update(T studyInsight);

    void delete(Long id) throws FileNotFoundException;

    String saveFile(Long paperId, MultipartFile file, PaperFileType fileType, String label, User user) throws IOException;

    String saveFile(Long paperId, String link, PaperFileType type, String label, User user) throws MalformedURLException;

    AbstractPaperFile getFile(Long paperId, String fileUuid, PaperFileType fileType) throws FileNotFoundException;

    void updateFile(Long paperId, String uuid, MultipartFile multipartFile, PaperFileType fileType, User user) throws IOException;

    void deleteFile(Long paperId, String fileUuid, PaperFileType fileType) throws FileNotFoundException;

    void setFavourite(Long userId, Long id, boolean isFavourite);

    void uploadPaperFile(
            Principal principal,
            MultipartFile multipartFile,
            String label,
            String link,
            PaperFileType type,
            Long id
    ) throws PermissionDeniedException,IOException,ValidationException;

    List<T> findByStudyIds(List<Long> ids);

    void fullDelete(List<T> ids);
}
