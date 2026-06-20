package com.sunfeax.citeria.controller;

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
import com.sunfeax.citeria.dto.slot.SlotResponseDto;
import com.sunfeax.citeria.dto.specialistservice.SpecialistServicePatchRequestDto;
import com.sunfeax.citeria.dto.specialistservice.SpecialistServicePostRequestDto;
import com.sunfeax.citeria.dto.specialistservice.SpecialistServiceResponseDto;
import com.sunfeax.citeria.service.SlotService;
import com.sunfeax.citeria.service.SpecialistServiceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/specialist-services")
@RequiredArgsConstructor
public class SpecialistServiceController {

    private final SpecialistServiceService specialistServiceService;
    private final SlotService slotService;

    @GetMapping
    public PageResponseDto<SpecialistServiceResponseDto> list(
        @RequestParam(required = false) UUID businessId,
        @RequestParam(required = false) UUID specialistId,
        @RequestParam(required = false) UUID serviceId,
        @RequestParam(required = false) Boolean active,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return specialistServiceService.list(businessId, specialistId, serviceId, active, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpecialistServiceResponseDto> getSpecialistServiceById(@PathVariable UUID id) {
        return ResponseEntity.ok(specialistServiceService.getById(id));
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
    public ResponseEntity<SpecialistServiceResponseDto> register(
        @Valid @RequestBody SpecialistServicePostRequestDto request
    ) {
        SpecialistServiceResponseDto response = specialistServiceService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SpecialistServiceResponseDto> update(
        @PathVariable UUID id,
        @Valid @RequestBody SpecialistServicePatchRequestDto request
    ) {
        return ResponseEntity.ok(specialistServiceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SpecialistServiceResponseDto> deactivateById(@PathVariable UUID id) {
        return ResponseEntity.ok(specialistServiceService.deactivateById(id));
    }

    @DeleteMapping("/{id}/hard")
    public ResponseEntity<SpecialistServiceResponseDto> hardDeleteById(@PathVariable UUID id) {
        return ResponseEntity.ok(specialistServiceService.hardDeleteById(id));
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<SpecialistServiceResponseDto> restoreById(@PathVariable UUID id) {
        return ResponseEntity.ok(specialistServiceService.restoreById(id));
    }
}
