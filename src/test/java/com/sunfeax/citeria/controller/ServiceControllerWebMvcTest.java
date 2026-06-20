package com.sunfeax.citeria.controller;

import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;
import com.sunfeax.citeria.dto.common.PageResponseDto;
import com.sunfeax.citeria.dto.service.ServicePatchRequestDto;
import com.sunfeax.citeria.dto.service.ServicePostRequestDto;
import com.sunfeax.citeria.dto.service.ServiceResponseDto;
import com.sunfeax.citeria.exception.GlobalExceptionHandler;
import com.sunfeax.citeria.exception.ResourceNotFoundException;
import com.sunfeax.citeria.config.JwtAuthenticationFilter;
import com.sunfeax.citeria.service.ServiceService;

@WebMvcTest(ServiceController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ServiceControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ServiceService serviceService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final UUID ID = new UUID(0, 1L);
    private static final UUID MISSING_ID = new UUID(0, 99L);

    @Test
    void getServicesShouldReturnPagedResponse() throws Exception {
        ServiceResponseDto dto = serviceDto(new UUID(0, 1L), new UUID(0, 10L), "Consultation");
        when(serviceService.list(any(), any(), any(), any(), any(), any())).thenReturn(new PageResponseDto<>(List.of(dto), 0, 20, 1, 1, true, true));

        mockMvc.perform(get("/api/services"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(ID.toString()))
            .andExpect(jsonPath("$.content[0].name").value("Consultation"));
    }

    @Test
    void getByIdShouldReturnOk() throws Exception {
        when(serviceService.getById(new UUID(0, 1L))).thenReturn(serviceDto(new UUID(0, 1L), new UUID(0, 10L), "Consultation"));

        mockMvc.perform(get("/api/services/" + ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(ID.toString()))
            .andExpect(jsonPath("$.name").value("Consultation"));
    }

    @Test
    void getByIdShouldReturnNotFoundWhenServiceThrows() throws Exception {
        when(serviceService.getById(new UUID(0, 99L))).thenThrow(new ResourceNotFoundException("Service with id 99 not found"));

        mockMvc.perform(get("/api/services/" + MISSING_ID))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.title").value("Resource Not Found"))
            .andExpect(jsonPath("$.detail").value("Service with id 99 not found"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createShouldReturnCreated() throws Exception {
        ServicePostRequestDto request = new ServicePostRequestDto(
            new UUID(0, 10L),
            "Consultation",
            "desc",
            60,
            BigDecimal.valueOf(95),
            "EUR"
        );
        when(serviceService.create(any(ServicePostRequestDto.class))).thenReturn(serviceDto(new UUID(0, 1L), new UUID(0, 10L), "Consultation"));

        mockMvc.perform(post("/api/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(ID.toString()))
            .andExpect(jsonPath("$.name").value("Consultation"));
    }

    @Test
    void createShouldReturnBadRequestForInvalidBody() throws Exception {
        String invalidBody = """
            {
              "businessId": "00000000-0000-0000-0000-000000000001",
              "name": "",
              "description": "desc",
              "durationMinutes": 60,
              "priceAmount": 95,
              "currency": "EURO"
            }
            """;

        mockMvc.perform(post("/api/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.title").value("Validation Failed"))
            .andExpect(jsonPath("$.detail").value("Request contains invalid fields."))
            .andExpect(jsonPath("$.errors").isMap())
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void updateShouldReturnOk() throws Exception {
        ServicePatchRequestDto request = new ServicePatchRequestDto(null, "Therapy", null, null, null, null);
        when(serviceService.update(eq(new UUID(0, 1L)), any(ServicePatchRequestDto.class))).thenReturn(serviceDto(new UUID(0, 1L), new UUID(0, 10L), "Therapy"));

        mockMvc.perform(patch("/api/services/" + ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(ID.toString()))
            .andExpect(jsonPath("$.name").value("Therapy"));
    }

    @Test
    void updateShouldReturnBadRequestForInvalidBody() throws Exception {
        String invalidBody = """
            {
              "durationMinutes": 10
            }
            """;

        mockMvc.perform(patch("/api/services/" + ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.title").value("Validation Failed"))
            .andExpect(jsonPath("$.detail").value("Request contains invalid fields."))
            .andExpect(jsonPath("$.errors").isMap())
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.errors.durationMinutes").exists());
    }

    @Test
    void deactivateShouldReturnOk() throws Exception {
        when(serviceService.deactivateById(new UUID(0, 1L))).thenReturn(serviceDto(new UUID(0, 1L), new UUID(0, 10L), "Consultation"));

        mockMvc.perform(delete("/api/services/" + ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(ID.toString()));
    }

    @Test
    void hardDeleteShouldReturnOk() throws Exception {
        when(serviceService.hardDeleteById(new UUID(0, 1L))).thenReturn(serviceDto(new UUID(0, 1L), new UUID(0, 10L), "Consultation"));

        mockMvc.perform(delete("/api/services/" + ID + "/hard"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(ID.toString()));
    }

    @Test
    void restoreShouldReturnOk() throws Exception {
        when(serviceService.restoreById(new UUID(0, 1L))).thenReturn(serviceDto(new UUID(0, 1L), new UUID(0, 10L), "Consultation"));

        mockMvc.perform(patch("/api/services/" + ID + "/restore"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(ID.toString()));
    }

    private ServiceResponseDto serviceDto(UUID id, UUID businessId, String name) {
        return new ServiceResponseDto(
            id,
            businessId,
            name,
            "Alpha Studio",
            "desc",
            BigDecimal.valueOf(95),
            60,
            "EUR",
            true,
            Instant.parse("2026-01-01T12:00:00Z")
        );
    }
}
