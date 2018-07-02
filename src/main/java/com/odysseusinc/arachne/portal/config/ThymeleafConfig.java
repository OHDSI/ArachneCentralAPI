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
 * Created: May 26, 2017
 *
 */

package com.odysseusinc.arachne.portal.config;

import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.StringTemplateResolver;

@Configuration
@ComponentScan("org.springframework.boot.autoconfigure.thymeleaf")
public class ThymeleafConfig {

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private SpringResourceTemplateResolver defaultTemplateResolver;

    @PostConstruct
    private void postConstruct() {
        Set<ITemplateResolver> resolverSet = new LinkedHashSet<>();

        // At first, try to load html template from resources by given name
        defaultTemplateResolver.setOrder(0);
        resolverSet.add(defaultTemplateResolver);

        //Then try to load txt template from resources by given name
        ClassLoaderTemplateResolver textTemplateResolver = getTextTemplateResolver();
        textTemplateResolver.setOrder(0);
        resolverSet.add(textTemplateResolver);

        // If there was found no such template, use the string as template by itself
        StringTemplateResolver stringTemplateResolver = getStringTemplateResolver();
        stringTemplateResolver.setOrder(1);
        resolverSet.add(stringTemplateResolver);

        templateEngine.setTemplateResolvers(resolverSet);
    }

    private ClassLoaderTemplateResolver getTextTemplateResolver() {

        final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".txt");
        templateResolver.setTemplateMode(TemplateMode.TEXT);
        templateResolver.setCacheable(false);
        return templateResolver;
    }

    private StringTemplateResolver getStringTemplateResolver() {
        final StringTemplateResolver templateResolver = new StringTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCacheable(false);
        return templateResolver;
    }
}
