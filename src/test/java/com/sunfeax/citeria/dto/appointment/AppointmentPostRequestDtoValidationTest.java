package com.sunfeax.citeria.dto.appointment;

import java.time.Duration;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Set;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

class AppointmentPostRequestDtoValidationTest {

    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validRequestShouldPassValidation() {
        AppointmentPostRequestDto request = validRequest();

        Set<ConstraintViolation<AppointmentPostRequestDto>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void nullServiceIdShouldFailValidation() {
        AppointmentPostRequestDto request = new AppointmentPostRequestDto(
            null,
            Instant.now().plus(Duration.ofDays(1))
        );

        Set<ConstraintViolation<AppointmentPostRequestDto>> violations = validator.validate(request);

        assertTrue(hasFieldMessage(violations, "serviceId", "Service id is required"));
    }

    @Test
    void nullStartTimeShouldFailValidation() {
        AppointmentPostRequestDto request = new AppointmentPostRequestDto(
            new UUID(0, 100L),
            null
        );

        Set<ConstraintViolation<AppointmentPostRequestDto>> violations = validator.validate(request);

        assertTrue(hasFieldMessage(violations, "startTime", "Start time is required"));
    }

    private AppointmentPostRequestDto validRequest() {
        return new AppointmentPostRequestDto(
            new UUID(0, 100L),
            Instant.now().plus(Duration.ofDays(1))
        );
    }

    private boolean hasFieldMessage(
        Set<ConstraintViolation<AppointmentPostRequestDto>> violations,
        String field,
        String message
    ) {
        return violations.stream()
            .filter(violation -> field.equals(violation.getPropertyPath().toString()))
            .map(ConstraintViolation::getMessage)
            .anyMatch(message::equals);
    }
}
