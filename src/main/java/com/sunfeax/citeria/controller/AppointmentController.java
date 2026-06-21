package com.sunfeax.citeria.controller;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sunfeax.citeria.dto.appointment.AppointmentPostRequestDto;
import com.sunfeax.citeria.dto.appointment.AppointmentResponseDto;
import com.sunfeax.citeria.dto.appointment.PaymentRequestDto;
import com.sunfeax.citeria.dto.common.PageResponseDto;
import com.sunfeax.citeria.enums.AppointmentStatus;
import com.sunfeax.citeria.service.AppointmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping
    public PageResponseDto<AppointmentResponseDto> list(
        @RequestParam(required = false) AppointmentStatus status,
        @RequestParam(required = false) Instant from,
        @RequestParam(required = false) Instant to,
        @RequestParam(required = false) UUID serviceId,
        @PageableDefault(size = 20, sort = "startTime", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return appointmentService.list(status, from, to, serviceId, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDto> getAppointmentById(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.getById(id));
    }

    @PostMapping
    public ResponseEntity<AppointmentResponseDto> create(@Valid @RequestBody AppointmentPostRequestDto request) {
        AppointmentResponseDto response = appointmentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<AppointmentResponseDto> accept(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.accept(id));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<AppointmentResponseDto> reject(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.reject(id));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<AppointmentResponseDto> pay(
        @PathVariable UUID id,
        @RequestBody(required = false) PaymentRequestDto card
    ) {
        // Mocked payment: card details are accepted but never validated or charged.
        return ResponseEntity.ok(appointmentService.pay(id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponseDto> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.cancel(id));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<AppointmentResponseDto> complete(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.complete(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<AppointmentResponseDto> deleteById(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.deleteById(id));
    }
}
