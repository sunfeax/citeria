package com.sunfeax.citeria.dto.business;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class BusinessPatchRequestDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void emptyPatchShouldPassValidation() {
        BusinessPatchRequestDto request = new BusinessPatchRequestDto(null, null, null, null, null, null, null);

        Set<ConstraintViolation<BusinessPatchRequestDto>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void blankNameShouldFailValidation() {
        BusinessPatchRequestDto request = new BusinessPatchRequestDto(null, "   ", null, null, null, null, null);

        Set<ConstraintViolation<BusinessPatchRequestDto>> violations = validator.validate(request);

        assertTrue(hasFieldMessage(violations, "name", "Business name must not be blank"));
    }

    @Test
    void invalidEmailShouldFailValidation() {
        BusinessPatchRequestDto request = new BusinessPatchRequestDto(null, null, null, null, "invalid", null, null);

        Set<ConstraintViolation<BusinessPatchRequestDto>> violations = validator.validate(request);

        assertTrue(hasFieldMessage(violations, "email", "Invalid email format"));
    }

    @Test
    void tooLongDescriptionShouldFailValidation() {
        BusinessPatchRequestDto request = new BusinessPatchRequestDto(
            null,
            null,
            "a".repeat(1001),
            null,
            null,
            null,
            null
        );

        Set<ConstraintViolation<BusinessPatchRequestDto>> violations = validator.validate(request);

        assertTrue(hasFieldViolation(violations, "description"));
    }

    @Test
    void shortPhoneShouldFailValidation() {
        BusinessPatchRequestDto request = new BusinessPatchRequestDto(null, null, null, "123", null, null, null);

        Set<ConstraintViolation<BusinessPatchRequestDto>> violations = validator.validate(request);

        assertTrue(hasFieldViolation(violations, "phone"));
    }

    private boolean hasFieldViolation(Set<ConstraintViolation<BusinessPatchRequestDto>> violations, String field) {
        return violations.stream().anyMatch(violation -> field.equals(violation.getPropertyPath().toString()));
    }

    private boolean hasFieldMessage(
        Set<ConstraintViolation<BusinessPatchRequestDto>> violations,
        String field,
        String message
    ) {
        return violations.stream()
            .filter(violation -> field.equals(violation.getPropertyPath().toString()))
            .map(ConstraintViolation::getMessage)
            .anyMatch(message::equals);
    }
}
