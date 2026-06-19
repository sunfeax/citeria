package com.sunfeax.citeria.service;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sunfeax.citeria.dto.appointment.AppointmentPatchRequestDto;
import com.sunfeax.citeria.dto.appointment.AppointmentPostRequestDto;
import com.sunfeax.citeria.dto.appointment.AppointmentResponseDto;
import com.sunfeax.citeria.entity.AppointmentEntity;
import com.sunfeax.citeria.entity.SpecialistServiceEntity;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.exception.ForbiddenException;
import com.sunfeax.citeria.exception.ResourceNotFoundException;
import com.sunfeax.citeria.repository.AppointmentRepository;
import com.sunfeax.citeria.repository.SpecialistServiceRepository;
import com.sunfeax.citeria.repository.UserRepository;
import com.sunfeax.citeria.mapper.AppointmentMapper;
import com.sunfeax.citeria.normalizer.AppointmentFieldNormalizer;
import com.sunfeax.citeria.security.CurrentUserProvider;
import com.sunfeax.citeria.validation.AppointmentValidator;

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

    @Transactional(readOnly = true)
    public Page<AppointmentResponseDto> getAll(Pageable pageable) {
        UserEntity current = currentUserProvider.getCurrentUser();
        Page<AppointmentEntity> appointmentPage = currentUserProvider.isAdmin(current)
            ? appointmentRepository.findAll(pageable)
            : appointmentRepository.findByClientIdOrSpecialistId(current.getId(), current.getId(), pageable);
        return appointmentPage.map(appointmentMapper::toResponseDto);
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