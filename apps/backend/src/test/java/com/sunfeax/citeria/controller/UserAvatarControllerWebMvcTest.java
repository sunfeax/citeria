package com.sunfeax.citeria.controller;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.sunfeax.citeria.config.JwtAuthenticationFilter;
import com.sunfeax.citeria.dto.user.UserAvatarDto;
import com.sunfeax.citeria.exception.GlobalExceptionHandler;
import com.sunfeax.citeria.exception.ResourceNotFoundException;
import com.sunfeax.citeria.service.UserAvatarService;

@WebMvcTest(UserAvatarController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class UserAvatarControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserAvatarService userAvatarService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final UUID ID = new UUID(0, 1L);

    @Test
    void uploadShouldReturnNoContent() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", new byte[] {1, 2, 3});
        doNothing().when(userAvatarService).upload(eq(ID), any());

        mockMvc.perform(multipart("/api/users/" + ID + "/avatar").file(file))
            .andExpect(status().isNoContent());
    }

    @Test
    void getShouldReturnImageBytes() throws Exception {
        byte[] data = {1, 2, 3};
        when(userAvatarService.get(ID))
            .thenReturn(new UserAvatarDto("image/png", data, Instant.parse("2026-01-01T12:00:00Z")));

        mockMvc.perform(get("/api/users/" + ID + "/avatar"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "image/png"))
            .andExpect(content().bytes(data));
    }

    @Test
    void getShouldReturnNotFoundWhenMissing() throws Exception {
        when(userAvatarService.get(ID)).thenThrow(new ResourceNotFoundException("Avatar for user " + ID + " not found"));

        mockMvc.perform(get("/api/users/" + ID + "/avatar"))
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteShouldReturnNoContent() throws Exception {
        doNothing().when(userAvatarService).delete(ID);

        mockMvc.perform(delete("/api/users/" + ID + "/avatar"))
            .andExpect(status().isNoContent());
    }
}
