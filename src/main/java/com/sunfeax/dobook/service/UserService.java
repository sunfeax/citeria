package com.sunfeax.dobook.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunfeax.dobook.dto.user.UserRegisterRequestDto;
import com.sunfeax.dobook.dto.user.UserResponseDto;
import com.sunfeax.dobook.entity.UserEntity;
import com.sunfeax.dobook.exception.UserAlreadyExistsException;
import com.sunfeax.dobook.mapper.UserMapper;
import com.sunfeax.dobook.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

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

        UserEntity entity = userMapper.createEntity(request);
        UserEntity saved = userRepository.save(entity);

        return userMapper.toResponseDto(saved);
    }
}
