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
import com.sunfeax.citeria.dto.offering.OfferingPatchRequestDto;
import com.sunfeax.citeria.dto.offering.OfferingPostRequestDto;
import com.sunfeax.citeria.dto.offering.OfferingResponseDto;
import com.sunfeax.citeria.dto.slot.SlotResponseDto;
import com.sunfeax.citeria.service.OfferingService;
import com.sunfeax.citeria.service.SlotService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/offerings")
@RequiredArgsConstructor
public class OfferingController {

    private final OfferingService offeringService;
    private final SlotService slotService;

    @GetMapping
    public PageResponseDto<OfferingResponseDto> list(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) UUID specialistId,
        @RequestParam(defaultValue = "true") Boolean active,
        @RequestParam(required = false) BigDecimal minPrice,
        @RequestParam(required = false) BigDecimal maxPrice,
        @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return offeringService.list(search, specialistId, active, minPrice, maxPrice, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfferingResponseDto> getOfferingById(@PathVariable UUID id) {
        return ResponseEntity.ok(offeringService.getById(id));
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
    public ResponseEntity<OfferingResponseDto> create(@Valid @RequestBody OfferingPostRequestDto request) {
        OfferingResponseDto response = offeringService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<OfferingResponseDto> update(
        @PathVariable UUID id,
        @Valid @RequestBody OfferingPatchRequestDto request
    ) {
        return ResponseEntity.ok(offeringService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<OfferingResponseDto> deactivateById(@PathVariable UUID id) {
        return ResponseEntity.ok(offeringService.deactivateById(id));
    }

    @DeleteMapping("/{id}/hard")
    public ResponseEntity<OfferingResponseDto> hardDeleteById(@PathVariable UUID id) {
        return ResponseEntity.ok(offeringService.hardDeleteById(id));
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<OfferingResponseDto> restoreById(@PathVariable UUID id) {
        return ResponseEntity.ok(offeringService.restoreById(id));
    }
}
