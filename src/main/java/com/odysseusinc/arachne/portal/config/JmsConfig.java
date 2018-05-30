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
 * Created: June 26, 2017
 *
 */

package com.odysseusinc.arachne.portal.config;

import java.net.URI;
import java.util.Arrays;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

@Configuration
public class JmsConfig {

    @Value("${spring.activemq.broker-url}")
    private String brokerUrl;

    // Required for tests
    // (SpringJUnit4ClassRunner has a feature: it caches all started contexts and
    // destroys them only at the end of running of all test cases. So PostConstruct, not followed by PreDestroy,
    // is called multiple times - multiple Brokers will be initialized w/o check)
    private Boolean isBrokerExist() {
        Boolean exists = true;
        try {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
            Connection conn = factory.createConnection();
            Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException e) {
            exists = false;
        }
        return exists;
    }

    @Bean
    public BrokerService createBrokerService() throws Exception {
        BrokerService broker = null;
        if (!isBrokerExist()) {
            broker = new BrokerService();
            TransportConnector connector = new TransportConnector();
            connector.setUri(new URI(brokerUrl));
            broker.addConnector(connector);
        }
        return broker;
    }

    @Bean
    public ConnectionFactory connectionFactory() {

        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        connectionFactory.setTrustedPackages(Arrays.asList("com.odysseusinc", "java.util", "java.lang"));
        return connectionFactory;
        // TODO:
        // return new PooledConnectionFactory(connectionFactory);
    }

    // http://chriswongdevblog.blogspot.ru/2013/01/jmstemplate-is-not-evil.html
    @Bean
    public JmsTemplate jmsTemplate() {

        JmsTemplate template = new JmsTemplate();
        template.setConnectionFactory(connectionFactory());
        return template;
    }
}
