package com.sunfeax.citeria.dto.specialistservice;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

class SpecialistServicePatchRequestDtoValidationTest {

    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void emptyPatchShouldPassValidation() {
        SpecialistServicePatchRequestDto request = new SpecialistServicePatchRequestDto(null, null, null);

        Set<ConstraintViolation<SpecialistServicePatchRequestDto>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }
}
