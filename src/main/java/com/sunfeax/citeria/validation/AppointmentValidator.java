package com.sunfeax.citeria.validation;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.appointment.AppointmentPostRequestDto;
import com.sunfeax.citeria.dto.slot.SlotResponseDto;
import com.sunfeax.citeria.entity.ServiceEntity;
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

    public void validateCreate(AppointmentPostRequestDto request, UserEntity client, ServiceEntity service) {
        new ValidationResult()
            .addErrorIf(client.getType() != UserType.CLIENT, "clientId", "User with id " + client.getId() + " is not a client")
            .addErrorIf(!client.isActive(), "clientId", "Client with id " + client.getId() + " is inactive")
            .addErrorIf(
                !service.isActive(),
                "serviceId",
                "Service with id " + service.getId() + " is inactive"
            )
            .addErrorIf(
                !service.getSpecialist().isActive(),
                "serviceId",
                "Specialist for service with id " + service.getId() + " is inactive"
            )
            .addErrorIf(
                !isAvailableSlot(request, service),
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
    private boolean isAvailableSlot(AppointmentPostRequestDto request, ServiceEntity service) {
        LocalDate date = request.startTime().atZone(ZoneId.of(bookingZone)).toLocalDate();
        return slotService.getAvailableSlots(service.getId(), date, date).stream()
            .map(SlotResponseDto::startTime)
            .anyMatch(request.startTime()::equals);
    }
}
