package com.sunfeax.citeria.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunfeax.citeria.dto.business.BusinessPatchRequestDto;
import com.sunfeax.citeria.dto.business.BusinessPostRequestDto;
import com.sunfeax.citeria.dto.business.BusinessResponseDto;
import com.sunfeax.citeria.entity.BusinessEntity;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.exception.ResourceNotFoundException;
import com.sunfeax.citeria.mapper.BusinessMapper;
import com.sunfeax.citeria.repository.BusinessRepository;
import com.sunfeax.citeria.repository.UserRepository;
import com.sunfeax.citeria.validation.BusinessFieldNormalizer;
import com.sunfeax.citeria.validation.ValidationResult;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final BusinessMapper businessMapper;
    private final UserRepository userRepository;
    private final BusinessFieldNormalizer businessFieldNormalizer;

    @Transactional(readOnly = true)
    public Page<BusinessResponseDto> getAll(Pageable pageable) {
        Page<BusinessEntity> businessPage = businessRepository.findAll(pageable);
        return businessPage.map(businessMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    public BusinessResponseDto getById(Long id) {
        return businessRepository.findById(id)
            .map(businessMapper::toResponseDto)
            .orElseThrow(() -> new ResourceNotFoundException("Business with id " + id + " not found"));
    }

    @Transactional
    public BusinessResponseDto register(BusinessPostRequestDto request) {
        BusinessPostRequestDto normalizedRequest = businessFieldNormalizer.normalizePostRequest(request);

        new ValidationResult()
            .addErrorIf(
                businessRepository.existsByNameIgnoreCase(normalizedRequest.name()),
                "name",
                "Business with name " + normalizedRequest.name() + " already exists"
            )
            .throwIfHasErrors();

        UserEntity owner = findOwnerOrThrow(normalizedRequest.ownerId());

        BusinessEntity entity = businessMapper.createEntity(normalizedRequest, owner);
        BusinessEntity saved = businessRepository.save(entity);

        return businessMapper.toResponseDto(saved);
    }

    @Transactional
    public BusinessResponseDto update(Long id, BusinessPatchRequestDto request) {
        BusinessEntity entity = findBusinessOrThrow(id);

        BusinessPatchRequestDto normalizedRequest = businessFieldNormalizer.normalizePatchRequest(request);

        new ValidationResult()
            .addErrorIf(!businessMapper.hasAnyPatchField(normalizedRequest), "request", "No fields to update")
            .addErrorIf(
                normalizedRequest.name() != null
                    && businessRepository.existsByNameIgnoreCaseAndIdNot(normalizedRequest.name(), id),
                "name",
                "Business with name " + normalizedRequest.name() + " already exists"
            )
            .throwIfHasErrors();

        UserEntity owner = normalizedRequest.ownerId() == null
            ? null
            : findOwnerOrThrow(normalizedRequest.ownerId());

        businessMapper.applyPatch(entity, normalizedRequest, owner);
        BusinessEntity saved = businessRepository.save(entity);

        return businessMapper.toResponseDto(saved);
    }

    @Transactional
    public BusinessResponseDto deactivateById(Long id) {
        BusinessEntity business = findBusinessOrThrow(id);

        business.setActive(false);
        BusinessEntity saved = businessRepository.save(business);

        return businessMapper.toResponseDto(saved);
    }

    @Transactional
    public BusinessResponseDto hardDeleteById(Long id) {
        BusinessEntity business = findBusinessOrThrow(id);

        BusinessResponseDto deletedBusiness = businessMapper.toResponseDto(business);
        businessRepository.delete(business);

        return deletedBusiness;
    }

    @Transactional
    public BusinessResponseDto restoreById(Long id) {
        BusinessEntity business = findBusinessOrThrow(id);

        business.setActive(true);
        BusinessEntity saved = businessRepository.save(business);

        return businessMapper.toResponseDto(saved);
    }

    private UserEntity findOwnerOrThrow(Long ownerId) {
        return userRepository.findById(ownerId)
            .orElseThrow(() -> new ResourceNotFoundException("Owner user with id " + ownerId + " not found"));
    }

    private BusinessEntity findBusinessOrThrow(Long id) {
        return businessRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Business with id " + id + " not found"));
    }
}
