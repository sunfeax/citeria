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

import com.sunfeax.citeria.dto.appointment.AppointmentPatchRequestDto;
import com.sunfeax.citeria.dto.appointment.AppointmentPostRequestDto;
import com.sunfeax.citeria.dto.appointment.AppointmentResponseDto;
import com.sunfeax.citeria.enums.AppointmentStatus;
import com.sunfeax.citeria.enums.PaymentMethod;
import com.sunfeax.citeria.exception.GlobalExceptionHandler;
import com.sunfeax.citeria.exception.RequestValidationException;
import com.sunfeax.citeria.exception.ResourceNotFoundException;
import com.sunfeax.citeria.service.AppointmentService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(AppointmentController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AppointmentControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AppointmentService appointmentService;

    @Test
    void getAppointmentsShouldReturnPagedResponse() throws Exception {
        AppointmentResponseDto dto = appointmentDto(1L);
        when(appointmentService.getAll(any())).thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/appointments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].clientEmail").value("client@example.com"));
    }

    @Test
    void getByIdShouldReturnOk() throws Exception {
        when(appointmentService.getById(1L)).thenReturn(appointmentDto(1L));

        mockMvc.perform(get("/api/appointments/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getByIdShouldReturnNotFoundWhenServiceThrows() throws Exception {
        when(appointmentService.getById(99L)).thenThrow(new ResourceNotFoundException("Appointment with id 99 not found"));

        mockMvc.perform(get("/api/appointments/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.title").value("Resource Not Found"))
            .andExpect(jsonPath("$.detail").value("Appointment with id 99 not found"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void registerShouldReturnCreated() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withSecond(0).withNano(0);
        AppointmentPostRequestDto request = new AppointmentPostRequestDto(
            10L,
            100L,
            start,
            start.plusMinutes(60),
            PaymentMethod.ONLINE
        );
        when(appointmentService.register(any(AppointmentPostRequestDto.class))).thenReturn(appointmentDto(1L));

        mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.paymentMethod").value("ONLINE"));
    }

    @Test
    void registerShouldReturnBadRequestForInvalidBody() throws Exception {
        String invalidBody = """
            {
              "clientId": 10,
              "specialistServiceId": 100,
              "startTime": null,
              "endTime": "2026-03-10T11:00:00",
              "paymentMethod": "ONLINE"
            }
            """;

        mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.title").value("Validation Failed"))
            .andExpect(jsonPath("$.detail").value("Request contains invalid fields."))
            .andExpect(jsonPath("$.errors.startTime").exists());
    }

    @Test
    void updateShouldReturnOk() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(2).withSecond(0).withNano(0);
        AppointmentPatchRequestDto request = new AppointmentPatchRequestDto(
            null,
            null,
            start,
            start.plusMinutes(60),
            AppointmentStatus.CONFIRMED,
            null
        );
        when(appointmentService.update(eq(1L), any(AppointmentPatchRequestDto.class))).thenReturn(appointmentDto(1L));

        mockMvc.perform(patch("/api/appointments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateShouldReturnBadRequestForServiceValidationError() throws Exception {
        AppointmentPatchRequestDto request = new AppointmentPatchRequestDto(null, null, null, null, null, null);
        when(appointmentService.update(eq(1L), any(AppointmentPatchRequestDto.class)))
            .thenThrow(new RequestValidationException(Map.of("request", "No fields to update")));

        mockMvc.perform(patch("/api/appointments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.title").value("Validation Failed"))
            .andExpect(jsonPath("$.errors.request").value("No fields to update"));
    }

    @Test
    void deactivateShouldReturnOk() throws Exception {
        when(appointmentService.deactivateById(1L)).thenReturn(appointmentDto(1L));

        mockMvc.perform(delete("/api/appointments/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void hardDeleteShouldReturnOk() throws Exception {
        when(appointmentService.hardDeleteById(1L)).thenReturn(appointmentDto(1L));

        mockMvc.perform(delete("/api/appointments/1/hard"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void restoreShouldReturnOk() throws Exception {
        when(appointmentService.restoreById(1L)).thenReturn(appointmentDto(1L));

        mockMvc.perform(patch("/api/appointments/1/restore"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    private AppointmentResponseDto appointmentDto(Long id) {
        LocalDateTime start = LocalDateTime.of(2026, 3, 10, 10, 0);
        return new AppointmentResponseDto(
            id,
            10L,
            "Client User",
            "client@example.com",
            100L,
            500L,
            "Specialist User",
            400L,
            "Consultation",
            "Alpha Studio",
            start,
            start.plusMinutes(60),
            AppointmentStatus.PENDING,
            PaymentMethod.ONLINE,
            BigDecimal.valueOf(95)
        );
    }
}
