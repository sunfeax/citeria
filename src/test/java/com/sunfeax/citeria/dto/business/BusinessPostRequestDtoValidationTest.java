package com.sunfeax.citeria.dto.business;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class BusinessPostRequestDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validRequestShouldPassValidation() {
        BusinessPostRequestDto request = validRequest();

        Set<ConstraintViolation<BusinessPostRequestDto>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void blankNameShouldFailValidation() {
        BusinessPostRequestDto request = new BusinessPostRequestDto(
            1L,
            "   ",
            "description",
            "1234567",
            "mail@example.com",
            "https://site.com",
            "Address"
        );

        Set<ConstraintViolation<BusinessPostRequestDto>> violations = validator.validate(request);

        assertTrue(hasFieldMessage(violations, "name", "Business name is required"));
    }

    @Test
    void shortNameShouldFailValidation() {
        BusinessPostRequestDto request = new BusinessPostRequestDto(
            1L,
            "A",
            "description",
            "1234567",
            "mail@example.com",
            "https://site.com",
            "Address"
        );

        Set<ConstraintViolation<BusinessPostRequestDto>> violations = validator.validate(request);

        assertTrue(hasFieldViolation(violations, "name"));
    }

    @Test
    void tooLongDescriptionShouldFailValidation() {
        String tooLongDescription = "a".repeat(1001);
        BusinessPostRequestDto request = new BusinessPostRequestDto(
            1L,
            "Alpha Studio",
            tooLongDescription,
            "1234567",
            "mail@example.com",
            "https://site.com",
            "Address"
        );

        Set<ConstraintViolation<BusinessPostRequestDto>> violations = validator.validate(request);

        assertTrue(hasFieldViolation(violations, "description"));
    }

    @Test
    void invalidEmailShouldFailValidation() {
        BusinessPostRequestDto request = new BusinessPostRequestDto(
            1L,
            "Alpha Studio",
            "description",
            "1234567",
            "invalid-email",
            "https://site.com",
            "Address"
        );

        Set<ConstraintViolation<BusinessPostRequestDto>> violations = validator.validate(request);

        assertTrue(hasFieldMessage(violations, "email", "Invalid email format"));
    }

    @Test
    void shortPhoneShouldFailValidation() {
        BusinessPostRequestDto request = new BusinessPostRequestDto(
            1L,
            "Alpha Studio",
            "description",
            "123",
            "mail@example.com",
            "https://site.com",
            "Address"
        );

        Set<ConstraintViolation<BusinessPostRequestDto>> violations = validator.validate(request);

        assertTrue(hasFieldViolation(violations, "phone"));
    }

    private BusinessPostRequestDto validRequest() {
        return new BusinessPostRequestDto(
            1L,
            "Alpha Studio",
            "description",
            "1234567890",
            "mail@example.com",
            "https://site.com",
            "Address"
        );
    }

    private boolean hasFieldViolation(Set<ConstraintViolation<BusinessPostRequestDto>> violations, String field) {
        return violations.stream().anyMatch(violation -> field.equals(violation.getPropertyPath().toString()));
    }

    private boolean hasFieldMessage(
        Set<ConstraintViolation<BusinessPostRequestDto>> violations,
        String field,
        String message
    ) {
        return violations.stream()
            .filter(violation -> field.equals(violation.getPropertyPath().toString()))
            .map(ConstraintViolation::getMessage)
            .anyMatch(message::equals);
    }
}
