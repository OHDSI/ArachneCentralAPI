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
 * Created: November 15, 2016
 *
 */

package com.odysseusinc.arachne.portal.service.impl;

import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.NotUniqueException;
import com.odysseusinc.arachne.portal.model.StudyStatus;
import com.odysseusinc.arachne.portal.repository.StudyStatusRepository;
import com.odysseusinc.arachne.portal.service.StudyStatusService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@SuppressWarnings("unused")
@Transactional(rollbackFor = Exception.class)
public class StudyStatusServiceImpl extends CRUDLServiceImpl<StudyStatus> implements StudyStatusService {

    private StudyStatusRepository studyStatusRepository;

    @Override
    public Class<StudyStatus> getType() {
        return StudyStatus.class;
    }

    public StudyStatusServiceImpl(StudyStatusRepository studyStatusRepository) {

        this.studyStatusRepository = studyStatusRepository;
    }

    @Override
    public StudyStatus create(StudyStatus studyStatus) throws NotUniqueException {

        StudyStatus existStudyStatus = studyStatusRepository.findByName(studyStatus.getName());
        if (existStudyStatus != null) {
            throw new NotUniqueException("name", "Not unique");
        }
        return studyStatusRepository.save(studyStatus);
    }

    @Override
    public StudyStatus update(StudyStatus studyStatus) throws NotUniqueException, NotExistException {

        if (!studyStatusRepository.exists(studyStatus.getId())) {
            throw new NotExistException("update: studyStatus with id=" + studyStatus.getId() + " not exist", StudyStatus.class);
        }
        StudyStatus existStudyStatus = studyStatusRepository.findByName(studyStatus.getName());
        if (existStudyStatus != null) {
            throw new NotUniqueException("name", "Not unique");
        }
        return studyStatusRepository.save(studyStatus);
    }

    @Override
    public CrudRepository<StudyStatus, Long> getRepository() {

        return studyStatusRepository;
    }


    @Override
    public StudyStatus findByName(String name) {

        return studyStatusRepository.findByName(name);
    }
}
