package com.sunfeax.citeria.controller;

import java.time.DayOfWeek;
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
import com.sunfeax.citeria.dto.workinghours.WorkingHoursPatchRequestDto;
import com.sunfeax.citeria.dto.workinghours.WorkingHoursPostRequestDto;
import com.sunfeax.citeria.dto.workinghours.WorkingHoursResponseDto;
import com.sunfeax.citeria.service.WorkingHoursService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/working-hours")
@RequiredArgsConstructor
public class WorkingHoursController {

    private final WorkingHoursService workingHoursService;

    @GetMapping
    public PageResponseDto<WorkingHoursResponseDto> list(
        @RequestParam(required = false) UUID specialistId,
        @RequestParam(required = false) DayOfWeek dayOfWeek,
        @RequestParam(required = false) Boolean active,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return workingHoursService.list(specialistId, dayOfWeek, active, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkingHoursResponseDto> getWorkingHoursById(@PathVariable UUID id) {
        return ResponseEntity.ok(workingHoursService.getById(id));
    }

    @PostMapping
    public ResponseEntity<WorkingHoursResponseDto> create(@Valid @RequestBody WorkingHoursPostRequestDto request) {
        WorkingHoursResponseDto response = workingHoursService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<WorkingHoursResponseDto> update(
        @PathVariable UUID id,
        @Valid @RequestBody WorkingHoursPatchRequestDto request
    ) {
        return ResponseEntity.ok(workingHoursService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<WorkingHoursResponseDto> deleteById(@PathVariable UUID id) {
        return ResponseEntity.ok(workingHoursService.deleteById(id));
    }
}
