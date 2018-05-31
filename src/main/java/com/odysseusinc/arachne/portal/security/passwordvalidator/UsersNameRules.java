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
 * Created: January 19, 2018
 *
 */

package com.odysseusinc.arachne.portal.security.passwordvalidator;

import edu.vt.middleware.password.PasswordData;
import edu.vt.middleware.password.Rule;
import edu.vt.middleware.password.RuleResult;
import edu.vt.middleware.password.RuleResultDetail;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class UsersNameRules implements Rule {

    private static final String ERROR_CODE = "ILLEGAL_PERSONAL_DATA";

    @Override
    public RuleResult validate(PasswordData passwordData) {

        final RuleResult result = new RuleResult(true);
        if (passwordData instanceof ArachnePasswordData) {
            String text = passwordData.getPassword().getText();
            ArachnePasswordData arachnePasswordData = (ArachnePasswordData) passwordData;
            final Map<String, String> names = new HashMap<>();
            names.put("first name", arachnePasswordData.getFirstName());
            names.put("last name", arachnePasswordData.getLastName());
            names.put("middle name", arachnePasswordData.getMiddleName());
            for (Map.Entry<String, String> entry : names.entrySet()) {
                final String value = entry.getValue();
                if (value != null && text.contains(value)) {
                    result.setValid(false);
                    result.getDetails().add(
                            new RuleResultDetail(
                                    ERROR_CODE,
                                    createRuleResultDetailParameters(entry.getKey(), value)));
                }
            }
        }
        return result;
    }

    protected Map<String, ?> createRuleResultDetailParameters(final String key, final String value) {

        final Map<String, Object> m = new LinkedHashMap<>();
        m.put(key, value);
        return m;
    }
}