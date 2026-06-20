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

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.sunfeax.citeria.dto.common.PageResponseDto;
import com.sunfeax.citeria.dto.specialistservice.SpecialistServicePatchRequestDto;
import com.sunfeax.citeria.dto.specialistservice.SpecialistServicePostRequestDto;
import com.sunfeax.citeria.dto.specialistservice.SpecialistServiceResponseDto;
import com.sunfeax.citeria.exception.GlobalExceptionHandler;
import com.sunfeax.citeria.exception.RequestValidationException;
import com.sunfeax.citeria.exception.ResourceNotFoundException;
import com.sunfeax.citeria.config.JwtAuthenticationFilter;
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

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final UUID ID = new UUID(0, 1L);
    private static final UUID MISSING_ID = new UUID(0, 99L);

    @Test
    void getSpecialistServicesShouldReturnPagedResponse() throws Exception {
        SpecialistServiceResponseDto dto = specialistServiceDto(new UUID(0, 1L), new UUID(0, 10L), new UUID(0, 20L), new UUID(0, 30L));
        when(specialistServiceService.list(any(), any(), any(), any(), any())).thenReturn(new PageResponseDto<>(List.of(dto), 0, 20, 1, 1, true, true));

        mockMvc.perform(get("/api/specialist-services"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(ID.toString()))
            .andExpect(jsonPath("$.content[0].businessId").value(new UUID(0, 10L).toString()));
    }

    @Test
    void getByIdShouldReturnNotFoundWhenServiceThrows() throws Exception {
        when(specialistServiceService.getById(new UUID(0, 99L)))
            .thenThrow(new ResourceNotFoundException("Specialist service with id 99 not found"));

        mockMvc.perform(get("/api/specialist-services/" + MISSING_ID))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.title").value("Resource Not Found"))
            .andExpect(jsonPath("$.detail").value("Specialist service with id 99 not found"));
    }

    @Test
    void registerShouldReturnCreated() throws Exception {
        SpecialistServicePostRequestDto request = new SpecialistServicePostRequestDto(new UUID(0, 10L), new UUID(0, 20L), new UUID(0, 30L));
        when(specialistServiceService.register(any(SpecialistServicePostRequestDto.class)))
            .thenReturn(specialistServiceDto(new UUID(0, 1L), new UUID(0, 10L), new UUID(0, 20L), new UUID(0, 30L)));

        mockMvc.perform(post("/api/specialist-services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(ID.toString()));
    }

    @Test
    void registerShouldReturnBadRequestForInvalidBody() throws Exception {
        String invalidBody = """
            {
              "businessId": "00000000-0000-0000-0000-000000000001",
              "specialistId": null,
              "serviceId": "00000000-0000-0000-0000-000000000001"
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
        SpecialistServicePatchRequestDto request = new SpecialistServicePatchRequestDto(new UUID(0, 11L), null, null);
        when(specialistServiceService.update(eq(new UUID(0, 1L)), any(SpecialistServicePatchRequestDto.class)))
            .thenReturn(specialistServiceDto(new UUID(0, 1L), new UUID(0, 11L), new UUID(0, 20L), new UUID(0, 30L)));

        mockMvc.perform(patch("/api/specialist-services/" + ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(ID.toString()));
    }

    @Test
    void updateShouldReturnBadRequestForServiceValidationError() throws Exception {
        SpecialistServicePatchRequestDto request = new SpecialistServicePatchRequestDto(null, null, null);
        when(specialistServiceService.update(eq(new UUID(0, 1L)), any(SpecialistServicePatchRequestDto.class)))
            .thenThrow(new RequestValidationException(Map.of("request", "No fields to update")));

        mockMvc.perform(patch("/api/specialist-services/" + ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.title").value("Validation Failed"))
            .andExpect(jsonPath("$.errors.request").value("No fields to update"));
    }

    @Test
    void deactivateShouldReturnOk() throws Exception {
        when(specialistServiceService.deactivateById(new UUID(0, 1L))).thenReturn(specialistServiceDto(new UUID(0, 1L), new UUID(0, 10L), new UUID(0, 20L), new UUID(0, 30L)));

        mockMvc.perform(delete("/api/specialist-services/" + ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(ID.toString()));
    }

    @Test
    void hardDeleteShouldReturnOk() throws Exception {
        when(specialistServiceService.hardDeleteById(new UUID(0, 1L))).thenReturn(specialistServiceDto(new UUID(0, 1L), new UUID(0, 10L), new UUID(0, 20L), new UUID(0, 30L)));

        mockMvc.perform(delete("/api/specialist-services/" + ID + "/hard"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(ID.toString()));
    }

    @Test
    void restoreShouldReturnOk() throws Exception {
        when(specialistServiceService.restoreById(new UUID(0, 1L))).thenReturn(specialistServiceDto(new UUID(0, 1L), new UUID(0, 10L), new UUID(0, 20L), new UUID(0, 30L)));

        mockMvc.perform(patch("/api/specialist-services/" + ID + "/restore"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(ID.toString()));
    }

    private SpecialistServiceResponseDto specialistServiceDto(UUID id, UUID businessId, UUID specialistId, UUID serviceId) {
        return new SpecialistServiceResponseDto(
            id,
            businessId,
            "Business " + businessId,
            specialistId,
            "Spec User",
            serviceId,
            "Service " + serviceId,
            true,
            Instant.parse("2026-01-01T12:00:00Z")
        );
    }
}
