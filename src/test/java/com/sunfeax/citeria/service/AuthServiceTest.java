package com.sunfeax.citeria.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.sunfeax.citeria.dto.auth.AuthSessionDto;
import com.sunfeax.citeria.dto.auth.LoginRequestDto;
import com.sunfeax.citeria.dto.auth.RegisterRequestDto;
import com.sunfeax.citeria.dto.user.UserResponseDto;
import com.sunfeax.citeria.entity.RefreshTokenEntity;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.enums.UserRole;
import com.sunfeax.citeria.enums.UserType;
import com.sunfeax.citeria.exception.RequestValidationException;
import com.sunfeax.citeria.exception.UnauthorizedException;
import com.sunfeax.citeria.mapper.UserMapper;
import com.sunfeax.citeria.normalizer.UserFieldNormalizer;
import com.sunfeax.citeria.repository.UserRepository;
import com.sunfeax.citeria.util.JwtProvider;
import com.sunfeax.citeria.validation.UserValidator;

import jakarta.validation.Validator;

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
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private Validator beanValidator;

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
            jwtProvider,
            authenticationManager,
            refreshTokenService,
            beanValidator
        );
    }

    @Test
    void registerShouldThrowWhenEmailAlreadyExists() {
        RegisterRequestDto request = registerRequest();

        when(beanValidator.validate(any(RegisterRequestDto.class))).thenReturn(Collections.emptySet());
        when(userFieldNormalizer.normalizePostRequest(request)).thenReturn(request);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);
        when(userRepository.existsByPhone("1234567")).thenReturn(false);

        assertThrows(RequestValidationException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void registerShouldThrowWhenPhoneAlreadyExists() {
        RegisterRequestDto request = registerRequest();

        when(beanValidator.validate(any(RegisterRequestDto.class))).thenReturn(Collections.emptySet());
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

        when(beanValidator.validate(any(RegisterRequestDto.class))).thenReturn(Collections.emptySet());
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
    void loginShouldReturnAccessAndRefreshTokenWhenCredentialsAreValid() {
        LoginRequestDto request = new LoginRequestDto("john@example.com", "Password!");
        UserEntity entity = userEntity(1L);

        when(userFieldNormalizer.normalizeEmail("john@example.com")).thenReturn("john@example.com");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(new UsernamePasswordAuthenticationToken("john@example.com", null));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(entity));
        when(jwtProvider.generateToken(entity)).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(1L)).thenReturn("refresh-token");

        AuthSessionDto result = authService.login(request);

        assertEquals("access-token", result.response().accessToken());
        assertEquals("Bearer", result.response().tokenType());
        assertEquals(1L, result.response().id());
        assertEquals("John", result.response().firstName());
        assertEquals("Snow", result.response().lastName());
        assertEquals(UserRole.USER, result.response().role());
        assertEquals(UserType.CLIENT, result.response().type());
        assertEquals("refresh-token", result.refreshToken());
    }

    @Test
    void refreshShouldRotateTokenAndReturnNewSession() {
        UserEntity entity = userEntity(1L);
        RefreshTokenEntity storedToken = RefreshTokenEntity.builder()
            .id(11L)
            .tokenHash("old-refresh-hash")
            .user(entity)
            .expiryDate(Instant.now().plusSeconds(3600))
            .build();

        when(refreshTokenService.findByToken("old-refresh")).thenReturn(Optional.of(storedToken));
        when(refreshTokenService.verifyExpiration(storedToken)).thenReturn(storedToken);
        when(jwtProvider.generateToken(entity)).thenReturn("new-access");
        when(refreshTokenService.rotateRefreshToken(storedToken)).thenReturn("new-refresh");

        AuthSessionDto result = authService.refresh("old-refresh");

        assertEquals("new-access", result.response().accessToken());
        assertEquals("new-refresh", result.refreshToken());
    }

    @Test
    void refreshShouldThrowWhenTokenNotFound() {
        when(refreshTokenService.findByToken("missing-token")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> authService.refresh("missing-token"));
        verify(jwtProvider, never()).generateToken(any(UserEntity.class));
    }

    @Test
    void logoutShouldDeleteRefreshToken() {
        authService.logout("refresh-token");

        verify(refreshTokenService).deleteByToken("refresh-token");
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
