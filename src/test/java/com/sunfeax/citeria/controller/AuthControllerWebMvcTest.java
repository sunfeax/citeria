package com.sunfeax.citeria.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.sunfeax.citeria.config.JwtAuthenticationFilter;
import com.sunfeax.citeria.dto.auth.AuthSessionDto;
import com.sunfeax.citeria.dto.auth.LoginRequestDto;
import com.sunfeax.citeria.dto.auth.AuthResponseDto;
import com.sunfeax.citeria.dto.auth.RegisterRequestDto;
import com.sunfeax.citeria.dto.user.UserResponseDto;
import com.sunfeax.citeria.enums.UserRole;
import com.sunfeax.citeria.enums.UserType;
import com.sunfeax.citeria.exception.GlobalExceptionHandler;
import com.sunfeax.citeria.exception.RequestValidationException;
import com.sunfeax.citeria.service.AuthService;

import jakarta.servlet.http.Cookie;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private AuthenticationProvider authenticationProvider;

    @Test
    void registerShouldReturnCreated() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto(
            "John",
            "Snow",
            "john@example.com",
            "+34123456789",
            "Password!",
            UserType.CLIENT
        );

        when(authService.register(any(RegisterRequestDto.class))).thenReturn(new AuthSessionDto(
            authResponseDto(),
            "refresh-token"
        ));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.accessToken").value("jwt-token"))
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.user.id").value(1))
            .andExpect(jsonPath("$.user.type").value("CLIENT"))
            .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("refresh_token=refresh-token")));
    }

    @Test
    void registerShouldReturnBadRequestForInvalidBody() throws Exception {
        String invalidBody = """
            {
              "firstName": "John",
              "lastName": "Snow",
              "email": "invalid-email",
              "phone": "+34123456789",
              "password": "Password!",
              "type": "CLIENT"
            }
            """;

        when(authService.register(any(RegisterRequestDto.class)))
            .thenThrow(new RequestValidationException(Map.of("email", "must be a well-formed email address")));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.title").value("Validation Failed"))
            .andExpect(jsonPath("$.detail").value("Request contains invalid fields."))
            .andExpect(jsonPath("$.errors").isMap())
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void loginShouldReturnOkAndSetRefreshCookie() throws Exception {
        LoginRequestDto request = new LoginRequestDto("john@example.com", "Password!");

        when(authService.login(any(LoginRequestDto.class))).thenReturn(new AuthSessionDto(
            authResponseDto(),
            "refresh-token"
        ));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("jwt-token"))
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("refresh_token=refresh-token")));
    }

    @Test
    void loginShouldReturnUnauthorizedWhenCredentialsInvalid() throws Exception {
        LoginRequestDto request = new LoginRequestDto("john@example.com", "WrongPassword!");
        when(authService.login(any(LoginRequestDto.class)))
            .thenThrow(new BadCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.title").value("Authentication Failed"))
            .andExpect(jsonPath("$.detail").value("Invalid email or password"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void loginShouldReturnBadRequestForInvalidBody() throws Exception {
        String invalidBody = """
            {
              "email": "invalid-email",
              "password": ""
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.title").value("Validation Failed"))
            .andExpect(jsonPath("$.detail").value("Request contains invalid fields."))
            .andExpect(jsonPath("$.errors").isMap())
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.errors.email").exists())
            .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void refreshShouldReturnOkAndRotateCookie() throws Exception {
        when(authService.refresh(eq("old-refresh"))).thenReturn(new AuthSessionDto(
            authResponseDto(),
            "new-refresh"
        ));

        mockMvc.perform(post("/api/auth/refresh")
                .cookie(new Cookie("refresh_token", "old-refresh")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("jwt-token"))
            .andExpect(jsonPath("$.user").doesNotExist())
            .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("refresh_token=new-refresh")));
    }

    @Test
    void refreshShouldReturnUnauthorizedWhenCookieMissing() throws Exception {
        mockMvc.perform(post("/api/auth/refresh"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.title").value("Unauthorized"));
    }

    @Test
    void logoutShouldClearCookieAndReturnNoContent() throws Exception {
        doNothing().when(authService).logout("refresh-token");

        mockMvc.perform(post("/api/auth/logout")
                .cookie(new Cookie("refresh_token", "refresh-token")))
            .andExpect(status().isNoContent())
            .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")));

        verify(authService).logout("refresh-token");
    }

    private UserResponseDto userDto(Long id) {
        return new UserResponseDto(
            id,
            "John",
            "Snow",
            "john@example.com",
            "123456789",
            UserRole.USER,
            UserType.CLIENT,
            true,
            LocalDateTime.of(2026, 1, 1, 12, 0)
        );
    }

    private AuthResponseDto authResponseDto() {
        return new AuthResponseDto(
            "jwt-token",
            "Bearer",
            userDto(1L)
        );
    }
}
