package com.sunfeax.citeria.validation;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.auth.RegisterRequestDto;
import com.sunfeax.citeria.dto.user.UserChangePasswordRequestDto;
import com.sunfeax.citeria.dto.user.UserUpdateRequestDto;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.repository.UserRepository;
import com.sunfeax.citeria.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserValidator {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public void validateRegister(RegisterRequestDto request) {
        collectRegisterErrors(request).throwIfHasErrors();
    }

    public ValidationResult collectRegisterErrors(RegisterRequestDto request) {
        return new ValidationResult()
            .addErrorIf(
                hasText(request.email()) && userRepository.existsByEmail(request.email()),
                "email",
                "Email " + request.email() + " is already taken"
            )
            .addErrorIf(
                hasText(request.phone()) && userRepository.existsByPhone(request.phone()),
                "phone",
                "Phone " + request.phone() + " is already busy"
            );
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public void validateUpdate(Long id, UserEntity existingEntity, UserUpdateRequestDto request) {
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
            .throwIfHasErrors();
    }

    public void validatePasswordChange(UserChangePasswordRequestDto request, UserEntity user) {
        new ValidationResult()
            .addErrorIf(
                !passwordEncoder.matches(request.currentPassword(), user.getPassword()),
                "currentPassword",
                "Current password is incorrect."
            )
            .addErrorIf(
                request.currentPassword().equals(request.newPassword()),
                "newPassword",
                "The new password must be different."
            )
            .throwIfHasErrors();
    }
}
