package com.sunfeax.citeria.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunfeax.citeria.dto.specialistservice.SpecialistServicePatchRequestDto;
import com.sunfeax.citeria.dto.specialistservice.SpecialistServicePostRequestDto;
import com.sunfeax.citeria.dto.specialistservice.SpecialistServiceResponseDto;
import com.sunfeax.citeria.entity.BusinessEntity;
import com.sunfeax.citeria.entity.ServiceEntity;
import com.sunfeax.citeria.entity.SpecialistServiceEntity;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.enums.UserType;
import com.sunfeax.citeria.exception.ResourceNotFoundException;
import com.sunfeax.citeria.mapper.SpecialistServiceMapper;
import com.sunfeax.citeria.repository.BusinessRepository;
import com.sunfeax.citeria.repository.ServiceRepository;
import com.sunfeax.citeria.repository.SpecialistServiceRepository;
import com.sunfeax.citeria.repository.UserRepository;
import com.sunfeax.citeria.validation.SpecialistServiceFieldNormalizer;
import com.sunfeax.citeria.validation.ValidationResult;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SpecialistServiceService {

    private final SpecialistServiceRepository specialistServiceRepository;
    private final SpecialistServiceMapper specialistServiceMapper;
    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final SpecialistServiceFieldNormalizer specialistServiceFieldNormalizer;

    @Transactional(readOnly = true)
    public Page<SpecialistServiceResponseDto> getAll(Pageable pageable) {
        Page<SpecialistServiceEntity> specialistServicePage = specialistServiceRepository.findAll(pageable);
        return specialistServicePage.map(specialistServiceMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    public SpecialistServiceResponseDto getById(Long id) {
        return specialistServiceRepository.findById(id)
            .map(specialistServiceMapper::toResponseDto)
            .orElseThrow(() -> new ResourceNotFoundException("Specialist service with id " + id + " not found"));
    }

    @Transactional
    public SpecialistServiceResponseDto register(SpecialistServicePostRequestDto request) {
        SpecialistServicePostRequestDto normalizedRequest = specialistServiceFieldNormalizer.normalizePostRequest(request);

        BusinessEntity business = findBusinessOrThrow(normalizedRequest.businessId());
        UserEntity specialist = findUserOrThrow(normalizedRequest.specialistId());
        ServiceEntity service = findServiceOrThrow(normalizedRequest.serviceId());

        new ValidationResult()
            .addErrorIf(
                specialistServiceRepository.existsByBusinessIdAndSpecialistIdAndServiceId(
                    normalizedRequest.businessId(),
                    normalizedRequest.specialistId(),
                    normalizedRequest.serviceId()
                ),
                "specialistService",
                "Specialist service for the same business, specialist and service already exists"
            )
            .addErrorIf(specialist.getType() != UserType.SPECIALIST, "specialistId", "User must have SPECIALIST type")
            .addErrorIf(!specialist.isActive(), "specialistId", "Specialist must be active")
            .addErrorIf(!business.isActive(), "businessId", "Business must be active")
            .addErrorIf(!service.isActive(), "serviceId", "Service must be active")
            .addErrorIf(
                !service.getBusiness().getId().equals(business.getId()),
                "serviceId",
                "Service does not belong to provided business"
            )
            .throwIfHasErrors();

        SpecialistServiceEntity entity = specialistServiceMapper.createEntity(normalizedRequest, business, specialist, service);
        SpecialistServiceEntity saved = specialistServiceRepository.save(entity);

        return specialistServiceMapper.toResponseDto(saved);
    }

    @Transactional
    public SpecialistServiceResponseDto update(Long id, SpecialistServicePatchRequestDto request) {
        SpecialistServiceEntity entity = findSpecialistServiceOrThrow(id);

        SpecialistServicePatchRequestDto normalizedRequest = specialistServiceFieldNormalizer.normalizePatchRequest(request);

        BusinessEntity targetBusiness = normalizedRequest.businessId() == null
            ? entity.getBusiness()
            : findBusinessOrThrow(normalizedRequest.businessId());

        UserEntity targetSpecialist = normalizedRequest.specialistId() == null
            ? entity.getSpecialist()
            : findUserOrThrow(normalizedRequest.specialistId());

        ServiceEntity targetService = normalizedRequest.serviceId() == null
            ? entity.getService()
            : findServiceOrThrow(normalizedRequest.serviceId());

        new ValidationResult()
            .addErrorIf(!specialistServiceMapper.hasAnyPatchField(normalizedRequest), "request", "No fields to update")
            .addErrorIf(
                specialistServiceRepository.existsByBusinessIdAndSpecialistIdAndServiceIdAndIdNot(
                    targetBusiness.getId(),
                    targetSpecialist.getId(),
                    targetService.getId(),
                    id
                ),
                "specialistService",
                "Specialist service for the same business, specialist and service already exists"
            )
            .addErrorIf(targetSpecialist.getType() != UserType.SPECIALIST, "specialistId", "User must have SPECIALIST type")
            .addErrorIf(!targetSpecialist.isActive(), "specialistId", "Specialist must be active")
            .addErrorIf(!targetBusiness.isActive(), "businessId", "Business must be active")
            .addErrorIf(!targetService.isActive(), "serviceId", "Service must be active")
            .addErrorIf(
                !targetService.getBusiness().getId().equals(targetBusiness.getId()),
                "serviceId",
                "Service does not belong to provided business"
            )
            .throwIfHasErrors();

        BusinessEntity businessToApply = normalizedRequest.businessId() == null ? null : targetBusiness;
        UserEntity specialistToApply = normalizedRequest.specialistId() == null ? null : targetSpecialist;
        ServiceEntity serviceToApply = normalizedRequest.serviceId() == null ? null : targetService;

        specialistServiceMapper.applyPatch(entity, normalizedRequest, businessToApply, specialistToApply, serviceToApply);
        SpecialistServiceEntity saved = specialistServiceRepository.save(entity);

        return specialistServiceMapper.toResponseDto(saved);
    }

    @Transactional
    public SpecialistServiceResponseDto deactivateById(Long id) {
        SpecialistServiceEntity specialistService = findSpecialistServiceOrThrow(id);

        specialistService.setActive(false);
        SpecialistServiceEntity saved = specialistServiceRepository.save(specialistService);

        return specialistServiceMapper.toResponseDto(saved);
    }

    @Transactional
    public SpecialistServiceResponseDto hardDeleteById(Long id) {
        SpecialistServiceEntity specialistService = findSpecialistServiceOrThrow(id);

        SpecialistServiceResponseDto deletedSpecialistService = specialistServiceMapper.toResponseDto(specialistService);
        specialistServiceRepository.delete(specialistService);

        return deletedSpecialistService;
    }

    @Transactional
    public SpecialistServiceResponseDto restoreById(Long id) {
        SpecialistServiceEntity specialistService = findSpecialistServiceOrThrow(id);

        specialistService.setActive(true);
        SpecialistServiceEntity saved = specialistServiceRepository.save(specialistService);

        return specialistServiceMapper.toResponseDto(saved);
    }

    private SpecialistServiceEntity findSpecialistServiceOrThrow(Long id) {
        return specialistServiceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Specialist service with id " + id + " not found"));
    }

    private BusinessEntity findBusinessOrThrow(Long businessId) {
        return businessRepository.findById(businessId)
            .orElseThrow(() -> new ResourceNotFoundException("Business with id " + businessId + " not found"));
    }

    private UserEntity findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " not found"));
    }

    private ServiceEntity findServiceOrThrow(Long serviceId) {
        return serviceRepository.findById(serviceId)
            .orElseThrow(() -> new ResourceNotFoundException("Service with id " + serviceId + " not found"));
    }
}
