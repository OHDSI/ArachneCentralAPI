/*
 *
 * Copyright 2018 Odysseus Data Services, inc.
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
 * Created: May 17, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.impl;

import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.model.AnalysisUnlockRequest;
import com.odysseusinc.arachne.portal.model.AnalysisUnlockRequestStatus;
import com.odysseusinc.arachne.portal.repository.AnalysisUnlockRequestRepository;
import com.odysseusinc.arachne.portal.service.AnalysisUnlockRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnalysisUnlockRequestServiceImpl implements AnalysisUnlockRequestService {

    private final AnalysisUnlockRequestRepository analysisUnlockRequestRepository;
    private static final String PENDING_REQUEST_NOT_EXIST_EXCEPTION
            = "Pending request with id='%s' & token='%s' does not exist";

    @Autowired
    public AnalysisUnlockRequestServiceImpl(AnalysisUnlockRequestRepository analysisUnlockRequestRepository) {

        this.analysisUnlockRequestRepository = analysisUnlockRequestRepository;
    }

    @Override
    public AnalysisUnlockRequest getByIdAndTokenAndStatusPending(Long id, String token) throws NotExistException {

        return analysisUnlockRequestRepository.findByIdAndTokenAndStatus(id, token, AnalysisUnlockRequestStatus.PENDING)
                .orElseThrow(() -> {

                    final String message = String.format(PENDING_REQUEST_NOT_EXIST_EXCEPTION, id, token);
                    return new NotExistException(message, AnalysisUnlockRequest.class);
                });
    }
}
