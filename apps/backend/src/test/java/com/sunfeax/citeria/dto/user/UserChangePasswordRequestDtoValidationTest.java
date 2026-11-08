package com.sunfeax.citeria.dto.user;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;

class UserChangePasswordRequestDtoValidationTest {

    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validRequestShouldPassValidation() {
        UserChangePasswordRequestDto request = new UserChangePasswordRequestDto("OldPassword!", "Password@2");

        Set<ConstraintViolation<UserChangePasswordRequestDto>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void tooShortPasswordShouldFailValidation() {
        Set<ConstraintViolation<UserChangePasswordRequestDto>> violations = validateNewPassword("Short!");

        assertTrue(hasNewPasswordMessage(violations, "Password must be at least 8 characters"));
    }

    @Test
    void passwordWithoutUppercaseShouldFailValidation() {
        Set<ConstraintViolation<UserChangePasswordRequestDto>> violations = validateNewPassword("newpassword1!");

        assertTrue(hasNewPasswordMessage(violations, "Password must contain at least one uppercase letter"));
    }

    @Test
    void passwordWithSpacesShouldFailValidation() {
        Set<ConstraintViolation<UserChangePasswordRequestDto>> violations = validateNewPassword("New Password1!");

        assertTrue(hasNewPasswordMessage(violations, "Password must not contain spaces"));
    }

    @Test
    void passwordWithoutDigitShouldFailValidation() {
        Set<ConstraintViolation<UserChangePasswordRequestDto>> violations = validateNewPassword("NewPassword!");

        assertTrue(hasNewPasswordMessage(violations, "Password must contain at least one digit"));
    }

    private Set<ConstraintViolation<UserChangePasswordRequestDto>> validateNewPassword(String newPassword) {
        UserChangePasswordRequestDto request = new UserChangePasswordRequestDto("OldPassword!", newPassword);
        return validator.validate(request);
    }

    private boolean hasNewPasswordMessage(
        Set<ConstraintViolation<UserChangePasswordRequestDto>> violations,
        String message
    ) {
        return violations.stream()
            .filter(violation -> "newPassword".equals(violation.getPropertyPath().toString()))
            .map(ConstraintViolation::getMessage)
            .anyMatch(message::equals);
    }
}
