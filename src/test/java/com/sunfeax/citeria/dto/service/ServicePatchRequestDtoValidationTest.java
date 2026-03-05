package com.sunfeax.citeria.dto.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ServicePatchRequestDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void emptyPatchShouldPassValidation() {
        ServicePatchRequestDto request = new ServicePatchRequestDto(null, null, null, null, null, null);

        Set<ConstraintViolation<ServicePatchRequestDto>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void blankNameShouldFailValidation() {
        ServicePatchRequestDto request = new ServicePatchRequestDto(null, "   ", null, null, null, null);

        Set<ConstraintViolation<ServicePatchRequestDto>> violations = validator.validate(request);

        assertTrue(hasFieldMessage(violations, "name", "Service name must not be empty"));
    }

    @Test
    void durationBelowMinimumShouldFailValidation() {
        ServicePatchRequestDto request = new ServicePatchRequestDto(null, null, null, 10, null, null);

        Set<ConstraintViolation<ServicePatchRequestDto>> violations = validator.validate(request);

        assertTrue(hasFieldMessage(violations, "durationMinutes", "Duration must be at least 15 minutes"));
    }

    @Test
    void durationAboveMaximumShouldFailValidation() {
        ServicePatchRequestDto request = new ServicePatchRequestDto(null, null, null, 600, null, null);

        Set<ConstraintViolation<ServicePatchRequestDto>> violations = validator.validate(request);

        assertTrue(hasFieldMessage(violations, "durationMinutes", "Duration must not exceed 480 minutes"));
    }

    @Test
    void negativePriceShouldFailValidation() {
        ServicePatchRequestDto request = new ServicePatchRequestDto(
            null,
            null,
            null,
            null,
            BigDecimal.valueOf(-1),
            null
        );

        Set<ConstraintViolation<ServicePatchRequestDto>> violations = validator.validate(request);

        assertTrue(hasFieldMessage(violations, "priceAmount", "Price must be non-negative"));
    }

    @Test
    void invalidCurrencyShouldFailValidation() {
        ServicePatchRequestDto request = new ServicePatchRequestDto(null, null, null, null, null, "US");

        Set<ConstraintViolation<ServicePatchRequestDto>> violations = validator.validate(request);

        assertTrue(hasFieldMessage(violations, "currency", "Currency must be a 3-letter code"));
    }

    private boolean hasFieldMessage(
        Set<ConstraintViolation<ServicePatchRequestDto>> violations,
        String field,
        String message
    ) {
        return violations.stream()
            .filter(violation -> field.equals(violation.getPropertyPath().toString()))
            .map(ConstraintViolation::getMessage)
            .anyMatch(message::equals);
    }
}
