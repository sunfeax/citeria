package com.sunfeax.citeria.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunfeax.citeria.dto.service.ServicePatchRequestDto;
import com.sunfeax.citeria.dto.service.ServicePostRequestDto;
import com.sunfeax.citeria.dto.service.ServiceResponseDto;
import com.sunfeax.citeria.entity.BusinessEntity;
import com.sunfeax.citeria.entity.ServiceEntity;
import com.sunfeax.citeria.exception.ResourceNotFoundException;
import com.sunfeax.citeria.mapper.ServiceMapper;
import com.sunfeax.citeria.repository.BusinessRepository;
import com.sunfeax.citeria.repository.ServiceRepository;
import com.sunfeax.citeria.validation.ServiceFieldNormalizer;
import com.sunfeax.citeria.validation.ValidationResult;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceService {

    private final ServiceRepository serviceRepository;
    private final ServiceMapper serviceMapper;
    private final BusinessRepository businessRepository;
    private final ServiceFieldNormalizer serviceFieldNormalizer;

    @Transactional(readOnly = true)
    public Page<ServiceResponseDto> getAll(Pageable pageable) {
        Page<ServiceEntity> servicePage = serviceRepository.findAll(pageable);
        return servicePage.map(serviceMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    public ServiceResponseDto getById(Long id) {
        return serviceRepository.findById(id)
            .map(serviceMapper::toResponseDto)
            .orElseThrow(() -> new ResourceNotFoundException("Service with id " + id + " not found"));
    }

    @Transactional
    public ServiceResponseDto create(ServicePostRequestDto request) {
        ServicePostRequestDto normalizedRequest = serviceFieldNormalizer.normalizePostRequest(request);

        new ValidationResult()
            .addErrorIf(
                serviceRepository.existsByBusinessIdAndNameIgnoreCase(
                    normalizedRequest.businessId(),
                    normalizedRequest.name()
                ),
                "name",
                "Service with name " + normalizedRequest.name() + " already exists in this business"
            )
            .throwIfHasErrors();

        BusinessEntity business = findBusinessOrThrow(normalizedRequest.businessId());

        ServiceEntity entity = serviceMapper.createEntity(normalizedRequest, business);
        ServiceEntity saved = serviceRepository.save(entity);

        return serviceMapper.toResponseDto(saved);
    }

    @Transactional
    public ServiceResponseDto update(Long id, ServicePatchRequestDto request) {
        ServiceEntity entity = findServiceOrThrow(id);

        ServicePatchRequestDto normalizedRequest = serviceFieldNormalizer.normalizePatchRequest(request);

        Long targetBusinessId = normalizedRequest.businessId() == null
            ? entity.getBusiness().getId()
            : normalizedRequest.businessId();
        String targetServiceName = normalizedRequest.name() == null
            ? entity.getName()
            : normalizedRequest.name();

        new ValidationResult()
            .addErrorIf(!serviceMapper.hasAnyPatchField(normalizedRequest), "request", "No fields to update")
            .addErrorIf(
                serviceRepository.existsByBusinessIdAndNameIgnoreCaseAndIdNot(
                    targetBusinessId,
                    targetServiceName,
                    id
                ),
                "name",
                "Service with name " + targetServiceName + " already exists in this business"
            )
            .throwIfHasErrors();

        BusinessEntity business = normalizedRequest.businessId() == null
            ? null
            : findBusinessOrThrow(normalizedRequest.businessId());

        serviceMapper.applyPatch(entity, normalizedRequest, business);
        ServiceEntity saved = serviceRepository.save(entity);

        return serviceMapper.toResponseDto(saved);
    }

    @Transactional
    public ServiceResponseDto deactivateById(Long id) {
        ServiceEntity service = findServiceOrThrow(id);

        service.setActive(false);
        ServiceEntity saved = serviceRepository.save(service);

        return serviceMapper.toResponseDto(saved);
    }

    @Transactional
    public ServiceResponseDto hardDeleteById(Long id) {
        ServiceEntity service = findServiceOrThrow(id);

        ServiceResponseDto deletedService = serviceMapper.toResponseDto(service);
        serviceRepository.delete(service);

        return deletedService;
    }

    @Transactional
    public ServiceResponseDto restoreById(Long id) {
        ServiceEntity service = findServiceOrThrow(id);

        service.setActive(true);
        ServiceEntity saved = serviceRepository.save(service);

        return serviceMapper.toResponseDto(saved);
    }

    private BusinessEntity findBusinessOrThrow(Long businessId) {
        return businessRepository.findById(businessId)
            .orElseThrow(() -> new ResourceNotFoundException("Business with id " + businessId + " not found"));
    }

    private ServiceEntity findServiceOrThrow(Long id) {
        return serviceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Service with id " + id + " not found"));
    }
}
