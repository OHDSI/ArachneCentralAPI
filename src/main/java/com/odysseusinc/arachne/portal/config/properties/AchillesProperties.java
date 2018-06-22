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
 * Created: May 19, 2017
 *
 */

package com.odysseusinc.arachne.portal.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "achilles")
@Validated
public class AchillesProperties {

    private Executor executor = new Executor();

    public Executor getExecutor() {

        return executor;
    }

    public void setExecutor(Executor executor) {

        this.executor = executor;
    }

    public static class Executor {

        private Integer corePoolSize = 1;
        private Integer maxPoolSize = 2;
        private Integer queueCapacity = 10;

        public Integer getCorePoolSize() {

            return corePoolSize;
        }

        public void setCorePoolSize(Integer corePoolSize) {

            this.corePoolSize = corePoolSize;
        }

        public Integer getMaxPoolSize() {

            return maxPoolSize;
        }

        public void setMaxPoolSize(Integer maxPoolSize) {

            this.maxPoolSize = maxPoolSize;
        }

        public Integer getQueueCapacity() {

            return queueCapacity;
        }

        public void setQueueCapacity(Integer queueCapacity) {

            this.queueCapacity = queueCapacity;
        }
    }
}
