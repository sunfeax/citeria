package com.sunfeax.citeria.service;

import java.time.DayOfWeek;
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

import com.sunfeax.citeria.dto.common.PageResponseDto;
import com.sunfeax.citeria.dto.workinghours.WorkingHoursPatchRequestDto;
import com.sunfeax.citeria.dto.workinghours.WorkingHoursPostRequestDto;
import com.sunfeax.citeria.dto.workinghours.WorkingHoursResponseDto;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.entity.WorkingHoursEntity;
import com.sunfeax.citeria.exception.ResourceNotFoundException;
import com.sunfeax.citeria.mapper.WorkingHoursMapper;
import com.sunfeax.citeria.normalizer.WorkingHoursFieldNormalizer;
import com.sunfeax.citeria.repository.WorkingHoursRepository;
import com.sunfeax.citeria.security.CurrentUserProvider;
import com.sunfeax.citeria.util.PageableUtil;
import com.sunfeax.citeria.validation.WorkingHoursValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkingHoursService {

    private final WorkingHoursRepository workingHoursRepository;
    private final WorkingHoursMapper workingHoursMapper;
    private final WorkingHoursFieldNormalizer workingHoursFieldNormalizer;
    private final WorkingHoursValidator workingHoursValidator;
    private final CurrentUserProvider currentUserProvider;

    private static final Set<String> SORTABLE = Set.of("createdAt");
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");

    @Transactional(readOnly = true)
    public PageResponseDto<WorkingHoursResponseDto> list(
        UUID specialistId,
        DayOfWeek dayOfWeek,
        Boolean active,
        Pageable pageable
    ) {
        List<Specification<WorkingHoursEntity>> specs = new ArrayList<>();
        if (specialistId != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("specialist").get("id"), specialistId));
        }
        if (dayOfWeek != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("dayOfWeek"), dayOfWeek));
        }
        if (active != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("isActive"), active));
        }

        Pageable sanitized = PageableUtil.sanitizeSort(pageable, SORTABLE, DEFAULT_SORT);
        Page<WorkingHoursResponseDto> page = workingHoursRepository.findAll(Specification.allOf(specs), sanitized)
            .map(workingHoursMapper::toResponseDto);

        return PageResponseDto.from(page);
    }

    @Transactional(readOnly = true)
    public WorkingHoursResponseDto getById(UUID id) {
        return workingHoursMapper.toResponseDto(findWorkingHoursOrThrow(id));
    }

    @Transactional
    public WorkingHoursResponseDto create(WorkingHoursPostRequestDto request) {
        WorkingHoursPostRequestDto normalizedRequest = workingHoursFieldNormalizer.normalizePostRequest(request);

        UserEntity specialist = currentUserProvider.getCurrentUser();
        workingHoursValidator.validateCreate(normalizedRequest, specialist);

        WorkingHoursEntity entity = workingHoursMapper.createEntity(normalizedRequest, specialist);
        WorkingHoursEntity saved = workingHoursRepository.save(entity);

        return workingHoursMapper.toResponseDto(saved);
    }

    @Transactional
    public WorkingHoursResponseDto update(UUID id, WorkingHoursPatchRequestDto request) {
        WorkingHoursEntity entity = findWorkingHoursOrThrow(id);
        currentUserProvider.requireSelfOrAdmin(entity.getSpecialist().getId());

        WorkingHoursPatchRequestDto normalizedRequest = workingHoursFieldNormalizer.normalizePatchRequest(request);
        workingHoursValidator.validateUpdate(entity, normalizedRequest);

        workingHoursMapper.applyPatch(entity, normalizedRequest);
        WorkingHoursEntity saved = workingHoursRepository.save(entity);

        return workingHoursMapper.toResponseDto(saved);
    }

    @Transactional
    public WorkingHoursResponseDto deleteById(UUID id) {
        WorkingHoursEntity entity = findWorkingHoursOrThrow(id);
        currentUserProvider.requireSelfOrAdmin(entity.getSpecialist().getId());

        WorkingHoursResponseDto deleted = workingHoursMapper.toResponseDto(entity);
        workingHoursRepository.delete(entity);

        return deleted;
    }

    private WorkingHoursEntity findWorkingHoursOrThrow(UUID id) {
        return workingHoursRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Working hours with id " + id + " not found"));
    }
}
