package com.sunfeax.citeria.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.sunfeax.citeria.dto.auth.LoginRequestDto;
import com.sunfeax.citeria.dto.auth.LoginResponseDto;
import com.sunfeax.citeria.dto.auth.RegisterRequestDto;
import com.sunfeax.citeria.dto.user.UserResponseDto;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.enums.UserRole;
import com.sunfeax.citeria.enums.UserType;
import com.sunfeax.citeria.exception.RequestValidationException;
import com.sunfeax.citeria.mapper.UserMapper;
import com.sunfeax.citeria.normalizer.UserFieldNormalizer;
import com.sunfeax.citeria.repository.UserRepository;
import com.sunfeax.citeria.util.JwtProvider;
import com.sunfeax.citeria.validation.UserValidator;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private UserFieldNormalizer userFieldNormalizer;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtProvider jwtProvider;

    private UserValidator userValidator;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userValidator = new UserValidator(userRepository, userMapper, passwordEncoder);
        authService = new AuthService(
            userRepository,
            userMapper,
            userFieldNormalizer,
            userValidator,
            passwordEncoder,
            jwtProvider
        );
    }

    @Test
    void registerShouldThrowWhenEmailAlreadyExists() {
        RegisterRequestDto request = registerRequest();

        when(userFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);
        when(userRepository.existsByPhone("1234567")).thenReturn(false);

        assertThrows(RequestValidationException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void registerShouldThrowWhenPhoneAlreadyExists() {
        RegisterRequestDto request = registerRequest();

        when(userFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("1234567")).thenReturn(true);

        assertThrows(RequestValidationException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void registerShouldEncodePasswordAndSave() {
        RegisterRequestDto request = registerRequest();
        UserEntity entity = userEntity(1L);
        UserResponseDto dto = userResponseDto(1L);

        when(userFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("1234567")).thenReturn(false);
        when(userMapper.createEntity(request)).thenReturn(entity);
        when(passwordEncoder.encode("Password!")).thenReturn("encoded");
        when(userRepository.save(entity)).thenReturn(entity);
        when(userMapper.toResponseDto(entity)).thenReturn(dto);

        UserResponseDto result = authService.register(request);

        assertEquals(dto, result);
        assertEquals("encoded", entity.getPassword());
    }

    @Test
    void loginShouldThrowWhenEmailNotFound() {
        LoginRequestDto request = new LoginRequestDto("john@example.com", "Password!");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
        verify(jwtProvider, never()).generateToken(any(UserEntity.class));
    }

    @Test
    void loginShouldThrowWhenPasswordIsInvalid() {
        LoginRequestDto request = new LoginRequestDto("john@example.com", "WrongPassword!");
        UserEntity entity = userEntity(1L);
        entity.setPassword("encoded-pass");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(entity));
        when(passwordEncoder.matches("WrongPassword!", "encoded-pass")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
        verify(jwtProvider, never()).generateToken(any(UserEntity.class));
    }

    @Test
    void loginShouldReturnTokenWhenCredentialsAreValid() {
        LoginRequestDto request = new LoginRequestDto("john@example.com", "Password!");
        UserEntity entity = userEntity(1L);
        entity.setPassword("encoded-pass");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(entity));
        when(passwordEncoder.matches("Password!", "encoded-pass")).thenReturn(true);
        when(jwtProvider.generateToken(entity)).thenReturn("jwt-token");

        LoginResponseDto result = authService.login(request);

        assertEquals("jwt-token", result.token());
        assertEquals("Bearer", result.tokenType());
        assertEquals(1L, result.id());
        assertEquals("John Snow", result.fullName());
        assertEquals(UserRole.USER, result.role());
        assertEquals(UserType.CLIENT, result.type());
    }

    private RegisterRequestDto registerRequest() {
        return new RegisterRequestDto(
            "John",
            "Snow",
            "john@example.com",
            "1234567",
            "Password!",
            UserType.CLIENT
        );
    }

    private UserEntity userEntity(Long id) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setFirstName("John");
        user.setLastName("Snow");
        user.setEmail("john@example.com");
        user.setPhone("1234567");
        user.setPassword("encoded-pass");
        user.setRole(UserRole.USER);
        user.setType(UserType.CLIENT);
        user.setActive(true);
        return user;
    }

    private UserResponseDto userResponseDto(Long id) {
        return new UserResponseDto(
            id,
            "John",
            "Snow",
            "john@example.com",
            "1234567",
            UserRole.USER,
            UserType.CLIENT,
            true,
            LocalDateTime.of(2026, 1, 1, 12, 0)
        );
    }
}
