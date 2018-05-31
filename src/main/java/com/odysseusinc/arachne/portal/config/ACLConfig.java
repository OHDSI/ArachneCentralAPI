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
 * Created: January 25, 2017
 *
 */

//package com.odysseusinc.arachne.portal.config;
//
//import javax.sql.DataSource;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cache.Cache;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.access.PermissionEvaluator;
//import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
//import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
//import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
//import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
//import org.springframework.security.acls.AclPermissionEvaluator;
//import org.springframework.security.acls.domain.AclAuthorizationStrategy;
//import org.springframework.security.acls.domain.AclAuthorizationStrategyImpl;
//import org.springframework.security.acls.domain.AuditLogger;
//import org.springframework.security.acls.domain.ConsoleAuditLogger;
//import org.springframework.security.acls.domain.DefaultPermissionGrantingStrategy;
//import org.springframework.security.acls.domain.SpringCacheBasedAclCache;
//import org.springframework.security.acls.jdbc.BasicLookupStrategy;
//import org.springframework.security.acls.jdbc.JdbcMutableAclService;
//import org.springframework.security.acls.jdbc.LookupStrategy;
//import org.springframework.security.acls.model.AclCache;
//import org.springframework.security.acls.model.AclService;
//import org.springframework.security.acls.model.PermissionGrantingStrategy;
//import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//
//
////@Configuration
////@EnableGlobalMethodSecurity(
////        prePostEnabled = true,
////        jsr250Enabled = true,
////        securedEnabled = true
////)
//public class ACLConfig {
//
//    @Autowired
//    private DataSource primaryDataSource;
//
//    @Autowired
//    private Cache springACLCache;
//
//    @Bean
//    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
//        DefaultMethodSecurityExpressionHandler methodSecurityExpressionHandler = new DefaultMethodSecurityExpressionHandler();
//        methodSecurityExpressionHandler.setPermissionEvaluator(permissionEvaluator());
//        methodSecurityExpressionHandler.setRoleHierarchy(roleHierarchy());
//        return methodSecurityExpressionHandler;
//    }
//
//    @Bean
//    public PermissionEvaluator permissionEvaluator() {
//        return new AclPermissionEvaluator(aclService()){
//            @Override
//            public boolean hasPermission(Authentication authentication, Object domainObject, Object permission) {
//
//                return super.hasPermission(authentication, domainObject, permission);
//            }
//        };
//    }
//
//    @Bean
//    public AclService aclService() {
//        return new JdbcMutableAclService(primaryDataSource, lookupStrategy(), aclCache());
//    }
//
//    @Bean
//    public LookupStrategy lookupStrategy() {
//        return new BasicLookupStrategy(primaryDataSource, aclCache(), aclAuthorizationStrategy(), auditLogger());
//    }
//
//    @Bean
//    public AclCache aclCache() {
//        return new SpringCacheBasedAclCache(springACLCache, permissionGrantingStrategy(), aclAuthorizationStrategy());
//    }
//
//    @Bean
//    public PermissionGrantingStrategy permissionGrantingStrategy() {
//        return new DefaultPermissionGrantingStrategy(auditLogger());
//    }
//
//    @Bean
//    public AclAuthorizationStrategy aclAuthorizationStrategy() {
//        // Authority needed to change ownership
//        GrantedAuthority changeOwnershipGrantedAuthority = new SimpleGrantedAuthority("ROLE_ADMIN");
//        // Authority needed to modify auditing details
//        GrantedAuthority modifyAuditingDetailsGrantedAuthority = new SimpleGrantedAuthority("ROLE_ADMIN");
//        // Authority needed to change other ACL and ACE details
//        GrantedAuthority otherACLAndACEDetailsGrantedAuthority = new SimpleGrantedAuthority("ROLE_ADMIN");
//        return new AclAuthorizationStrategyImpl(
//                changeOwnershipGrantedAuthority,
//                modifyAuditingDetailsGrantedAuthority,
//                otherACLAndACEDetailsGrantedAuthority
//        );
//    }
//
//    @Bean
//    public AuditLogger auditLogger() {
//        return new ConsoleAuditLogger();
//    }
//
//    @Bean
//    public RoleHierarchy roleHierarchy() {
//        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
//        roleHierarchy.setHierarchy(
//                "ROLE_ADMIN > ROLE_LEAD_INVESTIGATOR ROLE_ADMIN > ROLE_DATA_SET_OWNER "
//                + "ROLE_LEAD_INVESTIGATOR > ROLE_COLLABORATOR ROLE_DATA_SET_OWNER > ROLE_COLLABORATOR"
//        );
//        return roleHierarchy;
//    }
//
//}
