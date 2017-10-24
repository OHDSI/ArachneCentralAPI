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
 * Created: August 15, 2017
 *
 */

package com.odysseusinc.arachne.portal.config;

import static com.odysseusinc.arachne.commons.utils.TemplateUtils.loadTemplate;

import com.github.jknack.handlebars.Template;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HandlebarsConfig {

    private static final String PACKRAT_RUNNER_TEMPLATE = "packrat/packrat_run.mustache";

    @Bean
    public Template packratRunnerTemplate() {

        return loadTemplate(PACKRAT_RUNNER_TEMPLATE);
    }
}