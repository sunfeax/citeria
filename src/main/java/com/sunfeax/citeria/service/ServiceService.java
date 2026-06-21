package com.sunfeax.citeria.service;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunfeax.citeria.dto.service.ServicePatchRequestDto;
import com.sunfeax.citeria.dto.service.ServicePostRequestDto;
import com.sunfeax.citeria.dto.service.ServiceResponseDto;
import com.sunfeax.citeria.entity.ServiceEntity;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.exception.ResourceNotFoundException;
import com.sunfeax.citeria.repository.ServiceRepository;
import com.sunfeax.citeria.mapper.ServiceMapper;
import com.sunfeax.citeria.normalizer.ServiceFieldNormalizer;
import com.sunfeax.citeria.security.CurrentUserProvider;
import com.sunfeax.citeria.validation.ServiceValidator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.sunfeax.citeria.dto.common.PageResponseDto;
import com.sunfeax.citeria.util.PageableUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceService {

    private final ServiceRepository serviceRepository;
    private final ServiceMapper serviceMapper;
    private final ServiceFieldNormalizer serviceFieldNormalizer;
    private final ServiceValidator serviceValidator;
    private final CurrentUserProvider currentUserProvider;

    private static final Set<String> SORTABLE = Set.of("name", "priceAmount", "createdAt");
    private static final Sort DEFAULT_SORT = Sort.by("name");

    @Transactional(readOnly = true)
    public PageResponseDto<ServiceResponseDto> list(
        String search,
        UUID specialistId,
        Boolean active,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Pageable pageable
    ) {
        List<Specification<ServiceEntity>> specs = new ArrayList<>();
        if (specialistId != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("specialist").get("id"), specialistId));
        }
        if (active != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("isActive"), active));
        }
        if (minPrice != null) {
            specs.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.<BigDecimal>get("priceAmount"), minPrice));
        }
        if (maxPrice != null) {
            specs.add((root, query, cb) -> cb.lessThanOrEqualTo(root.<BigDecimal>get("priceAmount"), maxPrice));
        }
        if (StringUtils.hasText(search)) {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            specs.add((root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern));
        }

        Pageable sanitized = PageableUtil.sanitizeSort(pageable, SORTABLE, DEFAULT_SORT);
        Page<ServiceResponseDto> page = serviceRepository.findAll(Specification.allOf(specs), sanitized)
            .map(serviceMapper::toResponseDto);

        return PageResponseDto.from(page);
    }

    @Transactional(readOnly = true)
    public ServiceResponseDto getById(UUID id) {
        return serviceRepository.findById(id)
            .map(serviceMapper::toResponseDto)
            .orElseThrow(() -> new ResourceNotFoundException("Service with id " + id + " not found"));
    }

    @Transactional
    public ServiceResponseDto create(ServicePostRequestDto request) {
        ServicePostRequestDto normalizedRequest = serviceFieldNormalizer.normalizePostRequest(request);

        UserEntity specialist = currentUserProvider.getCurrentUser();
        serviceValidator.validateCreate(normalizedRequest, specialist);

        ServiceEntity entity = serviceMapper.createEntity(normalizedRequest, specialist);
        ServiceEntity saved = serviceRepository.save(entity);

        return serviceMapper.toResponseDto(saved);
    }

    @Transactional
    public ServiceResponseDto update(UUID id, ServicePatchRequestDto request) {
        ServiceEntity entity = findServiceOrThrow(id);
        currentUserProvider.requireSelfOrAdmin(entity.getSpecialist().getId());

        ServicePatchRequestDto normalizedRequest = serviceFieldNormalizer.normalizePatchRequest(request);
        serviceValidator.validateUpdate(id, entity, normalizedRequest);

        serviceMapper.applyPatch(entity, normalizedRequest);
        ServiceEntity saved = serviceRepository.save(entity);

        return serviceMapper.toResponseDto(saved);
    }

    @Transactional
    public ServiceResponseDto deactivateById(UUID id) {
        ServiceEntity service = findServiceOrThrow(id);
        currentUserProvider.requireSelfOrAdmin(service.getSpecialist().getId());

        service.setActive(false);
        ServiceEntity saved = serviceRepository.save(service);

        return serviceMapper.toResponseDto(saved);
    }

    @Transactional
    public ServiceResponseDto hardDeleteById(UUID id) {
        ServiceEntity service = findServiceOrThrow(id);
        currentUserProvider.requireSelfOrAdmin(service.getSpecialist().getId());

        ServiceResponseDto deletedService = serviceMapper.toResponseDto(service);
        serviceRepository.delete(service);

        return deletedService;
    }

    @Transactional
    public ServiceResponseDto restoreById(UUID id) {
        ServiceEntity service = findServiceOrThrow(id);
        currentUserProvider.requireSelfOrAdmin(service.getSpecialist().getId());

        service.setActive(true);
        ServiceEntity saved = serviceRepository.save(service);

        return serviceMapper.toResponseDto(saved);
    }

    private ServiceEntity findServiceOrThrow(UUID id) {
        return serviceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Service with id " + id + " not found"));
    }
}
