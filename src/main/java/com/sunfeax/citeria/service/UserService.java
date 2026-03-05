package com.sunfeax.citeria.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunfeax.citeria.dto.user.UserChangePasswordRequestDto;
import com.sunfeax.citeria.dto.user.UserPatchRequestDto;
import com.sunfeax.citeria.dto.user.UserPostRequestDto;
import com.sunfeax.citeria.dto.user.UserResponseDto;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.exception.ResourceNotFoundException;
import com.sunfeax.citeria.mapper.UserMapper;
import com.sunfeax.citeria.repository.UserRepository;
import com.sunfeax.citeria.validation.UserFieldNormalizer;
import com.sunfeax.citeria.validation.ValidationResult;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserFieldNormalizer userFieldNormalizer;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserResponseDto> getAll() {
        return userRepository.findAll()
            .stream()
            .map(userMapper::toResponseDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public UserResponseDto getById(Long id) {
        return userRepository.findById(id)
            .map(userMapper::toResponseDto)
            .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
    }

    @Transactional
    public UserResponseDto register(UserPostRequestDto request) {
        UserPostRequestDto normalizedRequest = userFieldNormalizer.normalizePostRequest(request);

        new ValidationResult()
            .addErrorIf(
                userRepository.existsByEmail(normalizedRequest.email()),
                "email",
                "Email " + normalizedRequest.email() + " is already taken"
            )
            .addErrorIf(
                userRepository.existsByPhone(normalizedRequest.phone()),
                "phone",
                "Phone " + normalizedRequest.phone() + " is already busy"
            )
            .throwIfHasErrors();

        UserEntity entity = userMapper.createEntity(normalizedRequest);
        entity.setPassword(passwordEncoder.encode(request.password()));
        UserEntity saved = userRepository.save(entity);

        return userMapper.toResponseDto(saved);
    }

    @Transactional
    public UserResponseDto deactivateById(Long id) {
        UserEntity user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));

        user.setActive(false);
        UserEntity saved = userRepository.save(user);

        return userMapper.toResponseDto(saved);
    }

    @Transactional
    public UserResponseDto hardDeleteById(Long id) {
        UserEntity user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));

        UserResponseDto deletedUser = userMapper.toResponseDto(user);
        userRepository.delete(user);

        return deletedUser;
    }

    @Transactional
    public UserResponseDto update(Long id, UserPatchRequestDto request) {
        UserEntity entity = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));

        UserPatchRequestDto normalizedRequest = userFieldNormalizer.normalizePatchRequest(request);

        new ValidationResult()
            .addErrorIf(!userMapper.hasAnyPatchField(normalizedRequest), "request", "No fields to update")            .addErrorIf(
                normalizedRequest.email() != null
                    && userRepository.existsByEmailAndIdNot(normalizedRequest.email(), id),
                "email",
                "Email " + normalizedRequest.email() + " is already taken"
            )
            .addErrorIf(
                normalizedRequest.phone() != null
                    && userRepository.existsByPhoneAndIdNot(normalizedRequest.phone(), id),
                "phone",
                "Phone " + normalizedRequest.phone() + " is already busy"
            )
            .throwIfHasErrors();

        userMapper.applyPatch(entity, normalizedRequest);
        UserEntity saved = userRepository.save(entity);

        return userMapper.toResponseDto(saved);
    }

    @Transactional
    public void changePassword(Long id, UserChangePasswordRequestDto request) {
        UserEntity user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        new ValidationResult()
            .addErrorIf(!passwordEncoder.matches(request.currentPassword(), user.getPassword()), 
                        "currentPassword", "Current password is incorrect.")
            .addErrorIf(request.currentPassword().equals(request.newPassword()), 
                        "newPassword", "The new password must be different.")
            .throwIfHasErrors();

        user.setPassword(passwordEncoder.encode(request.newPassword()));
    }
}

