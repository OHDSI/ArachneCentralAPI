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
 * Created: June 27, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.messaging;

import com.odysseusinc.arachne.portal.model.DataNode;

public class MessagingUtils {

    static final String ENTITIES_REQUESTS_DATANODE = "entities-requests-datanode-";
    static final String ENTITIES_DATANODE = "entities-datanode-";

    public static class EntitiesList {

        public static String getBaseQueue(DataNode dataNode) {

            return ENTITIES_DATANODE + dataNode.getId();
        }
    }

    public static class Entities {

        public static String getBaseQueue(DataNode dataNode) {

            return ENTITIES_REQUESTS_DATANODE + dataNode.getId();
        }
    }
}
