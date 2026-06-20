package com.sunfeax.citeria.validation;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.appointment.AppointmentPostRequestDto;
import com.sunfeax.citeria.dto.slot.SlotResponseDto;
import com.sunfeax.citeria.entity.SpecialistServiceEntity;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.enums.UserType;
import com.sunfeax.citeria.service.SlotService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AppointmentValidator {

    private final SlotService slotService;

    @Value("${app.booking.zone:UTC}")
    private String bookingZone;

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
                !isAvailableSlot(request, specialistService),
                "startTime",
                "Selected time is not an available slot"
            )
            .throwIfHasErrors();
    }

    /**
     * A booking is valid only if its start matches a slot the schedule currently offers.
     * Reusing {@link SlotService} keeps "what is shown" and "what is accepted" in sync:
     * it already enforces the active schedule, working-hours window, slot grid, the
     * "from next day" rule, and excludes slots taken by other appointments.
     */
    private boolean isAvailableSlot(AppointmentPostRequestDto request, SpecialistServiceEntity specialistService) {
        LocalDate date = request.startTime().atZone(ZoneId.of(bookingZone)).toLocalDate();
        return slotService.getAvailableSlots(specialistService.getId(), date, date).stream()
            .map(SlotResponseDto::startTime)
            .anyMatch(request.startTime()::equals);
    }
}
