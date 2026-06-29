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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.sunfeax.citeria.dto.common.PageResponseDto;
import com.sunfeax.citeria.enums.UserRole;
import com.sunfeax.citeria.enums.UserType;
import com.sunfeax.citeria.util.PageableUtil;

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

    private static final Set<String> SORTABLE = Set.of("createdAt", "lastName", "email");
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");

    @Transactional(readOnly = true)
    public PageResponseDto<UserResponseDto> list(
        UserRole role,
        UserType type,
        Boolean active,
        String search,
        Pageable pageable
    ) {
        currentUserProvider.requireAdmin();

        List<Specification<UserEntity>> specs = new ArrayList<>();
        if (role != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("role"), role));
        }
        if (type != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("type"), type));
        }
        if (active != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("isActive"), active));
        }
        if (StringUtils.hasText(search)) {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            specs.add((root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("firstName")), pattern),
                cb.like(cb.lower(root.get("lastName")), pattern),
                cb.like(cb.lower(root.get("email")), pattern)
            ));
        }

        Pageable sanitized = PageableUtil.sanitizeSort(pageable, SORTABLE, DEFAULT_SORT);
        Page<UserResponseDto> page = userRepository.findAll(Specification.allOf(specs), sanitized)
            .map(userMapper::toResponseDto);

        return PageResponseDto.from(page);
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
