package com.sunfeax.citeria.validation;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.user.UserChangePasswordRequestDto;
import com.sunfeax.citeria.dto.user.UserPatchRequestDto;
import com.sunfeax.citeria.dto.user.UserPostRequestDto;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.mapper.UserMapper;
import com.sunfeax.citeria.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserValidator {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public void validateRegister(UserPostRequestDto request) {
        new ValidationResult()
            .addErrorIf(
                userRepository.existsByEmail(request.email()),
                "email",
                "Email " + request.email() + " is already taken"
            )
            .addErrorIf(
                userRepository.existsByPhone(request.phone()),
                "phone",
                "Phone " + request.phone() + " is already busy"
            )
            .throwIfHasErrors();
    }

    public void validateUpdate(Long id, UserEntity existingEntity, UserPatchRequestDto request) {
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
