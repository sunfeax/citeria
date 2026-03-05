package com.sunfeax.citeria.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import com.sunfeax.citeria.normalizer.UserFieldNormalizer;
import com.sunfeax.citeria.repository.UserRepository;
import com.sunfeax.citeria.validation.UserValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserFieldNormalizer userFieldNormalizer;
    private final PasswordEncoder passwordEncoder;
    private final UserValidator userValidator;

    @Transactional(readOnly = true)
    public Page<UserResponseDto> getAll(Pageable pageable) {
        Page<UserEntity> userPage = userRepository.findAll(pageable);
        return userPage.map(userMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getById(Long id) {
        return userMapper.toResponseDto(findUserOrThrow(id));
    }

    @Transactional
    public UserResponseDto register(UserPostRequestDto request) {
        UserPostRequestDto normalizedRequest = userFieldNormalizer.normalizePostRequest(request);
        userValidator.validateRegister(normalizedRequest);

        UserEntity entity = userMapper.createEntity(normalizedRequest);
        entity.setPassword(passwordEncoder.encode(request.password()));
        UserEntity saved = userRepository.save(entity);

        return userMapper.toResponseDto(saved);
    }

    @Transactional
    public UserResponseDto update(Long id, UserPatchRequestDto request) {
        UserEntity entity = findUserOrThrow(id);

        UserPatchRequestDto normalizedRequest = userFieldNormalizer.normalizePatchRequest(request);
        userValidator.validateUpdate(id, normalizedRequest);

        userMapper.applyPatch(entity, normalizedRequest);
        UserEntity saved = userRepository.save(entity);

        return userMapper.toResponseDto(saved);
    }

    @Transactional
    public void changePassword(Long id, UserChangePasswordRequestDto request) {
        UserEntity user = findUserOrThrow(id);
        userValidator.validatePasswordChange(request, user);

        user.setPassword(passwordEncoder.encode(request.newPassword()));
    }

    @Transactional
    public UserResponseDto deactivateById(Long id) {
        UserEntity user = findUserOrThrow(id);
            
        user.setActive(false);
        UserEntity saved = userRepository.save(user);

        return userMapper.toResponseDto(saved);
    }

    @Transactional
    public UserResponseDto hardDeleteById(Long id) {
        UserEntity user = findUserOrThrow(id);

        UserResponseDto deletedUser = userMapper.toResponseDto(user);
        userRepository.delete(user);

        return deletedUser;
    }

    @Transactional
    public UserResponseDto restoreById(Long id) {
        UserEntity user = findUserOrThrow(id);

        user.setActive(true);
        UserEntity saved = userRepository.save(user);

        return userMapper.toResponseDto(saved);
    }

    private UserEntity findUserOrThrow(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
    }
}
