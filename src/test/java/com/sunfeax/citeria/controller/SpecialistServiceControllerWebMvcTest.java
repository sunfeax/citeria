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

import com.sunfeax.citeria.dto.specialistservice.SpecialistServicePatchRequestDto;
import com.sunfeax.citeria.dto.specialistservice.SpecialistServicePostRequestDto;
import com.sunfeax.citeria.dto.specialistservice.SpecialistServiceResponseDto;
import com.sunfeax.citeria.exception.GlobalExceptionHandler;
import com.sunfeax.citeria.exception.RequestValidationException;
import com.sunfeax.citeria.exception.ResourceNotFoundException;
import com.sunfeax.citeria.service.SpecialistServiceService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(SpecialistServiceController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class SpecialistServiceControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SpecialistServiceService specialistServiceService;

    @Test
    void getSpecialistServicesShouldReturnPagedResponse() throws Exception {
        SpecialistServiceResponseDto dto = specialistServiceDto(1L, 10L, 20L, 30L);
        when(specialistServiceService.getAll(any())).thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/specialist-services"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].businessId").value(10));
    }

    @Test
    void getByIdShouldReturnNotFoundWhenServiceThrows() throws Exception {
        when(specialistServiceService.getById(99L))
            .thenThrow(new ResourceNotFoundException("Specialist service with id 99 not found"));

        mockMvc.perform(get("/api/specialist-services/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.title").value("Resource Not Found"))
            .andExpect(jsonPath("$.detail").value("Specialist service with id 99 not found"));
    }

    @Test
    void registerShouldReturnCreated() throws Exception {
        SpecialistServicePostRequestDto request = new SpecialistServicePostRequestDto(10L, 20L, 30L);
        when(specialistServiceService.register(any(SpecialistServicePostRequestDto.class)))
            .thenReturn(specialistServiceDto(1L, 10L, 20L, 30L));

        mockMvc.perform(post("/api/specialist-services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void registerShouldReturnBadRequestForInvalidBody() throws Exception {
        String invalidBody = """
            {
              "businessId": 10,
              "specialistId": null,
              "serviceId": 30
            }
            """;

        mockMvc.perform(post("/api/specialist-services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.title").value("Validation Failed"))
            .andExpect(jsonPath("$.errors.specialistId").exists());
    }

    @Test
    void updateShouldReturnOk() throws Exception {
        SpecialistServicePatchRequestDto request = new SpecialistServicePatchRequestDto(11L, null, null);
        when(specialistServiceService.update(eq(1L), any(SpecialistServicePatchRequestDto.class)))
            .thenReturn(specialistServiceDto(1L, 11L, 20L, 30L));

        mockMvc.perform(patch("/api/specialist-services/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateShouldReturnBadRequestForServiceValidationError() throws Exception {
        SpecialistServicePatchRequestDto request = new SpecialistServicePatchRequestDto(null, null, null);
        when(specialistServiceService.update(eq(1L), any(SpecialistServicePatchRequestDto.class)))
            .thenThrow(new RequestValidationException(Map.of("request", "No fields to update")));

        mockMvc.perform(patch("/api/specialist-services/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.title").value("Validation Failed"))
            .andExpect(jsonPath("$.errors.request").value("No fields to update"));
    }

    @Test
    void deactivateShouldReturnOk() throws Exception {
        when(specialistServiceService.deactivateById(1L)).thenReturn(specialistServiceDto(1L, 10L, 20L, 30L));

        mockMvc.perform(delete("/api/specialist-services/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void hardDeleteShouldReturnOk() throws Exception {
        when(specialistServiceService.hardDeleteById(1L)).thenReturn(specialistServiceDto(1L, 10L, 20L, 30L));

        mockMvc.perform(delete("/api/specialist-services/1/hard"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void restoreShouldReturnOk() throws Exception {
        when(specialistServiceService.restoreById(1L)).thenReturn(specialistServiceDto(1L, 10L, 20L, 30L));

        mockMvc.perform(patch("/api/specialist-services/1/restore"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    private SpecialistServiceResponseDto specialistServiceDto(Long id, Long businessId, Long specialistId, Long serviceId) {
        return new SpecialistServiceResponseDto(
            id,
            businessId,
            "Business " + businessId,
            specialistId,
            "Spec User",
            serviceId,
            "Service " + serviceId,
            true,
            LocalDateTime.of(2026, 1, 1, 12, 0)
        );
    }
}
