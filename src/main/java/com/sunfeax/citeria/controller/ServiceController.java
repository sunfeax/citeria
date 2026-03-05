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

import com.sunfeax.citeria.dto.service.ServicePatchRequestDto;
import com.sunfeax.citeria.dto.service.ServicePostRequestDto;
import com.sunfeax.citeria.dto.service.ServiceResponseDto;
import com.sunfeax.citeria.service.ServiceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceService serviceService;

    @GetMapping
    public Page<ServiceResponseDto> getServices(
        @PageableDefault(
            size = 20,
            sort = "id",
            direction = Sort.Direction.ASC
        ) Pageable pageable
    ) {
        return serviceService.getAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponseDto> getServiceById(@PathVariable Long id) {
        return ResponseEntity.ok(serviceService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ServiceResponseDto> create(@Valid @RequestBody ServicePostRequestDto request) {
        ServiceResponseDto response = serviceService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ServiceResponseDto> update(
        @PathVariable Long id,
        @Valid @RequestBody ServicePatchRequestDto request
    ) {
        return ResponseEntity.ok(serviceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ServiceResponseDto> deactivateById(@PathVariable Long id) {
        return ResponseEntity.ok(serviceService.deactivateById(id));
    }

    @DeleteMapping("/{id}/hard")
    public ResponseEntity<ServiceResponseDto> hardDeleteById(@PathVariable Long id) {
        return ResponseEntity.ok(serviceService.hardDeleteById(id));
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<ServiceResponseDto> restoreById(@PathVariable Long id) {
        return ResponseEntity.ok(serviceService.restoreById(id));
    }
}
