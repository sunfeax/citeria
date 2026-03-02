package com.sunfeax.citeria.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunfeax.citeria.dto.service.ServiceResponseDto;
import com.sunfeax.citeria.mapper.ServiceMapper;
import com.sunfeax.citeria.repository.ServiceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceService {

    private final ServiceRepository serviceRepository;
    private final ServiceMapper serviceMapper;

    @Transactional(readOnly = true)
    public List<ServiceResponseDto> getAll() {
        return serviceRepository.findAll()
            .stream()
            .map(serviceMapper::toResponseDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public Optional<ServiceResponseDto> getById(Long id) {
        return serviceRepository.findById(id)
            .map(serviceMapper::toResponseDto);
    }
}
