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
import com.sunfeax.citeria.exception.InvalidPasswordException;
import com.sunfeax.citeria.exception.ResourceNotFoundException;
import com.sunfeax.citeria.exception.UserAlreadyExistsException;
import com.sunfeax.citeria.mapper.UserMapper;
import com.sunfeax.citeria.normalizer.UserFieldNormalizer;
import com.sunfeax.citeria.repository.UserRepository;

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

        if (userRepository.existsByEmail(normalizedRequest.email())) {
            throw new UserAlreadyExistsException("Email " + normalizedRequest.email() + " is already taken");
        }

        if (userRepository.existsByPhone(normalizedRequest.phone())) {
            throw new UserAlreadyExistsException("Phone " + normalizedRequest.phone() + " is already busy");
        }

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
        UserEntity user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));

        UserPatchRequestDto normalizedRequest = userFieldNormalizer.normalizePatchRequest(request);

        boolean hasChanges = false;

        if (normalizedRequest.firstName() != null) {
            user.setFirstName(normalizedRequest.firstName());
            hasChanges = true;
        }

        if (normalizedRequest.lastName() != null) {
            user.setLastName(normalizedRequest.lastName());
            hasChanges = true;
        }

        if (normalizedRequest.email() != null) {
            if (userRepository.existsByEmailAndIdNot(normalizedRequest.email(), id)) {
                throw new UserAlreadyExistsException("Email " + normalizedRequest.email() + " is already taken");
            }
            user.setEmail(normalizedRequest.email());
            hasChanges = true;
        }

        if (normalizedRequest.phone() != null) {
            if (userRepository.existsByPhoneAndIdNot(normalizedRequest.phone(), id)) {
                throw new UserAlreadyExistsException("Phone " + normalizedRequest.phone() + " is already busy");
            }
            user.setPhone(normalizedRequest.phone());
            hasChanges = true;
        }

        if (normalizedRequest.type() != null) {
            user.setType(normalizedRequest.type());
            hasChanges = true;
        }

        if (!hasChanges) {
            throw new IllegalArgumentException("No fields to update");
        }

        UserEntity saved = userRepository.save(user);
        return userMapper.toResponseDto(saved);
    }

    @Transactional
    public void changePassword(Long id, UserChangePasswordRequestDto request) {
        UserEntity user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("The passwords do not match.");
        }

        if (request.currentPassword().equals(request.newPassword())) {
            throw new InvalidPasswordException("The new password must be different.");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
    }
}


