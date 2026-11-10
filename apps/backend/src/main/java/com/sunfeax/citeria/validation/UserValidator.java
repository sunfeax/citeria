package com.sunfeax.citeria.validation;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.auth.RegisterRequestDto;
import com.sunfeax.citeria.dto.user.UserChangePasswordRequestDto;
import com.sunfeax.citeria.dto.user.UserUpdateRequestDto;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.enums.UserType;
import com.sunfeax.citeria.mapper.UserMapper;
import com.sunfeax.citeria.repository.UserRepository;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserValidator {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final Validator beanValidator;

    public void validateRegister(RegisterRequestDto request) {
        collectRegisterErrors(request).throwIfHasErrors();
    }

    public ValidationResult collectRegisterErrors(RegisterRequestDto request) {
        boolean emailTaken = hasText(request.email()) && userRepository.existsByEmail(request.email());
        boolean phoneTaken = hasText(request.phone()) && userRepository.existsByPhone(request.phone());

        return new ValidationResult()
            .addErrorIf(
                emailTaken || phoneTaken,
                "request",
                "We couldn't complete the registration. Please check your details or try logging in."
            );
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public void validateUpdate(UUID id, UserEntity existingEntity, UserUpdateRequestDto request) {
        String targetEmail = request.email() != null ? request.email() : existingEntity.getEmail();
        String targetPhone = request.phone() != null ? request.phone() : existingEntity.getPhone();

        new ValidationResult()
            .addErrorIf(!userMapper.hasAnyPatchField(request), "request", "No fields to update")
            .addErrorIf(
                request.email() != null && userRepository.existsByEmailAndIdNot(targetEmail, id),
                "email",
                "Email " + targetEmail + " is already taken"
            )
            .addErrorIf(
                request.phone() != null && userRepository.existsByPhoneAndIdNot(targetPhone, id),
                "phone",
                "Phone " + targetPhone + " is already busy"
            )
            .addErrorIf(
                request.about() != null && !request.about().isEmpty()
                    && existingEntity.getType() != UserType.SPECIALIST,
                "about",
                "Only specialists can have an About text"
            )
            .throwIfHasErrors();
    }

    public void validatePasswordChange(UserChangePasswordRequestDto request, UserEntity user) {
        boolean hasPassword = user.getPassword() != null;
        boolean currentPasswordBlank = request.currentPassword() == null || request.currentPassword().isBlank();

        if (hasPassword) {
            new ValidationResult()
                .addErrorIf(currentPasswordBlank, "currentPassword", "Current password is required")
                .throwIfHasErrors();

            new ValidationResult()
                .addErrorIf(
                    !passwordEncoder.matches(request.currentPassword(), user.getPassword()),
                    "currentPassword",
                    "Current password is incorrect."
                )
                .throwIfHasErrors();
        }

        collectPropertyErrors(request, "newPassword")
            .addErrorIf(
                hasPassword && request.newPassword() != null && request.newPassword().equals(request.currentPassword()),
                "newPassword",
                "The new password must be different."
            )
            .throwIfHasErrors();
    }

    private ValidationResult collectPropertyErrors(Object request, String property) {
        ValidationResult result = new ValidationResult();

        beanValidator.validateProperty(request, property).forEach(violation ->
            result.addError(resolveConstraintField(violation), violation.getMessage())
        );

        return result;
    }

    private String resolveConstraintField(ConstraintViolation<?> violation) {
        String propertyPath = violation.getPropertyPath().toString();
        int lastDot = propertyPath.lastIndexOf('.');
        return lastDot >= 0 ? propertyPath.substring(lastDot + 1) : propertyPath;
    }
}
