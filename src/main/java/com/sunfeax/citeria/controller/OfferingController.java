package com.sunfeax.citeria.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sunfeax.citeria.dto.offering.OfferingResponseDto;
import com.sunfeax.citeria.service.OfferingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/offerings")
@RequiredArgsConstructor
public class OfferingController {

    private final OfferingService offeringService;

    @GetMapping
    public List<OfferingResponseDto> getOfferings() {
        return offeringService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfferingResponseDto> getOfferingById(@PathVariable Long id) {
        return ResponseEntity.of(offeringService.getById(id));
    }
}
