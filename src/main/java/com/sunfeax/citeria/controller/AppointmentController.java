package com.sunfeax.citeria.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sunfeax.citeria.dto.appointment.AppointmentPatchRequestDto;
import com.sunfeax.citeria.dto.appointment.AppointmentPostRequestDto;
import com.sunfeax.citeria.dto.appointment.AppointmentResponseDto;
import com.sunfeax.citeria.service.AppointmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping
    public Page<AppointmentResponseDto> getAppointments(
        @PageableDefault(
            size = 20,
            sort = "id",
            direction = Sort.Direction.ASC
        ) Pageable pageable
    ) {
        return appointmentService.getAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDto> getAppointmentById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getById(id));
    }

    @PostMapping
    public ResponseEntity<AppointmentResponseDto> register(@Valid @RequestBody AppointmentPostRequestDto request) {
        AppointmentResponseDto response = appointmentService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AppointmentResponseDto> update(
        @PathVariable Long id,
        @Valid @RequestBody AppointmentPatchRequestDto request
    ) {
        return ResponseEntity.ok(appointmentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<AppointmentResponseDto> deactivateById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.deactivateById(id));
    }

    @DeleteMapping("/{id}/hard")
    public ResponseEntity<AppointmentResponseDto> hardDeleteById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.hardDeleteById(id));
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<AppointmentResponseDto> restoreById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.restoreById(id));
    }
}
