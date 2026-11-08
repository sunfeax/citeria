package com.sunfeax.citeria.validation;

import com.sunfeax.citeria.exception.RequestValidationException;
import java.util.LinkedHashMap;
import java.util.Map;

public class ValidationResult {
    private final Map<String, String> errors = new LinkedHashMap<>();

    public ValidationResult addError(String field, String message) {
        errors.putIfAbsent(field, message);
        return this;
    }

    public ValidationResult addErrorIf(boolean condition, String field, String message) {
        if (condition) {
            addError(field, message);
        }
        return this;
    }

    public ValidationResult merge(ValidationResult other) {
        if (other == null) {
            return this;
        }
        other.errors.forEach(errors::putIfAbsent);
        return this;
    }

    public Map<String, String> getErrors() {
        return Map.copyOf(errors);
    }

    public void throwIfHasErrors() {
        if (!errors.isEmpty()) {
            throw new RequestValidationException(errors);
        }
    }
}
