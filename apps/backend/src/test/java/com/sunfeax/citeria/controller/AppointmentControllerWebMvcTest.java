package com.sunfeax.citeria.controller;

import java.time.Duration;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
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

import com.sunfeax.citeria.config.JwtAuthenticationFilter;
import com.sunfeax.citeria.config.RateLimitFilter;
import com.sunfeax.citeria.dto.common.PageResponseDto;
import com.sunfeax.citeria.dto.appointment.AppointmentPostRequestDto;
import com.sunfeax.citeria.dto.appointment.AppointmentResponseDto;
import com.sunfeax.citeria.enums.AppointmentStatus;
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

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private RateLimitFilter rateLimitFilter;

    private static final UUID ID = new UUID(0, 1L);
    private static final UUID MISSING_ID = new UUID(0, 99L);

    @Test
    void getAppointmentsShouldReturnPagedResponse() throws Exception {
        AppointmentResponseDto dto = appointmentDto(ID);
        when(appointmentService.list(any(), any(), any(), any(), any())).thenReturn(new PageResponseDto<>(List.of(dto), 0, 20, 1, 1, true, true));

        mockMvc.perform(get("/api/appointments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(ID.toString()))
            .andExpect(jsonPath("$.content[0].clientEmail").value("client@example.com"));
    }

    @Test
    void getByIdShouldReturnOk() throws Exception {
        when(appointmentService.getById(ID)).thenReturn(appointmentDto(ID));

        mockMvc.perform(get("/api/appointments/" + ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(ID.toString()))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getByIdShouldReturnNotFoundWhenServiceThrows() throws Exception {
        when(appointmentService.getById(MISSING_ID)).thenThrow(new ResourceNotFoundException("Appointment with id 99 not found"));

        mockMvc.perform(get("/api/appointments/" + MISSING_ID))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.title").value("Resource Not Found"))
            .andExpect(jsonPath("$.detail").value("Appointment with id 99 not found"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createShouldReturnCreated() throws Exception {
        Instant start = Instant.now().plus(Duration.ofDays(1));
        AppointmentPostRequestDto request = new AppointmentPostRequestDto(new UUID(0, 100L), start);
        when(appointmentService.create(any(AppointmentPostRequestDto.class))).thenReturn(appointmentDto(ID));

        mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(ID.toString()))
            .andExpect(jsonPath("$.serviceName").value("Consultation"));
    }

    @Test
    void createShouldReturnBadRequestForInvalidBody() throws Exception {
        String invalidBody = """
            {
              "serviceId": "00000000-0000-0000-0000-000000000001",
              "startTime": null
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
    void acceptShouldReturnOk() throws Exception {
        when(appointmentService.accept(ID)).thenReturn(appointmentDto(ID));

        mockMvc.perform(post("/api/appointments/" + ID + "/accept"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(ID.toString()));
    }

    @Test
    void payShouldReturnOk() throws Exception {
        when(appointmentService.pay(ID)).thenReturn(appointmentDto(ID));

        mockMvc.perform(post("/api/appointments/" + ID + "/pay"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(ID.toString()));
    }

    @Test
    void cancelShouldReturnOk() throws Exception {
        when(appointmentService.cancel(ID)).thenReturn(appointmentDto(ID));

        mockMvc.perform(post("/api/appointments/" + ID + "/cancel"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(ID.toString()));
    }

    @Test
    void acceptShouldReturnBadRequestForInvalidTransition() throws Exception {
        when(appointmentService.accept(ID))
            .thenThrow(new RequestValidationException(Map.of("status", "Action not allowed for an appointment in status CONFIRMED")));

        mockMvc.perform(post("/api/appointments/" + ID + "/accept"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.title").value("Validation Failed"))
            .andExpect(jsonPath("$.errors.status").exists());
    }

    @Test
    void deleteShouldReturnOk() throws Exception {
        when(appointmentService.deleteById(ID)).thenReturn(appointmentDto(ID));

        mockMvc.perform(delete("/api/appointments/" + ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(ID.toString()));
    }

    private AppointmentResponseDto appointmentDto(UUID id) {
        Instant start = Instant.parse("2026-03-10T10:00:00Z");
        return new AppointmentResponseDto(
            id,
            new UUID(0, 10L),
            "Client User",
            "client@example.com",
            new UUID(0, 100L),
            "Consultation",
            new UUID(0, 500L),
            "Specialist User",
            start,
            start.plus(Duration.ofMinutes(60)),
            AppointmentStatus.PENDING,
            BigDecimal.valueOf(95),
            null
        );
    }
}
