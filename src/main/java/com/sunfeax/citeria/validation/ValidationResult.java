package com.sunfeax.citeria.validation;

import com.sunfeax.citeria.exception.RequestValidationException;
import java.util.LinkedHashMap;
import java.util.Map;

public class ValidationResult {
    private final Map<String, String> errors = new LinkedHashMap<>();

    public ValidationResult addErrorIf(boolean condition, String field, String message) {
        if (condition) {
            errors.put(field, message);
        }
        return this;
    }

    public void throwIfHasErrors() {
        if (!errors.isEmpty()) {
            throw new RequestValidationException(errors);
        }
    }
}