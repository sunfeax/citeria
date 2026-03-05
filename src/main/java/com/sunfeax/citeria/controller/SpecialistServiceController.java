package com.sunfeax.citeria.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sunfeax.citeria.dto.specialistservice.SpecialistServiceResponseDto;
import com.sunfeax.citeria.service.SpecialistServiceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/specialist-services")
@RequiredArgsConstructor
public class SpecialistServiceController {

    private final SpecialistServiceService specialistServiceService;

    @GetMapping
    public List<SpecialistServiceResponseDto> getSpecialistServices() {
        return specialistServiceService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpecialistServiceResponseDto> getSpecialistServiceById(@PathVariable Long id) {
        return ResponseEntity.of(specialistServiceService.getById(id));
    }
}
