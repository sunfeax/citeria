package com.sunfeax.citeria.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.sunfeax.citeria.dto.business.BusinessPatchRequestDto;
import com.sunfeax.citeria.dto.business.BusinessPostRequestDto;
import com.sunfeax.citeria.dto.business.BusinessResponseDto;
import com.sunfeax.citeria.dto.common.PageResponseDto;
import com.sunfeax.citeria.entity.BusinessEntity;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.exception.ResourceNotFoundException;
import com.sunfeax.citeria.repository.BusinessRepository;
import com.sunfeax.citeria.repository.UserRepository;
import com.sunfeax.citeria.mapper.BusinessMapper;
import com.sunfeax.citeria.normalizer.BusinessFieldNormalizer;
import com.sunfeax.citeria.security.CurrentUserProvider;
import com.sunfeax.citeria.util.PageableUtil;
import com.sunfeax.citeria.validation.BusinessValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final BusinessMapper businessMapper;
    private final UserRepository userRepository;
    private final BusinessFieldNormalizer businessFieldNormalizer;
    private final BusinessValidator businessValidator;
    private final CurrentUserProvider currentUserProvider;

    private static final Set<String> SORTABLE = Set.of("name", "createdAt", "updatedAt");
    private static final Sort DEFAULT_SORT = Sort.by("name");

    @Transactional(readOnly = true)
    public PageResponseDto<BusinessResponseDto> list(String search, Boolean active, Pageable pageable) {
        List<Specification<BusinessEntity>> specs = new ArrayList<>();
        if (active != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("isActive"), active));
        }
        if (StringUtils.hasText(search)) {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            specs.add((root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern));
        }

        Pageable sanitized = PageableUtil.sanitizeSort(pageable, SORTABLE, DEFAULT_SORT);
        Page<BusinessResponseDto> page = businessRepository.findAll(Specification.allOf(specs), sanitized)
            .map(businessMapper::toResponseDto);

        return PageResponseDto.from(page);
    }

    @Transactional(readOnly = true)
    public BusinessResponseDto getById(UUID id) {
        return businessRepository.findById(id)
            .map(businessMapper::toResponseDto)
            .orElseThrow(() -> new ResourceNotFoundException("Business with id " + id + " not found"));
    }

    @Transactional
    public BusinessResponseDto create(BusinessPostRequestDto request) {
        BusinessPostRequestDto normalizedRequest = businessFieldNormalizer.normalizePostRequest(request);
        businessValidator.validateCreate(normalizedRequest);

        UserEntity owner = currentUserProvider.getCurrentUser();

        BusinessEntity entity = businessMapper.createEntity(normalizedRequest, owner);
        BusinessEntity saved = businessRepository.save(entity);

        return businessMapper.toResponseDto(saved);
    }

    @Transactional
    public BusinessResponseDto update(UUID id, BusinessPatchRequestDto request) {
        BusinessEntity entity = findBusinessOrThrow(id);
        currentUserProvider.requireSelfOrAdmin(entity.getOwner().getId());

        BusinessPatchRequestDto normalizedRequest = businessFieldNormalizer.normalizePatchRequest(request);
        businessValidator.validateUpdate(id, entity, normalizedRequest);

        UserEntity owner = normalizedRequest.ownerId() == null
            ? null
            : findOwnerOrThrow(normalizedRequest.ownerId());

        businessMapper.applyPatch(entity, normalizedRequest, owner);
        BusinessEntity saved = businessRepository.save(entity);

        return businessMapper.toResponseDto(saved);
    }

    @Transactional
    public BusinessResponseDto deactivateById(UUID id) {
        BusinessEntity business = findBusinessOrThrow(id);
        currentUserProvider.requireSelfOrAdmin(business.getOwner().getId());

        business.setActive(false);
        BusinessEntity saved = businessRepository.save(business);

        return businessMapper.toResponseDto(saved);
    }

    @Transactional
    public BusinessResponseDto hardDeleteById(UUID id) {
        BusinessEntity business = findBusinessOrThrow(id);
        currentUserProvider.requireSelfOrAdmin(business.getOwner().getId());

        BusinessResponseDto deletedBusiness = businessMapper.toResponseDto(business);
        businessRepository.delete(business);

        return deletedBusiness;
    }

    @Transactional
    public BusinessResponseDto restoreById(UUID id) {
        BusinessEntity business = findBusinessOrThrow(id);
        currentUserProvider.requireSelfOrAdmin(business.getOwner().getId());

        business.setActive(true);
        BusinessEntity saved = businessRepository.save(business);

        return businessMapper.toResponseDto(saved);
    }

    private UserEntity findOwnerOrThrow(UUID ownerId) {
        return userRepository.findById(ownerId)
            .orElseThrow(() -> new ResourceNotFoundException("Owner user with id " + ownerId + " not found"));
    }

    private BusinessEntity findBusinessOrThrow(UUID id) {
        return businessRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Business with id " + id + " not found"));
    }
}
