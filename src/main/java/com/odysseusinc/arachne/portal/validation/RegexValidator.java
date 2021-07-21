package com.odysseusinc.arachne.portal.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class RegexValidator implements ConstraintValidator<Regex, String> {

    private String expression;

    @Override
    public void initialize(Regex annotation) {
        expression = annotation.value();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value.matches(expression);
    }
}

