package com.sunfeax.citeria.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunfeax.citeria.dto.business.BusinessResponseDto;
import com.sunfeax.citeria.mapper.BusinessMapper;
import com.sunfeax.citeria.repository.BusinessRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final BusinessMapper businessMapper;

    @Transactional(readOnly = true)
    public List<BusinessResponseDto> getAll() {
        return businessRepository.findAll()
            .stream()
            .map(businessMapper::toResponseDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public Optional<BusinessResponseDto> getById(Long id) {
        return businessRepository.findById(id)
            .map(businessMapper::toResponseDto);
    }
}
