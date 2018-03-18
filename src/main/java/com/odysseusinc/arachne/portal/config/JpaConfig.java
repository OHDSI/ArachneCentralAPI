///*
// *  Copyright 2017 Observational Health Data Sciences and Informatics
// *  Licensed under the Apache License, Version 2.0 (the "License");
// *  you may not use this file except in compliance with the License.
// *  You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// *  Unless required by applicable law or agreed to in writing, software
// *  distributed under the License is distributed on an "AS IS" BASIS,
// *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *  See the License for the specific language governing permissions and
// *  limitations under the License.
// *
// *  Company: Odysseus Data Services, Inc.
// *  Product Owner/Architecture: Gregory Klebanov
// *  Authors: Anton Gackovka
// *  Created: November 6, 2017
// *
// */
//
//package com.odysseusinc.arachne.portal.config;
//
//import com.odysseusinc.arachne.portal.config.interceptors.QueryInterceptor;
//import com.odysseusinc.arachne.portal.config.interceptors.RequestInterceptor;
//import org.springframework.beans.factory.ObjectProvider;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
//import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.transaction.jta.JtaTransactionManager;
//
//import javax.sql.DataSource;
//import java.util.Map;
//
//@Configuration
//@ConditionalOnProperty("portal.useQueryInterceptor")
//public class JpaConfig extends HibernateJpaConfiguration {
//
//    public JpaConfig(DataSource dataSource, JpaProperties jpaProperties, ObjectProvider<JtaTransactionManager> jtaTransactionManager, ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers) {
//        super(dataSource, jpaProperties, jtaTransactionManager, transactionManagerCustomizers);
//    }
//
//    @Override
//    protected void customizeVendorProperties(Map<String, Object> vendorProperties) {
//        vendorProperties.put("hibernate.ejb.interceptor", queryInterceptor());
//    }
//
//    @Bean
//    public QueryInterceptor queryInterceptor() {
//        return new QueryInterceptor();
//    }
//
//    @Bean
//    public RequestInterceptor requestInterceptor(QueryInterceptor queryInterceptor) {
//        return new RequestInterceptor(queryInterceptor);
//    }
//
//
//}