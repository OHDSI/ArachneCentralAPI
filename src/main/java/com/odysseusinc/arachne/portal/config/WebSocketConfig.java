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
 * Created: April 18, 2017
 *
 */

package com.odysseusinc.arachne.portal.config;

import com.odysseusinc.arachne.portal.service.BaseUserService;
import com.odysseusinc.arachne.portal.util.UserUtils;
import java.util.Optional;
import org.ohdsi.authenticator.service.authentication.Authenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.user.DefaultUserDestinationResolver;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.messaging.simp.user.UserDestinationResolver;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.DefaultSimpUserRegistry;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.security.Principal;
import java.util.List;

@Configuration
@ComponentScan(basePackages = {"com.odysseusinc.arachne.portal.service", "com.odysseusinc.arachne.portal.security"})
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${arachne.token.header}")
    private String tokenHeader;

    private final BaseUserService userService;
    private final Authenticator authenticator;
    private final DefaultSimpUserRegistry userRegistry = new DefaultSimpUserRegistry();
    private final DefaultUserDestinationResolver resolver = new DefaultUserDestinationResolver(userRegistry);

    @Autowired
    public WebSocketConfig(@Lazy BaseUserService userService,
                                   Authenticator authenticator) {
        this.userService = userService;
        this.authenticator = authenticator;
    }

    @Bean
    @Primary
    public SimpUserRegistry userRegistry() {
        return userRegistry;
    }

    @Bean
    @Primary
    public UserDestinationResolver userDestinationResolver() {
        return resolver;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/queue", "/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/arachne-websocket").setAllowedOrigins("*").withSockJS();
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {

        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {

                SimpMessageHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, SimpMessageHeaderAccessor.class);

                Optional<Principal> maybePrincipal = Optional.ofNullable(UserUtils.getCurrentUser()).map(user ->
                        user::getUsername
                );
                maybePrincipal.ifPresent(accessor::setUser);
                Principal principal = maybePrincipal.orElse(null);

                if (accessor.getMessageType() == SimpMessageType.CONNECT) {
                    userRegistry.onApplicationEvent(
                            new SessionConnectedEvent(this, (Message<byte[]>) message, principal)
                    );
                } else if (accessor.getMessageType() == SimpMessageType.SUBSCRIBE) {
                    userRegistry.onApplicationEvent(
                            new SessionSubscribeEvent(this, (Message<byte[]>) message, principal)
                    );
                } else if (accessor.getMessageType() == SimpMessageType.UNSUBSCRIBE) {
                    userRegistry.onApplicationEvent(
                            new SessionUnsubscribeEvent(this, (Message<byte[]>) message, principal)
                    );
                } else if (accessor.getMessageType() == SimpMessageType.DISCONNECT) {
                    userRegistry.onApplicationEvent(
                            new SessionDisconnectEvent(
                                    this,
                                    (Message<byte[]>) message,
                                    accessor.getSessionId(),
                                    CloseStatus.NORMAL
                            )
                    );
                }
                return message;
            }
        });
    }
}

