package com.sunfeax.citeria.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.sunfeax.citeria.dto.auth.LoginRequestDto;
import com.sunfeax.citeria.dto.auth.LoginResponseDto;
import com.sunfeax.citeria.dto.auth.RegisterRequestDto;
import com.sunfeax.citeria.dto.user.UserResponseDto;
import com.sunfeax.citeria.enums.UserRole;
import com.sunfeax.citeria.enums.UserType;
import com.sunfeax.citeria.exception.GlobalExceptionHandler;
import com.sunfeax.citeria.service.AuthService;

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

        when(authService.register(any(RegisterRequestDto.class))).thenReturn(userDto(1L));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.type").value("CLIENT"));
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

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.title").value("Validation Failed"))
            .andExpect(jsonPath("$.detail").value("Request contains invalid fields."))
            .andExpect(jsonPath("$.errors").isMap())
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void loginShouldReturnOk() throws Exception {
        LoginRequestDto request = new LoginRequestDto("john@example.com", "Password!");
        when(authService.login(any(LoginRequestDto.class))).thenReturn(loginResponseDto(1L));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt-token"))
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.fullName").value("John Snow"))
            .andExpect(jsonPath("$.role").value("USER"))
            .andExpect(jsonPath("$.type").value("CLIENT"));
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

    private LoginResponseDto loginResponseDto(Long id) {
        return new LoginResponseDto(
            "jwt-token",
            "Bearer",
            id,
            "John Snow",
            UserRole.USER,
            UserType.CLIENT
        );
    }
}
