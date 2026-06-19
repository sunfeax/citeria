package com.sunfeax.citeria.dto.specialistservice;

import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

class SpecialistServicePostRequestDtoValidationTest {

    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validRequestShouldPassValidation() {
        SpecialistServicePostRequestDto request = new SpecialistServicePostRequestDto(new UUID(0, 10L), new UUID(0, 20L), new UUID(0, 30L));

        Set<ConstraintViolation<SpecialistServicePostRequestDto>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void nullBusinessIdShouldFailValidation() {
        SpecialistServicePostRequestDto request = new SpecialistServicePostRequestDto(null, new UUID(0, 20L), new UUID(0, 30L));

        Set<ConstraintViolation<SpecialistServicePostRequestDto>> violations = validator.validate(request);

        assertTrue(hasFieldMessage(violations, "businessId", "Business id is required"));
    }

    @Test
    void nullSpecialistIdShouldFailValidation() {
        SpecialistServicePostRequestDto request = new SpecialistServicePostRequestDto(new UUID(0, 10L), null, new UUID(0, 30L));

        Set<ConstraintViolation<SpecialistServicePostRequestDto>> violations = validator.validate(request);

        assertTrue(hasFieldMessage(violations, "specialistId", "Specialist id is required"));
    }

    @Test
    void nullServiceIdShouldFailValidation() {
        SpecialistServicePostRequestDto request = new SpecialistServicePostRequestDto(new UUID(0, 10L), new UUID(0, 20L), null);

        Set<ConstraintViolation<SpecialistServicePostRequestDto>> violations = validator.validate(request);

        assertTrue(hasFieldMessage(violations, "serviceId", "Service id is required"));
    }

    private boolean hasFieldMessage(
        Set<ConstraintViolation<SpecialistServicePostRequestDto>> violations,
        String field,
        String message
    ) {
        return violations.stream()
            .filter(violation -> field.equals(violation.getPropertyPath().toString()))
            .map(ConstraintViolation::getMessage)
            .anyMatch(message::equals);
    }
}
