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
import com.sunfeax.citeria.enums.AppointmentStatus;
import com.sunfeax.citeria.enums.UserType;
import com.sunfeax.citeria.exception.ResourceNotFoundException;
import com.sunfeax.citeria.mapper.AppointmentMapper;
import com.sunfeax.citeria.repository.AppointmentRepository;
import com.sunfeax.citeria.repository.SpecialistServiceRepository;
import com.sunfeax.citeria.repository.UserRepository;
import com.sunfeax.citeria.validation.AppointmentFieldNormalizer;
import com.sunfeax.citeria.validation.ValidationResult;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;
    private final UserRepository userRepository;
    private final SpecialistServiceRepository specialistServiceRepository;
    private final AppointmentFieldNormalizer appointmentFieldNormalizer;

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
    public AppointmentResponseDto register(AppointmentPostRequestDto request) {
        AppointmentPostRequestDto normalizedRequest = appointmentFieldNormalizer.normalizePostRequest(request);

        UserEntity client = findClientOrThrow(normalizedRequest.clientId());
        SpecialistServiceEntity specialistService = findSpecialistServiceOrThrow(normalizedRequest.specialistServiceId());

        new ValidationResult()
            .addErrorIf(client.getType() != UserType.CLIENT, "clientId", "User with id " + client.getId() + " is not a client")
            .addErrorIf(!client.isActive(), "clientId", "Client with id " + client.getId() + " is inactive")
            .addErrorIf(
                !specialistService.isActive(),
                "specialistServiceId",
                "Specialist service with id " + specialistService.getId() + " is inactive"
            )
            .addErrorIf(
                !specialistService.getSpecialist().isActive(),
                "specialistServiceId",
                "Specialist for specialist service with id " + specialistService.getId() + " is inactive"
            )
            .addErrorIf(
                !specialistService.getService().isActive(),
                "specialistServiceId",
                "Service for specialist service with id " + specialistService.getId() + " is inactive"
            )
            .addErrorIf(
                !normalizedRequest.endTime().isAfter(normalizedRequest.startTime()),
                "time",
                "End time must be after start time"
            )
            .addErrorIf(
                normalizedRequest.startTime().isBefore(LocalDateTime.now()),
                "startTime",
                "Start time must be in the future"
            )
            .addErrorIf(
                appointmentRepository.existsBySpecialistServiceIdAndStartTimeLessThanAndEndTimeGreaterThan(
                    specialistService.getId(),
                    normalizedRequest.endTime(),
                    normalizedRequest.startTime()
                ),
                "time",
                "Specialist is already booked for this time slot"
            )
            .throwIfHasErrors();

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

        new ValidationResult()
            .addErrorIf(!appointmentMapper.hasAnyPatchField(normalizedRequest), "request", "No fields to update")
            .addErrorIf(
                normalizedRequest.clientId() != null && targetClient.getType() != UserType.CLIENT,
                "clientId",
                "User with id " + targetClient.getId() + " is not a client"
            )
            .addErrorIf(
                normalizedRequest.clientId() != null && !targetClient.isActive(),
                "clientId",
                "Client with id " + targetClient.getId() + " is inactive"
            )
            .addErrorIf(
                normalizedRequest.specialistServiceId() != null && !targetSpecialistService.isActive(),
                "specialistServiceId",
                "Specialist service with id " + targetSpecialistService.getId() + " is inactive"
            )
            .addErrorIf(
                normalizedRequest.specialistServiceId() != null && !targetSpecialistService.getSpecialist().isActive(),
                "specialistServiceId",
                "Specialist for specialist service with id " + targetSpecialistService.getId() + " is inactive"
            )
            .addErrorIf(
                normalizedRequest.specialistServiceId() != null && !targetSpecialistService.getService().isActive(),
                "specialistServiceId",
                "Service for specialist service with id " + targetSpecialistService.getId() + " is inactive"
            )
            .addErrorIf(
                (normalizedRequest.startTime() != null || normalizedRequest.endTime() != null)
                    && !targetEndTime.isAfter(targetStartTime),
                "time",
                "End time must be after start time"
            )
            .addErrorIf(
                scheduleChanged && targetStartTime.isBefore(LocalDateTime.now()),
                "startTime",
                "Start time must be in the future"
            )
            .addErrorIf(
                scheduleChanged
                    && appointmentRepository.existsBySpecialistServiceIdAndStartTimeLessThanAndEndTimeGreaterThanAndIdNot(
                        targetSpecialistService.getId(),
                        targetEndTime,
                        targetStartTime,
                        id
                    ),
                "time",
                "Specialist is already booked for this time slot"
            )
            .throwIfHasErrors();

        UserEntity clientToApply = normalizedRequest.clientId() == null ? null : targetClient;
        SpecialistServiceEntity specialistServiceToApply =
            normalizedRequest.specialistServiceId() == null ? null : targetSpecialistService;

        appointmentMapper.applyPatch(entity, normalizedRequest, clientToApply, specialistServiceToApply);
        AppointmentEntity saved = appointmentRepository.save(entity);

        return appointmentMapper.toResponseDto(saved);
    }

    @Transactional
    public AppointmentResponseDto deactivateById(Long id) {
        AppointmentEntity appointment = findAppointmentOrThrow(id);

        appointment.setStatus(AppointmentStatus.CANCELLED);
        AppointmentEntity saved = appointmentRepository.save(appointment);

        return appointmentMapper.toResponseDto(saved);
    }

    @Transactional
    public AppointmentResponseDto hardDeleteById(Long id) {
        AppointmentEntity appointment = findAppointmentOrThrow(id);

        AppointmentResponseDto deletedAppointment = appointmentMapper.toResponseDto(appointment);
        appointmentRepository.delete(appointment);

        return deletedAppointment;
    }

    @Transactional
    public AppointmentResponseDto restoreById(Long id) {
        AppointmentEntity appointment = findAppointmentOrThrow(id);

        appointment.setStatus(AppointmentStatus.PENDING);
        AppointmentEntity saved = appointmentRepository.save(appointment);

        return appointmentMapper.toResponseDto(saved);
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
