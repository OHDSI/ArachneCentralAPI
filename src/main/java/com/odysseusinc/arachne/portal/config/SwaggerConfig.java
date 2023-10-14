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
 * Created: November 02, 2016
 *
 */

package com.odysseusinc.arachne.portal.config;

import com.odysseusinc.arachne.commons.config.DocketWrapper;
import com.odysseusinc.arachne.portal.api.v1.controller.BuildNumberController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@ConditionalOnProperty(prefix = "swagger", name = "enable")
public class SwaggerConfig {

    private final String projectVersion;

    @Value("${arachne.token.header}")
    private String arachneTokenHeader;

    public SwaggerConfig(BuildNumberController buildNumberController) {
        projectVersion = buildNumberController.buildNumber().getProjectVersion();
    }

    @Bean
    public Docket docket(DocketWrapper docketWrapper) {
        return docketWrapper.getDocket();
    }

    @Bean
    public DocketWrapper docketWrapper() {
        return DocketWrapper.createDocketWrapper(
                "Arachne Central",
                "Arachne Central API",
                projectVersion,
                "",
                arachneTokenHeader,
                RestController.class,
                "com.odysseusinc.arachne.portal.api.v1.controller"
        );
    }
}
