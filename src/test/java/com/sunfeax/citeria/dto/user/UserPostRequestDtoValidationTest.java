package com.sunfeax.citeria.dto.user;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.sunfeax.citeria.enums.UserType;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

class UserPostRequestDtoValidationTest {

    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validPasswordShouldPassValidation() {
        Set<ConstraintViolation<UserPostRequestDto>> violations = validatePassword("Password!");

        assertTrue(violations.isEmpty());
    }

    @Test
    void passwordShorterThanEightShouldFailValidation() {
        Set<ConstraintViolation<UserPostRequestDto>> violations = validatePassword("Pass!");

        assertTrue(hasPasswordMessage(violations, "Password must be at least 8 characters"));
    }

    @Test
    void passwordWithoutUppercaseShouldFailValidation() {
        Set<ConstraintViolation<UserPostRequestDto>> violations = validatePassword("password!");

        assertTrue(hasPasswordMessage(violations, "Password must contain at least one uppercase letter"));
    }

    @Test
    void passwordWithoutSpecialCharacterShouldFailValidation() {
        Set<ConstraintViolation<UserPostRequestDto>> violations = validatePassword("Passworddd");

        assertTrue(hasPasswordMessage(violations, "Password must contain at least one special character"));
    }

    @Test
    void passwordWithSpaceShouldFailValidation() {
        Set<ConstraintViolation<UserPostRequestDto>> violations = validatePassword("Password !");

        assertTrue(hasPasswordMessage(violations, "Password must not contain spaces"));
    }

    @Test
    void passwordWithNonLatinCharactersShouldFailValidation() {
        Set<ConstraintViolation<UserPostRequestDto>> violations = validatePassword("ПарольA!");

        assertTrue(hasPasswordMessage(
            violations,
            "Password must contain only Latin letters and allowed special characters (@#$%^&+=!)"
        ));
    }

    private Set<ConstraintViolation<UserPostRequestDto>> validatePassword(String password) {
        return validator.validate(validRequestWithPassword(password));
    }

    private UserPostRequestDto validRequestWithPassword(String password) {
        return new UserPostRequestDto(
            "Anna",
            "Smith",
            "anna.smith@example.com",
            "+34123456789",
            password,
            UserType.CLIENT
        );
    }

    private boolean hasPasswordMessage(Set<ConstraintViolation<UserPostRequestDto>> violations, String message) {
        return violations.stream()
            .filter(violation -> "password".equals(violation.getPropertyPath().toString()))
            .map(ConstraintViolation::getMessage)
            .anyMatch(message::equals);
    }
}
