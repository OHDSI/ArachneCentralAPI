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
 * Created: January 22, 2018
 *
 */

package com.odysseusinc.arachne.portal.security.passwordvalidator;

import edu.vt.middleware.password.MessageResolver;
import edu.vt.middleware.password.Rule;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ArachnePasswordValidator {

    private final List<Rule> passwordRules;
    protected final ArachnePasswordInfo passwordInfo;
    private final MessageResolver messageResolver;

    protected ArachnePasswordValidator(MessageResolver messageResolver, List<Rule> passwordRules, ArachnePasswordInfo passwordInfo) {

        this.messageResolver = messageResolver;
        this.passwordRules = passwordRules;
        this.passwordInfo = passwordInfo;
    }

    public ArachnePasswordInfo getPasswordInfo() {

        return passwordInfo;
    }

    public ArachnePasswordValidationResult validate(ArachnePasswordData passwordData) {

        final ArachnePasswordValidationResult result = new ArachnePasswordValidationResult(true);

        for (Rule rule : passwordRules) {
            final ArachneRuleResult ruleResult = new ArachneRule(rule).validate(passwordData);
            if (!ruleResult.isValid()) {
                result.setValid(false);
                result.addResult(ruleResult);
            }
        }
        return result;
    }

    public ArachnePasswordInfo getMessages(ArachnePasswordValidationResult result) {

        return new ArachnePasswordInfo(process(result.getResults()));
    }

    private Set<RuleInfo> process(List<ArachneRuleResult> ruleResult) {

        final Set<RuleInfo> ruleInfos = new HashSet<>();
        for (ArachneRuleResult arachneRuleResult : ruleResult) {
            final RuleInfo ruleInfo;
            if (arachneRuleResult instanceof ArachneComplexRuleResult) {
                final ArachneComplexRuleResult arachneComplexRuleResult = (ArachneComplexRuleResult) arachneRuleResult;
                final Set<RuleInfo> process = process(arachneComplexRuleResult.getResults());
                ruleInfo = new ComplexRuleInfo(messageResolver.resolve(arachneComplexRuleResult.getDetails()), process);
                ruleInfos.add(ruleInfo);
            } else {
                ruleInfo = new RuleInfo(messageResolver.resolve(arachneRuleResult.getDetails()));
            }
            ruleInfos.add(ruleInfo);
        }
        return ruleInfos;
    }
}
