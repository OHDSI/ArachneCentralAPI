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
 * Created: July 18, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.controller;

import com.odysseusinc.arachne.commons.api.v1.dto.AtlasShortDTO;
import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.model.IAtlas;
import com.odysseusinc.arachne.portal.service.AtlasService;
import com.odysseusinc.arachne.portal.service.DataNodeService;
import com.odysseusinc.arachne.portal.service.messaging.DataNodeMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataNodeMessagingController extends BaseDataNodeMessagingController<DataNode, IAtlas> {

    @Autowired
    public DataNodeMessagingController(JmsTemplate jmsTemplate,
                                       DataNodeService dataNodeService,
                                       DataNodeMessageService dataNodeMessageService,
                                       AtlasService atlasService) {

        super(jmsTemplate, dataNodeService, dataNodeMessageService, atlasService);
    }

    @Override
    protected IAtlas convert(AtlasShortDTO atlasDTO) {

        return conversionService.convert(atlasDTO, IAtlas.class);
    }
}
