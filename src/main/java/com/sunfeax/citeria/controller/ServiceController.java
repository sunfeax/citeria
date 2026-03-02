package com.sunfeax.citeria.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sunfeax.citeria.dto.service.ServiceResponseDto;
import com.sunfeax.citeria.service.ServiceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceService serviceService;

    @GetMapping
    public List<ServiceResponseDto> getServices() {
        return serviceService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponseDto> getServiceById(@PathVariable Long id) {
        return ResponseEntity.of(serviceService.getById(id));
    }
}
