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

import com.sunfeax.citeria.dto.payment.PaymentPatchRequestDto;
import com.sunfeax.citeria.dto.payment.PaymentPostRequestDto;
import com.sunfeax.citeria.dto.payment.PaymentResponseDto;
import com.sunfeax.citeria.enums.PaymentStatus;
import com.sunfeax.citeria.exception.GlobalExceptionHandler;
import com.sunfeax.citeria.exception.RequestValidationException;
import com.sunfeax.citeria.exception.ResourceNotFoundException;
import com.sunfeax.citeria.service.PaymentService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class PaymentControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    void getPaymentsShouldReturnPagedResponse() throws Exception {
        PaymentResponseDto dto = paymentDto(1L, 10L);
        when(paymentService.getAll(any())).thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/payments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].appointmentId").value(10));
    }

    @Test
    void getByIdShouldReturnNotFoundWhenServiceThrows() throws Exception {
        when(paymentService.getById(99L)).thenThrow(new ResourceNotFoundException("Payment with id 99 not found"));

        mockMvc.perform(get("/api/payments/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.title").value("Resource Not Found"))
            .andExpect(jsonPath("$.detail").value("Payment with id 99 not found"));
    }

    @Test
    void registerShouldReturnCreated() throws Exception {
        PaymentPostRequestDto request = new PaymentPostRequestDto(10L);
        when(paymentService.register(any(PaymentPostRequestDto.class))).thenReturn(paymentDto(1L, 10L));

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void registerShouldReturnBadRequestForInvalidBody() throws Exception {
        String invalidBody = """
            {
              "appointmentId": null
            }
            """;

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.title").value("Validation Failed"))
            .andExpect(jsonPath("$.errors.appointmentId").exists());
    }

    @Test
    void updateShouldReturnOk() throws Exception {
        PaymentPatchRequestDto request = new PaymentPatchRequestDto(null, PaymentStatus.PAID);
        when(paymentService.update(eq(1L), any(PaymentPatchRequestDto.class))).thenReturn(paymentDto(1L, 10L));

        mockMvc.perform(patch("/api/payments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateShouldReturnBadRequestForServiceValidationError() throws Exception {
        PaymentPatchRequestDto request = new PaymentPatchRequestDto(null, null);
        when(paymentService.update(eq(1L), any(PaymentPatchRequestDto.class)))
            .thenThrow(new RequestValidationException(Map.of("request", "No fields to update")));

        mockMvc.perform(patch("/api/payments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.title").value("Validation Failed"))
            .andExpect(jsonPath("$.errors.request").value("No fields to update"));
    }

    @Test
    void deleteShouldReturnOk() throws Exception {
        when(paymentService.deleteById(1L)).thenReturn(paymentDto(1L, 10L));

        mockMvc.perform(delete("/api/payments/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    private PaymentResponseDto paymentDto(Long id, Long appointmentId) {
        return new PaymentResponseDto(
            id,
            appointmentId,
            BigDecimal.valueOf(95),
            "EUR",
            PaymentStatus.PENDING,
            LocalDateTime.of(2026, 1, 1, 12, 0),
            LocalDateTime.of(2026, 1, 1, 12, 30)
        );
    }
}
