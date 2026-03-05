package com.sunfeax.citeria.dto.user;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class UserChangePasswordRequestDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validRequestShouldPassValidation() {
        UserChangePasswordRequestDto request = new UserChangePasswordRequestDto("OldPassword!", "NewPassword!");

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
        Set<ConstraintViolation<UserChangePasswordRequestDto>> violations = validateNewPassword("newpassword!");

        assertTrue(hasNewPasswordMessage(violations, "Password must contain at least one uppercase letter"));
    }

    @Test
    void passwordWithSpacesShouldFailValidation() {
        Set<ConstraintViolation<UserChangePasswordRequestDto>> violations = validateNewPassword("New Password!");

        assertTrue(hasNewPasswordMessage(violations, "Password must not contain spaces"));
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
