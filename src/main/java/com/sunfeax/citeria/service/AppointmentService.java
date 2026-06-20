package com.sunfeax.citeria.service;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunfeax.citeria.dto.appointment.AppointmentPatchRequestDto;
import com.sunfeax.citeria.dto.appointment.AppointmentPostRequestDto;
import com.sunfeax.citeria.dto.appointment.AppointmentResponseDto;
import com.sunfeax.citeria.dto.common.PageResponseDto;
import com.sunfeax.citeria.entity.AppointmentEntity;
import com.sunfeax.citeria.entity.SpecialistServiceEntity;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.enums.AppointmentStatus;
import com.sunfeax.citeria.exception.ForbiddenException;
import com.sunfeax.citeria.exception.ResourceNotFoundException;
import com.sunfeax.citeria.repository.AppointmentRepository;
import com.sunfeax.citeria.repository.SpecialistServiceRepository;
import com.sunfeax.citeria.repository.UserRepository;
import com.sunfeax.citeria.mapper.AppointmentMapper;
import com.sunfeax.citeria.normalizer.AppointmentFieldNormalizer;
import com.sunfeax.citeria.security.CurrentUserProvider;
import com.sunfeax.citeria.util.PageableUtil;
import com.sunfeax.citeria.validation.AppointmentValidator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;
    private final UserRepository userRepository;
    private final SpecialistServiceRepository specialistServiceRepository;
    private final AppointmentFieldNormalizer appointmentFieldNormalizer;
    private final AppointmentValidator appointmentValidator;
    private final CurrentUserProvider currentUserProvider;

    private static final Set<String> SORTABLE = Set.of("startTime", "status", "createdAt");
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, "startTime");

    @Transactional(readOnly = true)
    public PageResponseDto<AppointmentResponseDto> list(
        AppointmentStatus status,
        Instant from,
        Instant to,
        UUID specialistServiceId,
        Pageable pageable
    ) {
        UserEntity current = currentUserProvider.getCurrentUser();

        List<Specification<AppointmentEntity>> specs = new ArrayList<>();
        if (!currentUserProvider.isAdmin(current)) {
            UUID me = current.getId();
            specs.add((root, query, cb) -> cb.or(
                cb.equal(root.get("client").get("id"), me),
                cb.equal(root.get("specialist").get("id"), me)
            ));
        }
        if (status != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (from != null) {
            specs.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.<Instant>get("startTime"), from));
        }
        if (to != null) {
            specs.add((root, query, cb) -> cb.lessThanOrEqualTo(root.<Instant>get("startTime"), to));
        }
        if (specialistServiceId != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("specialistService").get("id"), specialistServiceId));
        }

        Pageable sanitized = PageableUtil.sanitizeSort(pageable, SORTABLE, DEFAULT_SORT);
        Page<AppointmentResponseDto> page = appointmentRepository.findAll(Specification.allOf(specs), sanitized)
            .map(appointmentMapper::toResponseDto);

        return PageResponseDto.from(page);
    }

    @Transactional(readOnly = true)
    public AppointmentResponseDto getById(UUID id) {
        AppointmentEntity appointment = findAppointmentOrThrow(id);
        assertParticipantOrAdmin(appointment);
        return appointmentMapper.toResponseDto(appointment);
    }

    @Transactional
    public AppointmentResponseDto create(AppointmentPostRequestDto request) {
        AppointmentPostRequestDto normalizedRequest = appointmentFieldNormalizer.normalizePostRequest(request);

        UserEntity client = currentUserProvider.getCurrentUser();
        SpecialistServiceEntity specialistService = findSpecialistServiceOrThrow(normalizedRequest.specialistServiceId());

        appointmentValidator.validateCreate(normalizedRequest, client, specialistService);

        AppointmentEntity entity = appointmentMapper.createEntity(normalizedRequest, client, specialistService);
        AppointmentEntity saved = appointmentRepository.save(entity);

        return appointmentMapper.toResponseDto(saved);
    }

    @Transactional
    public AppointmentResponseDto update(UUID id, AppointmentPatchRequestDto request) {
        AppointmentEntity entity = findAppointmentOrThrow(id);
        assertParticipantOrAdmin(entity);
        AppointmentPatchRequestDto normalizedRequest = appointmentFieldNormalizer.normalizePatchRequest(request);

        UserEntity targetClient = normalizedRequest.clientId() == null
            ? entity.getClient()
            : findClientOrThrow(normalizedRequest.clientId());

        SpecialistServiceEntity targetSpecialistService = normalizedRequest.specialistServiceId() == null
            ? entity.getSpecialistService()
            : findSpecialistServiceOrThrow(normalizedRequest.specialistServiceId());

        appointmentValidator.validateUpdate(id, entity, normalizedRequest, targetClient, targetSpecialistService);

        UserEntity clientToApply = normalizedRequest.clientId() == null ? null : targetClient;
        SpecialistServiceEntity specialistServiceToApply =
            normalizedRequest.specialistServiceId() == null ? null : targetSpecialistService;

        appointmentMapper.applyPatch(entity, normalizedRequest, clientToApply, specialistServiceToApply);
        AppointmentEntity saved = appointmentRepository.save(entity);

        return appointmentMapper.toResponseDto(saved);
    }

    @Transactional
    public AppointmentResponseDto deleteById(UUID id) {
        AppointmentEntity appointment = findAppointmentOrThrow(id);
        assertParticipantOrAdmin(appointment);

        AppointmentResponseDto deletedAppointment = appointmentMapper.toResponseDto(appointment);
        appointmentRepository.delete(appointment);

        return deletedAppointment;
    }

    private void assertParticipantOrAdmin(AppointmentEntity appointment) {
        UserEntity current = currentUserProvider.getCurrentUser();
        if (currentUserProvider.isAdmin(current)) {
            return;
        }

        UUID currentId = current.getId();
        boolean participant = currentId.equals(appointment.getClient().getId())
            || currentId.equals(appointment.getSpecialist().getId());

        if (!participant) {
            throw new ForbiddenException("You do not have permission to access this appointment");
        }
    }

    private AppointmentEntity findAppointmentOrThrow(UUID id) {
        return appointmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment with id " + id + " not found"));
    }

    private UserEntity findClientOrThrow(UUID clientId) {
        return userRepository.findById(clientId)
            .orElseThrow(() -> new ResourceNotFoundException("Client user with id " + clientId + " not found"));
    }

    private SpecialistServiceEntity findSpecialistServiceOrThrow(UUID specialistServiceId) {
        return specialistServiceRepository.findById(specialistServiceId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Specialist service with id " + specialistServiceId + " not found")
            );
    }
}