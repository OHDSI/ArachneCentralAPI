package com.odysseusinc.arachne.portal.api.v1.dto.converters.study;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Length.List;

@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@List({
        @Length(min = 5, message = "The field must be at least 5 characters"),
        @Length(max = 1000, message = "The field must be no more than 1000 characters")
})
// Don't allow leading or trailing whitespace
@Pattern(regexp = "^\\S+(\\s+\\S+)*$", message = "Leading and trailing spaces are not allowed in the study title")
@Constraint(validatedBy = {})
public @interface StudyTitle {
    String message() default "{com.odysseusinc.arachne.portal.validation.Regex.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
