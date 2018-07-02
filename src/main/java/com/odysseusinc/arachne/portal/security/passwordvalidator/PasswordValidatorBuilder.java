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

import edu.vt.middleware.dictionary.ArrayWordList;
import edu.vt.middleware.dictionary.WordListDictionary;
import edu.vt.middleware.password.AlphabeticalCharacterRule;
import edu.vt.middleware.password.CharacterCharacteristicsRule;
import edu.vt.middleware.password.CharacterRule;
import edu.vt.middleware.password.DictionarySubstringRule;
import edu.vt.middleware.password.DigitCharacterRule;
import edu.vt.middleware.password.IllegalCharacterRule;
import edu.vt.middleware.password.LengthRule;
import edu.vt.middleware.password.LowercaseCharacterRule;
import edu.vt.middleware.password.MessageResolver;
import edu.vt.middleware.password.NonAlphanumericCharacterRule;
import edu.vt.middleware.password.QwertySequenceRule;
import edu.vt.middleware.password.RepeatCharacterRegexRule;
import edu.vt.middleware.password.Rule;
import edu.vt.middleware.password.UppercaseCharacterRule;
import edu.vt.middleware.password.UsernameRule;
import edu.vt.middleware.password.WhitespaceRule;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.core.io.Resource;

public class PasswordValidatorBuilder {

    private Integer minLength;
    private Integer maxLength;
    private boolean whiteSpaces = false;
    private boolean querty = false;
    private boolean username = false;
    private boolean usernameMatchBackward = false;
    private boolean usernameIgnoreCase = false;
    private boolean usersNames = false;
    private String[] blacklist;
    private Integer repeatCharsLength;
    private Integer alphabeticalCharsNum;
    private char[] illegalChars;
    private ComplexRulesBuilder complexRulesBuilder;
    private Resource messages;
    private ArachnePasswordInfo rulesInfo;

    private PasswordValidatorBuilder() {

    }

    public static PasswordValidatorBuilder create() {

        return new PasswordValidatorBuilder();
    }

    public PasswordValidatorBuilder withLength(final int minLength, final int maxLength) {

        this.minLength = minLength;
        this.maxLength = maxLength;
        return this;
    }

    public PasswordValidatorBuilder withWhitespace() {

        whiteSpaces = true;
        return this;
    }

    public PasswordValidatorBuilder withQuerty() {

        querty = true;
        return this;
    }

    public PasswordValidatorBuilder withUsername(final boolean matchBackward, final boolean ignoreCase) {

        this.usernameMatchBackward = matchBackward;
        this.usernameIgnoreCase = ignoreCase;
        return this;
    }

    public PasswordValidatorBuilder withUsersNames() {

        this.usersNames = true;
        return this;
    }

    public PasswordValidatorBuilder withBlacklist(String[] blacklist) {

        this.blacklist = blacklist;
        return this;
    }

    public PasswordValidatorBuilder withRepeatChars(final int sl) {

        this.repeatCharsLength = sl;
        return this;
    }

    public PasswordValidatorBuilder withAlphabeticalChars(final int num) {

        this.alphabeticalCharsNum = num;
        return this;
    }

    public PasswordValidatorBuilder withIllegalChars(final char[] illegalChars) {

        this.illegalChars = illegalChars;
        return this;
    }

    public PasswordValidatorBuilder withMessages(Resource messages) {

        this.messages = messages;
        return this;
    }

    public ComplexRulesBuilder withComplexRules() {

        this.complexRulesBuilder = new ComplexRulesBuilder();
        return complexRulesBuilder;
    }

    public ArachnePasswordValidator build() throws IOException {

        final Set<Rule> rules = new HashSet<>();
        final Set<RuleInfo> ruleInfos = new HashSet<>();

        if (Objects.nonNull(minLength) && Objects.nonNull(maxLength)) {
            rules.add(new LengthRule(minLength, maxLength));
            ruleInfos.add(new RuleInfo(String.format("Password length from %s to %s digits", minLength, maxLength)));
        }
        if (whiteSpaces) {
            rules.add(new WhitespaceRule());
            ruleInfos.add(new RuleInfo("Whitespaces are not allowed"));
        }
        if (querty) {
            rules.add(new QwertySequenceRule());
            ruleInfos.add(new RuleInfo("Common sequences are not allowed"));
        }

        if (username) {
            rules.add(new UsernameRule(usernameMatchBackward, usernameIgnoreCase));
            String substring = "";
            if (usernameMatchBackward || usernameIgnoreCase) {
                substring = "( ";
                if (usernameMatchBackward) {
                    substring += "with backwards ";
                }
                if (usernameIgnoreCase) {
                    substring += "with case ignoring ";
                }
                substring += ")";
            }
            ruleInfos.add(new RuleInfo("The username inside password is not allowed" + substring));
        }

        if (usersNames) {
            rules.add(new UsersNameRules());
            ruleInfos.add(new RuleInfo("The user's name inside password is not allowed"));
        }

        if (Objects.nonNull(blacklist)) {
            Arrays.sort(blacklist);
            final WordListDictionary dictionary = new WordListDictionary(new ArrayWordList(blacklist));
            final DictionarySubstringRule e = new DictionarySubstringRule(dictionary);
            rules.add(e);
            ruleInfos.add(new RuleInfo("Company and application names are not allowed"));
        }

        if (Objects.nonNull(repeatCharsLength)) {
            rules.add(new RepeatCharacterRegexRule(repeatCharsLength));
            ruleInfos.add(new RuleInfo(String.format("Character sequence more than %s chars is not allowed", repeatCharsLength)));
        }

        if (Objects.nonNull(alphabeticalCharsNum)) {
            rules.add(new AlphabeticalCharacterRule());
        }

        if (Objects.nonNull(illegalChars)) {
            rules.add(new IllegalCharacterRule(illegalChars));
            final String description = "Characters: "
                    + Arrays.stream(ArrayUtils.toObject(illegalChars))
                    .map(c -> "'" + c + "'")
                    .collect(Collectors.joining(","))
                    + " are not allowed";
            ruleInfos.add(new RuleInfo(description));
        }

        processComplexBuilder(complexRulesBuilder, rules, ruleInfos);

        final ArachnePasswordValidator arachnePasswordValidator = new ArachnePasswordValidator(getMessageResolver(messages), new ArrayList<>(rules), new ArachnePasswordInfo(ruleInfos));
        return arachnePasswordValidator;
    }

    private static void processComplexBuilder(ComplexRulesBuilder complexRulesBuilder, Set<Rule> rules, Set<RuleInfo> ruleInfos) {

        if (complexRulesBuilder != null) {
            final CharacterCharacteristicsRule characteristicsRule = new CharacterCharacteristicsRule();
            List<CharacterRule> characterRules = new ArrayList<>();
            Set<RuleInfo> complexRules = new HashSet<>();
            final Integer numberOfCharacteristics = complexRulesBuilder.numberOfCharacteristics;
            if (Objects.nonNull(numberOfCharacteristics)) {
                characteristicsRule.setNumberOfCharacteristics(numberOfCharacteristics);
            }
            final Integer uppercaseCharacterNum = complexRulesBuilder.uppercaseCharacterNum;
            if (Objects.nonNull(uppercaseCharacterNum)) {
                characterRules.add(new UppercaseCharacterRule(uppercaseCharacterNum));
                complexRules.add(new RuleInfo(String.format("At least %s uppercase character(s) (A-Z)", uppercaseCharacterNum)));
            }
            final Integer lowercaseCharacterNum = complexRulesBuilder.lowercaseCharacterNum;
            if (Objects.nonNull(lowercaseCharacterNum)) {
                characterRules.add(new LowercaseCharacterRule(lowercaseCharacterNum));
                complexRules.add(new RuleInfo(String.format("At least %s lowercase character(s) (a-z)", lowercaseCharacterNum)));
            }
            final Integer digitCharacterNum = complexRulesBuilder.digitCharacterNum;
            if (Objects.nonNull(digitCharacterNum)) {
                characterRules.add(new DigitCharacterRule(digitCharacterNum));
                complexRules.add(new RuleInfo(String.format("At least %s digit (0-9)", lowercaseCharacterNum)));
            }
            final Integer nonAlphanumericCharacterNum = complexRulesBuilder.nonAlphanumericCharacterNum;
            if (Objects.nonNull(nonAlphanumericCharacterNum)) {
                characterRules.add(new NonAlphanumericCharacterRule(nonAlphanumericCharacterNum));
                complexRules.add(new RuleInfo(String.format("At least %s special character (punctuation)", lowercaseCharacterNum)));
            }
            characteristicsRule.setRules(characterRules);
            rules.add(characteristicsRule);
            final int characterisicsMinimum = characteristicsRule.getNumberOfCharacteristics();
            final int characteristicsSize = characteristicsRule.getRules().size();
            final String description = String.format("Password must meet at least %s out of the following %s rules",
                    characterisicsMinimum, characteristicsSize);
            ruleInfos.add(new ComplexRuleInfo(description, complexRules));
        }
    }

    protected static MessageResolver getMessageResolver(Resource messages) throws IOException {

        final MessageResolver messageResolver;
        if (Objects.nonNull(messages)) {
            final Properties props = new Properties();
            try (final InputStream inputStream = messages.getInputStream()) {
                props.load(inputStream);
                messageResolver = new MessageResolver(props);
            }
        } else {
            messageResolver = new MessageResolver();
        }
        return messageResolver;
    }

    public class ComplexRulesBuilder {

        private Integer numberOfCharacteristics;
        private Integer uppercaseCharacterNum;
        private Integer lowercaseCharacterNum;
        private Integer digitCharacterNum;
        private Integer nonAlphanumericCharacterNum;

        private ComplexRulesBuilder() {

        }

        public ComplexRulesBuilder withNumberOfCharacteristics(int num) {

            this.numberOfCharacteristics = num;
            return this;

        }

        public ComplexRulesBuilder withUppercaseCharacter(final int num) {

            this.uppercaseCharacterNum = num;
            return this;
        }

        public ComplexRulesBuilder withLowercaseCharacter(final int num) {

            this.lowercaseCharacterNum = num;
            return this;
        }

        public ComplexRulesBuilder withDigitCharacter(final int num) {

            this.digitCharacterNum = num;
            return this;
        }

        public ComplexRulesBuilder withNonAlphanumericCharacter(final int num) {

            this.nonAlphanumericCharacterNum = num;
            return this;
        }

        public PasswordValidatorBuilder done() {

            return PasswordValidatorBuilder.this;
        }

    }
}

