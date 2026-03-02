package com.sunfeax.citeria.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sunfeax.citeria.dto.business.BusinessResponseDto;
import com.sunfeax.citeria.service.BusinessService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/businesses")
@RequiredArgsConstructor
public class BusinessController {

    private final BusinessService businessService;

    @GetMapping
    public List<BusinessResponseDto> getBusinesses() {
        return businessService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BusinessResponseDto> getBusinessById(@PathVariable Long id) {
        return ResponseEntity.of(businessService.getById(id));
    }
}
