package com.sunfeax.citeria.dto.appointment;

import java.time.Duration;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.sunfeax.citeria.enums.PaymentMethod;

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
    void nullSpecialistServiceIdShouldFailValidation() {
        AppointmentPostRequestDto request = new AppointmentPostRequestDto(
            null,
            Instant.now().plus(Duration.ofDays(1)),
            Instant.now().plus(Duration.ofDays(1)).plus(Duration.ofMinutes(60)),
            PaymentMethod.ONLINE
        );

        Set<ConstraintViolation<AppointmentPostRequestDto>> violations = validator.validate(request);

        assertTrue(hasFieldMessage(violations, "specialistServiceId", "Specialist service id is required"));
    }

    @Test
    void nullStartTimeShouldFailValidation() {
        AppointmentPostRequestDto request = new AppointmentPostRequestDto(
            new UUID(0, 100L),
            null,
            Instant.now().plus(Duration.ofDays(1)).plus(Duration.ofMinutes(60)),
            PaymentMethod.ONLINE
        );

        Set<ConstraintViolation<AppointmentPostRequestDto>> violations = validator.validate(request);

        assertTrue(hasFieldMessage(violations, "startTime", "Start time is required"));
    }

    @Test
    void nullPaymentMethodShouldFailValidation() {
        AppointmentPostRequestDto request = new AppointmentPostRequestDto(
            new UUID(0, 100L),
            Instant.now().plus(Duration.ofDays(1)),
            Instant.now().plus(Duration.ofDays(1)).plus(Duration.ofMinutes(60)),
            null
        );

        Set<ConstraintViolation<AppointmentPostRequestDto>> violations = validator.validate(request);

        assertTrue(hasFieldMessage(violations, "paymentMethod", "Payment method is required"));
    }

    private AppointmentPostRequestDto validRequest() {
        return new AppointmentPostRequestDto(
            new UUID(0, 100L),
            Instant.now().plus(Duration.ofDays(1)),
            Instant.now().plus(Duration.ofDays(1)).plus(Duration.ofMinutes(60)),
            PaymentMethod.ONLINE
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
