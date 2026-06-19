package com.sunfeax.citeria.service;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunfeax.citeria.dto.user.UserChangePasswordRequestDto;
import com.sunfeax.citeria.dto.user.UserResponseDto;
import com.sunfeax.citeria.dto.user.UserUpdateRequestDto;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.exception.ForbiddenException;
import com.sunfeax.citeria.exception.ResourceNotFoundException;
import com.sunfeax.citeria.exception.UnauthorizedException;
import com.sunfeax.citeria.mapper.UserMapper;
import com.sunfeax.citeria.normalizer.UserFieldNormalizer;
import com.sunfeax.citeria.repository.UserRepository;
import com.sunfeax.citeria.security.CurrentUserProvider;
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
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public Page<UserResponseDto> getAll(Pageable pageable) {
        currentUserProvider.requireAdmin();
        Page<UserEntity> userPage = userRepository.findAll(pageable);
        return userPage.map(userMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getById(UUID id) {
        currentUserProvider.requireSelfOrAdmin(id);
        return userMapper.toResponseDto(findUserOrThrow(id));
    }

    @Transactional(readOnly = true)
    public UserResponseDto getMe(String email) {
        UserEntity user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedException("User not found"));

        return userMapper.toResponseDto(user);
    }

    @Transactional
    public UserResponseDto update(UUID id, UserUpdateRequestDto request) {
        currentUserProvider.requireSelfOrAdmin(id);
        UserEntity entity = findUserOrThrow(id);

        UserUpdateRequestDto normalizedRequest = userFieldNormalizer.normalizePatchRequest(request);
        userValidator.validateUpdate(id, entity, normalizedRequest);

        userMapper.applyPatch(entity, normalizedRequest);
        UserEntity saved = userRepository.save(entity);

        return userMapper.toResponseDto(saved);
    }

    @Transactional
    public void changePassword(UUID id, UserChangePasswordRequestDto request) {
        UserEntity current = currentUserProvider.getCurrentUser();
        if (!current.getId().equals(id)) {
            throw new ForbiddenException("You can only change your own password");
        }

        UserEntity user = findUserOrThrow(id);
        userValidator.validatePasswordChange(request, user);

        user.setPassword(passwordEncoder.encode(request.newPassword()));
    }

    @Transactional
    public UserResponseDto deactivateById(UUID id) {
        currentUserProvider.requireSelfOrAdmin(id);
        UserEntity user = findUserOrThrow(id);

        user.setActive(false);
        UserEntity saved = userRepository.save(user);

        return userMapper.toResponseDto(saved);
    }

    @Transactional
    public UserResponseDto hardDeleteById(UUID id) {
        currentUserProvider.requireAdmin();
        UserEntity user = findUserOrThrow(id);

        UserResponseDto deletedUser = userMapper.toResponseDto(user);
        userRepository.delete(user);

        return deletedUser;
    }

    @Transactional
    public UserResponseDto restoreById(UUID id) {
        currentUserProvider.requireAdmin();
        UserEntity user = findUserOrThrow(id);

        user.setActive(true);
        UserEntity saved = userRepository.save(user);

        return userMapper.toResponseDto(saved);
    }

    private UserEntity findUserOrThrow(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
    }
}
