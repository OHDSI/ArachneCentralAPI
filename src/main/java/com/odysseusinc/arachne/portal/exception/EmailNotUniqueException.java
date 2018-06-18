package com.odysseusinc.arachne.portal.exception;

import edu.emory.mathcs.backport.java.util.Collections;
import java.util.Map;
import javax.validation.ValidationException;

public class EmailNotUniqueException extends ValidationException {
    private final Map<String, String> emailNotUniqueErrors;

    public EmailNotUniqueException(String message, Map<String, String> emailNotUniqueErrors) {

        super(message);
        if (emailNotUniqueErrors == null) {
            this.emailNotUniqueErrors = null;
        } else {
            this.emailNotUniqueErrors = Collections.unmodifiableMap(emailNotUniqueErrors);
        }

    }

    public EmailNotUniqueException(Map<String, String> emailNotUniqueErrors) {

        this((String) null, emailNotUniqueErrors);
    }

    public Map<String, String> getEmailNotUniqueErrors() {

        return this.emailNotUniqueErrors;
    }
}
