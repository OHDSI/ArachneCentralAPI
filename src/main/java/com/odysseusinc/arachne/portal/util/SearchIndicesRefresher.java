/*
 *
 * Copyright 2020 Odysseus Data Services, inc.
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
 * Authors: Alexandr Cumarav, Vitaly Koulakov, Yaroslav Molodkov
 * Created: November 04, 2020
 */

package com.odysseusinc.arachne.portal.util;

import com.odysseusinc.arachne.portal.exception.ArachneSystemRuntimeException;
import com.odysseusinc.arachne.portal.service.Indexable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;


@Component
public class SearchIndicesRefresher {

    private static final Logger log = LoggerFactory.getLogger(SearchIndicesRefresher.class);

    private final List<Indexable> searchableServices;

    @Autowired
    public SearchIndicesRefresher(List<Indexable> searchableServices) {

        this.searchableServices = searchableServices;
    }

    @EventListener(classes = ContextRefreshedEvent.class)
    @Transactional
    public void startIndicesRebuild() {

        log.info("Start indices rebuilding...");
        CompletableFuture.runAsync(this::rebuildAllIndices)
                .thenRun(() -> log.info("Search indices rebuild complete"))
                .exceptionally(err -> {
                    log.warn("Cannot rebuild search indices due to the error:", err);
                    return null;
                });

    }

    private void rebuildAllIndices() {

        try {
            for (Indexable service : searchableServices) {
                service.indexAllBySolr();
            }
        } catch (Exception ex) {
            throw new ArachneSystemRuntimeException(ex);
        }

    }
}