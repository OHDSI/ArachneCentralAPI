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
 * Authors: Alexandr Cumarav
 *
 */

package com.odysseusinc.arachne.portal.config.tenancy;

import com.odysseusinc.arachne.portal.config.interceptors.QueryInterceptor;
import org.hibernate.MultiTenancyStrategy;
import org.hibernate.cfg.Environment;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class HibernateConfig {

    @Autowired
    private JpaProperties jpaProperties;

    @Autowired(required = false)
    private QueryInterceptor queryInterceptor;

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {

        return new HibernateJpaVendorAdapter();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource,
                                                                       MultiTenantConnectionProvider multiTenantConnectionProviderImpl,
                                                                       CurrentTenantIdentifierResolver currentTenantIdentifierResolverImpl,
                                                                       HibernateProperties hibernateProperties) {

        Map<String, Object> properties = hibernateProperties.determineHibernateProperties(
                jpaProperties.getProperties(), new HibernateSettings());
        // NOTE:
        // dummy setting, just to force Hibernate to use custom connection provider through which we pass current tenant id to DB
        properties.put(Environment.MULTI_TENANT, MultiTenancyStrategy.SCHEMA);
        properties.put(Environment.MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProviderImpl);
        properties.put(Environment.MULTI_TENANT_IDENTIFIER_RESOLVER, currentTenantIdentifierResolverImpl);
        properties.put("hibernate.ejb.interceptor", queryInterceptor);
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.odysseusinc.arachne.*");
        em.setJpaVendorAdapter(jpaVendorAdapter());
        em.setJpaPropertyMap(properties);
        return em;
    }
}
