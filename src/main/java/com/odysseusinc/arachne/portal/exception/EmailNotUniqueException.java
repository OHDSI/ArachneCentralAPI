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
 * Authors: Sergey Maletsky
 * Created: June 18, 2018
 *
 */

package com.odysseusinc.arachne.portal.exception;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.validation.ValidationException;

public class EmailNotUniqueException extends ValidationException {
    private final Map<String, String> emailNotUniqueErrors;

    public EmailNotUniqueException(String message, Map<String, String> emailNotUniqueErrors) {

        super(message);
        this.emailNotUniqueErrors = emailNotUniqueErrors;
    }

    public EmailNotUniqueException(Map<String, String> emailNotUniqueErrors) {

        this((String) null, emailNotUniqueErrors);
    }

    public Map<String, String> getEmailNotUniqueErrors() {

        return this.emailNotUniqueErrors;
    }
}
