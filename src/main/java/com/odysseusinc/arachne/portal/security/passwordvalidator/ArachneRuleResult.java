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
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva, Konstantin Yaroshovets
 * Created: February 07, 2018
 *
 */

package com.odysseusinc.arachne.portal.security.passwordvalidator;

public class ArachneRuleResult {

    protected boolean valid;
    protected ArachneRuleResultDetail details;

    public ArachneRuleResult() {

    }

    public ArachneRuleResult(boolean valid) {

        this.valid = valid;
    }

    public ArachneRuleResult(boolean valid, ArachneRuleResultDetail details) {

        this.valid = valid;
        this.details = details;
    }

    public void setValid(boolean valid) {

        this.valid = valid;
    }

    public boolean isValid() {

        return valid;
    }

    public ArachneRuleResultDetail getDetails() {

        return details;
    }

    public void setDetails(final ArachneRuleResultDetail details) {

        this.details = details;
    }
}
