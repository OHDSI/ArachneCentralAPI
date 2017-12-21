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
 * Created: June 02, 2017
 *
 */

package com.odysseusinc.arachne.portal.config;

import static org.mockito.Mockito.mock;

import com.odysseusinc.arachne.portal.service.mail.ArachneMailSender;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.jackrabbit.api.JackrabbitRepository;
import org.apache.jackrabbit.core.JackrabbitRepositoryStub;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
@EnableWebMvc
public class TestConfig extends WebMvcConfigurationSupport {

    @Bean
    public MultipartResolver multipartResolver() {

        return new CommonsMultipartResolver();
    }

    @Bean
    @Primary
    public ArachneMailSender arachneMailSender() {

      return mock(ArachneMailSender.class);
    }

    @Bean
    @Primary
    public SimpMessagingTemplate simpMessagingTemplate() {

        return mock(SimpMessagingTemplate.class);
    }

    @Primary
    @Bean
    public WebSecurityConfig.HostFilter testHostFilter() {

        return new TestHostFilter("localhost:0");
    }

    public final class TestHostFilter extends WebSecurityConfig.HostFilter {

        private String portalHost;

        public TestHostFilter(String portalHost) {

            super(null);
            this.portalHost = portalHost;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

            WebSecurityConfig.portalHost.set(portalHost);
            filterChain.doFilter(request, response);
        }
    }

}