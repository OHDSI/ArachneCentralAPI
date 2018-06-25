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
 * Created: November 30, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.odysseusinc.arachne.portal.service.LoginAttemptService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class LoginAttemptServiceImpl implements LoginAttemptService {

    @Value("${arachne.loginAttempts.count}")
    private int maxAttempts;
    @Value("${arachne.loginAttempts.resetMinutes}")
    private int attemptsResetMinutes;

    private LoadingCache<String, Integer> attemptsCache;

    @PostConstruct
    private void init() {

        attemptsCache = CacheBuilder.newBuilder().
                expireAfterWrite(attemptsResetMinutes, TimeUnit.MINUTES).build(new CacheLoader<String, Integer>() {
            public Integer load(String key) {

                return 0;
            }
        });
    }

    public void loginSucceeded(String key) {

        attemptsCache.invalidate(key);
    }

    public void loginFailed(String key) {

        int attempts = ObjectUtils.firstNonNull(attemptsCache.getIfPresent(key), 0);
        attempts++;
        attemptsCache.put(key, attempts);
    }

    public boolean isBlocked(String key) {

        try {
            return attemptsCache.get(key) >= maxAttempts;
        } catch (ExecutionException e) {
            return false;
        }
    }
}
