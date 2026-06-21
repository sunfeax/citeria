package com.sunfeax.citeria.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sunfeax.citeria.dto.common.PageResponseDto;
import com.sunfeax.citeria.dto.service.ServicePatchRequestDto;
import com.sunfeax.citeria.dto.service.ServicePostRequestDto;
import com.sunfeax.citeria.dto.service.ServiceResponseDto;
import com.sunfeax.citeria.dto.slot.SlotResponseDto;
import com.sunfeax.citeria.service.ServiceService;
import com.sunfeax.citeria.service.SlotService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceService serviceService;
    private final SlotService slotService;

    @GetMapping
    public PageResponseDto<ServiceResponseDto> list(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) UUID specialistId,
        @RequestParam(required = false) Boolean active,
        @RequestParam(required = false) BigDecimal minPrice,
        @RequestParam(required = false) BigDecimal maxPrice,
        @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return serviceService.list(search, specialistId, active, minPrice, maxPrice, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponseDto> getServiceById(@PathVariable UUID id) {
        return ResponseEntity.ok(serviceService.getById(id));
    }

    @GetMapping("/{id}/slots")
    public ResponseEntity<List<SlotResponseDto>> getAvailableSlots(
        @PathVariable UUID id,
        @RequestParam(required = false) LocalDate from,
        @RequestParam(required = false) LocalDate to
    ) {
        return ResponseEntity.ok(slotService.getAvailableSlots(id, from, to));
    }

    @PostMapping
    public ResponseEntity<ServiceResponseDto> create(@Valid @RequestBody ServicePostRequestDto request) {
        ServiceResponseDto response = serviceService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ServiceResponseDto> update(
        @PathVariable UUID id,
        @Valid @RequestBody ServicePatchRequestDto request
    ) {
        return ResponseEntity.ok(serviceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ServiceResponseDto> deactivateById(@PathVariable UUID id) {
        return ResponseEntity.ok(serviceService.deactivateById(id));
    }

    @DeleteMapping("/{id}/hard")
    public ResponseEntity<ServiceResponseDto> hardDeleteById(@PathVariable UUID id) {
        return ResponseEntity.ok(serviceService.hardDeleteById(id));
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<ServiceResponseDto> restoreById(@PathVariable UUID id) {
        return ResponseEntity.ok(serviceService.restoreById(id));
    }
}
