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
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva, Konstantin Yaroshovets
 * Created: January 22, 2017
 *
 */

package com.odysseusinc.arachne.portal.config;

import com.odysseusinc.arachne.portal.config.properties.AntivirusProperties;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.ExpressionRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import xyz.capybara.clamav.CommunicationException;

@Configuration
@EnableAsync
@EnableConfigurationProperties(AntivirusProperties.class)
public class AntivirusConfig {

    @Bean(name = "antivirusScanExecutor")
    public TaskExecutor antivirusScanExecutor(AntivirusProperties properties) {

        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getExecutor().getCorePoolSize());
        executor.setMaxPoolSize(properties.getExecutor().getMaxPoolSize());
        executor.setQueueCapacity(properties.getExecutor().getQueueCapacity());
        return executor;
    }

    @Bean(name = "antivirusRetryTemplate")
    public RetryTemplate antivirusRetryTemplate(AntivirusProperties properties) {

        RetryTemplate retryTemplate = new RetryTemplate();
        AntivirusProperties.RetryConfig retryConfig = properties.getRetry();
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(CommunicationException.class, true);
        RetryPolicy policy = new ExpressionRetryPolicy(retryConfig.getMaxAttempts(), retryableExceptions, true, "#{message.contains('Error while communicating with the server')}");
        retryTemplate.setRetryPolicy(policy);
        AntivirusProperties.BackOffPolicyConfig backOffPolicyConfig = retryConfig.getBackoff();
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(backOffPolicyConfig.getInitialInterval());
        backOffPolicy.setMaxInterval(backOffPolicyConfig.getMaxInterval());
        backOffPolicy.setMultiplier(backOffPolicyConfig.getMultiplier());
        retryTemplate.setBackOffPolicy(backOffPolicy);
        return retryTemplate;
    }
}
