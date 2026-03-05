package com.sunfeax.citeria.dto.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;

class ServicePostRequestDtoValidationTest {

    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validBoundaryValuesShouldPassValidation() {
        ServicePostRequestDto request = new ServicePostRequestDto(
            1L,
            "Consultation",
            "description",
            15,
            BigDecimal.ZERO,
            "EUR"
        );

        Set<ConstraintViolation<ServicePostRequestDto>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void maxBoundaryDurationShouldPassValidation() {
        ServicePostRequestDto request = new ServicePostRequestDto(
            1L,
            "Consultation",
            "description",
            480,
            BigDecimal.valueOf(100),
            "USD"
        );

        Set<ConstraintViolation<ServicePostRequestDto>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void durationBelowMinimumShouldFailValidation() {
        ServicePostRequestDto request = new ServicePostRequestDto(
            1L,
            "Consultation",
            "description",
            14,
            BigDecimal.valueOf(50),
            "EUR"
        );

        Set<ConstraintViolation<ServicePostRequestDto>> violations = validator.validate(request);

        assertTrue(hasFieldMessage(violations, "durationMinutes", "Duration must be at least 15 minutes"));
    }

    @Test
    void durationAboveMaximumShouldFailValidation() {
        ServicePostRequestDto request = new ServicePostRequestDto(
            1L,
            "Consultation",
            "description",
            481,
            BigDecimal.valueOf(50),
            "EUR"
        );

        Set<ConstraintViolation<ServicePostRequestDto>> violations = validator.validate(request);

        assertTrue(hasFieldMessage(violations, "durationMinutes", "Duration must not exceed 480 minutes"));
    }

    @Test
    void negativePriceShouldFailValidation() {
        ServicePostRequestDto request = new ServicePostRequestDto(
            1L,
            "Consultation",
            "description",
            60,
            BigDecimal.valueOf(-0.01),
            "EUR"
        );

        Set<ConstraintViolation<ServicePostRequestDto>> violations = validator.validate(request);

        assertTrue(hasFieldMessage(violations, "priceAmount", "Price must be non-negative"));
    }

    @Test
    void invalidCurrencyShouldFailValidation() {
        ServicePostRequestDto request = new ServicePostRequestDto(
            1L,
            "Consultation",
            "description",
            60,
            BigDecimal.valueOf(50),
            "EURO"
        );

        Set<ConstraintViolation<ServicePostRequestDto>> violations = validator.validate(request);

        assertTrue(hasFieldMessage(violations, "currency", "Currency must be a 3-letter code"));
    }

    private boolean hasFieldMessage(
        Set<ConstraintViolation<ServicePostRequestDto>> violations,
        String field,
        String message
    ) {
        return violations.stream()
            .filter(violation -> field.equals(violation.getPropertyPath().toString()))
            .map(ConstraintViolation::getMessage)
            .anyMatch(message::equals);
    }
}
