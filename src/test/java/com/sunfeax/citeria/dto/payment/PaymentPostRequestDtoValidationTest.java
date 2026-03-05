package com.sunfeax.citeria.dto.payment;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

class PaymentPostRequestDtoValidationTest {

    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validRequestShouldPassValidation() {
        PaymentPostRequestDto request = new PaymentPostRequestDto(10L);

        Set<ConstraintViolation<PaymentPostRequestDto>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void nullAppointmentIdShouldFailValidation() {
        PaymentPostRequestDto request = new PaymentPostRequestDto(null);

        Set<ConstraintViolation<PaymentPostRequestDto>> violations = validator.validate(request);

        assertTrue(hasFieldMessage(violations, "appointmentId", "Appointment id is required"));
    }

    private boolean hasFieldMessage(
        Set<ConstraintViolation<PaymentPostRequestDto>> violations,
        String field,
        String message
    ) {
        return violations.stream()
            .filter(violation -> field.equals(violation.getPropertyPath().toString()))
            .map(ConstraintViolation::getMessage)
            .anyMatch(message::equals);
    }
}
