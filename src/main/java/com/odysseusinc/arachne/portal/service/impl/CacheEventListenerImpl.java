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
 * Created: April 22, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.impl;

import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheEventListenerImpl implements CacheEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(CacheEventListenerImpl.class);
    private static final String EVENT_LOG = "Event: %s Key: %s old value: %s new value: %s";

    @Override
    public void onEvent(CacheEvent event) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format(EVENT_LOG,
                    event.getType(), event.getKey(), event.getOldValue(), event.getNewValue()));
        }
    }
}
