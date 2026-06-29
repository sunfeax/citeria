package com.sunfeax.citeria.validation;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.sunfeax.citeria.dto.appointment.AppointmentPostRequestDto;
import com.sunfeax.citeria.dto.slot.SlotResponseDto;
import com.sunfeax.citeria.entity.ServiceEntity;
import com.sunfeax.citeria.entity.UserEntity;
import com.sunfeax.citeria.enums.AppointmentStatus;
import com.sunfeax.citeria.enums.UserType;
import com.sunfeax.citeria.repository.AppointmentRepository;
import com.sunfeax.citeria.service.SlotService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AppointmentValidator {

    private final SlotService slotService;
    private final AppointmentRepository appointmentRepository;

    @Value("${app.booking.zone:UTC}")
    private String bookingZone;

    @Value("${app.booking.maxPendingPerClient:10}")
    private long maxPendingPerClient;

    public void validateCreate(AppointmentPostRequestDto request, UserEntity client, ServiceEntity service) {
        Instant start = request.startTime();
        Instant end = start.plus(Duration.ofMinutes(service.getDurationMinutes()));

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
                appointmentRepository.countByClientIdAndStatus(client.getId(), AppointmentStatus.PENDING)
                    >= maxPendingPerClient,
                "request",
                "You have too many pending requests (max " + maxPendingPerClient + "). Wait for them to be handled first."
            )
            .addErrorIf(
                appointmentRepository.existsByClientIdAndStatusInAndEndTimeGreaterThanAndStartTimeLessThan(
                    client.getId(), AppointmentStatus.CLIENT_ACTIVE, start, end
                ),
                "startTime",
                "You already have a booking that overlaps this time"
            )
            .addErrorIf(
                !isAvailableSlot(request, service),
                "startTime",
                "Selected time is not an available slot"
            )
            .throwIfHasErrors();
    }

    private boolean isAvailableSlot(AppointmentPostRequestDto request, ServiceEntity service) {
        LocalDate date = request.startTime().atZone(ZoneId.of(bookingZone)).toLocalDate();
        return slotService.getAvailableSlots(service.getId(), date, date).stream()
            .map(SlotResponseDto::startTime)
            .anyMatch(request.startTime()::equals);
    }
}
