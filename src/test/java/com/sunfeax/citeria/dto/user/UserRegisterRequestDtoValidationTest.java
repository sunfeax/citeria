package com.sunfeax.citeria.dto.user;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import com.sunfeax.citeria.enums.UserType;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class UserRegisterRequestDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validPasswordShouldPassValidation() {
        Set<ConstraintViolation<UserRegisterRequestDto>> violations = validatePassword("Password!");

        assertTrue(violations.isEmpty());
    }

    @Test
    void passwordShorterThanEightShouldFailValidation() {
        Set<ConstraintViolation<UserRegisterRequestDto>> violations = validatePassword("Pass!");

        assertTrue(hasPasswordMessage(violations, "Password must be at least 8 characters"));
    }

    @Test
    void passwordWithoutUppercaseShouldFailValidation() {
        Set<ConstraintViolation<UserRegisterRequestDto>> violations = validatePassword("password!");

        assertTrue(hasPasswordMessage(violations, "Password must contain at least one uppercase letter"));
    }

    @Test
    void passwordWithoutSpecialCharacterShouldFailValidation() {
        Set<ConstraintViolation<UserRegisterRequestDto>> violations = validatePassword("Passworddd");

        assertTrue(hasPasswordMessage(violations, "Password must contain at least one special character"));
    }

    @Test
    void passwordWithSpaceShouldFailValidation() {
        Set<ConstraintViolation<UserRegisterRequestDto>> violations = validatePassword("Password !");

        assertTrue(hasPasswordMessage(violations, "Password must not contain spaces"));
    }

    @Test
    void passwordWithNonLatinCharactersShouldFailValidation() {
        Set<ConstraintViolation<UserRegisterRequestDto>> violations = validatePassword("ПарольA!");

        assertTrue(hasPasswordMessage(
            violations,
            "Password must contain only Latin letters and allowed special characters (@#$%^&+=!)"
        ));
    }

    private Set<ConstraintViolation<UserRegisterRequestDto>> validatePassword(String password) {
        return validator.validate(validRequestWithPassword(password));
    }

    private UserRegisterRequestDto validRequestWithPassword(String password) {
        return new UserRegisterRequestDto(
            "Anna",
            "Smith",
            "anna.smith@example.com",
            "+34123456789",
            password,
            UserType.CLIENT
        );
    }

    private boolean hasPasswordMessage(Set<ConstraintViolation<UserRegisterRequestDto>> violations, String message) {
        return violations.stream()
            .filter(violation -> "password".equals(violation.getPropertyPath().toString()))
            .map(ConstraintViolation::getMessage)
            .anyMatch(message::equals);
    }
}
