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

package com.odysseusinc.arachne.portal.config;

import static java.util.concurrent.TimeUnit.SECONDS;

import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.Duration;
import javax.cache.expiry.TouchedExpiryPolicy;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
@ConditionalOnProperty(value = "cache.enabled", matchIfMissing = true)
public class CachingConfig implements JCacheManagerCustomizer {

    @Override
    public void customize(javax.cache.CacheManager cacheManager) {

        cacheManager.createCache("comments", new MutableConfiguration<>()
                .setExpiryPolicyFactory(TouchedExpiryPolicy.factoryOf(new Duration(SECONDS, 60 * 60)))
                .setStoreByValue(false)
                .setStatisticsEnabled(true));
    }

    /* for acl implementation
    private static final String ACL_CACHE_NAME = "aclCache";

    @Bean
    public Cache springACLCache() {
        return cacheManager().getCache(ACL_CACHE_NAME);
    }

    @Bean
    public CacheManager cacheManager() {

        return new EhCacheCacheManager(ehCacheManager());
    }

    @Bean(destroyMethod = "shutdown")
    public net.sf.ehcache.CacheManager ehCacheManager() {
        CacheConfiguration cacheConfiguration = new CacheConfiguration();
        cacheConfiguration.setName("ehcache");

        net.sf.ehcache.config.Configuration configuration = new net.sf.ehcache.config.Configuration();
        configuration.addCache(cacheConfiguration);

        net.sf.ehcache.CacheManager.getInstance().addCache(ACL_CACHE_NAME);
        return net.sf.ehcache.CacheManager.getInstance();
    }
    */
}
