package com.sunfeax.citeria.service;

import java.time.LocalDateTime;

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
import com.sunfeax.citeria.exception.ResourceNotFoundException;
import com.sunfeax.citeria.mapper.AppointmentMapper;
import com.sunfeax.citeria.normalizer.AppointmentFieldNormalizer;
import com.sunfeax.citeria.repository.AppointmentRepository;
import com.sunfeax.citeria.repository.SpecialistServiceRepository;
import com.sunfeax.citeria.repository.UserRepository;
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

    @Transactional(readOnly = true)
    public Page<AppointmentResponseDto> getAll(Pageable pageable) {
        Page<AppointmentEntity> appointmentPage = appointmentRepository.findAll(pageable);
        return appointmentPage.map(appointmentMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    public AppointmentResponseDto getById(Long id) {
        return appointmentRepository.findById(id)
            .map(appointmentMapper::toResponseDto)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment with id " + id + " not found"));
    }

    @Transactional
    public AppointmentResponseDto create(AppointmentPostRequestDto request) {
        AppointmentPostRequestDto normalizedRequest = appointmentFieldNormalizer.normalizePostRequest(request);

        UserEntity client = findClientOrThrow(normalizedRequest.clientId());
        SpecialistServiceEntity specialistService = findSpecialistServiceOrThrow(normalizedRequest.specialistServiceId());
        appointmentValidator.validateCreate(normalizedRequest, client, specialistService);

        AppointmentEntity entity = appointmentMapper.createEntity(normalizedRequest, client, specialistService);
        AppointmentEntity saved = appointmentRepository.save(entity);

        return appointmentMapper.toResponseDto(saved);
    }

    @Transactional
    public AppointmentResponseDto update(Long id, AppointmentPatchRequestDto request) {
        AppointmentEntity entity = findAppointmentOrThrow(id);

        AppointmentPatchRequestDto normalizedRequest = appointmentFieldNormalizer.normalizePatchRequest(request);

        UserEntity targetClient = normalizedRequest.clientId() == null
            ? entity.getClient()
            : findClientOrThrow(normalizedRequest.clientId());

        SpecialistServiceEntity targetSpecialistService = normalizedRequest.specialistServiceId() == null
            ? entity.getSpecialistService()
            : findSpecialistServiceOrThrow(normalizedRequest.specialistServiceId());

        LocalDateTime targetStartTime = normalizedRequest.startTime() == null
            ? entity.getStartTime()
            : normalizedRequest.startTime();

        LocalDateTime targetEndTime = normalizedRequest.endTime() == null
            ? entity.getEndTime()
            : normalizedRequest.endTime();

        boolean scheduleChanged = normalizedRequest.specialistServiceId() != null
            || normalizedRequest.startTime() != null
            || normalizedRequest.endTime() != null;
            
        appointmentValidator.validateUpdate(
            id,
            normalizedRequest,
            targetClient,
            targetSpecialistService,
            targetStartTime,
            targetEndTime,
            scheduleChanged
        );

        UserEntity clientToApply = normalizedRequest.clientId() == null ? null : targetClient;
        SpecialistServiceEntity specialistServiceToApply =
            normalizedRequest.specialistServiceId() == null ? null : targetSpecialistService;

        appointmentMapper.applyPatch(entity, normalizedRequest, clientToApply, specialistServiceToApply);
        AppointmentEntity saved = appointmentRepository.save(entity);

        return appointmentMapper.toResponseDto(saved);
    }

    @Transactional
    public AppointmentResponseDto deleteById(Long id) {
        AppointmentEntity appointment = findAppointmentOrThrow(id);

        AppointmentResponseDto deletedAppointment = appointmentMapper.toResponseDto(appointment);
        appointmentRepository.delete(appointment);

        return deletedAppointment;
    }

    private AppointmentEntity findAppointmentOrThrow(Long id) {
        return appointmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment with id " + id + " not found"));
    }

    private UserEntity findClientOrThrow(Long clientId) {
        return userRepository.findById(clientId)
            .orElseThrow(() -> new ResourceNotFoundException("Client user with id " + clientId + " not found"));
    }

    private SpecialistServiceEntity findSpecialistServiceOrThrow(Long specialistServiceId) {
        return specialistServiceRepository.findById(specialistServiceId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Specialist service with id " + specialistServiceId + " not found")
            );
    }
}
