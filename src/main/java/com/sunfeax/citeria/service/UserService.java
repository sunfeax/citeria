package com.sunfeax.citeria.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunfeax.citeria.dto.user.UserRegisterRequestDto;
import com.sunfeax.citeria.dto.user.UserResponseDto;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.exception.UserAlreadyExistsException;
import com.sunfeax.citeria.mapper.UserMapper;
import com.sunfeax.citeria.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserResponseDto> getAll() {
        return userRepository.findAll()
            .stream()
            .map(userMapper::toResponseDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public Optional<UserResponseDto> getById(Long id) {
        return userRepository.findById(id)
            .map(userMapper::toResponseDto);
    }

    @Transactional
    public UserResponseDto register(UserRegisterRequestDto request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("Email " + request.email() + " is already taken");
        }

        UserEntity entity = userMapper.toEntity(request);
        entity.setPassword(passwordEncoder.encode(request.password()));
        UserEntity saved = userRepository.save(entity);

        return userMapper.toResponseDto(saved);
    }
}
