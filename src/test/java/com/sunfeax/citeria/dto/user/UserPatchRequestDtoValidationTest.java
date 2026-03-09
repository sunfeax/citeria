package com.sunfeax.citeria.dto.user;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;

class UserPatchRequestDtoValidationTest {

    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void emptyPatchShouldPassValidation() {
        UserUpdateRequestDto request = new UserUpdateRequestDto(null, null, null, null, null);

        Set<ConstraintViolation<UserUpdateRequestDto>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void invalidEmailShouldFailValidation() {
        UserUpdateRequestDto request = new UserUpdateRequestDto(null, null, "invalid-email", null, null);

        Set<ConstraintViolation<UserUpdateRequestDto>> violations = validator.validate(request);

        assertTrue(hasFieldMessage(violations, "email", "Invalid email format"));
    }

    @Test
    void invalidFirstNameShouldFailValidation() {
        UserUpdateRequestDto request = new UserUpdateRequestDto("123", null, null, null, null);

        Set<ConstraintViolation<UserUpdateRequestDto>> violations = validator.validate(request);

        assertTrue(hasFieldMessage(violations, "firstName", "First name must contain letters only"));
    }

    private boolean hasFieldMessage(
        Set<ConstraintViolation<UserUpdateRequestDto>> violations,
        String field,
        String message
    ) {
        return violations.stream()
            .filter(violation -> field.equals(violation.getPropertyPath().toString()))
            .map(ConstraintViolation::getMessage)
            .anyMatch(message::equals);
    }
}
