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

import com.sunfeax.citeria.dto.business.BusinessPatchRequestDto;
import com.sunfeax.citeria.dto.business.BusinessPostRequestDto;
import com.sunfeax.citeria.dto.business.BusinessResponseDto;
import com.sunfeax.citeria.service.BusinessService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/businesses")
@RequiredArgsConstructor
public class BusinessController {

    private final BusinessService businessService;

    @GetMapping
    public Page<BusinessResponseDto> getBusinesses(
        @PageableDefault(
            size = 20,
            sort = "id",
            direction = Sort.Direction.ASC
        ) Pageable pageable
    ) {
        return businessService.getAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BusinessResponseDto> getBusinessById(@PathVariable Long id) {
        return ResponseEntity.ok(businessService.getById(id));
    }

    @PostMapping
    public ResponseEntity<BusinessResponseDto> register(@Valid @RequestBody BusinessPostRequestDto request) {
        BusinessResponseDto response = businessService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BusinessResponseDto> update(
        @PathVariable Long id,
        @Valid @RequestBody BusinessPatchRequestDto request
    ) {
        return ResponseEntity.ok(businessService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BusinessResponseDto> deactivateById(@PathVariable Long id) {
        return ResponseEntity.ok(businessService.deactivateById(id));
    }

    @DeleteMapping("/{id}/hard")
    public ResponseEntity<BusinessResponseDto> hardDeleteById(@PathVariable Long id) {
        return ResponseEntity.ok(businessService.hardDeleteById(id));
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<BusinessResponseDto> restoreById(@PathVariable Long id) {
        return ResponseEntity.ok(businessService.restoreById(id));
    }
}
