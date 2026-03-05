package com.sunfeax.citeria.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunfeax.citeria.dto.specialistservice.SpecialistServiceResponseDto;
import com.sunfeax.citeria.mapper.SpecialistServiceMapper;
import com.sunfeax.citeria.repository.SpecialistServiceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SpecialistServiceService {

    private final SpecialistServiceRepository specialistServiceRepository;
    private final SpecialistServiceMapper specialistServiceMapper;

    @Transactional(readOnly = true)
    public List<SpecialistServiceResponseDto> getAll() {
        return specialistServiceRepository.findAll()
            .stream()
            .map(specialistServiceMapper::toResponseDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public Optional<SpecialistServiceResponseDto> getById(Long id) {
        return specialistServiceRepository.findById(id)
            .map(specialistServiceMapper::toResponseDto);
    }
}
