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
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva
 * Created: February 15, 2018
 *
 */

package com.odysseusinc.arachne.portal.service.impl;

import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.model.RawUser;
import com.odysseusinc.arachne.portal.repository.RawUserRepository;
import com.odysseusinc.arachne.portal.service.RawUserService;
import com.odysseusinc.arachne.portal.service.SolrService;
import java.io.IOException;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RawUserServiceImpl implements RawUserService {

    @Value("${portal.authMethod}")
    protected String userOrigin;

    private RawUserRepository rawUserRepository;
    private SolrService solrService;

    @Autowired
    public RawUserServiceImpl(
            RawUserRepository rawUserRepository,
            SolrService solrService
    ) {

        this.rawUserRepository = rawUserRepository;
        this.solrService = solrService;
    }

    public RawUser getById(Long id) {

        return rawUserRepository.findById(id)
                .orElseThrow(() -> new NotExistException(RawUser.class));
    }

    public RawUser findLoginCandidate(String userOrigin, String username) {

        return rawUserRepository.findLoginCandidate(userOrigin, username)
                .orElse(null);
    }

    public RawUser findLoginCandidate(String username) {

        return findLoginCandidate(userOrigin, username);
    }

    public RawUser update(RawUser user) throws IOException, NoSuchFieldException, SolrServerException, IllegalAccessException {

        RawUser updated = rawUserRepository.saveAndFlush(user);
        indexBySolr(updated);
        return updated;
    }

    public void indexBySolr(RawUser user)
            throws IllegalAccessException, IOException, SolrServerException, NotExistException, NoSuchFieldException {

        if (user.getEnabled()) {
            solrService.putDocument(
                    SolrServiceImpl.USER_COLLECTION,
                    user.getId(),
                    solrService.getValuesByEntity(user)
            );
        }
    }
}
