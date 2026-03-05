package com.sunfeax.citeria.dto.appointment;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

class AppointmentPatchRequestDtoValidationTest {

    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void emptyPatchShouldPassValidation() {
        AppointmentPatchRequestDto request = new AppointmentPatchRequestDto(null, null, null, null, null, null);

        Set<ConstraintViolation<AppointmentPatchRequestDto>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }
}
