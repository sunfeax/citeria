package com.sunfeax.citeria.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunfeax.citeria.dto.auth.LoginRequestDto;
import com.sunfeax.citeria.dto.auth.LoginResponseDto;
import com.sunfeax.citeria.dto.auth.RegisterRequestDto;
import com.sunfeax.citeria.dto.user.UserResponseDto;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.mapper.UserMapper;
import com.sunfeax.citeria.normalizer.UserFieldNormalizer;
import com.sunfeax.citeria.repository.UserRepository;
import com.sunfeax.citeria.util.JwtProvider;
import com.sunfeax.citeria.validation.UserValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserFieldNormalizer userFieldNormalizer;
    private final UserValidator userValidator;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public UserResponseDto register(RegisterRequestDto request) {
        RegisterRequestDto normalizedRequest = userFieldNormalizer.normalizePostRequest(request);
        userValidator.validateRegister(normalizedRequest);

        UserEntity entity = userMapper.createEntity(normalizedRequest);
        entity.setPassword(passwordEncoder.encode(request.password()));
        UserEntity saved = userRepository.save(entity);

        return userMapper.toResponseDto(saved);
    }

    @Transactional
    public LoginResponseDto login(LoginRequestDto request) {
        UserEntity user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtProvider.generateToken(user);

        return new LoginResponseDto(
            token,
            "Bearer",
            user.getId(),
            user.getFirstName() + " " + user.getLastName(),
            user.getRole(),
            user.getType()
        );
    }
}
