package com.sunfeax.citeria.dto.appointment;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
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
    void nullClientIdShouldFailValidation() {
        AppointmentPostRequestDto request = new AppointmentPostRequestDto(
            null,
            100L,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(1).plusMinutes(60),
            PaymentMethod.ONLINE
        );

        Set<ConstraintViolation<AppointmentPostRequestDto>> violations = validator.validate(request);

        assertTrue(hasFieldMessage(violations, "clientId", "Client id is required"));
    }

    @Test
    void nullSpecialistServiceIdShouldFailValidation() {
        AppointmentPostRequestDto request = new AppointmentPostRequestDto(
            10L,
            null,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(1).plusMinutes(60),
            PaymentMethod.ONLINE
        );

        Set<ConstraintViolation<AppointmentPostRequestDto>> violations = validator.validate(request);

        assertTrue(hasFieldMessage(violations, "specialistServiceId", "Specialist service id is required"));
    }

    @Test
    void nullStartTimeShouldFailValidation() {
        AppointmentPostRequestDto request = new AppointmentPostRequestDto(
            10L,
            100L,
            null,
            LocalDateTime.now().plusDays(1).plusMinutes(60),
            PaymentMethod.ONLINE
        );

        Set<ConstraintViolation<AppointmentPostRequestDto>> violations = validator.validate(request);

        assertTrue(hasFieldMessage(violations, "startTime", "Start time is required"));
    }

    @Test
    void nullPaymentMethodShouldFailValidation() {
        AppointmentPostRequestDto request = new AppointmentPostRequestDto(
            10L,
            100L,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(1).plusMinutes(60),
            null
        );

        Set<ConstraintViolation<AppointmentPostRequestDto>> violations = validator.validate(request);

        assertTrue(hasFieldMessage(violations, "paymentMethod", "Payment method is required"));
    }

    private AppointmentPostRequestDto validRequest() {
        return new AppointmentPostRequestDto(
            10L,
            100L,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(1).plusMinutes(60),
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
