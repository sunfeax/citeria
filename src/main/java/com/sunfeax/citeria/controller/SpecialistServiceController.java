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

import com.sunfeax.citeria.dto.specialistservice.SpecialistServicePatchRequestDto;
import com.sunfeax.citeria.dto.specialistservice.SpecialistServicePostRequestDto;
import com.sunfeax.citeria.dto.specialistservice.SpecialistServiceResponseDto;
import com.sunfeax.citeria.service.SpecialistServiceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/specialist-services")
@RequiredArgsConstructor
public class SpecialistServiceController {

    private final SpecialistServiceService specialistServiceService;

    @GetMapping
    public Page<SpecialistServiceResponseDto> getSpecialistServices(
        @PageableDefault(
            size = 20,
            sort = "id",
            direction = Sort.Direction.ASC
        ) Pageable pageable
    ) {
        return specialistServiceService.getAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpecialistServiceResponseDto> getSpecialistServiceById(@PathVariable Long id) {
        return ResponseEntity.ok(specialistServiceService.getById(id));
    }

    @PostMapping
    public ResponseEntity<SpecialistServiceResponseDto> register(
        @Valid @RequestBody SpecialistServicePostRequestDto request
    ) {
        SpecialistServiceResponseDto response = specialistServiceService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SpecialistServiceResponseDto> update(
        @PathVariable Long id,
        @Valid @RequestBody SpecialistServicePatchRequestDto request
    ) {
        return ResponseEntity.ok(specialistServiceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SpecialistServiceResponseDto> deactivateById(@PathVariable Long id) {
        return ResponseEntity.ok(specialistServiceService.deactivateById(id));
    }

    @DeleteMapping("/{id}/hard")
    public ResponseEntity<SpecialistServiceResponseDto> hardDeleteById(@PathVariable Long id) {
        return ResponseEntity.ok(specialistServiceService.hardDeleteById(id));
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<SpecialistServiceResponseDto> restoreById(@PathVariable Long id) {
        return ResponseEntity.ok(specialistServiceService.restoreById(id));
    }
}
