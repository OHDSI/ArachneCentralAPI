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
 * Created: November 30, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.odysseusinc.arachne.portal.service.LoginAttemptService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;


@Service
public class LoginAttemptServiceImpl implements LoginAttemptService {

    @Value("${arachne.loginAttempts.count}")
    private int maxAttempts;
    @Value("${arachne.loginAttempts.resetMinutes}")
    private int attemptsResetMinutes;

    private Cache<String, Pair<Integer, LocalDateTime>> attemptsCache;

    @PostConstruct
    private void init() {

        attemptsCache = CacheBuilder.newBuilder().
                expireAfterWrite(attemptsResetMinutes, TimeUnit.MINUTES).build();
    }

    @Override
    public void loginSucceeded(String key) {

        attemptsCache.invalidate(key);
    }

    @Override
    public void loginFailed(String key) {
        if (StringUtils.isEmpty(key)) {
            return;
        }
        final Pair<Integer, LocalDateTime> attemptsCounter = attemptsCache.getIfPresent(key);
        if (attemptsCounter == null) {
            attemptsCache.put(key, Pair.of(1, LocalDateTime.now()));
        } else if (attemptsCounter.getKey() <= maxAttempts) {
            final Integer failedAttemptsBefore = attemptsCounter.getKey();
            attemptsCache.put(key, Pair.of(failedAttemptsBefore + 1, LocalDateTime.now()));
        }
    }

    @Override
    public Long getRemainingAccountLockPeriod(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        final Pair<Integer, LocalDateTime> attemptsCounter = this.attemptsCache.getIfPresent(key);
        if (attemptsCounter != null && attemptsCounter.getKey() >= maxAttempts) {
            return Duration.between(LocalDateTime.now(), attemptsCounter.getValue().plusMinutes(attemptsResetMinutes)).getSeconds();
        }
        return null;
    }
}
