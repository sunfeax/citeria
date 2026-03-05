package com.sunfeax.citeria.controller;

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
import java.time.LocalDateTime;
import java.util.List;

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
import com.sunfeax.citeria.dto.service.ServicePatchRequestDto;
import com.sunfeax.citeria.dto.service.ServicePostRequestDto;
import com.sunfeax.citeria.dto.service.ServiceResponseDto;
import com.sunfeax.citeria.exception.GlobalExceptionHandler;
import com.sunfeax.citeria.exception.ResourceNotFoundException;
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

    @Test
    void getServicesShouldReturnPagedResponse() throws Exception {
        ServiceResponseDto dto = serviceDto(1L, 10L, "Consultation");
        when(serviceService.getAll(any())).thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/services"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].name").value("Consultation"));
    }

    @Test
    void getByIdShouldReturnOk() throws Exception {
        when(serviceService.getById(1L)).thenReturn(serviceDto(1L, 10L, "Consultation"));

        mockMvc.perform(get("/api/services/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Consultation"));
    }

    @Test
    void getByIdShouldReturnNotFoundWhenServiceThrows() throws Exception {
        when(serviceService.getById(99L)).thenThrow(new ResourceNotFoundException("Service with id 99 not found"));

        mockMvc.perform(get("/api/services/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.title").value("Resource Not Found"))
            .andExpect(jsonPath("$.detail").value("Service with id 99 not found"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createShouldReturnCreated() throws Exception {
        ServicePostRequestDto request = new ServicePostRequestDto(
            10L,
            "Consultation",
            "desc",
            60,
            BigDecimal.valueOf(95),
            "EUR"
        );
        when(serviceService.create(any(ServicePostRequestDto.class))).thenReturn(serviceDto(1L, 10L, "Consultation"));

        mockMvc.perform(post("/api/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Consultation"));
    }

    @Test
    void createShouldReturnBadRequestForInvalidBody() throws Exception {
        String invalidBody = """
            {
              "businessId": 10,
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
        when(serviceService.update(eq(1L), any(ServicePatchRequestDto.class))).thenReturn(serviceDto(1L, 10L, "Therapy"));

        mockMvc.perform(patch("/api/services/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Therapy"));
    }

    @Test
    void updateShouldReturnBadRequestForInvalidBody() throws Exception {
        String invalidBody = """
            {
              "durationMinutes": 10
            }
            """;

        mockMvc.perform(patch("/api/services/1")
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
        when(serviceService.deactivateById(1L)).thenReturn(serviceDto(1L, 10L, "Consultation"));

        mockMvc.perform(delete("/api/services/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void hardDeleteShouldReturnOk() throws Exception {
        when(serviceService.hardDeleteById(1L)).thenReturn(serviceDto(1L, 10L, "Consultation"));

        mockMvc.perform(delete("/api/services/1/hard"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void restoreShouldReturnOk() throws Exception {
        when(serviceService.restoreById(1L)).thenReturn(serviceDto(1L, 10L, "Consultation"));

        mockMvc.perform(patch("/api/services/1/restore"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    private ServiceResponseDto serviceDto(Long id, Long businessId, String name) {
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
            LocalDateTime.of(2026, 1, 1, 12, 0)
        );
    }
}
