package com.sunfeax.citeria.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

import com.sunfeax.citeria.dto.user.UserChangePasswordRequestDto;
import com.sunfeax.citeria.dto.user.UserUpdateRequestDto;
import com.sunfeax.citeria.dto.user.UserResponseDto;
import com.sunfeax.citeria.enums.UserRole;
import com.sunfeax.citeria.enums.UserType;
import com.sunfeax.citeria.exception.GlobalExceptionHandler;
import com.sunfeax.citeria.exception.RequestValidationException;
import com.sunfeax.citeria.exception.ResourceNotFoundException;
import com.sunfeax.citeria.service.UserService;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class UserControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void getUsersShouldReturnPagedResponse() throws Exception {
        UserResponseDto dto = userDto(1L);
        when(userService.getAll(any())).thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].email").value("john@example.com"));
    }

    @Test
    void getByIdShouldReturnOk() throws Exception {
        when(userService.getById(1L)).thenReturn(userDto(1L));

        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void getByIdShouldReturnNotFoundWhenServiceThrows() throws Exception {
        when(userService.getById(99L)).thenThrow(new ResourceNotFoundException("User with id 99 not found"));

        mockMvc.perform(get("/api/users/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.title").value("Resource Not Found"))
            .andExpect(jsonPath("$.detail").value("User with id 99 not found"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updateShouldReturnOk() throws Exception {
        UserUpdateRequestDto request = new UserUpdateRequestDto("Jane", null, null, null, null);
        when(userService.update(eq(1L), any(UserUpdateRequestDto.class))).thenReturn(userDto(1L));

        mockMvc.perform(patch("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateShouldReturnBadRequestForServiceValidationError() throws Exception {
        UserUpdateRequestDto request = new UserUpdateRequestDto(null, null, "taken@example.com", null, null);
        when(userService.update(eq(1L), any(UserUpdateRequestDto.class)))
            .thenThrow(new RequestValidationException(Map.of("email", "Email taken@example.com is already taken")));

        mockMvc.perform(patch("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.title").value("Validation Failed"))
            .andExpect(jsonPath("$.detail").value("Request contains invalid fields."))
            .andExpect(jsonPath("$.errors").isMap())
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.errors.email").value("Email taken@example.com is already taken"));
    }

    @Test
    void changePasswordShouldReturnNoContent() throws Exception {
        UserChangePasswordRequestDto request = new UserChangePasswordRequestDto("OldPassword!", "NewPassword!");
        doNothing().when(userService).changePassword(eq(1L), any(UserChangePasswordRequestDto.class));

        mockMvc.perform(patch("/api/users/1/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNoContent());
    }

    @Test
    void changePasswordShouldReturnBadRequestForInvalidBody() throws Exception {
        String invalidBody = """
            {
              "currentPassword": "OldPassword!",
              "newPassword": "short"
            }
            """;

        mockMvc.perform(patch("/api/users/1/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.title").value("Validation Failed"))
            .andExpect(jsonPath("$.detail").value("Request contains invalid fields."))
            .andExpect(jsonPath("$.errors").isMap())
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.errors.newPassword").exists());
    }

    @Test
    void deactivateShouldReturnOk() throws Exception {
        when(userService.deactivateById(1L)).thenReturn(userDto(1L));

        mockMvc.perform(delete("/api/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void hardDeleteShouldReturnOk() throws Exception {
        when(userService.hardDeleteById(1L)).thenReturn(userDto(1L));

        mockMvc.perform(delete("/api/users/1/hard"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void restoreShouldReturnOk() throws Exception {
        when(userService.restoreById(1L)).thenReturn(userDto(1L));

        mockMvc.perform(patch("/api/users/1/restore"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
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
}
