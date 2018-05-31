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
 * Created: November 16, 2016
 *
 */

package com.odysseusinc.arachne.portal.service.impl;

import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.NotUniqueException;
import com.odysseusinc.arachne.portal.model.StudyType;
import com.odysseusinc.arachne.portal.repository.StudyTypeRepository;
import com.odysseusinc.arachne.portal.service.StudyTypeService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@SuppressWarnings("unused")
@Transactional(rollbackFor = Exception.class)
public class StudyTypeServiceImpl extends CRUDLServiceImpl<StudyType> implements StudyTypeService {

    private StudyTypeRepository studyTypeRepository;

    public StudyTypeServiceImpl(StudyTypeRepository studyTypeRepository) {

        this.studyTypeRepository = studyTypeRepository;
    }

    @Override
    public Class<StudyType> getType() {
        return StudyType.class;
    }

    @Override
    public StudyType create(StudyType studyType) throws NotUniqueException {

        StudyType existSStudyType = studyTypeRepository.findByName(studyType.getName());
        if (existSStudyType != null) {
            throw new NotUniqueException("name", "Not unique");
        }
        return studyTypeRepository.save(studyType);
    }

    @Override
    public StudyType update(StudyType studyType) throws NotUniqueException, NotExistException {

        if (!studyTypeRepository.exists(studyType.getId())) {
            throw new NotExistException("update: studyType with id=" + studyType.getId() + " not exist",
                    StudyType.class);
        }
        StudyType existStudyType = studyTypeRepository.findByName(studyType.getName());
        if (existStudyType != null) {
            throw new NotUniqueException("name", "Not unique");
        }
        return studyTypeRepository.save(studyType);
    }

    @Override
    public CrudRepository<StudyType, Long> getRepository() {

        return studyTypeRepository;
    }


    @Override
    public StudyType findByName(String name) {

        return studyTypeRepository.findByName(name);
    }
}
