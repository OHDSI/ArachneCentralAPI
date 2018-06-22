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
 * Created: July 03, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.impl;

import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.model.DataReference;
import com.odysseusinc.arachne.portal.repository.DataReferenceRepository;
import com.odysseusinc.arachne.portal.service.DataReferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DataReferenceServiceImpl implements DataReferenceService {
    private static final String NOT_EXIST_EXCEPTION
            = "DataReference with guid='%s' and DataNode sid='%' does not exist";

    private final DataReferenceRepository dataReferenceRepository;

    @Autowired
    public DataReferenceServiceImpl(DataReferenceRepository dataReferenceRepository) {

        this.dataReferenceRepository = dataReferenceRepository;
    }

    @Override
    public DataReference get(String guid, DataNode dataNode) {

        return dataReferenceRepository.getFirstByGuidAndDataNode(guid, dataNode)
                .orElseThrow(() -> {
                    final String message = String.format(NOT_EXIST_EXCEPTION, guid, dataNode.getId());
                    return new NotExistException(message, DataReference.class);
                });
    }

    @Override
    public DataReference addOrUpdate(String guid, DataNode dataNode) {

        final Optional<DataReference> existsOptional = dataReferenceRepository.getFirstByGuidAndDataNode(guid, dataNode);
        if (existsOptional.isPresent()) {
            return existsOptional.get();
        }
        return dataReferenceRepository.save(new DataReference(guid, dataNode));
    }

    @Override
    public DataReference add(DataReference dataReference) {

        return dataReferenceRepository.save(dataReference);
    }
}
