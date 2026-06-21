package com.sunfeax.citeria.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sunfeax.citeria.dto.specialist.SpecialistDetailResponseDto;
import com.sunfeax.citeria.service.SpecialistDetailService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/specialist-detail")
@RequiredArgsConstructor
public class SpecialistDetailController {

    private final SpecialistDetailService specialistDetailService;

    @GetMapping("/{id}")
    public ResponseEntity<SpecialistDetailResponseDto> getDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(specialistDetailService.getDetail(id));
    }
}
