package com.sunfeax.citeria.dto.payment;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

class PaymentPatchRequestDtoValidationTest {

    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void emptyPatchShouldPassValidation() {
        PaymentPatchRequestDto request = new PaymentPatchRequestDto(null, null);

        Set<ConstraintViolation<PaymentPatchRequestDto>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }
}
