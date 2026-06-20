package com.sunfeax.citeria.service;

import java.util.UUID;
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
import com.sunfeax.citeria.exception.ResourceNotFoundException;
import com.sunfeax.citeria.repository.BusinessRepository;
import com.sunfeax.citeria.repository.ServiceRepository;
import com.sunfeax.citeria.repository.SpecialistServiceRepository;
import com.sunfeax.citeria.repository.UserRepository;
import com.sunfeax.citeria.mapper.SpecialistServiceMapper;
import com.sunfeax.citeria.normalizer.SpecialistServiceFieldNormalizer;
import com.sunfeax.citeria.security.CurrentUserProvider;
import com.sunfeax.citeria.validation.SpecialistServiceValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.sunfeax.citeria.dto.common.PageResponseDto;
import com.sunfeax.citeria.util.PageableUtil;

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
    private final SpecialistServiceValidator specialistServiceValidator;
    private final CurrentUserProvider currentUserProvider;

    private static final Set<String> SORTABLE = Set.of("createdAt");
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");

    @Transactional(readOnly = true)
    public PageResponseDto<SpecialistServiceResponseDto> list(
        UUID businessId,
        UUID specialistId,
        UUID serviceId,
        Boolean active,
        Pageable pageable
    ) {
        List<Specification<SpecialistServiceEntity>> specs = new ArrayList<>();
        if (businessId != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("business").get("id"), businessId));
        }
        if (specialistId != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("specialist").get("id"), specialistId));
        }
        if (serviceId != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("service").get("id"), serviceId));
        }
        if (active != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("isActive"), active));
        }

        Pageable sanitized = PageableUtil.sanitizeSort(pageable, SORTABLE, DEFAULT_SORT);
        Page<SpecialistServiceResponseDto> page =
            specialistServiceRepository.findAll(Specification.allOf(specs), sanitized)
                .map(specialistServiceMapper::toResponseDto);

        return PageResponseDto.from(page);
    }

    @Transactional(readOnly = true)
    public SpecialistServiceResponseDto getById(UUID id) {
        return specialistServiceRepository.findById(id)
            .map(specialistServiceMapper::toResponseDto)
            .orElseThrow(() -> new ResourceNotFoundException("Specialist service with id " + id + " not found"));
    }

    @Transactional
    public SpecialistServiceResponseDto register(SpecialistServicePostRequestDto request) {
        SpecialistServicePostRequestDto normalizedRequest = specialistServiceFieldNormalizer.normalizePostRequest(request);

        BusinessEntity business = findBusinessOrThrow(normalizedRequest.businessId());
        currentUserProvider.requireSelfOrAdmin(business.getOwner().getId());
        UserEntity specialist = findUserOrThrow(normalizedRequest.specialistId());
        ServiceEntity service = findServiceOrThrow(normalizedRequest.serviceId());
        specialistServiceValidator.validateRegister(normalizedRequest, business, specialist, service);

        SpecialistServiceEntity entity = specialistServiceMapper.createEntity(normalizedRequest, business, specialist, service);
        SpecialistServiceEntity saved = specialistServiceRepository.save(entity);

        return specialistServiceMapper.toResponseDto(saved);
    }

    @Transactional
    public SpecialistServiceResponseDto update(UUID id, SpecialistServicePatchRequestDto request) {
        SpecialistServiceEntity entity = findSpecialistServiceOrThrow(id);
        currentUserProvider.requireSelfOrAdmin(entity.getBusiness().getOwner().getId());

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
        specialistServiceValidator.validateUpdate(id, entity, normalizedRequest, targetBusiness, targetSpecialist, targetService);

        BusinessEntity businessToApply = normalizedRequest.businessId() == null ? null : targetBusiness;
        UserEntity specialistToApply = normalizedRequest.specialistId() == null ? null : targetSpecialist;
        ServiceEntity serviceToApply = normalizedRequest.serviceId() == null ? null : targetService;

        specialistServiceMapper.applyPatch(entity, normalizedRequest, businessToApply, specialistToApply, serviceToApply);
        SpecialistServiceEntity saved = specialistServiceRepository.save(entity);

        return specialistServiceMapper.toResponseDto(saved);
    }

    @Transactional
    public SpecialistServiceResponseDto deactivateById(UUID id) {
        SpecialistServiceEntity specialistService = findSpecialistServiceOrThrow(id);
        currentUserProvider.requireSelfOrAdmin(specialistService.getBusiness().getOwner().getId());

        specialistService.setActive(false);
        SpecialistServiceEntity saved = specialistServiceRepository.save(specialistService);

        return specialistServiceMapper.toResponseDto(saved);
    }

    @Transactional
    public SpecialistServiceResponseDto hardDeleteById(UUID id) {
        SpecialistServiceEntity specialistService = findSpecialistServiceOrThrow(id);
        currentUserProvider.requireSelfOrAdmin(specialistService.getBusiness().getOwner().getId());

        SpecialistServiceResponseDto deletedSpecialistService = specialistServiceMapper.toResponseDto(specialistService);
        specialistServiceRepository.delete(specialistService);

        return deletedSpecialistService;
    }

    @Transactional
    public SpecialistServiceResponseDto restoreById(UUID id) {
        SpecialistServiceEntity specialistService = findSpecialistServiceOrThrow(id);
        currentUserProvider.requireSelfOrAdmin(specialistService.getBusiness().getOwner().getId());

        specialistService.setActive(true);
        SpecialistServiceEntity saved = specialistServiceRepository.save(specialistService);

        return specialistServiceMapper.toResponseDto(saved);
    }

    private SpecialistServiceEntity findSpecialistServiceOrThrow(UUID id) {
        return specialistServiceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Specialist service with id " + id + " not found"));
    }

    private BusinessEntity findBusinessOrThrow(UUID businessId) {
        return businessRepository.findById(businessId)
            .orElseThrow(() -> new ResourceNotFoundException("Business with id " + businessId + " not found"));
    }

    private UserEntity findUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " not found"));
    }

    private ServiceEntity findServiceOrThrow(UUID serviceId) {
        return serviceRepository.findById(serviceId)
            .orElseThrow(() -> new ResourceNotFoundException("Service with id " + serviceId + " not found"));
    }
}
