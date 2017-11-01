/*
 *
 * Copyright 2017 Observational Health Data Sciences and Informatics
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

package com.odysseusinc.arachne.portal.service.messaging;

import static com.odysseusinc.arachne.commons.service.messaging.MessagingUtils.getRequestQueueName;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonListEntityRequest;
import com.odysseusinc.arachne.commons.service.messaging.ConsumerTemplate;
import com.odysseusinc.arachne.commons.service.messaging.ProducerConsumerTemplate;
import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.model.messaging.RequestObject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DestinationResolver;

public abstract class BaseDataNodeMessageServiceImpl<DN extends DataNode> implements BaseDataNodeMessageService<DN> {

    private final JmsTemplate jmsTemplate;
    private final DestinationResolver destinationResolver;

    @Value("${datanode.messaging.importTimeout}")
    private Long messagingTimeout;

    public BaseDataNodeMessageServiceImpl(JmsTemplate jmsTemplate) {

        this.jmsTemplate = jmsTemplate;
        this.destinationResolver = jmsTemplate.getDestinationResolver();
    }

    @Override
    public <T> List<T> getDataList(DN dataNode, CommonAnalysisType analysisType) throws JMSException {

        Long waitForResponse = messagingTimeout;
        Long messageLifeTime = messagingTimeout;

        String baseQueue = MessagingUtils.EntitiesList.getBaseQueue(dataNode);

        ProducerConsumerTemplate exchangeTpl = new ProducerConsumerTemplate(
                destinationResolver,
                new RequestObject(analysisType),
                baseQueue,
                waitForResponse,
                messageLifeTime
        );

        ObjectMessage responseMessage = jmsTemplate.execute(
                exchangeTpl,
                true
        );

        return (List<T>) responseMessage.getObject();
    }

    @Override
    public CommonListEntityRequest getListRequest(DN dataNode) throws JMSException {

        Map<String, CommonAnalysisType> requestMap = new LinkedHashMap<>();

        String requestQueue = getRequestQueueName(MessagingUtils.EntitiesList.getBaseQueue(dataNode));

        ConsumerTemplate consumerTpl = new ConsumerTemplate(
                destinationResolver,
                requestQueue,
                // Give some time for case when new connection to a broker is established
                1000L
        );

        while (true) {
            ObjectMessage requestMessage = jmsTemplate.execute(consumerTpl, true);
            if (requestMessage == null) {
                break;
            }
            RequestObject requestObject = (RequestObject) requestMessage.getObject();
            requestMap.put(requestMessage.getJMSCorrelationID(), requestObject.getEntityType());
        }
        return new CommonListEntityRequest(requestMap);
    }
}
