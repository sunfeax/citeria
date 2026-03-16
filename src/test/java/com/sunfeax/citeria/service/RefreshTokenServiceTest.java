package com.sunfeax.citeria.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.sunfeax.citeria.entity.RefreshTokenEntity;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.repository.RefreshTokenRepository;
import com.sunfeax.citeria.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, userRepository);
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDuration", 60_000L);
    }

    @Test
    void createRefreshTokenShouldReuseExistingTokenRowForUser() {
        UserEntity user = new UserEntity();
        user.setId(1L);

        RefreshTokenEntity existingToken = RefreshTokenEntity.builder()
            .id(10L)
            .user(user)
            .tokenHash("old-hash")
            .expiryDate(Instant.now().minusSeconds(60))
            .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.of(existingToken));
        when(refreshTokenRepository.save(any(RefreshTokenEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String rawToken = refreshTokenService.createRefreshToken(1L);

        ArgumentCaptor<RefreshTokenEntity> savedTokenCaptor = ArgumentCaptor.forClass(RefreshTokenEntity.class);
        verify(refreshTokenRepository).save(savedTokenCaptor.capture());
        verify(refreshTokenRepository, never()).deleteByUser(any(UserEntity.class));

        RefreshTokenEntity savedToken = savedTokenCaptor.getValue();

        assertThat(rawToken).isNotBlank();
        assertThat(savedToken.getId()).isEqualTo(10L);
        assertThat(savedToken.getUser()).isSameAs(user);
        assertThat(savedToken.getTokenHash()).hasSize(64).isNotEqualTo("old-hash");
        assertThat(savedToken.getExpiryDate()).isAfter(Instant.now());
    }
}
