package com.sunfeax.citeria.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

class SecurityConfigTest {

    @Test
    void corsConfigurationShouldAllowAngularDevServerWithCredentials() {
        SecurityConfig securityConfig = new SecurityConfig(
            mock(JwtAuthenticationFilter.class),
            mock(AuthenticationProvider.class)
        );
        ReflectionTestUtils.setField(securityConfig, "allowedOrigin", "http://localhost:4200");

        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/auth/login");
        request.addHeader("Origin", "http://localhost:4200");

        CorsConfiguration corsConfiguration = source.getCorsConfiguration(request);

        assertThat(corsConfiguration).isNotNull();
        assertThat(corsConfiguration.getAllowedOrigins()).containsExactly("http://localhost:4200");
        assertThat(corsConfiguration.getAllowedMethods()).contains("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
        assertThat(corsConfiguration.getAllowedHeaders()).contains("Authorization", "Content-Type", "Accept");
        assertThat(corsConfiguration.getAllowCredentials()).isTrue();
    }
}
