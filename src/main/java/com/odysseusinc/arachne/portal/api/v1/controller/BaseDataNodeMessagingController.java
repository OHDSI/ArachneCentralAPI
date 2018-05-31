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
 * Created: September 18, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.controller;

import static com.odysseusinc.arachne.commons.service.messaging.MessagingUtils.getRequestQueueName;
import static com.odysseusinc.arachne.commons.service.messaging.MessagingUtils.getResponseQueueName;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import com.odysseusinc.arachne.commons.api.v1.dto.AtlasShortDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonEntityDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonEntityRequestDTO;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonListEntityRequest;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonListEntityResponseDTO;
import com.odysseusinc.arachne.commons.service.messaging.ConsumerTemplate;
import com.odysseusinc.arachne.portal.exception.NotExistException;
import com.odysseusinc.arachne.portal.exception.PermissionDeniedException;
import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.model.IAtlas;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.service.BaseAtlasService;
import com.odysseusinc.arachne.portal.service.BaseDataNodeService;
import com.odysseusinc.arachne.portal.service.messaging.BaseDataNodeMessageService;
import com.odysseusinc.arachne.portal.service.messaging.MessagingUtils;
import com.odysseusinc.arachne.portal.util.ImportedFile;
import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

public abstract class BaseDataNodeMessagingController<DN extends DataNode, A extends IAtlas> extends BaseController<DN, IUser> {

    private static final Logger log = LoggerFactory.getLogger(BaseDataNodeController.class);

    private final JmsTemplate jmsTemplate;
    private final DestinationResolver destinationResolver;
    private final BaseDataNodeService<DN> baseDataNodeService;
    private final BaseDataNodeMessageService<DN> dataNodeMessageService;
    private final BaseAtlasService<A> atlasService;

    @Autowired
    public BaseDataNodeMessagingController(
            JmsTemplate jmsTemplate,
            BaseDataNodeService<DN> dataNodeService,
            BaseDataNodeMessageService<DN> dataNodeMessageService,
            BaseAtlasService<A> atlasService) {

        this.jmsTemplate = jmsTemplate;
        this.destinationResolver = jmsTemplate.getDestinationResolver();
        this.baseDataNodeService = dataNodeService;
        this.dataNodeMessageService = dataNodeMessageService;
        this.atlasService = atlasService;
    }

    /**
     * Returns pending requests for CommonEntity list
     * (for polling by Node's back)
     */
    @RequestMapping(
            value = "/api/v1/data-nodes/entity-lists/requests",
            method = GET
    )
    public CommonListEntityRequest getListRequests(
            Principal principal
    ) throws Exception {

        DN dataNode = getDatanode(principal);
        return dataNodeMessageService.getListRequest(dataNode);
    }

    /**
     * Posts responses for for CommonEntity list requests
     * (for pushing by Node's back)
     */
    @RequestMapping(
            value = "/api/v1/data-nodes/entity-lists/responses",
            method = POST
    )
    public void saveListResponse(
            @RequestBody @Valid CommonListEntityResponseDTO commonListEntityResponseDTO,
            Principal principal
    ) throws PermissionDeniedException {

        DataNode dataNode = getDatanode(principal);
        String responseQueue = getResponseQueueName(MessagingUtils.EntitiesList.getBaseQueue(dataNode));
        List<CommonEntityDTO> response = (List<CommonEntityDTO>) commonListEntityResponseDTO.getEntities();

        for (String correlationId : commonListEntityResponseDTO.getRequestIds()) {
            jmsTemplate.send(
                    responseQueue,
                    session -> {
                        ObjectMessage message = session.createObjectMessage((Serializable) response);
                        message.setJMSCorrelationID(correlationId);
                        return message;
                    }
            );
        }
    }

    @RequestMapping(value = "/api/v1/data-nodes/atlases", method = POST)
    public AtlasShortDTO createOrUpdateAtlas(
            @RequestBody @Valid AtlasShortDTO atlasShortDTO,
            Principal principal
    ) throws PermissionDeniedException, NotExistException {

        A atlas = convert(atlasShortDTO);
        A updated;

        if (atlas.getId() == null || atlasService.findByIdInAnyTenant(atlas.getId()) == null) {
            final DN dataNode = getDatanode(principal);

            A existing;
            if ((existing = atlasService.findByNameAndNodeInAnyTenant(atlas.getName(), dataNode.getId())) != null) {
                // If for some reason IDs of Atlas at Central and Node sides become unsynced,
                // search whether there is Atlas with given name owned by the Node (and its ID at Node side will be updated)
                updated = atlasService.update(existing.getId(), atlas);
            } else {
                atlas.setId(null);
                atlas.setDataNode(dataNode);
                updated = atlasService.register(atlas);
            }
        } else {
            updated = atlasService.update(atlas.getId(), atlas);
        }

        return conversionService.convert(updated, AtlasShortDTO.class);
    }

    @RequestMapping(value = "/api/v1/data-nodes/atlases/{id}", method = DELETE)
    public void deleteAtlas(
            @PathVariable("id") Long id
    ) {

        atlasService.delete(id);
    }

    @RequestMapping(
            value = "/api/v1/data-nodes/entities",
            method = GET
    )
    public List<CommonEntityRequestDTO> getEntityRequests(
            Principal principal
    ) throws PermissionDeniedException, JMSException {

        DataNode dataNode = getDatanode(principal);
        final String requestQueue = getRequestQueueName(MessagingUtils.Entities.getBaseQueue(dataNode));
        ConsumerTemplate consumerTpl = new ConsumerTemplate(
                destinationResolver,
                requestQueue,
                // Give some time for case when new connection to a broker is established
                1000L
        );

        List<CommonEntityRequestDTO> cohortRequests = new ArrayList<>();
        while (true) {
            ObjectMessage requestMessage = jmsTemplate.execute(consumerTpl, true);
            if (requestMessage == null) {
                break;
            }
            final CommonEntityRequestDTO cohortRequest = (CommonEntityRequestDTO) requestMessage.getObject();
            cohortRequest.setId(requestMessage.getJMSCorrelationID());
            cohortRequests.add(cohortRequest);
        }
        return cohortRequests;
    }

    @RequestMapping(
            value = "/api/v1/data-nodes/common-entity/{id}",
            method = POST
    )
    public void saveEntity(
            Principal principal,
            @PathVariable("id") String id,
            @RequestParam(required = false) MultipartFile[] files
    ) throws PermissionDeniedException, IOException {

        LinkedList<ImportedFile> importedFiles = new LinkedList<>();
        for (MultipartFile mpf : files) {
            importedFiles.add(new ImportedFile(mpf.getOriginalFilename(), mpf.getBytes()));
        }

        saveCommonEntity(
                principal,
                id,
                importedFiles
        );
    }

    protected abstract A convert(AtlasShortDTO atlasDTO);

    private void saveCommonEntity(
            Principal principal,
            String id,
            Serializable object
    ) throws PermissionDeniedException {

        DataNode dataNode = getDatanode(principal);
        String queueBase = MessagingUtils.Entities.getBaseQueue(dataNode);
        String responseQueue = getResponseQueueName(queueBase);
        jmsTemplate.send(
                responseQueue,
                session -> {
                    ObjectMessage message = session.createObjectMessage(object);
                    message.setJMSCorrelationID(id);
                    return message;
                }
        );
    }
}
