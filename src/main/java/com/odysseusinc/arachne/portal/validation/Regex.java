package com.odysseusinc.arachne.portal.validation;

import java.lang.annotation.ElementType;
import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {RegexValidator.class})
public @interface Regex {
    String value();

    String message() default "{com.odysseusinc.arachne.portal.validation.Regex.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
