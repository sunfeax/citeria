package com.sunfeax.citeria.validation;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.appointment.AppointmentPatchRequestDto;
import com.sunfeax.citeria.dto.appointment.AppointmentPostRequestDto;
import com.sunfeax.citeria.entity.SpecialistServiceEntity;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.enums.UserType;
import com.sunfeax.citeria.mapper.AppointmentMapper;
import com.sunfeax.citeria.repository.AppointmentRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AppointmentValidator {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;

    public void validateCreate(AppointmentPostRequestDto request, UserEntity client, SpecialistServiceEntity specialistService) {
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
                !request.endTime().isAfter(request.startTime()),
                "time",
                "End time must be after start time"
            )
            .addErrorIf(
                request.startTime().isBefore(LocalDateTime.now()),
                "startTime",
                "Start time must be in the future"
            )
            .addErrorIf(
                appointmentRepository.existsBySpecialistServiceIdAndStartTimeLessThanAndEndTimeGreaterThan(
                    specialistService.getId(),
                    request.endTime(),
                    request.startTime()
                ),
                "time",
                "Specialist is already booked for this time slot"
            )
            .throwIfHasErrors();
    }

    public void validateUpdate(
        Long id,
        AppointmentPatchRequestDto request,
        UserEntity targetClient,
        SpecialistServiceEntity targetSpecialistService,
        LocalDateTime targetStartTime,
        LocalDateTime targetEndTime,
        boolean scheduleChanged
    ) {
        new ValidationResult()
            .addErrorIf(!appointmentMapper.hasAnyPatchField(request), "request", "No fields to update")
            .addErrorIf(
                request.clientId() != null && targetClient.getType() != UserType.CLIENT,
                "clientId",
                "User with id " + targetClient.getId() + " is not a client"
            )
            .addErrorIf(
                request.clientId() != null && !targetClient.isActive(),
                "clientId",
                "Client with id " + targetClient.getId() + " is inactive"
            )
            .addErrorIf(
                request.specialistServiceId() != null && !targetSpecialistService.isActive(),
                "specialistServiceId",
                "Specialist service with id " + targetSpecialistService.getId() + " is inactive"
            )
            .addErrorIf(
                request.specialistServiceId() != null && !targetSpecialistService.getSpecialist().isActive(),
                "specialistServiceId",
                "Specialist for specialist service with id " + targetSpecialistService.getId() + " is inactive"
            )
            .addErrorIf(
                request.specialistServiceId() != null && !targetSpecialistService.getService().isActive(),
                "specialistServiceId",
                "Service for specialist service with id " + targetSpecialistService.getId() + " is inactive"
            )
            .addErrorIf(
                (request.startTime() != null || request.endTime() != null)
                    && targetEndTime != null
                    && !targetEndTime.isAfter(targetStartTime),
                "time",
                "End time must be after start time"
            )
            .addErrorIf(
                targetStartTime != null && scheduleChanged && targetStartTime.isBefore(LocalDateTime.now()),
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
    }
}
