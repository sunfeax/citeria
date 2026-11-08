package com.sunfeax.citeria.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunfeax.citeria.dto.auth.AuthSessionDto;
import com.sunfeax.citeria.dto.auth.LoginRequestDto;
import com.sunfeax.citeria.dto.auth.AuthResponseDto;
import com.sunfeax.citeria.dto.auth.GoogleLoginRequestDto;
import com.sunfeax.citeria.dto.auth.RegisterRequestDto;
import com.sunfeax.citeria.dto.user.UserResponseDto;
import com.sunfeax.citeria.entity.RefreshTokenEntity;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.enums.UserType;
import com.sunfeax.citeria.exception.UnauthorizedException;
import com.sunfeax.citeria.mapper.UserMapper;
import com.sunfeax.citeria.normalizer.UserFieldNormalizer;
import com.sunfeax.citeria.repository.UserRepository;
import com.sunfeax.citeria.security.GoogleIdTokenVerifier;
import com.sunfeax.citeria.security.GoogleProfile;
import com.sunfeax.citeria.util.JwtProvider;
import com.sunfeax.citeria.validation.UserValidator;
import com.sunfeax.citeria.validation.ValidationResult;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
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
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final Validator beanValidator;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;

    private static final int NAME_MAX_LENGTH = 50;

    @Transactional
    public AuthSessionDto login(LoginRequestDto request) {
        String normalizedEmail = userFieldNormalizer.normalizeEmail(request.email());

        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(normalizedEmail, request.password())
        );

        UserEntity user = userRepository.findByEmail(normalizedEmail)
            .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        return createSession(user);
    }

    @Transactional
    public AuthSessionDto loginWithGoogle(GoogleLoginRequestDto request) {
        GoogleProfile profile = googleIdTokenVerifier.verify(request.credential());
        if (!profile.emailVerified() || profile.email() == null) {
            throw new UnauthorizedException("Google account email is not verified.");
        }

        UserEntity user = userRepository.findByGoogleId(profile.subject())
            .or(() -> userRepository.findByEmail(userFieldNormalizer.normalizeEmail(profile.email()))
                .map(existing -> linkGoogleAccount(existing, profile.subject())))
            .orElseGet(() -> userRepository.save(createGoogleUser(profile, request.type())));

        if (!user.isActive()) {
            throw new UnauthorizedException("User is deactivated");
        }

        return createSession(user);
    }

    @Transactional
    public AuthSessionDto register(RegisterRequestDto request) {
        RegisterRequestDto normalizedRequest = userFieldNormalizer.normalizePostRequest(request);
        validateRegister(normalizedRequest).throwIfHasErrors();

        UserEntity user = userMapper.createEntity(normalizedRequest);
        user.setPassword(passwordEncoder.encode(normalizedRequest.password()));
        UserEntity savedUser = userRepository.save(user);

        return createSession(savedUser);
    }

    @Transactional
    public AuthSessionDto refresh(String refreshToken) {
        RefreshTokenEntity storedToken = refreshTokenService.findByToken(refreshToken)
            .map(refreshTokenService::verifyExpiration)
            .orElseThrow(() -> new UnauthorizedException("Refresh token is invalid"));

        UserEntity user = storedToken.getUser();

        if (!user.isActive()) {
            refreshTokenService.deleteByToken(refreshToken);
            throw new UnauthorizedException("User is deactivated");
        }

        String accessToken = jwtProvider.generateToken(user);
        String rotatedRefreshToken = refreshTokenService.rotateRefreshToken(storedToken);
        AuthResponseDto response = new AuthResponseDto(accessToken, "Bearer", null);

        return new AuthSessionDto(response, rotatedRefreshToken);
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.deleteByToken(refreshToken);
    }

    private AuthSessionDto createSession(UserEntity user) {
        String accessToken = jwtProvider.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user.getId());
        AuthResponseDto response = buildLoginResponse(user, accessToken);

        return new AuthSessionDto(response, refreshToken);
    }

    private UserEntity linkGoogleAccount(UserEntity user, String googleId) {
        if (user.getGoogleId() != null && !user.getGoogleId().equals(googleId)) {
            throw new UnauthorizedException("This email is linked to another Google account.");
        }

        user.setGoogleId(googleId);
        return user;
    }

    private UserEntity createGoogleUser(GoogleProfile profile, UserType type) {
        String email = userFieldNormalizer.normalizeEmail(profile.email());
        String firstName = profile.givenName() == null || profile.givenName().isBlank()
            ? email.substring(0, email.indexOf('@'))
            : profile.givenName();
        String lastName = profile.familyName() == null ? "" : profile.familyName();

        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setFirstName(truncate(userFieldNormalizer.normalizeName(firstName)));
        user.setLastName(truncate(userFieldNormalizer.normalizeName(lastName)));
        user.setGoogleId(profile.subject());
        user.setType(type == null ? UserType.CLIENT : type);

        return user;
    }

    private String truncate(String value) {
        return value.length() > NAME_MAX_LENGTH ? value.substring(0, NAME_MAX_LENGTH) : value;
    }

    private ValidationResult validateRegister(RegisterRequestDto request) {
        ValidationResult result = new ValidationResult();

        beanValidator.validate(request).forEach(violation ->
            result.addError(resolveConstraintField(violation), violation.getMessage())
        );

        return result.merge(userValidator.collectRegisterErrors(request));
    }

    private String resolveConstraintField(ConstraintViolation<?> violation) {
        String propertyPath = violation.getPropertyPath().toString();
        int lastDot = propertyPath.lastIndexOf('.');
        return lastDot >= 0 ? propertyPath.substring(lastDot + 1) : propertyPath;
    }

    private AuthResponseDto buildLoginResponse(UserEntity user, String accessToken) {
        UserResponseDto userResponse = userMapper.toResponseDto(user);
        return new AuthResponseDto(accessToken, "Bearer", userResponse);
    }
}
