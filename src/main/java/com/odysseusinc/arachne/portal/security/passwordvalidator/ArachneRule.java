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

import edu.vt.middleware.password.CharacterCharacteristicsRule;
import edu.vt.middleware.password.Rule;
import edu.vt.middleware.password.RuleResult;
import edu.vt.middleware.password.RuleResultDetail;
import java.util.List;
import java.util.stream.Collectors;

public class ArachneRule {

    private final Rule rule;

    public ArachneRule(Rule rule) {

        this.rule = rule;
    }

    public ArachneRuleResult validate(ArachnePasswordData passwordData) {

        final RuleResult ruleResult = rule.validate(passwordData);
        final boolean valid = ruleResult.isValid();
        final ArachneRuleResult arachneRuleResult;
        if (!valid) {
            if (rule instanceof CharacterCharacteristicsRule) {
                arachneRuleResult = new ArachneComplexRuleResult(valid);
                final List<RuleResultDetail> details = ruleResult.getDetails();
                final RuleResultDetail top = details.remove(details.size() - 1);
                final List<ArachneRuleResult> collect = details.stream().map(detail -> new ArachneRuleResult(false, new ArachneRuleResultDetail(detail))).collect(Collectors.toList());
                arachneRuleResult.setDetails(new ArachneRuleResultDetail(top));
                ((ArachneComplexRuleResult) arachneRuleResult).setResults(collect);

            } else {
                arachneRuleResult = new ArachneRuleResult(valid);
                arachneRuleResult.setDetails(new ArachneRuleResultDetail(ruleResult.getDetails().get(0)));
            }
        } else {
            arachneRuleResult = new ArachneRuleResult(true);
        }
        return arachneRuleResult;
    }
}
