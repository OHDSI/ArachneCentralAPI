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
 * Created: December 30, 2016
 *
 */

package com.odysseusinc.arachne.portal.service.impl;

import com.odysseusinc.arachne.portal.model.PasswordReset;
import com.odysseusinc.arachne.portal.repository.PasswordResetRepository;
import com.odysseusinc.arachne.portal.service.PasswordResetService;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by PGrafkin on 30.12.2016.
 */
@Service
@SuppressWarnings("unused")
@Transactional(rollbackFor = Exception.class)
public class PasswordResetServiceImpl implements PasswordResetService {
    @Autowired
    private PasswordResetRepository passwordResetRepository;

    // Duration of token life
    @Value("${arachne.resetPasswordToken.expiresMinutes}")
    private int expires;

    /**
     * Creates new reset password token for a given email.
     *
     * @param email Email entered by user.
     * @return Password Reset model.
     */
    public PasswordReset generate(String email) {

        PasswordReset passwordReset = new PasswordReset();
        passwordReset.setEmail(email);
        passwordReset.setToken(UUID.randomUUID().toString());
        passwordResetRepository.save(passwordReset);
        return passwordReset;
    }

    /**
     * Checks whether token is expired or not.
     *
     * @param createdAt Date, when token was created.
     * @return Is token expired?
     */
    private boolean isTokenExpired(Date createdAt) {

        DateTime expiredAt = new DateTime(createdAt).plusMinutes(this.expires);
        return expiredAt.isBeforeNow();
    }

    /**
     * Checks whether such token exists and can be used for password resetting.
     *
     * @param email Email of user resetting password.
     * @param token Token from the mail.
     * @return Can we reset pass?
     */
    public boolean canReset(String email, String token) {

        boolean result = false;
        List<PasswordReset> passwordResetList = passwordResetRepository.findByEmailAndTokenOrderByCreatedDesc(email, token);
        if (!passwordResetList.isEmpty()) {
            PasswordReset passwordReset = passwordResetList.get(0);
            Date createdAt = passwordReset.getCreated();
            if (!this.isTokenExpired(createdAt)) {
                result = true;
            }
        }
        return result;
    }
}
