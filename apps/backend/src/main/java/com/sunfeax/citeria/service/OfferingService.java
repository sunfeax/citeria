package com.sunfeax.citeria.service;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunfeax.citeria.dto.offering.OfferingPatchRequestDto;
import com.sunfeax.citeria.dto.offering.OfferingPostRequestDto;
import com.sunfeax.citeria.dto.offering.OfferingResponseDto;
import com.sunfeax.citeria.entity.OfferingEntity;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.enums.UserType;
import com.sunfeax.citeria.exception.ResourceNotFoundException;
import com.sunfeax.citeria.repository.OfferingRepository;
import com.sunfeax.citeria.mapper.OfferingMapper;
import com.sunfeax.citeria.normalizer.OfferingFieldNormalizer;
import com.sunfeax.citeria.security.CurrentUserProvider;
import com.sunfeax.citeria.validation.OfferingValidator;

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
public class OfferingService {

    private final OfferingRepository offeringRepository;
    private final OfferingMapper offeringMapper;
    private final OfferingFieldNormalizer offeringFieldNormalizer;
    private final OfferingValidator offeringValidator;
    private final CurrentUserProvider currentUserProvider;

    private static final Set<String> SORTABLE = Set.of("name", "priceAmount", "createdAt");
    private static final Sort DEFAULT_SORT = Sort.by("name");

    @Transactional(readOnly = true)
    public PageResponseDto<OfferingResponseDto> list(
        String search,
        UUID specialistId,
        Boolean active,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Pageable pageable
    ) {
        boolean effectiveActive = resolveActiveFilter(active, specialistId);

        List<Specification<OfferingEntity>> specs = new ArrayList<>();
        if (specialistId != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("specialist").get("id"), specialistId));
        }
        specs.add((root, query, cb) -> cb.equal(root.get("isActive"), effectiveActive));
        if (!canViewInactive(specialistId)) {
            specs.add((root, query, cb) -> cb.isTrue(root.get("specialist").get("isActive")));
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
        Page<OfferingResponseDto> page = offeringRepository.findAll(Specification.allOf(specs), sanitized)
            .map(offeringMapper::toResponseDto);

        return PageResponseDto.from(page);
    }

    @Transactional(readOnly = true)
    public OfferingResponseDto getById(UUID id) {
        return offeringRepository.findById(id)
            .filter(offering -> (offering.isActive() && offering.getSpecialist().isActive())
                || canViewInactive(offering.getSpecialist().getId()))
            .map(offeringMapper::toResponseDto)
            .orElseThrow(() -> new ResourceNotFoundException("Service with id " + id + " not found"));
    }

    @Transactional
    public OfferingResponseDto create(OfferingPostRequestDto request) {
        OfferingPostRequestDto normalizedRequest = offeringFieldNormalizer.normalizePostRequest(request);

        UserEntity specialist = currentUserProvider.getCurrentUser();
        offeringValidator.validateCreate(normalizedRequest, specialist);

        OfferingEntity entity = offeringMapper.createEntity(normalizedRequest, specialist);
        OfferingEntity saved = offeringRepository.save(entity);

        return offeringMapper.toResponseDto(saved);
    }

    @Transactional
    public OfferingResponseDto update(UUID id, OfferingPatchRequestDto request) {
        OfferingEntity entity = findOfferingOrThrow(id);
        currentUserProvider.requireSelfOrAdmin(entity.getSpecialist().getId());

        OfferingPatchRequestDto normalizedRequest = offeringFieldNormalizer.normalizePatchRequest(request);
        offeringValidator.validateUpdate(id, entity, normalizedRequest);

        offeringMapper.applyPatch(entity, normalizedRequest);
        OfferingEntity saved = offeringRepository.save(entity);

        return offeringMapper.toResponseDto(saved);
    }

    @Transactional
    public OfferingResponseDto deactivateById(UUID id) {
        OfferingEntity offering = findOfferingOrThrow(id);
        currentUserProvider.requireSelfOrAdmin(offering.getSpecialist().getId());

        offering.setActive(false);
        OfferingEntity saved = offeringRepository.save(offering);

        return offeringMapper.toResponseDto(saved);
    }

    @Transactional
    public OfferingResponseDto hardDeleteById(UUID id) {
        OfferingEntity offering = findOfferingOrThrow(id);
        currentUserProvider.requireSelfOrAdmin(offering.getSpecialist().getId());

        OfferingResponseDto deletedOffering = offeringMapper.toResponseDto(offering);
        offeringRepository.delete(offering);

        return deletedOffering;
    }

    @Transactional
    public OfferingResponseDto restoreById(UUID id) {
        OfferingEntity offering = findOfferingOrThrow(id);
        currentUserProvider.requireSelfOrAdmin(offering.getSpecialist().getId());

        offering.setActive(true);
        OfferingEntity saved = offeringRepository.save(offering);

        return offeringMapper.toResponseDto(saved);
    }

    private boolean resolveActiveFilter(Boolean requestedActive, UUID specialistId) {
        if (!Boolean.FALSE.equals(requestedActive)) {
            return true;
        }

        return !canViewInactive(specialistId);
    }

    private boolean canViewInactive(UUID specialistId) {
        return currentUserProvider.findCurrentUser()
            .map(current -> currentUserProvider.isAdmin(current)
                || (current.getType() == UserType.SPECIALIST && current.getId().equals(specialistId)))
            .orElse(false);
    }

    private OfferingEntity findOfferingOrThrow(UUID id) {
        return offeringRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Service with id " + id + " not found"));
    }
}
