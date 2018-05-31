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
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva, Konstantin Yaroshovets
 * Created: February 07, 2018
 *
 */

package com.odysseusinc.arachne.portal.security.passwordvalidator;

import java.util.ArrayList;
import java.util.List;

public class ArachneComplexRuleResult extends ArachneRuleResult {

    public ArachneComplexRuleResult(boolean valid) {

        super(valid);
    }

    protected List<ArachneRuleResult> results = new ArrayList<>();

    public List<ArachneRuleResult> getResults() {

        return results;
    }

    public void setResults(List<ArachneRuleResult> results) {

        this.results = results;
    }
}
