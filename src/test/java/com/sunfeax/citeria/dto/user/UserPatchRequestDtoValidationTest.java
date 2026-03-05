package com.sunfeax.citeria.dto.user;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class UserPatchRequestDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void emptyPatchShouldPassValidation() {
        UserPatchRequestDto request = new UserPatchRequestDto(null, null, null, null, null);

        Set<ConstraintViolation<UserPatchRequestDto>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void invalidEmailShouldFailValidation() {
        UserPatchRequestDto request = new UserPatchRequestDto(null, null, "invalid-email", null, null);

        Set<ConstraintViolation<UserPatchRequestDto>> violations = validator.validate(request);

        assertTrue(hasFieldMessage(violations, "email", "Invalid email format"));
    }

    @Test
    void invalidFirstNameShouldFailValidation() {
        UserPatchRequestDto request = new UserPatchRequestDto("123", null, null, null, null);

        Set<ConstraintViolation<UserPatchRequestDto>> violations = validator.validate(request);

        assertTrue(hasFieldMessage(violations, "firstName", "First name must contain letters only"));
    }

    private boolean hasFieldMessage(
        Set<ConstraintViolation<UserPatchRequestDto>> violations,
        String field,
        String message
    ) {
        return violations.stream()
            .filter(violation -> field.equals(violation.getPropertyPath().toString()))
            .map(ConstraintViolation::getMessage)
            .anyMatch(message::equals);
    }
}
